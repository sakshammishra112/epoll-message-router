# Real-Time Chat System  
**Spring Boot + WebSocket + JWT + C++ epoll Message Engine**

A **production-grade real-time chat system** composed of:

- **Spring Boot application** for authentication, APIs, WebSocket handling, presence, and persistence
- **High-performance C++ epoll-based TCP server** for low-latency message routing

This project demonstrates **distributed system design**, **event-driven I/O**, **secure WebSocket communication**, and **low-level network programming**.

---

## 🚀 Key Features

### 🔐 Authentication & Security (Spring Boot)
- JWT-based stateless authentication
- Secure login & registration
- Authenticated WebSocket handshake
- Role-ready Spring Security architecture

### 💬 Real-Time Messaging
- One-to-one chat
- Email-based messaging (backend resolves email → userId)
- Messages routed via **C++ epoll engine**
- Reliable delivery using ACK-based protocol
- Offline message buffering & resend

### 👥 Friends System
- Auto-add friend on first message
- Friends-only chat enforcement
- Friends list management
- Unread message counts per friend

### 🔵 Presence & Activity
- Online / offline presence (WebSocket lifecycle-based)
- Presence snapshot for late joiners
- Typing indicators (ephemeral, WebSocket-only)

### ✓✓ Message State
- Persistent message storage (MySQL)
- Unread tracking
- Read receipts
- Conversation history loading

---

## 🧠 System Architecture

```
Browser (Thymeleaf + JS)
        │
        │ WebSocket (JWT-secured)
        ▼
Spring Boot Application
    ├─ Authentication (JWT)
    ├─ REST APIs
    ├─ WebSocket Gateway
    ├─ Friend Management
    ├─ Presence & Typing
    └─ Message Persistence (MySQL)
        │
        │ TCP (Custom Binary Protocol)
        ▼
C++ epoll Message Engine
    ├─ Non-blocking TCP sockets
    ├─ epoll-based event loop
    ├─ Message routing
    ├─ ACK handling
    └─ Offline buffering
```

---

## 🧩 Component Responsibilities

### Spring Boot (Control Plane)
- User authentication & authorization
- WebSocket session management
- Presence & typing indicators
- Friend relationships
- Message persistence
- REST APIs

### C++ epoll Server (Data Plane)
- High-throughput message routing
- Single-threaded, event-driven I/O
- Binary protocol parsing
- ACK-based reliability
- Offline message buffering

---

## ⚡ C++ Epoll-Based Chat Server

A **high-performance, single-threaded TCP chat server** built using Linux `epoll` and non-blocking sockets.

### 🚀 Features
- epoll-based event loop (scales to many connections)
- Non-blocking TCP sockets
- Custom binary protocol
- User login with unique IDs
- Reliable delivery via ACKs
- Offline message buffering
- Zero threads, zero external libraries

### 📡 Binary Protocol Format

```
+------------+----------+------------+-------------+
| Length (4) | Type (2) | Msg ID (8) | Payload (...)|
+------------+----------+------------+-------------+
```

| Type | Name | Description |
|------|------|-------------|
| 1 | LOGIN | User login |
| 2 | SEND | Send message |
| 3 | MSG | Server → client message |
| 4 | ACK | Acknowledgement |

- Network byte order
- Partial read/write safe
- Offline messages resent on login

---

## 🛠 Tech Stack

### Backend (Java)
- Java 23
- Spring Boot
- Spring Security (JWT)
- Spring WebSocket
- Hibernate / JPA
- MySQL

### Message Engine (C++)
- C++
- epoll
- TCP sockets
- Custom binary protocol
- CMake

### Frontend
- Thymeleaf
- Vanilla JavaScript
- HTML / CSS
- WebSocket API

---

## 📁 Repository Structure

```
chat-system/
├── springboot-server/
│   ├── src/
│   ├── pom.xml
│   └── README.md (optional)
│
├── epoll-server/
│   ├── CMakeLists.txt
│   ├── include/
│   │   ├── connection.h
│   │   ├── protocol.h
│   │   ├── net_utils.h
│   │   └── server_state.h
│   └── src/
│       ├── main.cpp
│       ├── server.cpp
│       ├── protocol.cpp
│       └── net_utils.cpp
│
└── README.md
```

---

## 🗄 Database Schema (Core)

### users
- id
- first_name
- last_name
- email (unique)
- password_hash
- enabled
- created_at

### friends
- user_id
- friend_id
- status (ACCEPTED)

### messages
- id
- sender_id
- receiver_id
- content
- is_read
- delivered
- created_at

---

## ▶️ Running the System

### 1️⃣ Start the C++ Message Engine

```bash
cd epoll-server-v5
mkdir build && cd build
cmake ..
make -j
./server
```

**Expected output:**

```
epoll server listening on port 9000
```

### 2️⃣ Configure Database (Spring Boot)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/chat
spring.datasource.username=root
spring.datasource.password=*****
```

## 🗄 Database Setup

```sql
CREATE DATABASE chat;

```
mysql -u root -p chat < db/schema.sql
```

### 3️⃣ Run Spring Boot Server

```bash
cd springConnector
mvn springConnector:run
```

### 4️⃣ Open Browser

```
http://localhost:8080/login
```

---

## 🔵 Presence Model

- Presence is connection-based, not login-based
- Tracked in-memory via WebSocket lifecycle
- Presence snapshot sent to late joiners
- Robust against crashes, refreshes, and network drops

---

## ✍️ Typing Indicators

- WebSocket-only events
- No persistence
- Auto-expire after inactivity
- Per-conversation visibility

---

## 🧪 Testing & Validation

### C++ Server Testing
- Manually construct binary frames
- Send LOGIN, SEND, ACK frames
- Inject malformed or partial frames
- Validate epoll robustness

### Spring Boot Testing
- JWT auth validation
- WebSocket reconnect scenarios
- Presence sync
- Unread count reset
- Offline message delivery

---

## 📚 Learning Objectives

This project demonstrates:

- epoll-based event loops
- Non-blocking I/O
- Binary protocol design
- WebSocket authentication
- Distributed system boundaries
- Hybrid Java + C++ architecture
- Real-time system correctness

---

## 🔮 Future Enhancements

- Group chats
- File sharing
- Push notifications
- Redis-backed presence
- Message pagination
- Mobile client support
- TLS encryption for TCP engine

---

## 👨‍💻 Author

**Saksham Mishra**  
Backend & Systems Developer  
Java | Spring Boot | C++ | epoll | Networking
