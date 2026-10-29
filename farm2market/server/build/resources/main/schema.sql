-- ============================================
-- FARM2MARKET DATABASE SCHEMA
-- MySQL 8 / SQLite Compatible
-- ============================================

-- Users table (base for all user types)
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    mobile VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    profile_image_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

-- Farmer profiles (extends users)
CREATE TABLE IF NOT EXISTS farmer_profiles (
    farmer_id VARCHAR(36) PRIMARY KEY,
    farm_name VARCHAR(200),
    location VARCHAR(500),
    description TEXT,
    farming_practices TEXT,
    harvest_frequency VARCHAR(100),
    service_area TEXT,
    fulfilment_options VARCHAR(50) NOT NULL DEFAULT 'PICKUP',
    rating_sum REAL DEFAULT 0,
    rating_count INTEGER DEFAULT 0,
    verified INTEGER DEFAULT 0,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    FOREIGN KEY (farmer_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Products/Listings
CREATE TABLE IF NOT EXISTS products (
    product_id VARCHAR(36) PRIMARY KEY,
    farmer_id VARCHAR(36) NOT NULL,
    category VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price REAL NOT NULL CHECK (price > 0),
    unit VARCHAR(20) NOT NULL,
    stock_quantity REAL NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    min_order_quantity REAL DEFAULT 1,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_seasonal INTEGER DEFAULT 0,
    is_organic INTEGER DEFAULT 0,
    is_locally_grown INTEGER DEFAULT 0,
    harvest_date BIGINT,
    best_before BIGINT,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    FOREIGN KEY (farmer_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_products_farmer ON products(farmer_id);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);

-- Product images
CREATE TABLE IF NOT EXISTS product_images (
    image_id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    is_primary INTEGER DEFAULT 0,
    display_order INTEGER DEFAULT 0,
    created_at BIGINT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- Shopping carts
CREATE TABLE IF NOT EXISTS carts (
    cart_id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL UNIQUE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Cart items
CREATE TABLE IF NOT EXISTS cart_items (
    cart_item_id VARCHAR(36) PRIMARY KEY,
    cart_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    quantity REAL NOT NULL CHECK (quantity > 0),
    price_snapshot REAL NOT NULL,
    added_at BIGINT NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    UNIQUE(cart_id, product_id)
);

-- Cart action history (for undo feature)
CREATE TABLE IF NOT EXISTS cart_actions (
    action_id VARCHAR(36) PRIMARY KEY,
    cart_id VARCHAR(36) NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    old_quantity REAL,
    new_quantity REAL,
    created_at BIGINT NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE
);

-- Customer addresses
CREATE TABLE IF NOT EXISTS addresses (
    address_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    label VARCHAR(50),
    full_address TEXT NOT NULL,
    landmark VARCHAR(255),
    city VARCHAR(100),
    pincode VARCHAR(10),
    contact_name VARCHAR(100),
    contact_phone VARCHAR(20),
    is_default INTEGER DEFAULT 0,
    created_at BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Orders
CREATE TABLE IF NOT EXISTS orders (
    order_id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    farmer_id VARCHAR(36) NOT NULL,
    subtotal REAL NOT NULL,
    delivery_fee REAL DEFAULT 0,
    total REAL NOT NULL,
    fulfilment_method VARCHAR(20) NOT NULL,
    delivery_address_snapshot TEXT,
    contact_name VARCHAR(100),
    contact_phone VARCHAR(20),
    payment_method VARCHAR(30) NOT NULL,
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    order_status VARCHAR(30) DEFAULT 'PENDING',
    rejection_reason TEXT,
    cancellation_reason TEXT,
    customer_note TEXT,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    accepted_at BIGINT,
    completed_at BIGINT,
    FOREIGN KEY (customer_id) REFERENCES users(user_id),
    FOREIGN KEY (farmer_id) REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_orders_customer ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_farmer ON orders(farmer_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(order_status);
CREATE INDEX IF NOT EXISTS idx_orders_created ON orders(created_at);

-- Order items (snapshots)
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    product_name_snapshot VARCHAR(200) NOT NULL,
    unit_price_snapshot REAL NOT NULL,
    unit_snapshot VARCHAR(20) NOT NULL,
    quantity REAL NOT NULL,
    line_total REAL NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- Order status history
CREATE TABLE IF NOT EXISTS order_status_history (
    history_id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    old_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    changed_by VARCHAR(36),
    note TEXT,
    created_at BIGINT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(user_id)
);

-- Reviews
CREATE TABLE IF NOT EXISTS reviews (
    review_id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL UNIQUE,
    customer_id VARCHAR(36) NOT NULL,
    farmer_id VARCHAR(36) NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (customer_id) REFERENCES users(user_id),
    FOREIGN KEY (farmer_id) REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_reviews_farmer ON reviews(farmer_id);

-- Notifications
CREATE TABLE IF NOT EXISTS notifications (
    notification_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(30) DEFAULT 'GENERAL',
    related_order_id VARCHAR(36),
    is_read INTEGER DEFAULT 0,
    read_at BIGINT,
    created_at BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (related_order_id) REFERENCES orders(order_id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_unread ON notifications(user_id, is_read);

-- Reports (for moderation)
CREATE TABLE IF NOT EXISTS reports (
    report_id VARCHAR(36) PRIMARY KEY,
    reporter_id VARCHAR(36) NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id VARCHAR(36) NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'OPEN',
    admin_id VARCHAR(36),
    admin_note TEXT,
    created_at BIGINT NOT NULL,
    resolved_at BIGINT,
    FOREIGN KEY (reporter_id) REFERENCES users(user_id),
    FOREIGN KEY (admin_id) REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_reports_status ON reports(status);

-- Sessions (for token-based auth)
CREATE TABLE IF NOT EXISTS sessions (
    session_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    created_at BIGINT NOT NULL,
    expires_at BIGINT NOT NULL,
    is_active INTEGER DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_sessions_token ON sessions(token);
CREATE INDEX IF NOT EXISTS idx_sessions_user_active ON sessions(user_id, is_active);

-- Audit log
CREATE TABLE IF NOT EXISTS audit_log (
    log_id VARCHAR(36) PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    user_id VARCHAR(36),
    target_type VARCHAR(50),
    target_id VARCHAR(36),
    details TEXT,
    ip_address VARCHAR(45),
    created_at BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_event_type ON audit_log(event_type);
CREATE INDEX IF NOT EXISTS idx_audit_created ON audit_log(created_at);

-- Recent searches (per customer)
CREATE TABLE IF NOT EXISTS recent_searches (
    search_id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    search_term VARCHAR(255) NOT NULL,
    created_at BIGINT NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_searches_customer ON recent_searches(customer_id, created_at);
