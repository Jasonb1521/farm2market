package com.farm2market.server;

import com.farm2market.core.exception.InsufficientStockException;
import com.farm2market.core.model.Product;
import com.farm2market.server.db.JdbcProductRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class InventoryManager {

    private static InventoryManager instance;
    private final JdbcProductRepository productRepository;
    private final ConcurrentHashMap<String, ReentrantLock> productLocks;
    private final ConcurrentHashMap<String, Double> reservedStock;

    private InventoryManager() {
        this.productRepository = new JdbcProductRepository();
        this.productLocks = new ConcurrentHashMap<>();
        this.reservedStock = new ConcurrentHashMap<>();
    }

    public static synchronized InventoryManager getInstance() {
        if (instance == null) {
            instance = new InventoryManager();
        }
        return instance;
    }

    private ReentrantLock getLock(String productId) {
        return productLocks.computeIfAbsent(productId, k -> new ReentrantLock(true));
    }

    public synchronized boolean reserveStock(String productId, double quantity) throws InsufficientStockException {
        ReentrantLock lock = getLock(productId);
        lock.lock();
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new InsufficientStockException(productId, quantity, 0));

            double currentStock = product.getStockQuantity();
            double currentReserved = reservedStock.getOrDefault(productId, 0.0);
            double available = currentStock - currentReserved;

            if (available < quantity) {
                throw new InsufficientStockException(
                        product.getName(), productId, quantity, available
                );
            }

            reservedStock.put(productId, currentReserved + quantity);
            System.out.printf("[Inventory] Reserved %.2f of %s (available: %.2f)%n",
                    quantity, product.getName(), available - quantity);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public synchronized void releaseReservation(String productId, double quantity) {
        ReentrantLock lock = getLock(productId);
        lock.lock();
        try {
            double currentReserved = reservedStock.getOrDefault(productId, 0.0);
            double newReserved = Math.max(0, currentReserved - quantity);

            if (newReserved > 0) {
                reservedStock.put(productId, newReserved);
            } else {
                reservedStock.remove(productId);
            }

            System.out.printf("[Inventory] Released %.2f reservation for product %s%n", quantity, productId);
        } finally {
            lock.unlock();
        }
    }

    public synchronized void commitReservation(String productId, double quantity) {
        ReentrantLock lock = getLock(productId);
        lock.lock();
        try {
            productRepository.decrementStock(productId, quantity);

            double currentReserved = reservedStock.getOrDefault(productId, 0.0);
            double newReserved = Math.max(0, currentReserved - quantity);

            if (newReserved > 0) {
                reservedStock.put(productId, newReserved);
            } else {
                reservedStock.remove(productId);
            }

            System.out.printf("[Inventory] Committed %.2f of product %s%n", quantity, productId);
        } finally {
            lock.unlock();
        }
    }

    public synchronized void restoreStock(String productId, double quantity) {
        ReentrantLock lock = getLock(productId);
        lock.lock();
        try {
            productRepository.incrementStock(productId, quantity);
            System.out.printf("[Inventory] Restored %.2f to product %s%n", quantity, productId);
        } finally {
            lock.unlock();
        }
    }

    public double getAvailableStock(String productId) {
        ReentrantLock lock = getLock(productId);
        lock.lock();
        try {
            return productRepository.findById(productId)
                    .map(p -> p.getStockQuantity() - reservedStock.getOrDefault(productId, 0.0))
                    .orElse(0.0);
        } finally {
            lock.unlock();
        }
    }

    public synchronized Map<String, Boolean> reserveMultiple(Map<String, Double> items) throws InsufficientStockException {
        Map<String, Boolean> results = new HashMap<>();
        Map<String, Double> successfulReservations = new HashMap<>();

        try {
            for (Map.Entry<String, Double> entry : items.entrySet()) {
                reserveStock(entry.getKey(), entry.getValue());
                successfulReservations.put(entry.getKey(), entry.getValue());
                results.put(entry.getKey(), true);
            }
        } catch (InsufficientStockException e) {
            // Rollback successful reservations
            for (Map.Entry<String, Double> entry : successfulReservations.entrySet()) {
                releaseReservation(entry.getKey(), entry.getValue());
            }
            throw e;
        }

        return results;
    }

    public void clearReservation(String productId) {
        reservedStock.remove(productId);
    }

    public void clearAllReservations() {
        reservedStock.clear();
    }
}
