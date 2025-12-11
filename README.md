📘 Banking System – Microservices Project (Spring Boot + Cloud)

This project is developed as part of the Virtusa Java FSD Program – Phase 2
with focus on Microservices, Spring Cloud, Feign, Resilience, MongoDB, and CI-ready architecture.

🚀 Microservices Included
Service	Description	Port
Account Service	Manages accounts, balance, update, delete	8081
Transaction Service	Handles deposit, withdraw, transfer	8082
Notification Service	Receives and logs notifications	8083
API Gateway	Routes external requests to microservices	8080
Eureka Server	Service registry for discovery	8761
🧩 Tech Stack

Java 17

Spring Boot 3

Spring Cloud 2024

Eureka Server / Discovery Client

Spring Cloud Gateway

Feign Client

MongoDB

Resilience / Circuit Breaker (Manual Fallback)

JUnit + Mockito

🔗 Service Interaction Flow
Client → API Gateway → Transaction Service → Feign → Account Service
↓
Notification Service

⚙️ How to Run Services (Order Matters)
1️⃣ Start Eureka Server
http://localhost:8761

2️⃣ Start Account Service
3️⃣ Start Transaction Service
4️⃣ Start Notification Service
5️⃣ Start API Gateway
🧪 Testing APIs
Create Account
POST http://localhost:8080/api/accounts/create

Deposit
POST http://localhost:8080/api/transactions/deposit

Withdraw
POST http://localhost:8080/api/transactions/withdraw

Transfer
POST http://localhost:8080/api/transactions/transfer

🔥 Features Implemented

✔ Full microservices architecture
✔ Communication via Feign Client
✔ Service registry with Eureka
✔ API Gateway routing
✔ Manual circuit breaker for Account-Service failure
✔ MongoDB persistence
✔ Logging using SLF4J + Logback
✔ Unit tests (JUnit + Mockito) with 70%+ coverage

🧑‍💻 Developed By

Sarthak Joshi
Virtusa Java Full Stack Developer Program – Phase 2
(2025)