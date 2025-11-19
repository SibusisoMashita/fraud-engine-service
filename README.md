# Fraud Rule Engine Service

A production-grade backend service that evaluates financial transactions using a rule-based fraud detection engine.  
Built with **Spring Boot**, **PostgreSQL**, **Flyway**, **OpenAPI/Swagger**, and **Docker**.

This project was developed as part of a Software Engineer technical assessment, and demonstrates:
- Clean domain-driven architecture  
- Proper layering (Controller → Service → Rule Engine → Persistence)  
- Robust validation & global error handling  
- A modular, extensible fraud-rule pipeline  
- End-to-end testing with Docker + Postgres  
- Fully documented REST API (Swagger/OpenAPI)

## 🚀 Features

### ✔ Rule-based fraud evaluation pipeline  
The engine applies multiple rules to each transaction:

- High-Value Transaction Rule  
- Velocity Rule  
- Impossible Travel Rule  
- Merchant Blacklist Rule  
- Off-Hours High-Risk Rule  

Each rule produces a `RuleResult`, and the combined score determines fraud severity.

### ✔ REST API with Swagger UI  
- `POST /api/v1/transactions` → Evaluate transaction  
- `GET /api/v1/fraud/{transactionId}` → View decision + rule breakdown  
- `GET /api/v1/fraud/flagged` → List flagged transactions with filters  

### ✔ PostgreSQL + Flyway migrations  
Database schema is version-controlled and auto-bootstrapped.

### ✔ Global Exception Handling  
Consistent, safe JSON error responses.

### ✔ Modular design  
Rules are auto-discovered — adding a new rule = create a class.

## 🧱 Architecture Overview

Controller → DTO → Service → Rule Engine → Persistence → Database

## 🛠️ Tech Stack

Java 21, Spring Boot 3.3.4, PostgreSQL, Flyway, Swagger/OpenAPI, Maven, Docker

## 📦 Project Structure

src/main/java/com/fraudengine  
├── api  
├── domain  
├── dto  
├── exception  
├── mapper  
├── repository  
└── service

## 🐳 Running with Docker

docker-compose up --build

## 📘 API Documentation

Swagger UI: http://localhost:8080/swagger-ui.html  
OpenAPI JSON: http://localhost:8080/v3/api-docs

