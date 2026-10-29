# Farm2Market

A direct farmer-to-buyer marketplace mobile application built with Java. This project consists of three components:

1. **Android App** - Customer and farmer mobile application
2. **Java Socket Server** - Backend server with JDBC/SQLite
3. **Java Swing Admin Console** - Desktop admin management tool

## Project Structure

```
farm2market/
├── android/          # Android mobile app (Kotlin/Java)
├── server/           # Java socket server with JDBC
├── admin/            # Java Swing admin console
├── core/             # Shared models, DTOs, and protocol
├── gradle/           # Gradle wrapper
├── build.gradle      # Root build configuration
└── settings.gradle   # Module settings
```

## Prerequisites

- **Java 17** or higher
- **Android Studio** (for Android app development)
- **Gradle 8.x** (included via wrapper)

## Quick Start

### 1. Start the Server

```bash
cd farm2market
./gradlew :server:run
```

The server will start on **port 8888** with seed data automatically loaded.

### 2. Run the Admin Console

```bash
cd farm2market
./gradlew :admin:run
```

A Swing GUI will open. Login with admin credentials (see below).

### 3. Build the Android App

```bash
cd farm2market
./gradlew :android:assembleDebug
```

The APK will be generated at:
```
android/app/build/outputs/apk/debug/app-debug.apk
```

#### Running on Android Emulator

1. Install the APK on your emulator
2. The app connects to `10.0.2.2:8888` (Android emulator's alias for host machine)

#### Running on Physical Device

1. Find your computer's IP address
2. Update the server host in the Android app's network configuration
3. Ensure your device and computer are on the same network

## Demo Credentials

### Admin Account

| Field | Value |
|-------|-------|
| Email | `admin@farm2market.com` |
| Password | `password123` |

### Farmer Accounts

| Name | Email | Password |
|------|-------|----------|
| Ravi Kumar | `ravi@demo.com` | `password123` |
| Lakshmi Devi | `lakshmi@demo.com` | `password123` |
| Muthu Selvam | `muthu@demo.com` | `password123` |
| Anitha Krishnan | `anitha@demo.com` | `password123` |

### Customer Accounts

| Name | Email | Password |
|------|-------|----------|
| Priya Sharma | `priya@demo.com` | `password123` |
| Arun Prakash | `arun@demo.com` | `password123` |

## Features

### Customer Features
- Browse products by category
- Search products
- Add to cart and checkout
- Track order status
- Review farmers
- Receive notifications

### Farmer Features
- Manage product listings
- Accept/reject orders
- Update order status
- View reviews and ratings
- Receive order notifications

### Admin Features (Desktop Console)
- Platform statistics dashboard
- User management (view, suspend, activate)
- Product listing moderation
- Report management
- Export orders/sales to CSV

## Architecture

### Communication Protocol

The system uses a TCP socket-based protocol with JSON messages:

```
Client → Server: {"type":"MESSAGE_TYPE","token":"session_token","payload":{...}}
Server → Client: {"type":"RESPONSE_TYPE","success":true,"payload":{...}}
```

### Database

- **SQLite** for development (default)
- **MySQL** support available (configure in `server.properties`)

### Key Technologies

| Component | Technologies |
|-----------|-------------|
| Android App | Kotlin, Jetpack Compose, Material 3 |
| Server | Java 17, Socket, JDBC, HikariCP, BCrypt |
| Admin Console | Java Swing, FlatLaf Look & Feel |
| Shared Core | Java models, DTOs, Protocol classes |

## Configuration

### Server Configuration

Edit `server/src/main/resources/server.properties`:

```properties
server.port=8888
server.max.connections=100
db.type=sqlite
db.url=jdbc:sqlite:farm2market.db
```

### Database Reset

To reset the database with fresh seed data:

```bash
rm -f server/farm2market.db
./gradlew :server:run
```

## Development

### Building All Modules

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

### Clean Build

```bash
./gradlew clean build
```

## Sample Data

The server loads sample data on startup including:

- **32 products** across categories (Vegetables, Fruits, Grains, Pulses, Dairy, Eggs)
- **7 users** (1 admin, 4 farmers, 2 customers)
- **6 sample orders** with various statuses
- **5 sample reports** for admin review
- **Product images** from Unsplash

## Troubleshooting

### Server won't start - "Address already in use"

```bash
# Kill existing Java processes
pkill -f java
# Or find and kill the specific process
lsof -i :8888
kill <PID>
```

### Admin console shows all zeros

Ensure you're running the latest server code. This was a bug that has been fixed.

### Android app can't connect to server

1. Check server is running: `lsof -i :8888`
2. For emulator: Server should be accessible at `10.0.2.2:8888`
3. For physical device: Use your computer's local IP and ensure firewall allows port 8888

### Database issues

Delete the database file and restart the server:

```bash
rm -f server/farm2market.db
./gradlew :server:run
```

## License

This project is for educational purposes.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request
