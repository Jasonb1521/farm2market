# Farm2Market — Complete Build System Prompt

You are building **Farm2Market**, a direct farmer-to-buyer marketplace. This is a Java-based system with three components that share a common core module. Build everything described below without omitting any feature, entity, or requirement.

---

## 1. ARCHITECTURE OVERVIEW

```
┌─────────────────────────────────────────────────────────────────────┐
│                         FARM2MARKET SYSTEM                          │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────┐    TCP/Socket    ┌──────────────────────────┐    │
│  │  Android App │◄────────────────►│   Farm2Market Server     │    │
│  │  (Customer & │    JSON Protocol │   (Java SE, JDK 25)      │    │
│  │   Farmer)    │                  │                          │    │
│  └──────────────┘                  │  ┌────────────────────┐  │    │
│                                    │  │  Thread Pool       │  │    │
│  ┌──────────────┐    TCP/Socket    │  │  InventoryManager  │  │    │
│  │ Admin Console│◄────────────────►│  │  NotificationQueue │  │    │
│  │ (Java Swing) │    JSON Protocol │  └────────────────────┘  │    │
│  └──────────────┘                  │            │             │    │
│                                    └────────────┼─────────────┘    │
│                                                 │ JDBC             │
│                                    ┌────────────▼─────────────┐    │
│                                    │   MySQL 8 / SQLite       │    │
│                                    │   Database               │    │
│                                    └──────────────────────────┘    │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                    SHARED CORE MODULE                         │  │
│  │  Models, Enums, Exceptions, Repository Interfaces, DTOs      │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 2. TECHNOLOGY STACK

| Component | Technology | Version |
|-----------|------------|---------|
| Android App | Java + Android SDK | API 26+ (Android 8.0+) |
| Server | Java SE | JDK 25 LTS |
| Admin Console | Java Swing | JDK 25 LTS |
| Database | MySQL 8 or SQLite | Latest stable |
| Database Access | JDBC | Standard |
| Network | java.net (Socket/ServerSocket) | Standard |
| IDE | Android Studio | Latest |
| Build | Gradle (Android), Maven/Gradle (Server/Admin) | Latest |

---

## 3. PROJECT STRUCTURE

```
farm2market/
├── core/                          # Shared module (Android-compatible Java level)
│   ├── src/main/java/com/farm2market/core/
│   │   ├── model/
│   │   │   ├── Entity.java                    # Base interface with getId()
│   │   │   ├── User.java                      # Abstract base class
│   │   │   ├── Customer.java                  # extends User
│   │   │   ├── Farmer.java                    # extends User
│   │   │   ├── Admin.java                     # extends User
│   │   │   ├── FarmerProfile.java
│   │   │   ├── Product.java
│   │   │   ├── Cart.java
│   │   │   ├── CartItem.java
│   │   │   ├── Order.java
│   │   │   ├── OrderItem.java
│   │   │   ├── Review.java
│   │   │   ├── Notification.java
│   │   │   └── Report.java
│   │   ├── enums/
│   │   │   ├── Role.java                      # GUEST, CUSTOMER, FARMER, ADMIN
│   │   │   ├── OrderStatus.java               # With allowed transitions
│   │   │   ├── Category.java                  # VEGETABLES, FRUITS, GRAINS, DAIRY, etc.
│   │   │   ├── Unit.java                      # KG, G, LITRE, BUNCH, PIECE, DOZEN
│   │   │   ├── PaymentMethodType.java         # CASH_ON_DELIVERY, PAY_ON_PICKUP, DEMO
│   │   │   ├── FulfilmentMethod.java          # PICKUP, DELIVERY, BOTH
│   │   │   ├── ProductStatus.java             # ACTIVE, INACTIVE, HIDDEN
│   │   │   ├── PaymentStatus.java             # PENDING, PAID, REFUNDED
│   │   │   └── ReportStatus.java              # OPEN, REVIEWED, RESOLVED, DISMISSED
│   │   ├── exception/
│   │   │   ├── Farm2MarketException.java      # Base exception
│   │   │   ├── InsufficientStockException.java
│   │   │   ├── InvalidStatusTransitionException.java
│   │   │   ├── UnauthorizedActionException.java
│   │   │   ├── InvalidInputException.java
│   │   │   ├── AuthenticationException.java
│   │   │   └── EntityNotFoundException.java
│   │   ├── repository/
│   │   │   ├── Repository.java                # Generic<T extends Entity, ID>
│   │   │   ├── UserRepository.java
│   │   │   ├── ProductRepository.java
│   │   │   ├── OrderRepository.java
│   │   │   ├── ReviewRepository.java
│   │   │   ├── CartRepository.java
│   │   │   ├── NotificationRepository.java
│   │   │   └── ReportRepository.java
│   │   ├── dto/
│   │   │   ├── LoginRequest.java
│   │   │   ├── LoginResponse.java
│   │   │   ├── RegisterRequest.java
│   │   │   ├── ProductDTO.java
│   │   │   ├── OrderDTO.java
│   │   │   ├── CartDTO.java
│   │   │   └── ... (all request/response DTOs)
│   │   ├── protocol/
│   │   │   ├── MessageType.java               # Enum of all message types
│   │   │   ├── Request.java                   # Base request class
│   │   │   ├── Response.java                  # Base response class
│   │   │   └── ProtocolSerializer.java        # JSON serialization
│   │   ├── service/
│   │   │   ├── PaymentMethod.java             # Interface
│   │   │   ├── CashOnDelivery.java
│   │   │   ├── PayOnPickup.java
│   │   │   ├── NotificationService.java       # Interface
│   │   │   └── ValidationService.java
│   │   └── util/
│   │       ├── AppConstants.java              # static final values
│   │       ├── IdGenerator.java               # static ID generation
│   │       ├── InputValidator.java
│   │       ├── PriceFormatter.java
│   │       ├── DateTimeUtil.java
│   │       └── Result.java                    # Generic Result<T> wrapper
│   └── build.gradle
│
├── server/                        # Java SE Server (JDK 25)
│   ├── src/main/java/com/farm2market/server/
│   │   ├── ServerMain.java                    # Entry point, ServerSocket
│   │   ├── ClientHandler.java                 # implements Runnable
│   │   ├── RequestRouter.java                 # Pattern matching for switch
│   │   ├── SessionManager.java                # Token-based sessions
│   │   ├── InventoryManager.java              # Synchronized stock operations
│   │   ├── NotificationDispatcher.java        # extends Thread, BlockingQueue
│   │   ├── db/
│   │   │   ├── DatabaseConnection.java        # Connection pool
│   │   │   ├── JdbcUserRepository.java
│   │   │   ├── JdbcProductRepository.java
│   │   │   ├── JdbcOrderRepository.java
│   │   │   ├── JdbcCartRepository.java
│   │   │   ├── JdbcReviewRepository.java
│   │   │   ├── JdbcNotificationRepository.java
│   │   │   └── JdbcReportRepository.java
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── ProductService.java
│   │   │   ├── OrderService.java
│   │   │   ├── CartService.java
│   │   │   ├── ReviewService.java
│   │   │   ├── ReportService.java
│   │   │   └── AdminService.java
│   │   └── io/
│   │       ├── CsvExporter.java
│   │       ├── CsvImporter.java
│   │       ├── AuditLogger.java
│   │       └── ReceiptWriter.java
│   ├── src/main/resources/
│   │   ├── schema.sql                         # Database schema
│   │   ├── seed_data.sql                      # Demo data
│   │   └── server.properties                  # Configuration
│   ├── DemoDataSeeder.java                    # Instance main method (JDK 25)
│   └── build.gradle
│
├── admin/                         # Java Swing Admin Console (JDK 25)
│   ├── src/main/java/com/farm2market/admin/
│   │   ├── AdminMain.java                     # Entry point
│   │   ├── ui/
│   │   │   ├── AdminLoginFrame.java           # JFrame login
│   │   │   ├── AdminDashboardFrame.java       # Main JFrame with JTabbedPane
│   │   │   ├── ReportsPanel.java              # JPanel for reports
│   │   │   ├── ListingsPanel.java             # JPanel for listing moderation
│   │   │   ├── UsersPanel.java                # JPanel for user management
│   │   │   └── SummaryPanel.java              # JPanel for platform stats
│   │   ├── service/
│   │   │   └── AdminClientService.java        # Socket client to server
│   │   └── util/
│   │       ├── SwingUtils.java
│   │       └── TableModelFactory.java
│   └── build.gradle
│
├── android/                       # Android App
│   ├── app/src/main/java/com/farm2market/app/
│   │   ├── Farm2MarketApp.java                # Application class
│   │   ├── network/
│   │   │   ├── SocketClient.java              # Background socket communication
│   │   │   └── ApiService.java                # Wraps socket operations
│   │   ├── repository/
│   │   │   ├── LocalUserRepository.java       # Room/SQLite cache
│   │   │   ├── LocalProductRepository.java
│   │   │   └── LocalCartRepository.java
│   │   ├── ui/
│   │   │   ├── auth/
│   │   │   │   ├── LoginActivity.java
│   │   │   │   ├── RegisterActivity.java
│   │   │   │   └── RoleSelectionActivity.java
│   │   │   ├── customer/
│   │   │   │   ├── CustomerHomeActivity.java
│   │   │   │   ├── SearchActivity.java
│   │   │   │   ├── ProductDetailActivity.java
│   │   │   │   ├── CartActivity.java
│   │   │   │   ├── CheckoutActivity.java
│   │   │   │   ├── OrderListActivity.java
│   │   │   │   ├── OrderDetailActivity.java
│   │   │   │   └── ReviewActivity.java
│   │   │   ├── farmer/
│   │   │   │   ├── FarmerDashboardActivity.java
│   │   │   │   ├── ManageListingsActivity.java
│   │   │   │   ├── AddEditProductActivity.java
│   │   │   │   ├── IncomingOrdersActivity.java
│   │   │   │   ├── OrderFulfilmentActivity.java
│   │   │   │   └── SalesSummaryActivity.java
│   │   │   ├── profile/
│   │   │   │   ├── ProfileActivity.java
│   │   │   │   ├── EditProfileActivity.java
│   │   │   │   └── FarmerProfileActivity.java
│   │   │   ├── common/
│   │   │   │   ├── NotificationsActivity.java
│   │   │   │   ├── HelpActivity.java
│   │   │   │   └── ReportActivity.java
│   │   │   └── adapter/
│   │   │       ├── ProductAdapter.java
│   │   │       ├── CartAdapter.java
│   │   │       ├── OrderAdapter.java
│   │   │       └── ReviewAdapter.java
│   │   ├── service/
│   │   │   ├── CartService.java
│   │   │   ├── OrderService.java
│   │   │   └── NotificationService.java
│   │   └── util/
│   │       ├── SessionManager.java
│   │       ├── ImageUtils.java
│   │       └── NetworkUtils.java
│   ├── app/src/main/res/
│   │   ├── layout/                            # All XML layouts
│   │   ├── values/                            # strings, colors, styles
│   │   ├── drawable/                          # Icons, backgrounds
│   │   └── menu/                              # Navigation menus
│   ├── app/src/main/AndroidManifest.xml
│   └── build.gradle
│
└── docs/
    ├── API_PROTOCOL.md                        # Message protocol spec
    ├── DATABASE_SCHEMA.md                     # Full schema docs
    └── SETUP.md                               # Installation guide
```

---

## 4. DATABASE SCHEMA

Create all tables with proper constraints, indexes, and foreign keys:

```sql
-- ============================================
-- FARM2MARKET DATABASE SCHEMA
-- ============================================

-- Users table (base for all user types)
CREATE TABLE users (
    user_id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    mobile VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    role ENUM('GUEST', 'CUSTOMER', 'FARMER', 'ADMIN') NOT NULL,
    profile_image_url VARCHAR(500),
    status ENUM('ACTIVE', 'SUSPENDED', 'DELETED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status)
);

-- Farmer profiles (extends users)
CREATE TABLE farmer_profiles (
    farmer_id VARCHAR(36) PRIMARY KEY,
    farm_name VARCHAR(200),
    location VARCHAR(500),
    description TEXT,
    farming_practices TEXT,
    harvest_frequency VARCHAR(100),
    service_area TEXT,
    fulfilment_options SET('PICKUP', 'DELIVERY') NOT NULL,
    rating_sum DECIMAL(10,2) DEFAULT 0,
    rating_count INT DEFAULT 0,
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (farmer_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Product categories
CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    icon_url VARCHAR(500)
);

-- Products/Listings
CREATE TABLE products (
    product_id VARCHAR(36) PRIMARY KEY,
    farmer_id VARCHAR(36) NOT NULL,
    category_id INT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price > 0),
    unit ENUM('KG', 'G', 'LITRE', 'BUNCH', 'PIECE', 'DOZEN') NOT NULL,
    stock_quantity DECIMAL(10,2) NOT NULL CHECK (stock_quantity >= 0),
    min_order_quantity DECIMAL(10,2) DEFAULT 1,
    status ENUM('ACTIVE', 'INACTIVE', 'HIDDEN', 'DELETED') DEFAULT 'ACTIVE',
    is_seasonal BOOLEAN DEFAULT FALSE,
    is_organic BOOLEAN DEFAULT FALSE,
    is_locally_grown BOOLEAN DEFAULT FALSE,
    harvest_date DATE,
    best_before DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (farmer_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    INDEX idx_farmer (farmer_id),
    INDEX idx_category (category_id),
    INDEX idx_status (status),
    INDEX idx_name (name),
    FULLTEXT INDEX idx_search (name, description)
);

-- Product images
CREATE TABLE product_images (
    image_id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- Shopping carts
CREATE TABLE carts (
    cart_id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Cart items
CREATE TABLE cart_items (
    cart_item_id VARCHAR(36) PRIMARY KEY,
    cart_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL CHECK (quantity > 0),
    price_snapshot DECIMAL(10,2) NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    UNIQUE KEY unique_cart_product (cart_id, product_id)
);

-- Cart action history (for undo feature)
CREATE TABLE cart_actions (
    action_id VARCHAR(36) PRIMARY KEY,
    cart_id VARCHAR(36) NOT NULL,
    action_type ENUM('ADD', 'REMOVE', 'UPDATE_QUANTITY') NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    old_quantity DECIMAL(10,2),
    new_quantity DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE
);

-- Customer addresses
CREATE TABLE addresses (
    address_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    label VARCHAR(50),
    full_address TEXT NOT NULL,
    landmark VARCHAR(255),
    city VARCHAR(100),
    pincode VARCHAR(10),
    contact_name VARCHAR(100),
    contact_phone VARCHAR(20),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Orders
CREATE TABLE orders (
    order_id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    farmer_id VARCHAR(36) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    delivery_fee DECIMAL(10,2) DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    fulfilment_method ENUM('PICKUP', 'DELIVERY') NOT NULL,
    delivery_address_snapshot TEXT,
    contact_name VARCHAR(100),
    contact_phone VARCHAR(20),
    payment_method ENUM('CASH_ON_DELIVERY', 'PAY_ON_PICKUP', 'DEMO', 'ONLINE') NOT NULL,
    payment_status ENUM('PENDING', 'PAID', 'REFUNDED', 'FAILED') DEFAULT 'PENDING',
    order_status ENUM('PENDING', 'ACCEPTED', 'PREPARING', 'READY_FOR_PICKUP', 
                      'OUT_FOR_DELIVERY', 'COMPLETED', 'REJECTED', 'CANCELLED') DEFAULT 'PENDING',
    rejection_reason TEXT,
    cancellation_reason TEXT,
    customer_note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (customer_id) REFERENCES users(user_id),
    FOREIGN KEY (farmer_id) REFERENCES users(user_id),
    INDEX idx_customer (customer_id),
    INDEX idx_farmer (farmer_id),
    INDEX idx_status (order_status),
    INDEX idx_created (created_at)
);

-- Order items (snapshots)
CREATE TABLE order_items (
    order_item_id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    product_name_snapshot VARCHAR(200) NOT NULL,
    unit_price_snapshot DECIMAL(10,2) NOT NULL,
    unit_snapshot ENUM('KG', 'G', 'LITRE', 'BUNCH', 'PIECE', 'DOZEN') NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    line_total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- Order status history
CREATE TABLE order_status_history (
    history_id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    old_status ENUM('PENDING', 'ACCEPTED', 'PREPARING', 'READY_FOR_PICKUP', 
                    'OUT_FOR_DELIVERY', 'COMPLETED', 'REJECTED', 'CANCELLED'),
    new_status ENUM('PENDING', 'ACCEPTED', 'PREPARING', 'READY_FOR_PICKUP', 
                    'OUT_FOR_DELIVERY', 'COMPLETED', 'REJECTED', 'CANCELLED') NOT NULL,
    changed_by VARCHAR(36),
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(user_id)
);

-- Reviews
CREATE TABLE reviews (
    review_id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL UNIQUE,
    customer_id VARCHAR(36) NOT NULL,
    farmer_id VARCHAR(36) NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (customer_id) REFERENCES users(user_id),
    FOREIGN KEY (farmer_id) REFERENCES users(user_id),
    INDEX idx_farmer (farmer_id)
);

-- Notifications
CREATE TABLE notifications (
    notification_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    related_order_id VARCHAR(36),
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (related_order_id) REFERENCES orders(order_id) ON DELETE SET NULL,
    INDEX idx_user_unread (user_id, is_read)
);

-- Reports (for moderation)
CREATE TABLE reports (
    report_id VARCHAR(36) PRIMARY KEY,
    reporter_id VARCHAR(36) NOT NULL,
    target_type ENUM('LISTING', 'REVIEW', 'USER') NOT NULL,
    target_id VARCHAR(36) NOT NULL,
    reason TEXT NOT NULL,
    status ENUM('OPEN', 'REVIEWED', 'RESOLVED', 'DISMISSED') DEFAULT 'OPEN',
    admin_id VARCHAR(36),
    admin_note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    FOREIGN KEY (reporter_id) REFERENCES users(user_id),
    FOREIGN KEY (admin_id) REFERENCES users(user_id),
    INDEX idx_status (status)
);

-- Sessions (for token-based auth)
CREATE TABLE sessions (
    session_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_user_active (user_id, is_active)
);

-- Audit log
CREATE TABLE audit_log (
    log_id VARCHAR(36) PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    user_id VARCHAR(36),
    target_type VARCHAR(50),
    target_id VARCHAR(36),
    details TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_event_type (event_type),
    INDEX idx_created (created_at)
);

-- Recent searches (per customer)
CREATE TABLE recent_searches (
    search_id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    search_term VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_customer_recent (customer_id, created_at DESC)
);

-- Insert default categories
INSERT INTO categories (name, description) VALUES
('VEGETABLES', 'Fresh vegetables'),
('FRUITS', 'Fresh fruits'),
('GRAINS', 'Rice, wheat, millets, and other grains'),
('DAIRY', 'Milk, curd, butter, and dairy products'),
('PULSES', 'Lentils, beans, and legumes'),
('SPICES', 'Fresh and dried spices'),
('HERBS', 'Fresh herbs and greens'),
('EGGS', 'Farm fresh eggs'),
('HONEY', 'Natural honey products'),
('OTHER', 'Other farm products');
```

---

## 5. COMMUNICATION PROTOCOL

Define a JSON-based request/response protocol over TCP sockets:

### Message Format
```json
{
  "type": "MESSAGE_TYPE",
  "token": "session_token_here",
  "requestId": "unique_request_id",
  "timestamp": 1698500000000,
  "payload": { ... }
}
```

### Response Format
```json
{
  "type": "RESPONSE_TYPE",
  "requestId": "matching_request_id",
  "success": true,
  "timestamp": 1698500000001,
  "payload": { ... },
  "error": null
}
```

### Message Types (implement ALL)

**Authentication:**
- `LOGIN` → `LOGIN_RESPONSE`
- `REGISTER` → `REGISTER_RESPONSE`
- `LOGOUT` → `LOGOUT_RESPONSE`
- `REFRESH_TOKEN` → `REFRESH_TOKEN_RESPONSE`
- `RESET_PASSWORD` → `RESET_PASSWORD_RESPONSE`

**User/Profile:**
- `GET_PROFILE` → `PROFILE_RESPONSE`
- `UPDATE_PROFILE` → `UPDATE_PROFILE_RESPONSE`
- `GET_FARMER_PROFILE` → `FARMER_PROFILE_RESPONSE`
- `UPDATE_FARMER_PROFILE` → `UPDATE_FARMER_PROFILE_RESPONSE`

**Products:**
- `LIST_PRODUCTS` → `PRODUCTS_RESPONSE` (with filters, pagination)
- `SEARCH_PRODUCTS` → `PRODUCTS_RESPONSE`
- `GET_PRODUCT` → `PRODUCT_RESPONSE`
- `CREATE_PRODUCT` → `PRODUCT_RESPONSE`
- `UPDATE_PRODUCT` → `PRODUCT_RESPONSE`
- `DELETE_PRODUCT` → `DELETE_RESPONSE`
- `UPDATE_STOCK` → `STOCK_RESPONSE`
- `GET_FARMER_PRODUCTS` → `PRODUCTS_RESPONSE`
- `GET_CATEGORIES` → `CATEGORIES_RESPONSE`

**Cart:**
- `GET_CART` → `CART_RESPONSE`
- `ADD_TO_CART` → `CART_RESPONSE`
- `UPDATE_CART_ITEM` → `CART_RESPONSE`
- `REMOVE_FROM_CART` → `CART_RESPONSE`
- `CLEAR_CART` → `CART_RESPONSE`
- `UNDO_CART_ACTION` → `CART_RESPONSE`

**Orders:**
- `VALIDATE_CHECKOUT` → `CHECKOUT_VALIDATION_RESPONSE`
- `PLACE_ORDER` → `ORDER_RESPONSE`
- `GET_ORDER` → `ORDER_RESPONSE`
- `GET_CUSTOMER_ORDERS` → `ORDERS_RESPONSE`
- `GET_FARMER_ORDERS` → `ORDERS_RESPONSE`
- `UPDATE_ORDER_STATUS` → `ORDER_RESPONSE`
- `CANCEL_ORDER` → `ORDER_RESPONSE`

**Reviews:**
- `CREATE_REVIEW` → `REVIEW_RESPONSE`
- `GET_FARMER_REVIEWS` → `REVIEWS_RESPONSE`
- `GET_ORDER_REVIEW` → `REVIEW_RESPONSE`

**Notifications:**
- `GET_NOTIFICATIONS` → `NOTIFICATIONS_RESPONSE`
- `MARK_NOTIFICATION_READ` → `NOTIFICATION_RESPONSE`
- `MARK_ALL_READ` → `NOTIFICATIONS_RESPONSE`

**Reports/Admin:**
- `CREATE_REPORT` → `REPORT_RESPONSE`
- `GET_REPORTS` → `REPORTS_RESPONSE` (admin)
- `RESOLVE_REPORT` → `REPORT_RESPONSE` (admin)
- `GET_USERS` → `USERS_RESPONSE` (admin)
- `SUSPEND_USER` → `USER_RESPONSE` (admin)
- `HIDE_LISTING` → `PRODUCT_RESPONSE` (admin)
- `GET_PLATFORM_SUMMARY` → `SUMMARY_RESPONSE` (admin)

**Files:**
- `EXPORT_ORDERS_CSV` → `FILE_RESPONSE`
- `EXPORT_SALES_CSV` → `FILE_RESPONSE`
- `IMPORT_PRODUCTS_CSV` → `IMPORT_RESPONSE` (admin)

**Real-time:**
- `SUBSCRIBE_NOTIFICATIONS` → starts push notifications
- `NOTIFICATION_PUSH` → server pushes to client

---

## 6. FUNCTIONAL REQUIREMENTS — IMPLEMENT ALL

### 6.1 Authentication & Registration (FR-001 to FR-007)
- [ ] Register with name, email/mobile, password, role selection
- [ ] Validate required fields, show clear error messages
- [ ] Hash passwords (never store plain text)
- [ ] Authenticate users, issue session tokens
- [ ] Logout invalidates session
- [ ] Password reset/recovery flow
- [ ] Edit profile, change profile photo
- [ ] Optional farmer verification with submitted documents

### 6.2 Farmer Profile & Farm Details (FR-008 to FR-011)
- [ ] Seller profile: display name, farm name, contact, location
- [ ] Fulfilment options: pickup, delivery, or both
- [ ] Farm description, practices, harvest frequency, service area
- [ ] Customer-facing profile shows name, location, listings, ratings

### 6.3 Product Listing Management (FR-012 to FR-020)
- [ ] Create listing: name, category, description, price, unit, quantity, images
- [ ] Support units: kg, g, litre, bunch, piece, dozen
- [ ] Edit, mark unavailable, remove own listings
- [ ] Display price/unit clearly (e.g., "₹40 per kg")
- [ ] Prevent invalid prices and negative stock
- [ ] Show availability, prevent ordering more than stock
- [ ] Labels: seasonal, newly harvested, organic, locally grown
- [ ] Harvest date and best-before notes
- [ ] Duplicate previous listing feature

### 6.4 Product Discovery & Search (FR-021 to FR-027)
- [ ] Browse all active listings
- [ ] Search by product name (full-text)
- [ ] Filter by category, price range, availability
- [ ] Sort by price, newest, highest-rated seller
- [ ] Filter by location/service area
- [ ] Product detail: image, price/unit, stock, farmer, fulfilment options
- [ ] Featured/seasonal/recently added sections

### 6.5 Cart & Checkout (FR-028 to FR-036)
- [ ] Add products to cart, change quantities, remove
- [ ] Validate stock and price at checkout
- [ ] Checkout summary: items, quantities, totals, delivery fee
- [ ] Select delivery or pickup
- [ ] Delivery address and contact for delivery orders
- [ ] Order review and explicit confirmation
- [ ] Generate unique order ID, show confirmation
- [ ] Note to farmer field
- [ ] Wishlist/saved products

### 6.6 Order Management & Tracking (FR-037 to FR-046)
- [ ] Customer views current and past orders
- [ ] Farmer views orders containing their products
- [ ] Farmer accept/reject with reason
- [ ] Order statuses: Pending → Accepted → Preparing → Ready for Pickup/Out for Delivery → Completed
- [ ] Also: Rejected, Cancelled
- [ ] Show status and history to customer
- [ ] Customer cancel while in cancellable state
- [ ] Update inventory on confirm/reject/cancel
- [ ] Notifications for status changes
- [ ] Mark ready for pickup or out for delivery

### 6.7 Payments & Receipts (FR-047 to FR-050)
- [ ] Cash on Delivery / Pay on Pickup / Demo payment
- [ ] Display payment method and status
- [ ] In-app order summary/receipt
- [ ] Placeholder for online payment extension

### 6.8 Ratings, Reviews & Trust (FR-051 to FR-055)
- [ ] Rate completed orders (1-5 stars)
- [ ] Prevent duplicate reviews per order
- [ ] Short text review of quality/fulfilment
- [ ] Report misleading listing/review/user
- [ ] Show average ratings only with sufficient data

### 6.9 Notifications & Communication (FR-056 to FR-058)
- [ ] In-app notifications after order placement and changes
- [ ] Push notifications for order events
- [ ] Optional masked contact/chat for order questions

### 6.10 Admin & Moderation (FR-059 to FR-063)
- [ ] Admin role restriction
- [ ] Review and hide/remove reported content
- [ ] Disable accounts with reason
- [ ] View platform summaries (users, listings, orders)
- [ ] Dispute tracking and resolution

### 6.11 Convenience & Accessibility (FR-064 to FR-068)
- [ ] Clear navigation: home, search, cart, orders, profile
- [ ] Readable text, adequate touch targets, clear contrast
- [ ] Empty states, loading/success/error messages
- [ ] Tamil and English language options
- [ ] Simplified farmer mode (larger controls, fewer steps)

### 6.12 Server, Admin Console, Files & Concurrency (FR-069 to FR-081)
- [ ] Server accepts multiple TCP connections via thread pool
- [ ] JSON message protocol for all operations
- [ ] **Synchronized stock reservation** — no overselling
- [ ] JDBC with PreparedStatement, transactions for orders
- [ ] Swing admin console with login and tabbed dashboard
- [ ] Event listeners and layout managers
- [ ] User-defined exceptions with clear messages
- [ ] CSV export (orders, sales) and import (demo products)
- [ ] Audit log for important events
- [ ] Producer-consumer notification queue (BlockingQueue)
- [ ] Cart undo (stack of actions)
- [ ] Farmer sales report with lambdas/streams
- [ ] Demo data seeder with instance main method

---

## 7. NON-FUNCTIONAL REQUIREMENTS — IMPLEMENT ALL

| ID | Requirement |
|----|-------------|
| NFR-01 | Common screens load within ~3 seconds |
| NFR-02 | Handle failed requests gracefully, no duplicate orders on retry |
| NFR-03 | Passwords hashed, never stored plain text |
| NFR-04 | Role permissions enforced server-side, not just UI |
| NFR-05 | Collect only necessary data, explain permissions |
| NFR-06 | Clear unit prices, final total shown before confirmation |
| NFR-07 | Test on emulator and physical device |
| NFR-08 | Separate models, UI, data access, business logic |
| NFR-09 | Scalable data model for growth |
| NFR-10 | Readable text, status not by color alone |
| NFR-11 | Validate totals/stock at checkout time |
| NFR-12 | Use documented dependencies, provide setup instructions |
| NFR-13 | Clear failure messages, retry option |
| NFR-14 | Audit timestamps and responsible user for status changes |
| NFR-15 | Thread-safe shared state (stock, sessions) |
| NFR-16 | Catch I/O/socket/JDBC exceptions, use try-with-resources |
| NFR-17 | Background threads for network/database/file work |

---

## 8. OOP DESIGN — IMPLEMENT ALL CONCEPTS

### Classes to Implement:

**Abstract Classes:**
- `User` (abstract) — userId, name, email, mobile, profileImage, status

**Inheritance Hierarchy:**
- `Customer extends User` — cart, order history
- `Farmer extends User` — listings, seller profile, incoming orders
- `Admin extends User` — moderation actions

**Interfaces:**
- `Entity` — getId() method
- `Repository<T extends Entity, ID>` — generic CRUD
- `PaymentMethod` — processPayment(), getMethodType()
- `NotificationService` — send(Notification)

**Interface Implementations:**
- `CashOnDelivery implements PaymentMethod`
- `PayOnPickup implements PaymentMethod`
- `InAppNotificationService implements NotificationService`
- `JdbcUserRepository implements UserRepository`
- etc.

**Enums with Behavior:**
```java
public enum OrderStatus {
    PENDING, ACCEPTED, PREPARING, READY_FOR_PICKUP, 
    OUT_FOR_DELIVERY, COMPLETED, REJECTED, CANCELLED;
    
    public Set<OrderStatus> getAllowedNextStatuses() {
        return switch (this) {
            case PENDING -> Set.of(ACCEPTED, REJECTED, CANCELLED);
            case ACCEPTED -> Set.of(PREPARING, CANCELLED);
            case PREPARING -> Set.of(READY_FOR_PICKUP, OUT_FOR_DELIVERY);
            case READY_FOR_PICKUP -> Set.of(COMPLETED);
            case OUT_FOR_DELIVERY -> Set.of(COMPLETED);
            case COMPLETED, REJECTED, CANCELLED -> Set.of();
        };
    }
    
    public boolean canTransitionTo(OrderStatus next) {
        return getAllowedNextStatuses().contains(next);
    }
}
```

**Custom Exceptions:**
```java
public class Farm2MarketException extends Exception { }
public class InsufficientStockException extends Farm2MarketException { }
public class InvalidStatusTransitionException extends Farm2MarketException { }
public class UnauthorizedActionException extends Farm2MarketException { }
```

**Generics:**
```java
public interface Repository<T extends Entity, ID> {
    T findById(ID id);
    List<T> findAll();
    T save(T entity);
    void delete(ID id);
}

public class Result<T> {
    private final T value;
    private final String error;
    public boolean isSuccess() { return error == null; }
}
```

**Multithreading:**
```java
// ClientHandler
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    @Override
    public void run() { /* handle client */ }
}

// NotificationDispatcher
public class NotificationDispatcher extends Thread {
    private final BlockingQueue<Notification> queue;
    @Override
    public void run() { /* consume and push */ }
}

// InventoryManager
public class InventoryManager {
    public synchronized boolean reserveStock(String productId, double quantity) { }
    public synchronized void releaseStock(String productId, double quantity) { }
}
```

**Collections:**
- `ArrayList` — cart items, orders, search results
- `Stack` — cart action undo history
- `Queue/BlockingQueue` — notification queue, pending orders
- `Map<String, Integer>` — stock counts
- `List<Double>` — ratings for average calculation

**Lambdas & Streams:**
```java
// Filter active products
products.stream()
    .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
    .filter(p -> p.getStock() > 0)
    .sorted(Comparator.comparing(Product::getCreatedAt).reversed())
    .collect(Collectors.toList());

// Sales report grouped by product
orders.stream()
    .flatMap(o -> o.getItems().stream())
    .collect(Collectors.groupingBy(
        OrderItem::getProductId,
        Collectors.summingDouble(OrderItem::getLineTotal)
    ));
```

**Pattern Matching (JDK 25):**
```java
// RequestRouter
return switch (request) {
    case LoginRequest r -> handleLogin(r);
    case PlaceOrderRequest r -> handlePlaceOrder(r);
    case UpdateStatusRequest r -> handleUpdateStatus(r);
    default -> new ErrorResponse("Unknown request type");
};
```

**Unnamed Patterns (JDK 25):**
```java
try {
    int quantity = Integer.parseInt(input);
} catch (NumberFormatException _) {
    showError("Invalid number");
}
```

---

## 9. SWING ADMIN CONSOLE — COMPLETE IMPLEMENTATION

### AdminLoginFrame
- JFrame with username/password fields
- Login button with ActionListener
- Connect to server, validate credentials
- On success, open AdminDashboardFrame

### AdminDashboardFrame
- JFrame with JTabbedPane
- Tabs: Reports, Listings, Users, Summary
- WindowListener for cleanup on close
- Menu bar with logout option

### ReportsPanel
- JTable showing open reports
- ListSelectionListener for row selection
- View report details
- Resolve/Dismiss buttons with ActionListener
- Add admin note

### ListingsPanel
- JTable showing all listings
- Search/filter controls
- View listing details
- Hide/Unhide button
- Delete button with confirmation

### UsersPanel
- JTable showing all users
- Filter by role
- View user details and order history
- Suspend/Activate button
- View associated reports

### SummaryPanel
- Platform statistics:
  - Total users by role
  - Active listings count
  - Orders today/this week/this month
  - Revenue summary
- JLabel displays
- Refresh button

### Layout Managers Used:
- `BorderLayout` — main frame layout
- `GridLayout` — form layouts
- `FlowLayout` — button panels
- `CardLayout` — switching views

---

## 10. ANDROID APP — COMPLETE UI

### Screens to Build:

**Auth Flow:**
- SplashActivity → check session
- LoginActivity → email/password, login button, register link
- RegisterActivity → name, email, mobile, password, role selection
- RoleSelectionActivity → Customer or Farmer cards

**Customer Flow:**
- CustomerHomeActivity
  - Search bar
  - Categories horizontal scroll
  - Featured products grid
  - Recent listings
  - Bottom navigation
- SearchActivity
  - Search input
  - Filter chips (category, price, availability)
  - Sort dropdown
  - Results RecyclerView
- ProductDetailActivity
  - Product images carousel
  - Name, price/unit, stock
  - Farmer info card (tap to view profile)
  - Description
  - Add to cart with quantity selector
- CartActivity
  - Cart items list with quantity +/-
  - Remove item
  - Subtotal display
  - Proceed to checkout
- CheckoutActivity
  - Order summary
  - Delivery/Pickup toggle
  - Address selection or input (for delivery)
  - Payment method selection
  - Note to farmer
  - Total breakdown
  - Place Order button
- OrderListActivity
  - Tabs: Active, Completed, Cancelled
  - Order cards with status
- OrderDetailActivity
  - Order items
  - Status timeline
  - Farmer contact
  - Cancel button (if eligible)
  - Rate button (if completed)
- ReviewActivity
  - Star rating
  - Text comment
  - Submit

**Farmer Flow:**
- FarmerDashboardActivity
  - Stats: active listings, pending orders, today's sales
  - Quick actions
  - Recent orders
- ManageListingsActivity
  - Listings list with status
  - Add new button
  - Edit/delete actions
- AddEditProductActivity
  - All product fields
  - Image picker (camera/gallery)
  - Validation
- IncomingOrdersActivity
  - Pending orders list
  - Accept/Reject actions
- OrderFulfilmentActivity
  - Order details
  - Status update buttons
- SalesSummaryActivity
  - Date range selector
  - Sales chart/stats
  - Export CSV

**Common:**
- ProfileActivity
- EditProfileActivity
- FarmerProfileActivity (public view)
- NotificationsActivity
- SettingsActivity
- HelpActivity
- ReportActivity

---

## 11. FILE I/O OPERATIONS

### CsvExporter
```java
public class CsvExporter {
    public void exportOrders(List<Order> orders, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Order ID,Customer,Date,Total,Status\n");
            for (Order order : orders) {
                writer.write(String.format("%s,%s,%s,%.2f,%s\n",
                    order.getId(),
                    order.getCustomerName(),
                    order.getCreatedAt(),
                    order.getTotal(),
                    order.getStatus()
                ));
            }
        }
    }
    
    public void exportSales(String farmerId, LocalDate from, LocalDate to, String filePath) {
        // Group by product, calculate totals
    }
}
```

### CsvImporter
```java
public class CsvImporter {
    public List<Product> importProducts(String filePath, String farmerId) throws IOException {
        List<Product> products = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String header = reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                // Parse and create Product
                products.add(product);
            }
        }
        return products;
    }
}
```

### AuditLogger
```java
public class AuditLogger {
    private static final String LOG_FILE = "audit.log";
    
    public synchronized void log(String eventType, String userId, String details) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.printf("%s|%s|%s|%s%n",
                LocalDateTime.now(),
                eventType,
                userId,
                details
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

---

## 12. TESTING REQUIREMENTS

### Test Cases to Implement:

| ID | Scenario | Expected |
|----|----------|----------|
| TC-01 | Register with missing fields | Blocked, field errors shown |
| TC-02 | Farmer enters negative price/stock | Validation rejects |
| TC-03 | Customer orders more than stock | Checkout blocked |
| TC-04 | Customer confirms valid order | Order ID created, visible to both |
| TC-05 | Farmer edits another's listing | Denied |
| TC-06 | Review incomplete order | Blocked |
| TC-07 | Invalid status transition | Rejected |
| TC-08 | Network fails during load | Error state, retry option |
| TC-09 | Customer cancels eligible order | Cancelled, inventory restored |
| TC-10 | Non-admin opens admin action | Access denied |
| TC-11 | Two clients order last 2kg simultaneously | Only one succeeds |
| TC-12 | Server stopped while app open | Connection error, no crash |
| TC-13 | Admin hides listing from console | Not in customer search |
| TC-14 | CSV export/import | Correct format, products appear |
| TC-15 | Invalid status via protocol | Exception raised, logged |
| TC-16 | DB unavailable during checkout | Transaction rolled back, no partial state |

---

## 13. IMPLEMENTATION ORDER

### Phase 1: Foundation (MVP Core)
1. Set up project structure (all 4 modules)
2. Create core module: models, enums, exceptions, interfaces
3. Database schema and connection
4. Server skeleton: ServerSocket, thread pool, basic routing
5. Basic protocol: LOGIN, REGISTER
6. Android: Login/Register screens

### Phase 2: Product Management
1. JDBC repositories for products
2. Server: product CRUD endpoints
3. Android: Farmer add/edit product
4. Android: Product listing, search, detail

### Phase 3: Cart & Orders
1. Cart implementation (server + Android)
2. InventoryManager with synchronization
3. Order creation with transaction
4. Order management (farmer accept/reject)
5. Status transitions with validation

### Phase 4: Admin Console
1. Swing login frame
2. Dashboard structure
3. Reports panel
4. Listings panel
5. Users panel
6. Summary panel

### Phase 5: Enhancements
1. Reviews and ratings
2. Notifications (in-app + push queue)
3. CSV export/import
4. Audit logging
5. Cart undo feature
6. Sales reports with streams

### Phase 6: Polish
1. Error handling throughout
2. UI polish and empty states
3. Testing all scenarios
4. Documentation

---

## 14. BUSINESS RULES — ENFORCE ALL

- BR-01: User has customer or farmer role; admin assigned separately
- BR-02: Only listing owner can edit/remove it
- BR-03: Only active listings with stock can be ordered
- BR-04: Order total from validated prices + fees
- BR-05: Order not Completed until fulfilment confirmed
- BR-06: Rejected/cancelled orders release reserved stock
- BR-07: Only eligible completed orders can be reviewed
- BR-08: "Organic"/"certified" claims not presented as verified without evidence
- BR-09: Status transitions follow allowed paths
- BR-10: Baseline uses manual fulfilment, no guaranteed delivery

---

## 15. SECURITY CHECKLIST

- [ ] Hash all passwords (BCrypt recommended)
- [ ] Validate session tokens on every request
- [ ] Server-side authorization checks (not just UI hiding)
- [ ] SQL injection prevention (PreparedStatement only)
- [ ] Input validation on all user data
- [ ] No sensitive data in logs
- [ ] Session expiration
- [ ] Rate limiting (optional but recommended)

---

## 16. DELIVERABLES CHECKLIST

- [ ] Core module with all models, enums, exceptions, interfaces
- [ ] Server with all endpoints, thread pool, synchronization
- [ ] JDBC repositories with transactions
- [ ] Admin console with all panels and functionality
- [ ] Android app with all screens for customer and farmer
- [ ] Database schema with seed data
- [ ] CSV export/import working
- [ ] Audit logging
- [ ] All 16 test cases passing
- [ ] Setup documentation

---

**Build this system completely. Do not skip any requirement, feature, or OOP concept. Each component must be production-quality code demonstrating the Java concepts listed above.**
