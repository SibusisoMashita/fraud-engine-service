# fraud-engine-service

## 🚀 Overview
Fraud Rule Engine Service is a production-grade backend service designed to evaluate financial transactions using a rule-based fraud detection engine. Built with **Spring Boot**, **PostgreSQL**, **Flyway**, **OpenAPI/Swagger**, and **Docker**, this service demonstrates clean domain-driven architecture, proper layering, robust validation, and a modular, extensible fraud-rule pipeline.

## ✨ Features
- **Rule-based fraud evaluation pipeline**: Applies multiple rules to each transaction, including High-Value Transaction Rule, Velocity Rule, Impossible Travel Rule, Merchant Blacklist Rule, and Off-Hours High-Risk Rule.
- **REST API with Swagger UI**: Provides endpoints for evaluating transactions and viewing decisions and rule breakdowns.
- **PostgreSQL + Flyway migrations**: Database schema is version-controlled and auto-bootstrapped.
- **Global Exception Handling**: Consistent, safe JSON error responses.
- **Modular design**: Rules are auto-discovered, allowing easy addition of new rules.

## 🛠️ Tech Stack
- **Programming Language**: Java 21
- **Frameworks**: Spring Boot 3.3.4
- **Database**: PostgreSQL
- **Migration Tool**: Flyway
- **API Documentation**: Swagger/OpenAPI
- **Build Tool**: Maven
- **Containerization**: Docker

## 📦 Installation

### Prerequisites
- Java 21
- Maven
- Docker
- PostgreSQL

### Quick Start
```bash
# Clone the repository
git clone https://github.com/yourusername/fraud-engine-service.git

# Navigate to the project directory
cd fraud-engine-service

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

### Alternative Installation Methods
- **Docker**: Use the provided `docker-compose.yml` file to run the application in a container.
  ```bash
  docker-compose up --build
  ```

## 🔐 Login & Authentication (Default Credentials)
After the service starts, obtain a JWT token using the default admin credentials.

- Username: `admin`
- Password: `password123`

Example:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'
```
Successful response returns JSON with a `token` field:
```json
{
  "token": "<JWT_TOKEN>",
  "expiresIn": 3600
}
```
Use the token in subsequent requests:
```bash
curl http://localhost:8080/api/v1/transactions \
  -H "Authorization: Bearer <JWT_TOKEN>"
```
Security note: Change the default password in non-local environments.

## 🎯 Usage

### Basic Usage
```java
// Example of how to use the Fraud Rule Engine Service
TransactionRequest request = TransactionRequest.builder()
    .transactionId("tx123")
    .customerId("cust123")
    .amount(new BigDecimal("15000"))
    .timestamp(LocalDateTime.now())
    .merchant("SafeMerchant")
    .channel("Online")
    .build();

TransactionController controller = new TransactionController();
TransactionResponse response = controller.processTransaction(request);
System.out.println(response);
```

### Advanced Usage
- **Configuration**: Customize the application by modifying the `application-local.yml` file.
- **API Documentation**: Access the Swagger UI at `http://localhost:8080/swagger-ui.html` and the OpenAPI JSON at `http://localhost:8080/v3/api-docs`.

## 📁 Project Structure
```
fraud-engine-service
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── fraudengine
│   │   │           ├── api
│   │   │           ├── config
│   │   │           ├── domain
│   │   │           ├── dto
│   │   │           ├── exception
│   │   │           ├── mapper
│   │   │           ├── repository
│   │   │           ├── service
│   │   │           └── service
│   │   │               └── rules
│   │   └── resources
│   │       ├── application-local.yml
│   │       └── application.yml
│   └── test
│       └── java
│           └── com
│               └── fraudengine
│                   └── FraudEngineServiceApplicationTests.java
├── .gitignore
├── pom.xml
└── README.md
```

## 🔧 Configuration
- **Environment Variables**: Configure environment variables in the `application-local.yml` file.
- **Configuration Files**: Modify the `application-local.yml` file for local development settings.


🐳 Run the Application Locally

Running the Fraud Engine Service locally is easy using Docker Compose.
This starts PostgreSQL, applies Flyway migrations, and boots the Spring Boot service automatically.

✅ Prerequisites

Install Docker Desktop
https://www.docker.com/products/docker-desktop/

✅ 1. Open a terminal inside the project
cd fraud-engine-service

✅ 2. Start the entire stack (App + PostgreSQL)
docker compose up --build


This will:

Start PostgreSQL

Run Flyway DB migrations

Build the application

Start the Fraud Engine REST API

✅ 3. Verify the service is running

Swagger UI:

http://localhost:8080/swagger-ui/index.html


Health check:

http://localhost:8080/api/v1/health

✅ 4. Stop all containers

Press CTRL + C, or run:

docker compose down

🎉 Done!

You now have a complete, production-like environment running with one simple command.

## 📚 Documentation
- Confluence space: https://fraudruleengine.atlassian.net/wiki/spaces/SD/folder/98348?atlOrigin=eyJpIjoiNWE2YmVkZGRkYWFkNDkxYzk2ODUzMGQ5MzE4Mjg1MjQiLCJwIjoiYyJ9

## 🤝 Contributing
- **How to Contribute**: Fork the repository, create a new branch, and submit a pull request.
- **Development Setup**: Clone the repository and run `mvn clean install` to build the project.
- **Code Style Guidelines**: Follow the Java coding conventions and use Lombok for boilerplate code reduction.
- **Pull Request Process**: Ensure your code is well-tested and documented. Include a clear description of the changes.

## 📝 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors & Contributors
- **Maintainers**: Sibusiso.Mashita@gmail.com

## 🐛 Issues & Support
- **Report Issues**: Create a new issue on the GitHub repository.
- **Get Help**: Join the project's discussion forum or contact the maintainers via email.
- **FAQ**: [Link to FAQ]

## 🗺️ Roadmap
- **Planned Features**: [List of planned features]
- **Known Issues**: [List of known issues]
- **Future Improvements**: [List of future improvements]

---

**Badges:**
[![Build Status](https://travis-ci.org/yourusername/fraud-engine-service.svg?branch=main)](https://travis-ci.org/yourusername/fraud-engine-service)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.java.com/en/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-green.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-13-blue.svg)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-8.5.8-blue.svg)](https://flywaydb.org/)
[![Swagger](https://img.shields.io/badge/Swagger-3.0.0-blue.svg)](https://swagger.io/)
[![Docker](https://img.shields.io/badge/Docker-20.10.7-blue.svg)](https://www.docker.com/)

---

This README is designed to be comprehensive and engaging, providing clear instructions and examples to help developers get started with the Fraud Rule Engine Service.
