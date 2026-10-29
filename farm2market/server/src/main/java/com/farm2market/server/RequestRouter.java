package com.farm2market.server;

import com.farm2market.core.dto.*;
import com.farm2market.core.enums.*;
import com.farm2market.core.exception.*;
import com.farm2market.core.model.*;
import com.farm2market.core.protocol.*;
import com.farm2market.core.util.IdGenerator;
import com.farm2market.server.db.*;
import com.farm2market.server.io.*;
import com.farm2market.server.service.AuthService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class RequestRouter {

    private final ClientHandler clientHandler;
    private final AuthService authService;
    private final JdbcUserRepository userRepository;
    private final JdbcProductRepository productRepository;
    private final JdbcCartRepository cartRepository;
    private final JdbcOrderRepository orderRepository;
    private final JdbcReviewRepository reviewRepository;
    private final JdbcNotificationRepository notificationRepository;
    private final JdbcReportRepository reportRepository;
    private final SessionManager sessionManager;
    private final InventoryManager inventoryManager;
    private final AuditLogger auditLogger;
    private final CsvExporter csvExporter;

    public RequestRouter(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
        this.authService = new AuthService();
        this.userRepository = new JdbcUserRepository();
        this.productRepository = new JdbcProductRepository();
        this.cartRepository = new JdbcCartRepository();
        this.orderRepository = new JdbcOrderRepository();
        this.reviewRepository = new JdbcReviewRepository();
        this.notificationRepository = new JdbcNotificationRepository();
        this.reportRepository = new JdbcReportRepository();
        this.sessionManager = SessionManager.getInstance();
        this.inventoryManager = InventoryManager.getInstance();
        this.auditLogger = AuditLogger.getInstance();
        this.csvExporter = new CsvExporter();
    }

    public Response route(Request request) {
        String requestId = request.getRequestId();

        try {
            // Validate token for protected endpoints
            if (requiresAuth(request.getType())) {
                if (!validateToken(request.getToken())) {
                    return Response.error(requestId, "AUTH_ERROR", "Invalid or expired session");
                }
            }

            return switch (request.getType()) {
                // Auth
                case LOGIN -> handleLogin(request);
                case REGISTER -> handleRegister(request);
                case LOGOUT -> handleLogout(request);

                // Products
                case LIST_PRODUCTS -> handleListProducts(request);
                case SEARCH_PRODUCTS -> handleSearchProducts(request);
                case GET_PRODUCT -> handleGetProduct(request);
                case CREATE_PRODUCT -> handleCreateProduct(request);
                case UPDATE_PRODUCT -> handleUpdateProduct(request);
                case GET_FARMER_PRODUCTS -> handleGetFarmerProducts(request);
                case GET_CATEGORIES -> handleGetCategories(request);

                // Profile
                case GET_PROFILE -> handleGetProfile(request);
                case UPDATE_PROFILE -> handleUpdateProfile(request);
                case GET_FARMER_PROFILE -> handleGetFarmerProfile(request);

                // Cart
                case GET_CART -> handleGetCart(request);
                case ADD_TO_CART -> handleAddToCart(request);
                case UPDATE_CART_ITEM -> handleUpdateCartItem(request);
                case REMOVE_FROM_CART -> handleRemoveFromCart(request);
                case CLEAR_CART -> handleClearCart(request);

                // Orders
                case PLACE_ORDER -> handlePlaceOrder(request);
                case GET_ORDER -> handleGetOrder(request);
                case GET_CUSTOMER_ORDERS -> handleGetCustomerOrders(request);
                case GET_FARMER_ORDERS -> handleGetFarmerOrders(request);
                case UPDATE_ORDER_STATUS -> handleUpdateOrderStatus(request);
                case CANCEL_ORDER -> handleCancelOrder(request);

                // Notifications
                case GET_NOTIFICATIONS -> handleGetNotifications(request);
                case MARK_NOTIFICATION_READ -> handleMarkNotificationRead(request);
                case MARK_ALL_READ -> handleMarkAllRead(request);

                // Reviews
                case CREATE_REVIEW -> handleCreateReview(request);
                case GET_FARMER_REVIEWS -> handleGetFarmerReviews(request);

                // Reports
                case CREATE_REPORT -> handleCreateReport(request);
                case GET_REPORTS -> handleGetReports(request);
                case GET_REPORT -> handleGetReport(request);
                case RESOLVE_REPORT -> handleResolveReport(request);

                // Admin
                case GET_USERS -> handleGetUsers(request);
                case GET_USER -> handleGetUser(request);
                case SUSPEND_USER -> handleSuspendUser(request);
                case ACTIVATE_USER -> handleActivateUser(request);
                case HIDE_LISTING -> handleHideListing(request);
                case UNHIDE_LISTING -> handleUnhideListing(request);
                case DELETE_PRODUCT -> handleDeleteProduct(request);
                case GET_PLATFORM_SUMMARY -> handleGetPlatformSummary(request);
                case GET_USER_ORDERS -> handleGetUserOrders(request);
                case GET_USER_REPORTS -> handleGetUserReports(request);

                // CSV Export
                case EXPORT_ORDERS_CSV -> handleExportOrdersCsv(request);
                case EXPORT_SALES_CSV -> handleExportSalesCsv(request);

                // Stats
                case GET_FARMER_STATS -> handleGetFarmerStats(request);

                // Ping
                case PING -> Response.success(MessageType.PONG, requestId, "pong");

                default -> Response.error(requestId, "UNKNOWN_TYPE", "Unknown message type: " + request.getType());
            };

        } catch (Farm2MarketException e) {
            return Response.error(requestId, e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error(requestId, "SERVER_ERROR", "Internal server error");
        }
    }

    private boolean requiresAuth(MessageType type) {
        return switch (type) {
            case LOGIN, REGISTER, PING, LIST_PRODUCTS, SEARCH_PRODUCTS,
                 GET_PRODUCT, GET_CATEGORIES, GET_FARMER_PROFILE -> false;
            default -> true;
        };
    }

    private boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        Optional<User> userOpt = sessionManager.validateToken(token);
        if (userOpt.isPresent()) {
            clientHandler.setCurrentUser(userOpt.get());
            clientHandler.setSessionToken(token);
            return true;
        }
        return false;
    }

    // ==================== AUTH ====================

    private Response handleLogin(Request request) throws Farm2MarketException {
        LoginRequest loginReq = ProtocolSerializer.deserializePayload(request.getPayload(), LoginRequest.class);

        if (loginReq == null || loginReq.getEmail() == null || loginReq.getPassword() == null) {
            throw new InvalidInputException("Email and password are required");
        }

        LoginResponse response = authService.login(loginReq.getEmail(), loginReq.getPassword());

        clientHandler.setSessionToken(response.getToken());
        userRepository.findById(response.getUserId()).ifPresent(clientHandler::setCurrentUser);

        System.out.printf("[Auth] User logged in: %s (%s)%n", response.getEmail(), response.getRole());

        return Response.success(MessageType.LOGIN_RESPONSE, request.getRequestId(), response);
    }

    private Response handleRegister(Request request) throws Farm2MarketException {
        RegisterRequest regReq = ProtocolSerializer.deserializePayload(request.getPayload(), RegisterRequest.class);

        if (regReq == null) {
            throw new InvalidInputException("Registration data is required");
        }

        LoginResponse response = authService.register(
                regReq.getName(),
                regReq.getEmail(),
                regReq.getMobile(),
                regReq.getPassword(),
                regReq.getRole()
        );

        clientHandler.setSessionToken(response.getToken());
        userRepository.findById(response.getUserId()).ifPresent(clientHandler::setCurrentUser);

        System.out.printf("[Auth] New user registered: %s (%s)%n", response.getEmail(), response.getRole());

        return Response.success(MessageType.REGISTER_RESPONSE, request.getRequestId(), response);
    }

    private Response handleLogout(Request request) {
        String token = request.getToken();
        if (token != null) {
            sessionManager.invalidateSession(token);
        }

        User user = clientHandler.getCurrentUser();
        if (user != null) {
            System.out.printf("[Auth] User logged out: %s%n", user.getEmail());
            NotificationDispatcher.getInstance().unregisterClient(user.getUserId());
        }

        clientHandler.setCurrentUser(null);
        clientHandler.setSessionToken(null);

        return Response.success(MessageType.LOGOUT_RESPONSE, request.getRequestId(), "Logged out");
    }

    // ==================== PRODUCTS ====================

    private Response handleListProducts(Request request) {
        SearchRequest searchReq = ProtocolSerializer.deserializePayload(request.getPayload(), SearchRequest.class);

        int limit = searchReq != null ? searchReq.getLimit() : 20;
        int offset = searchReq != null ? searchReq.getOffset() : 0;

        List<Product> products = productRepository.findAvailable(limit, offset);
        List<ProductDTO> dtos = products.stream()
                .map(ProductDTO::fromProduct)
                .collect(Collectors.toList());

        return Response.success(MessageType.PRODUCTS_RESPONSE, request.getRequestId(), dtos);
    }

    private Response handleSearchProducts(Request request) {
        SearchRequest searchReq = ProtocolSerializer.deserializePayload(request.getPayload(), SearchRequest.class);

        if (searchReq == null) {
            searchReq = new SearchRequest();
        }

        List<Product> products = productRepository.searchWithFilters(
                searchReq.getQuery(),
                searchReq.getCategory(),
                searchReq.getMinPrice(),
                searchReq.getMaxPrice(),
                searchReq.getInStock(),
                searchReq.getLimit(),
                searchReq.getOffset()
        );

        List<ProductDTO> dtos = products.stream()
                .map(ProductDTO::fromProduct)
                .collect(Collectors.toList());

        return Response.success(MessageType.PRODUCTS_RESPONSE, request.getRequestId(), dtos);
    }

    private Response handleGetProduct(Request request) throws Farm2MarketException {
        String productId = ProtocolSerializer.deserializePayload(request.getPayload(), String.class);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> EntityNotFoundException.product(productId));

        return Response.success(MessageType.PRODUCT_RESPONSE, request.getRequestId(), ProductDTO.fromProduct(product));
    }

    private Response handleCreateProduct(Request request) throws Farm2MarketException {
        User user = clientHandler.getCurrentUser();
        if (user.getRole() != Role.FARMER) {
            throw new UnauthorizedActionException(user.getUserId(), "create product");
        }

        ProductDTO dto = ProtocolSerializer.deserializePayload(request.getPayload(), ProductDTO.class);
        Product product = dto.toProduct();
        product.setFarmerId(user.getUserId());
        product.setProductId(IdGenerator.generateProductId());

        Product saved = productRepository.save(product);
        System.out.printf("[Product] Created: %s by farmer %s%n", saved.getName(), user.getName());

        return Response.success(MessageType.PRODUCT_RESPONSE, request.getRequestId(), ProductDTO.fromProduct(saved));
    }

    private Response handleUpdateProduct(Request request) throws Farm2MarketException {
        User user = clientHandler.getCurrentUser();
        ProductDTO dto = ProtocolSerializer.deserializePayload(request.getPayload(), ProductDTO.class);

        Product existing = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> EntityNotFoundException.product(dto.getProductId()));

        if (!existing.getFarmerId().equals(user.getUserId()) && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException(user.getUserId(), "update", "product", dto.getProductId());
        }

        Product updated = dto.toProduct();
        updated.setFarmerId(existing.getFarmerId());
        updated.setCreatedAt(existing.getCreatedAt());

        Product saved = productRepository.update(updated);

        return Response.success(MessageType.PRODUCT_RESPONSE, request.getRequestId(), ProductDTO.fromProduct(saved));
    }

    private Response handleGetFarmerProducts(Request request) {
        User user = clientHandler.getCurrentUser();
        String farmerId = user.getRole() == Role.FARMER ? user.getUserId() :
                ProtocolSerializer.deserializePayload(request.getPayload(), String.class);

        List<Product> products = productRepository.findByFarmerId(farmerId);
        List<ProductDTO> dtos = products.stream()
                .map(ProductDTO::fromProduct)
                .collect(Collectors.toList());

        return Response.success(MessageType.PRODUCTS_RESPONSE, request.getRequestId(), dtos);
    }

    private Response handleGetCategories(Request request) {
        return Response.success(MessageType.CATEGORIES_RESPONSE, request.getRequestId(), Category.values());
    }

    // ==================== PROFILE ====================

    private Response handleGetProfile(Request request) {
        User user = clientHandler.getCurrentUser();
        return Response.success(MessageType.PROFILE_RESPONSE, request.getRequestId(), user);
    }

    private Response handleUpdateProfile(Request request) throws Farm2MarketException {
        User user = clientHandler.getCurrentUser();
        User updates = ProtocolSerializer.deserializePayload(request.getPayload(), User.class);

        if (updates.getName() != null) user.setName(updates.getName());
        if (updates.getMobile() != null) user.setMobile(updates.getMobile());
        if (updates.getProfileImageUrl() != null) user.setProfileImageUrl(updates.getProfileImageUrl());

        User saved = userRepository.update(user);
        return Response.success(MessageType.UPDATE_PROFILE_RESPONSE, request.getRequestId(), saved);
    }

    private Response handleGetFarmerProfile(Request request) throws Farm2MarketException {
        String farmerId = ProtocolSerializer.deserializePayload(request.getPayload(), String.class);

        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> EntityNotFoundException.user(farmerId));

        if (farmer.getRole() != Role.FARMER) {
            throw new EntityNotFoundException("Farmer", farmerId);
        }

        return Response.success(MessageType.FARMER_PROFILE_RESPONSE, request.getRequestId(), farmer);
    }

    // ==================== CART ====================

    private Response handleGetCart(Request request) {
        User user = clientHandler.getCurrentUser();
        Cart cart = cartRepository.getOrCreateCart(user.getUserId());
        return Response.success(MessageType.CART_RESPONSE, request.getRequestId(), CartDTO.fromCart(cart));
    }

    private Response handleAddToCart(Request request) throws Farm2MarketException {
        User user = clientHandler.getCurrentUser();
        CartItemRequest itemReq = ProtocolSerializer.deserializePayload(request.getPayload(), CartItemRequest.class);

        // Get current product price
        Product product = productRepository.findById(itemReq.getProductId())
                .orElseThrow(() -> EntityNotFoundException.product(itemReq.getProductId()));

        Cart cart = cartRepository.getOrCreateCart(user.getUserId());
        cartRepository.addItem(cart.getCartId(), itemReq.getProductId(), itemReq.getQuantity(), product.getPrice());

        Cart updated = cartRepository.getOrCreateCart(user.getUserId());
        System.out.printf("[Cart] Added %s x%.1f for user %s%n", product.getName(), itemReq.getQuantity(), user.getName());

        return Response.success(MessageType.CART_RESPONSE, request.getRequestId(), CartDTO.fromCart(updated));
    }

    private Response handleUpdateCartItem(Request request) {
        User user = clientHandler.getCurrentUser();
        CartItemRequest itemReq = ProtocolSerializer.deserializePayload(request.getPayload(), CartItemRequest.class);

        if (itemReq.getQuantity() <= 0) {
            cartRepository.removeItem(itemReq.getCartItemId());
        } else {
            cartRepository.updateItemQuantity(itemReq.getCartItemId(), itemReq.getQuantity());
        }

        Cart updated = cartRepository.getOrCreateCart(user.getUserId());
        return Response.success(MessageType.CART_RESPONSE, request.getRequestId(), CartDTO.fromCart(updated));
    }

    private Response handleRemoveFromCart(Request request) {
        User user = clientHandler.getCurrentUser();
        String cartItemId = ProtocolSerializer.deserializePayload(request.getPayload(), String.class);

        cartRepository.removeItem(cartItemId);

        Cart updated = cartRepository.getOrCreateCart(user.getUserId());
        return Response.success(MessageType.CART_RESPONSE, request.getRequestId(), CartDTO.fromCart(updated));
    }

    private Response handleClearCart(Request request) {
        User user = clientHandler.getCurrentUser();
        Cart cart = cartRepository.getOrCreateCart(user.getUserId());
        cartRepository.clearCart(cart.getCartId());

        cart.setItems(new java.util.ArrayList<>());
        return Response.success(MessageType.CART_RESPONSE, request.getRequestId(), CartDTO.fromCart(cart));
    }

    // ==================== ORDERS ====================

    private Response handlePlaceOrder(Request request) throws Farm2MarketException {
        User user = clientHandler.getCurrentUser();

        // Parse payload flexibly to handle Android app format
        com.google.gson.JsonObject payload = ProtocolSerializer.deserializePayload(request.getPayload(), com.google.gson.JsonObject.class);

        // Extract fields from Android format
        String deliveryAddress = "";
        if (payload.has("deliveryAddress")) {
            com.google.gson.JsonElement addrEl = payload.get("deliveryAddress");
            if (addrEl.isJsonObject()) {
                com.google.gson.JsonObject addr = addrEl.getAsJsonObject();
                String street = addr.has("street") ? addr.get("street").getAsString() : "";
                String city = addr.has("city") ? addr.get("city").getAsString() : "";
                String pincode = addr.has("pincode") ? addr.get("pincode").getAsString() : "";
                deliveryAddress = street + ", " + city + " - " + pincode;
            } else {
                deliveryAddress = addrEl.getAsString();
            }
        }

        String phone = payload.has("phone") ? payload.get("phone").getAsString() :
                (payload.has("contactPhone") ? payload.get("contactPhone").getAsString() : user.getMobile());
        String notes = payload.has("notes") ? payload.get("notes").getAsString() :
                (payload.has("customerNote") ? payload.get("customerNote").getAsString() : "");
        String contactName = payload.has("contactName") ? payload.get("contactName").getAsString() : user.getName();

        FulfilmentMethod fulfilmentMethod = FulfilmentMethod.DELIVERY;
        if (payload.has("fulfilmentMethod")) {
            try {
                fulfilmentMethod = FulfilmentMethod.valueOf(payload.get("fulfilmentMethod").getAsString());
            } catch (Exception e) { /* use default */ }
        }

        PaymentMethodType paymentMethod = PaymentMethodType.CASH_ON_DELIVERY;
        if (payload.has("paymentMethod")) {
            try {
                paymentMethod = PaymentMethodType.valueOf(payload.get("paymentMethod").getAsString());
            } catch (Exception e) { /* use default */ }
        }

        // Get cart
        Cart cart = cartRepository.getOrCreateCart(user.getUserId());
        if (cart.getItems().isEmpty()) {
            throw new InvalidInputException("Cart is empty");
        }

        // Group items by farmer and create orders
        java.util.Map<String, java.util.List<CartItem>> itemsByFarmer = new java.util.HashMap<>();
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product == null) {
                product = productRepository.findById(item.getProductId()).orElse(null);
            }
            if (product != null) {
                itemsByFarmer.computeIfAbsent(product.getFarmerId(), k -> new java.util.ArrayList<>()).add(item);
            }
        }

        java.util.List<Order> orders = new java.util.ArrayList<>();

        for (java.util.Map.Entry<String, java.util.List<CartItem>> entry : itemsByFarmer.entrySet()) {
            String farmerId = entry.getKey();
            java.util.List<CartItem> farmerItems = entry.getValue();

            Order order = new Order();
            order.setOrderId(IdGenerator.generateOrderId());
            order.setCustomerId(user.getUserId());
            order.setFarmerId(farmerId);
            order.setFulfilmentMethod(fulfilmentMethod);
            order.setDeliveryAddressSnapshot(deliveryAddress);
            order.setContactName(contactName);
            order.setContactPhone(phone);
            order.setPaymentMethod(paymentMethod);
            order.setCustomerNote(notes);
            order.setOrderStatus(OrderStatus.PENDING);
            order.setPaymentStatus(PaymentStatus.PENDING);

            double subtotal = 0;
            for (CartItem cartItem : farmerItems) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderItemId(IdGenerator.generateUUID());
                orderItem.setOrderId(order.getOrderId());
                orderItem.setProductId(cartItem.getProductId());
                orderItem.setProductNameSnapshot(cartItem.getProduct() != null ? cartItem.getProduct().getName() : "Product");
                orderItem.setUnitPriceSnapshot(cartItem.getPriceSnapshot());
                orderItem.setUnitSnapshot(cartItem.getProduct() != null ? cartItem.getProduct().getUnit() : Unit.KG);
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setLineTotal(cartItem.getLineTotal());
                order.getItems().add(orderItem);
                subtotal += cartItem.getLineTotal();
            }

            order.setSubtotal(subtotal);
            order.setDeliveryFee(order.getFulfilmentMethod() == FulfilmentMethod.DELIVERY ? 30.0 : 0);
            order.setTotal(subtotal + order.getDeliveryFee());

            orderRepository.save(order);
            orders.add(order);

            System.out.printf("[Order] Created %s for customer %s from farmer %s (₹%.2f)%n",
                    order.getOrderId(), user.getName(), farmerId, order.getTotal());
        }

        // Clear cart after successful order
        cartRepository.clearCart(cart.getCartId());

        // Return first order (or all orders if multiple)
        if (orders.size() == 1) {
            return Response.success(MessageType.ORDER_RESPONSE, request.getRequestId(), OrderDTO.fromOrder(orders.get(0)));
        } else {
            java.util.List<OrderDTO> orderDtos = orders.stream().map(OrderDTO::fromOrder).collect(Collectors.toList());
            return Response.success(MessageType.ORDERS_RESPONSE, request.getRequestId(), orderDtos);
        }
    }

    private Response handleGetOrder(Request request) throws Farm2MarketException {
        String orderId = ProtocolSerializer.deserializePayload(request.getPayload(), String.class);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));

        return Response.success(MessageType.ORDER_RESPONSE, request.getRequestId(), OrderDTO.fromOrder(order));
    }

    private Response handleGetCustomerOrders(Request request) {
        User user = clientHandler.getCurrentUser();
        java.util.List<Order> orders = orderRepository.findByCustomerId(user.getUserId());
        java.util.List<OrderDTO> dtos = orders.stream().map(OrderDTO::fromOrder).collect(Collectors.toList());

        return Response.success(MessageType.ORDERS_RESPONSE, request.getRequestId(), dtos);
    }

    private Response handleGetFarmerOrders(Request request) {
        User user = clientHandler.getCurrentUser();
        java.util.List<Order> orders = orderRepository.findByFarmerId(user.getUserId());
        java.util.List<OrderDTO> dtos = orders.stream().map(OrderDTO::fromOrder).collect(Collectors.toList());

        return Response.success(MessageType.ORDERS_RESPONSE, request.getRequestId(), dtos);
    }

    private Response handleUpdateOrderStatus(Request request) throws Farm2MarketException {
        User user = clientHandler.getCurrentUser();
        UpdateOrderStatusRequest statusReq = ProtocolSerializer.deserializePayload(request.getPayload(), UpdateOrderStatusRequest.class);

        Order order = orderRepository.findById(statusReq.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order", statusReq.getOrderId()));

        // Only farmer or admin can update status
        if (!order.getFarmerId().equals(user.getUserId()) && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException(user.getUserId(), "update order status");
        }

        orderRepository.updateStatus(statusReq.getOrderId(), statusReq.getNewStatus(), statusReq.getReason());

        Order updated = orderRepository.findById(statusReq.getOrderId()).orElse(order);
        System.out.printf("[Order] %s status updated to %s%n", statusReq.getOrderId(), statusReq.getNewStatus());

        return Response.success(MessageType.ORDER_RESPONSE, request.getRequestId(), OrderDTO.fromOrder(updated));
    }

    private Response handleCancelOrder(Request request) throws Farm2MarketException {
        User user = clientHandler.getCurrentUser();
        String orderId = ProtocolSerializer.deserializePayload(request.getPayload(), String.class);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));

        // Only customer who placed the order can cancel
        if (!order.getCustomerId().equals(user.getUserId())) {
            throw new UnauthorizedActionException(user.getUserId(), "cancel order");
        }

        // Can only cancel pending orders
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new InvalidInputException("Can only cancel pending orders");
        }

        orderRepository.updateStatus(orderId, OrderStatus.CANCELLED, "Cancelled by customer");

        Order updated = orderRepository.findById(orderId).orElse(order);
        System.out.printf("[Order] %s cancelled by customer %s%n", orderId, user.getName());

        return Response.success(MessageType.ORDER_RESPONSE, request.getRequestId(), OrderDTO.fromOrder(updated));
    }

    // ==================== STATS ====================

    private Response handleGetFarmerStats(Request request) {
        User user = clientHandler.getCurrentUser();

        List<Order> orders = orderRepository.findByFarmerId(user.getUserId());
        List<Product> products = productRepository.findByFarmerId(user.getUserId());

        int totalOrders = orders.size();
        int pendingOrders = (int) orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.PENDING).count();
        int completedOrders = (int) orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.COMPLETED).count();
        double totalRevenue = orders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.COMPLETED)
                .mapToDouble(Order::getTotal)
                .sum();
        int activeListings = (int) products.stream().filter(p -> p.getStatus() == ProductStatus.ACTIVE).count();

        com.google.gson.JsonObject stats = new com.google.gson.JsonObject();
        stats.addProperty("totalOrders", totalOrders);
        stats.addProperty("pendingOrders", pendingOrders);
        stats.addProperty("completedOrders", completedOrders);
        stats.addProperty("totalRevenue", totalRevenue);
        stats.addProperty("activeListings", activeListings);
        stats.addProperty("totalListings", products.size());

        return Response.success(MessageType.SUMMARY_RESPONSE, request.getRequestId(), stats);
    }

    // ==================== NOTIFICATIONS ====================

    private Response handleGetNotifications(Request request) {
        User user = clientHandler.getCurrentUser();
        List<Notification> notifications = notificationRepository.findByUserId(user.getUserId());
        return Response.success(MessageType.NOTIFICATIONS_RESPONSE, request.getRequestId(), notifications);
    }

    private Response handleMarkNotificationRead(Request request) {
        String notificationId = ProtocolSerializer.deserializePayload(request.getPayload(), String.class);
        notificationRepository.markAsRead(notificationId);
        return Response.success(MessageType.NOTIFICATION_RESPONSE, request.getRequestId(), "Marked as read");
    }

    private Response handleMarkAllRead(Request request) {
        User user = clientHandler.getCurrentUser();
        notificationRepository.markAllAsRead(user.getUserId());
        return Response.success(MessageType.NOTIFICATION_RESPONSE, request.getRequestId(), "All marked as read");
    }

    // ==================== REVIEWS ====================

    private Response handleCreateReview(Request request) throws Farm2MarketException {
        User user = clientHandler.getCurrentUser();
        com.google.gson.JsonObject payload = ProtocolSerializer.deserializePayload(request.getPayload(), com.google.gson.JsonObject.class);

        String orderId = payload.get("orderId").getAsString();
        int rating = payload.get("rating").getAsInt();
        String comment = payload.has("comment") ? payload.get("comment").getAsString() : "";

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));

        if (!order.getCustomerId().equals(user.getUserId())) {
            throw new UnauthorizedActionException(user.getUserId(), "review order");
        }

        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new InvalidInputException("Can only review completed orders");
        }

        if (reviewRepository.findByOrderId(orderId).isPresent()) {
            throw new InvalidInputException("Review already exists for this order");
        }

        Review review = new Review();
        review.setOrderId(orderId);
        review.setCustomerId(user.getUserId());
        review.setFarmerId(order.getFarmerId());
        review.setRating(rating);
        review.setComment(comment);

        Review saved = reviewRepository.save(review);
        System.out.printf("[Review] Created for order %s, rating: %d%n", orderId, rating);

        return Response.success(MessageType.REVIEW_RESPONSE, request.getRequestId(), saved);
    }

    private Response handleGetFarmerReviews(Request request) {
        String farmerId = ProtocolSerializer.deserializePayload(request.getPayload(), String.class);
        if (farmerId == null) {
            User user = clientHandler.getCurrentUser();
            farmerId = user.getUserId();
        }

        List<Review> reviews = reviewRepository.findByFarmerId(farmerId);
        return Response.success(MessageType.REVIEWS_RESPONSE, request.getRequestId(), reviews);
    }

    // ==================== REPORTS ====================

    private Response handleCreateReport(Request request) throws Farm2MarketException {
        User user = clientHandler.getCurrentUser();
        com.google.gson.JsonObject payload = ProtocolSerializer.deserializePayload(request.getPayload(), com.google.gson.JsonObject.class);

        String targetType = payload.get("targetType").getAsString();
        String targetId = payload.get("targetId").getAsString();
        String reason = payload.get("reason").getAsString();

        Report report = new Report();
        report.setReporterId(user.getUserId());
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReason(reason);

        Report saved = reportRepository.save(report);
        auditLogger.logReportCreated(user.getUserId(), saved.getReportId(), targetType, targetId);

        return Response.success(MessageType.REPORT_RESPONSE, request.getRequestId(), saved);
    }

    private Response handleGetReports(Request request) throws Farm2MarketException {
        User user = clientHandler.getCurrentUser();
        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException(user.getUserId(), "view reports");
        }

        com.google.gson.JsonObject payload = ProtocolSerializer.deserializePayload(request.getPayload(), com.google.gson.JsonObject.class);

        List<Report> reports;
        if (payload != null && payload.has("status")) {
            ReportStatus status = ReportStatus.valueOf(payload.get("status").getAsString());
            reports = reportRepository.findByStatus(status);
        } else {
            reports = reportRepository.findAll();
        }

        return Response.success(MessageType.REPORTS_RESPONSE, request.getRequestId(), reports);
    }

    private Response handleGetReport(Request request) throws Farm2MarketException {
        User user = clientHandler.getCurrentUser();
        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException(user.getUserId(), "view report");
        }

        String reportId = ProtocolSerializer.deserializePayload(request.getPayload(), String.class);
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report", reportId));

        return Response.success(MessageType.REPORT_RESPONSE, request.getRequestId(), report);
    }

    private Response handleResolveReport(Request request) throws Farm2MarketException {
        User user = clientHandler.getCurrentUser();
        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException(user.getUserId(), "resolve report");
        }

        com.google.gson.JsonObject payload = ProtocolSerializer.deserializePayload(request.getPayload(), com.google.gson.JsonObject.class);
        String reportId = payload.get("reportId").getAsString();
        ReportStatus status = ReportStatus.valueOf(payload.get("status").getAsString());
        String adminNote = payload.has("adminNote") ? payload.get("adminNote").getAsString() : "";

        reportRepository.resolve(reportId, user.getUserId(), status, adminNote);
        auditLogger.logReportResolved(user.getUserId(), reportId, status.name());

        return Response.success(MessageType.REPORT_RESPONSE, request.getRequestId(), "Report resolved");
    }

    // ==================== ADMIN ====================

    private Response handleGetUsers(Request request) throws Farm2MarketException {
        User user = clientHandler.getCurrentUser();
        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException(user.getUserId(), "view users");
        }

        com.google.gson.JsonObject payload = ProtocolSerializer.deserializePayload(request.getPayload(), com.google.gson.JsonObject.class);

        String roleFilter = payload != null && payload.has("role") ? payload.get("role").getAsString() : null;
        String statusFilter = payload != null && payload.has("status") ? payload.get("status").getAsString() : null;
        String query = payload != null && payload.has("query") ? payload.get("query").getAsString() : null;

        List<User> users = userRepository.findAll();

        if (roleFilter != null) {
            Role role = Role.valueOf(roleFilter);
            users = users.stream().filter(u -> u.getRole() == role).collect(Collectors.toList());
        }

        if (statusFilter != null) {
            users = users.stream().filter(u -> statusFilter.equals(u.getStatus())).collect(Collectors.toList());
        }

        if (query != null && !query.isEmpty()) {
            String q = query.toLowerCase();
            users = users.stream()
                .filter(u -> u.getName().toLowerCase().contains(q) || u.getEmail().toLowerCase().contains(q))
                .collect(Collectors.toList());
        }

        return Response.success(MessageType.USERS_RESPONSE, request.getRequestId(), users);
    }

    private Response handleGetUser(Request request) throws Farm2MarketException {
        User currentUser = clientHandler.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException(currentUser.getUserId(), "view user");
        }

        String userId = ProtocolSerializer.deserializePayload(request.getPayload(), String.class);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));

        return Response.success(MessageType.USER_RESPONSE, request.getRequestId(), user);
    }

    private Response handleSuspendUser(Request request) throws Farm2MarketException {
        User admin = clientHandler.getCurrentUser();
        if (admin.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException(admin.getUserId(), "suspend user");
        }

        com.google.gson.JsonObject payload = ProtocolSerializer.deserializePayload(request.getPayload(), com.google.gson.JsonObject.class);
        String userId = payload.get("userId").getAsString();
        String reason = payload.has("reason") ? payload.get("reason").getAsString() : "";

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));

        if (user.getRole() == Role.ADMIN) {
            throw new InvalidInputException("Cannot suspend admin users");
        }

        user.setStatus(UserStatus.SUSPENDED);
        userRepository.update(user);
        auditLogger.logUserSuspended(admin.getUserId(), userId, reason);

        return Response.success(MessageType.USER_RESPONSE, request.getRequestId(), "User suspended");
    }

    private Response handleActivateUser(Request request) throws Farm2MarketException {
        User admin = clientHandler.getCurrentUser();
        if (admin.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException(admin.getUserId(), "activate user");
        }

        com.google.gson.JsonObject payload = ProtocolSerializer.deserializePayload(request.getPayload(), com.google.gson.JsonObject.class);
        String userId = payload.get("userId").getAsString();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));

        user.setStatus(UserStatus.ACTIVE);
        userRepository.update(user);
        auditLogger.logUserActivated(admin.getUserId(), userId);

        return Response.success(MessageType.USER_RESPONSE, request.getRequestId(), "User activated");
    }

    private Response handleHideListing(Request request) throws Farm2MarketException {
        User admin = clientHandler.getCurrentUser();
        if (admin.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException(admin.getUserId(), "hide listing");
        }

        com.google.gson.JsonObject payload = ProtocolSerializer.deserializePayload(request.getPayload(), com.google.gson.JsonObject.class);
        String productId = payload.get("productId").getAsString();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));

        product.setStatus(ProductStatus.HIDDEN);
        productRepository.update(product);
        auditLogger.logListingHidden(admin.getUserId(), productId);

        return Response.success(MessageType.PRODUCT_RESPONSE, request.getRequestId(), "Listing hidden");
    }

    private Response handleUnhideListing(Request request) throws Farm2MarketException {
        User admin = clientHandler.getCurrentUser();
        if (admin.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException(admin.getUserId(), "unhide listing");
        }

        com.google.gson.JsonObject payload = ProtocolSerializer.deserializePayload(request.getPayload(), com.google.gson.JsonObject.class);
        String productId = payload.get("productId").getAsString();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));

        product.setStatus(ProductStatus.ACTIVE);
        productRepository.update(product);

        return Response.success(MessageType.PRODUCT_RESPONSE, request.getRequestId(), "Listing unhidden");
    }

    private Response handleDeleteProduct(Request request) throws Farm2MarketException {
        User user = clientHandler.getCurrentUser();
        String productId = ProtocolSerializer.deserializePayload(request.getPayload(), String.class);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));

        if (!product.getFarmerId().equals(user.getUserId()) && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException(user.getUserId(), "delete product");
        }

        productRepository.delete(productId);
        auditLogger.logProductDeleted(user.getUserId(), productId, product.getName());

        return Response.success(MessageType.DELETE_RESPONSE, request.getRequestId(), "Product deleted");
    }

    private Response handleGetPlatformSummary(Request request) throws Farm2MarketException {
        User user = clientHandler.getCurrentUser();
        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException(user.getUserId(), "view platform summary");
        }

        List<User> allUsers = userRepository.findAll();
        List<Product> allProducts = productRepository.findAll();
        List<Order> allOrders = orderRepository.findAll();

        long now = System.currentTimeMillis();
        long dayAgo = now - 24 * 60 * 60 * 1000L;
        long weekAgo = now - 7 * 24 * 60 * 60 * 1000L;
        long monthAgo = now - 30 * 24 * 60 * 60 * 1000L;

        int customers = (int) allUsers.stream().filter(u -> u.getRole() == Role.CUSTOMER).count();
        int farmers = (int) allUsers.stream().filter(u -> u.getRole() == Role.FARMER).count();
        int admins = (int) allUsers.stream().filter(u -> u.getRole() == Role.ADMIN).count();
        int activeListings = (int) allProducts.stream().filter(p -> p.getStatus() == ProductStatus.ACTIVE).count();

        int ordersToday = (int) allOrders.stream().filter(o -> o.getCreatedAt() > dayAgo).count();
        int ordersWeek = (int) allOrders.stream().filter(o -> o.getCreatedAt() > weekAgo).count();
        int ordersMonth = (int) allOrders.stream().filter(o -> o.getCreatedAt() > monthAgo).count();

        double revenueToday = allOrders.stream()
                .filter(o -> o.getCreatedAt() > dayAgo && o.getOrderStatus() == OrderStatus.COMPLETED)
                .mapToDouble(Order::getTotal).sum();
        double revenueWeek = allOrders.stream()
                .filter(o -> o.getCreatedAt() > weekAgo && o.getOrderStatus() == OrderStatus.COMPLETED)
                .mapToDouble(Order::getTotal).sum();
        double revenueMonth = allOrders.stream()
                .filter(o -> o.getCreatedAt() > monthAgo && o.getOrderStatus() == OrderStatus.COMPLETED)
                .mapToDouble(Order::getTotal).sum();

        int pendingReports = reportRepository.countByStatus(ReportStatus.OPEN);

        com.google.gson.JsonObject summary = new com.google.gson.JsonObject();
        summary.addProperty("totalUsers", allUsers.size());
        summary.addProperty("customers", customers);
        summary.addProperty("farmers", farmers);
        summary.addProperty("admins", admins);
        summary.addProperty("activeListings", activeListings);
        summary.addProperty("ordersToday", ordersToday);
        summary.addProperty("ordersWeek", ordersWeek);
        summary.addProperty("ordersMonth", ordersMonth);
        summary.addProperty("revenueToday", revenueToday);
        summary.addProperty("revenueWeek", revenueWeek);
        summary.addProperty("revenueMonth", revenueMonth);
        summary.addProperty("pendingReports", pendingReports);

        return Response.success(MessageType.SUMMARY_RESPONSE, request.getRequestId(), summary);
    }

    private Response handleGetUserOrders(Request request) throws Farm2MarketException {
        User admin = clientHandler.getCurrentUser();
        if (admin.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException(admin.getUserId(), "view user orders");
        }

        com.google.gson.JsonObject payload = ProtocolSerializer.deserializePayload(request.getPayload(), com.google.gson.JsonObject.class);
        String userId = payload.get("userId").getAsString();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));

        List<Order> orders;
        if (user.getRole() == Role.FARMER) {
            orders = orderRepository.findByFarmerId(userId);
        } else {
            orders = orderRepository.findByCustomerId(userId);
        }

        List<OrderDTO> dtos = orders.stream().map(OrderDTO::fromOrder).collect(Collectors.toList());
        return Response.success(MessageType.ORDERS_RESPONSE, request.getRequestId(), dtos);
    }

    private Response handleGetUserReports(Request request) throws Farm2MarketException {
        User admin = clientHandler.getCurrentUser();
        if (admin.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException(admin.getUserId(), "view user reports");
        }

        com.google.gson.JsonObject payload = ProtocolSerializer.deserializePayload(request.getPayload(), com.google.gson.JsonObject.class);
        String userId = payload.get("userId").getAsString();

        List<Report> reports = reportRepository.findByTargetUser(userId);
        return Response.success(MessageType.REPORTS_RESPONSE, request.getRequestId(), reports);
    }

    // ==================== CSV EXPORT ====================

    private Response handleExportOrdersCsv(Request request) throws Farm2MarketException {
        User user = clientHandler.getCurrentUser();
        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException(user.getUserId(), "export orders");
        }

        com.google.gson.JsonObject payload = ProtocolSerializer.deserializePayload(request.getPayload(), com.google.gson.JsonObject.class);
        String filePath = payload.get("filePath").getAsString();

        try {
            List<Order> orders = orderRepository.findAll();
            List<Map<String, Object>> orderMaps = orders.stream()
                .map(this::orderToMap)
                .collect(Collectors.toList());

            csvExporter.exportOrders(orderMaps, filePath);
            return Response.success(MessageType.FILE_RESPONSE, request.getRequestId(), "Orders exported to " + filePath);
        } catch (Exception e) {
            throw new InvalidInputException("Export failed: " + e.getMessage());
        }
    }

    private Response handleExportSalesCsv(Request request) throws Farm2MarketException {
        User user = clientHandler.getCurrentUser();
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.FARMER) {
            throw new UnauthorizedActionException(user.getUserId(), "export sales");
        }

        com.google.gson.JsonObject payload = ProtocolSerializer.deserializePayload(request.getPayload(), com.google.gson.JsonObject.class);
        String filePath = payload.get("filePath").getAsString();

        try {
            String farmerId = user.getRole() == Role.FARMER ? user.getUserId() : null;
            List<Order> orders = farmerId != null
                ? orderRepository.findByFarmerId(farmerId)
                : orderRepository.findAll();

            List<Map<String, Object>> orderMaps = orders.stream()
                .map(this::orderToMap)
                .collect(Collectors.toList());

            csvExporter.exportSales(farmerId, orderMaps, LocalDate.now().minusMonths(1), LocalDate.now(), filePath);
            return Response.success(MessageType.FILE_RESPONSE, request.getRequestId(), "Sales exported to " + filePath);
        } catch (Exception e) {
            throw new InvalidInputException("Export failed: " + e.getMessage());
        }
    }

    private Map<String, Object> orderToMap(Order order) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", order.getOrderId());
        map.put("customerId", order.getCustomerId());
        map.put("farmerId", order.getFarmerId());
        map.put("customerName", order.getCustomerName());
        map.put("farmerName", order.getFarmerName());
        map.put("subtotal", order.getSubtotal());
        map.put("deliveryFee", order.getDeliveryFee());
        map.put("total", order.getTotal());
        map.put("orderStatus", order.getOrderStatus().name());
        map.put("paymentMethod", order.getPaymentMethod().name());
        map.put("paymentStatus", order.getPaymentStatus().name());
        map.put("fulfilmentMethod", order.getFulfilmentMethod().name());
        map.put("createdAt", order.getCreatedAt());

        List<Map<String, Object>> items = order.getItems().stream().map(item -> {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("productId", item.getProductId());
            itemMap.put("productName", item.getProductNameSnapshot());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("unitPrice", item.getUnitPriceSnapshot());
            itemMap.put("lineTotal", item.getLineTotal());
            itemMap.put("unit", item.getUnitSnapshot().name());
            return itemMap;
        }).collect(Collectors.toList());
        map.put("items", items);

        return map;
    }
}
