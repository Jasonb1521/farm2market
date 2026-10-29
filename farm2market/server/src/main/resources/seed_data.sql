-- ============================================
-- FARM2MARKET SEED DATA
-- Rich demo data with real images
-- ============================================

-- Admin user (password: password123)
INSERT OR IGNORE INTO users (user_id, email, mobile, password_hash, name, role, status, created_at, updated_at)
VALUES ('ADM001', 'admin@farm2market.com', '9999999999',
        '$2a$10$eWOGPcJUm90OKh2/1Kov9.y07O7KvbZyNAyutuyTeDlV.PE48je8G',
        'System Admin', 'ADMIN', 'ACTIVE', 1698500000000, 1698500000000);

-- Demo Farmers (password for all: password123)
INSERT OR IGNORE INTO users (user_id, email, mobile, password_hash, name, role, status, created_at, updated_at)
VALUES
('FRM001', 'ravi@demo.com', '9876543210',
 '$2a$10$eWOGPcJUm90OKh2/1Kov9.y07O7KvbZyNAyutuyTeDlV.PE48je8G',
 'Ravi Kumar', 'FARMER', 'ACTIVE', 1698500000000, 1698500000000),
('FRM002', 'lakshmi@demo.com', '9876543211',
 '$2a$10$eWOGPcJUm90OKh2/1Kov9.y07O7KvbZyNAyutuyTeDlV.PE48je8G',
 'Lakshmi Devi', 'FARMER', 'ACTIVE', 1698500000000, 1698500000000),
('FRM003', 'muthu@demo.com', '9876543212',
 '$2a$10$eWOGPcJUm90OKh2/1Kov9.y07O7KvbZyNAyutuyTeDlV.PE48je8G',
 'Muthu Selvam', 'FARMER', 'ACTIVE', 1698500000000, 1698500000000),
('FRM004', 'anitha@demo.com', '9876543213',
 '$2a$10$eWOGPcJUm90OKh2/1Kov9.y07O7KvbZyNAyutuyTeDlV.PE48je8G',
 'Anitha Krishnan', 'FARMER', 'ACTIVE', 1698500000000, 1698500000000);

-- Farmer Profiles
INSERT OR IGNORE INTO farmer_profiles (farmer_id, farm_name, location, description, fulfilment_options, rating_sum, rating_count, verified, created_at, updated_at)
VALUES
('FRM001', 'Ravi Fresh Farms', 'Kanchipuram, Tamil Nadu',
 'Family-owned farm growing fresh vegetables for 3 generations.', 'PICKUP,DELIVERY', 45.0, 10, 1, 1698500000000, 1698500000000),
('FRM002', 'Lakshmi Organic Garden', 'Tiruvallur, Tamil Nadu',
 'Certified organic produce grown with traditional methods.', 'PICKUP,DELIVERY', 36.0, 8, 1, 1698500000000, 1698500000000),
('FRM003', 'Muthu Natural Farm', 'Chengalpattu, Tamil Nadu',
 'Chemical-free farming with focus on native varieties.', 'PICKUP,DELIVERY', 24.0, 6, 1, 1698500000000, 1698500000000),
('FRM004', 'Anitha Dairy & Poultry', 'Vellore, Tamil Nadu',
 'Fresh dairy products and free-range eggs.', 'DELIVERY', 20.0, 5, 1, 1698500000000, 1698500000000);

-- Demo Customers (password for all: password123)
INSERT OR IGNORE INTO users (user_id, email, mobile, password_hash, name, role, status, created_at, updated_at)
VALUES
('CUS001', 'priya@demo.com', '9123456780',
 '$2a$10$eWOGPcJUm90OKh2/1Kov9.y07O7KvbZyNAyutuyTeDlV.PE48je8G',
 'Priya Sharma', 'CUSTOMER', 'ACTIVE', 1698500000000, 1698500000000),
('CUS002', 'arun@demo.com', '9123456781',
 '$2a$10$eWOGPcJUm90OKh2/1Kov9.y07O7KvbZyNAyutuyTeDlV.PE48je8G',
 'Arun Prakash', 'CUSTOMER', 'ACTIVE', 1698500000000, 1698500000000);

-- Customer Addresses
INSERT OR IGNORE INTO addresses (address_id, user_id, label, full_address, city, pincode, contact_name, contact_phone, is_default, created_at)
VALUES
('ADR001', 'CUS001', 'Home', '42, Gandhi Street, T Nagar', 'Chennai', '600017', 'Priya Sharma', '9123456780', 1, 1698500000000),
('ADR002', 'CUS002', 'Home', '15, Anna Nagar Main Road', 'Chennai', '600040', 'Arun Prakash', '9123456781', 1, 1698500000000);

-- ============================================
-- PRODUCTS
-- ============================================

-- Ravi Fresh Farms - Vegetables
INSERT OR IGNORE INTO products (product_id, farmer_id, category, name, description, price, unit, stock_quantity, status, is_organic, is_locally_grown, created_at, updated_at)
VALUES
('PRD001', 'FRM001', 'VEGETABLES', 'Fresh Tomatoes', 'Vine-ripened red tomatoes, perfect for cooking', 45.00, 'KG', 50.0, 'ACTIVE', 0, 1, 1698500000000, 1698500000000),
('PRD002', 'FRM001', 'VEGETABLES', 'Green Chillies', 'Spicy green chillies, locally grown', 65.00, 'KG', 20.0, 'ACTIVE', 0, 1, 1698500000000, 1698500000000),
('PRD003', 'FRM001', 'VEGETABLES', 'Red Onions', 'Fresh red onions from our farm', 38.00, 'KG', 100.0, 'ACTIVE', 0, 1, 1698500000000, 1698500000000),
('PRD004', 'FRM001', 'VEGETABLES', 'Farm Potatoes', 'Fresh potatoes perfect for any recipe', 32.00, 'KG', 80.0, 'ACTIVE', 0, 1, 1698500000000, 1698500000000),
('PRD005', 'FRM001', 'HERBS', 'Fresh Coriander', 'Fragrant coriander leaves, hand-picked', 25.00, 'BUNCH', 30.0, 'ACTIVE', 0, 1, 1698500000000, 1698500000000),
('PRD006', 'FRM001', 'HERBS', 'Curry Leaves', 'Aromatic curry leaves for South Indian cooking', 15.00, 'BUNCH', 40.0, 'ACTIVE', 0, 1, 1698500000000, 1698500000000),
('PRD007', 'FRM001', 'VEGETABLES', 'Fresh Ginger', 'Juicy ginger root for cooking and drinks', 120.00, 'KG', 15.0, 'ACTIVE', 0, 1, 1698500000000, 1698500000000),
('PRD008', 'FRM001', 'VEGETABLES', 'Garlic', 'Fresh garlic bulbs with strong aroma', 180.00, 'KG', 25.0, 'ACTIVE', 0, 1, 1698500000000, 1698500000000);

-- Lakshmi Organic Garden - Organic Produce
INSERT OR IGNORE INTO products (product_id, farmer_id, category, name, description, price, unit, stock_quantity, status, is_organic, is_locally_grown, created_at, updated_at)
VALUES
('PRD009', 'FRM002', 'VEGETABLES', 'Organic Brinjal', 'Purple brinjal grown 100% organically', 55.00, 'KG', 25.0, 'ACTIVE', 1, 1, 1698500000000, 1698500000000),
('PRD010', 'FRM002', 'VEGETABLES', 'Organic Spinach', 'Fresh organic spinach leaves, packed with iron', 45.00, 'BUNCH', 40.0, 'ACTIVE', 1, 1, 1698500000000, 1698500000000),
('PRD011', 'FRM002', 'FRUITS', 'Organic Bananas', 'Naturally ripened Robusta bananas', 55.00, 'DOZEN', 50.0, 'ACTIVE', 1, 1, 1698500000000, 1698500000000),
('PRD012', 'FRM002', 'VEGETABLES', 'Organic Carrots', 'Sweet organic carrots, great for salads', 60.00, 'KG', 30.0, 'ACTIVE', 1, 1, 1698500000000, 1698500000000),
('PRD013', 'FRM002', 'VEGETABLES', 'Organic Beetroot', 'Deep red beetroots, organic and nutritious', 50.00, 'KG', 20.0, 'ACTIVE', 1, 1, 1698500000000, 1698500000000),
('PRD014', 'FRM002', 'FRUITS', 'Organic Papaya', 'Sweet ripe papayas, naturally grown', 40.00, 'KG', 15.0, 'ACTIVE', 1, 1, 1698500000000, 1698500000000),
('PRD015', 'FRM002', 'FRUITS', 'Fresh Mangoes', 'Sweet Alphonso mangoes from organic farm', 150.00, 'KG', 25.0, 'ACTIVE', 1, 1, 1698500000000, 1698500000000),
('PRD016', 'FRM002', 'FRUITS', 'Organic Oranges', 'Juicy organic oranges, vitamin C rich', 80.00, 'KG', 35.0, 'ACTIVE', 1, 1, 1698500000000, 1698500000000);

-- Muthu Natural Farm - Grains & Pulses
INSERT OR IGNORE INTO products (product_id, farmer_id, category, name, description, price, unit, stock_quantity, status, is_organic, is_locally_grown, created_at, updated_at)
VALUES
('PRD017', 'FRM003', 'GRAINS', 'Mappillai Samba Rice', 'Traditional native red rice, rich in fiber', 125.00, 'KG', 100.0, 'ACTIVE', 1, 1, 1698500000000, 1698500000000),
('PRD018', 'FRM003', 'GRAINS', 'Basmati Rice', 'Premium long grain basmati rice', 95.00, 'KG', 80.0, 'ACTIVE', 0, 1, 1698500000000, 1698500000000),
('PRD019', 'FRM003', 'PULSES', 'Toor Dal', 'Unpolished toor dal with natural nutrients', 160.00, 'KG', 40.0, 'ACTIVE', 0, 1, 1698500000000, 1698500000000),
('PRD020', 'FRM003', 'PULSES', 'Urad Dal', 'Whole black urad dal, stone-ground', 140.00, 'KG', 35.0, 'ACTIVE', 0, 1, 1698500000000, 1698500000000),
('PRD021', 'FRM003', 'PULSES', 'Green Moong Dal', 'Split green gram for healthy cooking', 130.00, 'KG', 45.0, 'ACTIVE', 0, 1, 1698500000000, 1698500000000),
('PRD022', 'FRM003', 'GRAINS', 'Ragi Flour', 'Nutritious finger millet flour', 75.00, 'KG', 60.0, 'ACTIVE', 1, 1, 1698500000000, 1698500000000),
('PRD023', 'FRM003', 'GRAINS', 'Wheat Flour', 'Fresh ground whole wheat flour', 55.00, 'KG', 70.0, 'ACTIVE', 0, 1, 1698500000000, 1698500000000),
('PRD024', 'FRM003', 'HONEY', 'Wild Forest Honey', 'Pure wild honey from Western Ghats', 450.00, 'KG', 10.0, 'ACTIVE', 1, 1, 1698500000000, 1698500000000);

-- Anitha Dairy & Poultry
INSERT OR IGNORE INTO products (product_id, farmer_id, category, name, description, price, unit, stock_quantity, status, is_organic, is_locally_grown, created_at, updated_at)
VALUES
('PRD025', 'FRM004', 'EGGS', 'Country Eggs', 'Free-range country chicken eggs', 85.00, 'DOZEN', 60.0, 'ACTIVE', 0, 1, 1698500000000, 1698500000000),
('PRD026', 'FRM004', 'EGGS', 'Duck Eggs', 'Fresh duck eggs, larger and creamier', 95.00, 'DOZEN', 30.0, 'ACTIVE', 0, 1, 1698500000000, 1698500000000),
('PRD027', 'FRM004', 'DAIRY', 'Fresh Cow Milk', 'Pure A2 cow milk, delivered fresh', 60.00, 'LITRE', 50.0, 'ACTIVE', 0, 1, 1698500000000, 1698500000000),
('PRD028', 'FRM004', 'DAIRY', 'Buffalo Milk', 'Rich and creamy buffalo milk', 75.00, 'LITRE', 40.0, 'ACTIVE', 0, 1, 1698500000000, 1698500000000),
('PRD029', 'FRM004', 'DAIRY', 'Fresh Paneer', 'Homemade soft paneer from pure milk', 320.00, 'KG', 15.0, 'ACTIVE', 0, 1, 1698500000000, 1698500000000),
('PRD030', 'FRM004', 'DAIRY', 'Curd (Dahi)', 'Thick set curd made from whole milk', 50.00, 'KG', 25.0, 'ACTIVE', 0, 1, 1698500000000, 1698500000000),
('PRD031', 'FRM004', 'DAIRY', 'Fresh Butter', 'Hand-churned white butter, pure', 550.00, 'KG', 10.0, 'ACTIVE', 0, 1, 1698500000000, 1698500000000),
('PRD032', 'FRM004', 'DAIRY', 'Desi Ghee', 'Pure desi ghee made from A2 cow milk', 750.00, 'KG', 8.0, 'ACTIVE', 0, 1, 1698500000000, 1698500000000);

-- ============================================
-- PRODUCT IMAGES (Real Unsplash Images)
-- ============================================
INSERT OR IGNORE INTO product_images (image_id, product_id, image_url, is_primary, display_order, created_at)
VALUES
-- Vegetables
('IMG001', 'PRD001', 'https://images.unsplash.com/photo-1546470427-227c7369a9b5?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG002', 'PRD002', 'https://images.unsplash.com/photo-1583119022894-919a68a3d0e3?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG003', 'PRD003', 'https://images.unsplash.com/photo-1618512496248-a07fe83aa8cb?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG004', 'PRD004', 'https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG005', 'PRD005', 'https://images.unsplash.com/photo-1592924802359-ce9d3c23f8c7?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG006', 'PRD006', 'https://images.unsplash.com/photo-1600857544200-b2f666a9a2ec?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG007', 'PRD007', 'https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG008', 'PRD008', 'https://images.unsplash.com/photo-1540148426945-6cf22a6b2383?w=400&h=400&fit=crop', 1, 0, 1698500000000),
-- Organic Vegetables & Fruits
('IMG009', 'PRD009', 'https://images.unsplash.com/photo-1615484477778-ca3b77940c25?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG010', 'PRD010', 'https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG011', 'PRD011', 'https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG012', 'PRD012', 'https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG013', 'PRD013', 'https://images.unsplash.com/photo-1593105544559-ecb03bf76f82?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG014', 'PRD014', 'https://images.unsplash.com/photo-1517282009859-f000ec3b26fe?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG015', 'PRD015', 'https://images.unsplash.com/photo-1553279768-865429fa0078?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG016', 'PRD016', 'https://images.unsplash.com/photo-1547514701-42782101795e?w=400&h=400&fit=crop', 1, 0, 1698500000000),
-- Grains & Pulses
('IMG017', 'PRD017', 'https://images.unsplash.com/photo-1586201375761-83865001e31c?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG018', 'PRD018', 'https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG019', 'PRD019', 'https://images.unsplash.com/photo-1585996231593-d86fb3b8c43e?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG020', 'PRD020', 'https://images.unsplash.com/photo-1612257416648-ee7a6c5b2260?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG021', 'PRD021', 'https://images.unsplash.com/photo-1515543904809-c9d8a56a3632?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG022', 'PRD022', 'https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG023', 'PRD023', 'https://images.unsplash.com/photo-1556910103-1c02745aae4d?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG024', 'PRD024', 'https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=400&h=400&fit=crop', 1, 0, 1698500000000),
-- Dairy & Eggs
('IMG025', 'PRD025', 'https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG026', 'PRD026', 'https://images.unsplash.com/photo-1569288063643-5d29ad64df09?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG027', 'PRD027', 'https://images.unsplash.com/photo-1563636619-e9143da7973b?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG028', 'PRD028', 'https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG029', 'PRD029', 'https://images.unsplash.com/photo-1631452180519-c014fe946bc7?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG030', 'PRD030', 'https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG031', 'PRD031', 'https://images.unsplash.com/photo-1589985270826-4b7bb135bc9d?w=400&h=400&fit=crop', 1, 0, 1698500000000),
('IMG032', 'PRD032', 'https://images.unsplash.com/photo-1631898039984-fd5b788f8c95?w=400&h=400&fit=crop', 1, 0, 1698500000000);

-- Demo Carts
INSERT OR IGNORE INTO carts (cart_id, customer_id, created_at, updated_at)
VALUES
('CRT001', 'CUS001', 1698500000000, 1698500000000),
('CRT002', 'CUS002', 1698500000000, 1698500000000);

-- Sample Orders
INSERT OR IGNORE INTO orders (order_id, customer_id, farmer_id, subtotal, delivery_fee, total, fulfilment_method,
                    delivery_address_snapshot, contact_name, contact_phone, payment_method, payment_status,
                    order_status, created_at, updated_at, completed_at)
VALUES
('ORD001', 'CUS001', 'FRM001', 195.00, 30.00, 225.00, 'DELIVERY',
 '42, Gandhi Street, T Nagar, Chennai - 600017', 'Priya Sharma', '9123456780',
 'CASH_ON_DELIVERY', 'PAID', 'COMPLETED', 1698400000000, 1698450000000, 1698450000000);

-- Order Items
INSERT OR IGNORE INTO order_items (order_item_id, order_id, product_id, product_name_snapshot, unit_price_snapshot, unit_snapshot, quantity, line_total)
VALUES
('ORI001', 'ORD001', 'PRD001', 'Fresh Tomatoes', 45.00, 'KG', 2.0, 90.00),
('ORI002', 'ORD001', 'PRD003', 'Red Onions', 38.00, 'KG', 2.0, 76.00);

-- Reviews
INSERT OR IGNORE INTO reviews (review_id, order_id, customer_id, farmer_id, rating, comment, created_at, updated_at)
VALUES
('REV001', 'ORD001', 'CUS001', 'FRM001', 5, 'Excellent quality vegetables!', 1698460000000, 1698460000000);

-- Notifications
INSERT OR IGNORE INTO notifications (notification_id, user_id, title, message, related_order_id, is_read, created_at)
VALUES
('NTF001', 'CUS001', 'Order Delivered!', 'Your order ORD001 has been delivered. Enjoy!', 'ORD001', 1, 1698450000000),
('NTF002', 'CUS001', 'Order Accepted', 'Your order ORD002 has been accepted by Lakshmi Organic Garden.', 'ORD002', 0, 1698550000000),
('NTF003', 'CUS002', 'Order Placed', 'Your order ORD003 has been placed successfully.', 'ORD003', 0, 1698600000000),
('NTF004', 'FRM001', 'New Order!', 'You have a new order ORD004 from Arun Prakash.', 'ORD004', 0, 1698650000000),
('NTF005', 'FRM002', 'New Review', 'Priya Sharma rated your service 5 stars!', NULL, 0, 1698460000000);

-- Additional Orders with various statuses
INSERT OR IGNORE INTO orders (order_id, customer_id, farmer_id, subtotal, delivery_fee, total, fulfilment_method,
                    delivery_address_snapshot, contact_name, contact_phone, payment_method, payment_status,
                    order_status, created_at, updated_at)
VALUES
('ORD002', 'CUS001', 'FRM002', 320.00, 30.00, 350.00, 'DELIVERY',
 '42, Gandhi Street, T Nagar, Chennai - 600017', 'Priya Sharma', '9123456780',
 'CASH_ON_DELIVERY', 'PENDING', 'ACCEPTED', 1698550000000, 1698550000000),
('ORD003', 'CUS002', 'FRM001', 156.00, 0.00, 156.00, 'PICKUP',
 NULL, 'Arun Prakash', '9123456781',
 'PAY_ON_PICKUP', 'PENDING', 'PENDING', 1698600000000, 1698600000000),
('ORD004', 'CUS002', 'FRM001', 450.00, 30.00, 480.00, 'DELIVERY',
 '15, Anna Nagar, Chennai - 600040', 'Arun Prakash', '9123456781',
 'CASH_ON_DELIVERY', 'PENDING', 'PREPARING', 1698650000000, 1698650000000),
('ORD005', 'CUS001', 'FRM003', 580.00, 30.00, 610.00, 'DELIVERY',
 '42, Gandhi Street, T Nagar, Chennai - 600017', 'Priya Sharma', '9123456780',
 'CASH_ON_DELIVERY', 'PENDING', 'READY_FOR_PICKUP', 1698700000000, 1698700000000),
('ORD006', 'CUS002', 'FRM004', 260.00, 30.00, 290.00, 'DELIVERY',
 '15, Anna Nagar, Chennai - 600040', 'Arun Prakash', '9123456781',
 'CASH_ON_DELIVERY', 'REFUNDED', 'CANCELLED', 1698300000000, 1698350000000);

-- Order Items for additional orders
INSERT OR IGNORE INTO order_items (order_item_id, order_id, product_id, product_name_snapshot, unit_price_snapshot, unit_snapshot, quantity, line_total)
VALUES
('ORI003', 'ORD002', 'PRD009', 'Organic Spinach', 80.00, 'KG', 2.0, 160.00),
('ORI004', 'ORD002', 'PRD010', 'Organic Carrots', 80.00, 'KG', 2.0, 160.00),
('ORI005', 'ORD003', 'PRD004', 'Farm Potatoes', 32.00, 'KG', 3.0, 96.00),
('ORI006', 'ORD003', 'PRD005', 'Fresh Coriander', 25.00, 'BUNCH', 2.0, 50.00),
('ORI007', 'ORD004', 'PRD001', 'Fresh Tomatoes', 45.00, 'KG', 5.0, 225.00),
('ORI008', 'ORD004', 'PRD002', 'Green Chillies', 65.00, 'KG', 2.0, 130.00),
('ORI009', 'ORD004', 'PRD006', 'Fresh Mint', 20.00, 'BUNCH', 3.0, 60.00),
('ORI010', 'ORD005', 'PRD017', 'Traditional Rice', 85.00, 'KG', 5.0, 425.00),
('ORI011', 'ORD005', 'PRD019', 'Toor Dal', 155.00, 'KG', 1.0, 155.00),
('ORI012', 'ORD006', 'PRD025', 'Country Eggs', 90.00, 'DOZEN', 2.0, 180.00),
('ORI013', 'ORD006', 'PRD027', 'Fresh Cow Milk', 60.00, 'LITRE', 2.0, 120.00);

-- Sample Reports for Admin to review
INSERT OR IGNORE INTO reports (report_id, reporter_id, target_type, target_id, reason, status, created_at)
VALUES
('RPT001', 'CUS001', 'LISTING', 'PRD008', 'Product description seems misleading. Claims organic but price is very low.', 'OPEN', 1698500000000),
('RPT002', 'CUS002', 'USER', 'FRM003', 'Seller did not deliver order on time and was unresponsive.', 'OPEN', 1698550000000),
('RPT003', 'FRM001', 'REVIEW', 'REV001', 'This review seems fake or from a competitor.', 'REVIEWED', 1698400000000),
('RPT004', 'CUS001', 'LISTING', 'PRD015', 'Images do not match the actual product received.', 'OPEN', 1698600000000),
('RPT005', 'CUS002', 'USER', 'FRM004', 'Excellent service! Just wanted to commend the seller.', 'DISMISSED', 1698300000000);

-- Order Status History
INSERT OR IGNORE INTO order_status_history (history_id, order_id, old_status, new_status, changed_by, note, created_at)
VALUES
('OSH001', 'ORD001', NULL, 'PENDING', 'CUS001', 'Order placed', 1698400000000),
('OSH002', 'ORD001', 'PENDING', 'ACCEPTED', 'FRM001', 'Order accepted', 1698410000000),
('OSH003', 'ORD001', 'ACCEPTED', 'PREPARING', 'FRM001', 'Preparing your order', 1698420000000),
('OSH004', 'ORD001', 'PREPARING', 'OUT_FOR_DELIVERY', 'FRM001', 'On the way!', 1698440000000),
('OSH005', 'ORD001', 'OUT_FOR_DELIVERY', 'COMPLETED', 'FRM001', 'Delivered', 1698450000000),
('OSH006', 'ORD002', NULL, 'PENDING', 'CUS001', 'Order placed', 1698550000000),
('OSH007', 'ORD002', 'PENDING', 'ACCEPTED', 'FRM002', 'Order accepted', 1698555000000),
('OSH008', 'ORD006', NULL, 'PENDING', 'CUS002', 'Order placed', 1698300000000),
('OSH009', 'ORD006', 'PENDING', 'CANCELLED', 'CUS002', 'Customer requested cancellation', 1698350000000);

-- Additional Reviews
INSERT OR IGNORE INTO reviews (review_id, order_id, customer_id, farmer_id, rating, comment, created_at, updated_at)
VALUES
('REV002', 'ORD001', 'CUS001', 'FRM001', 4, 'Good quality but delivery was slightly delayed.', 1698461000000, 1698461000000);
