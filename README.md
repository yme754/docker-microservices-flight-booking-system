# Flight Booking System – Reactive Microservices (Secure & Containerized)

Java | 
Spring Boot | 
Docker | 
Kafka | 
MongoDB 

A fully **reactive**, **event-driven**, **secure microservices-based** Flight Booking System. This project demonstrates a production-grade architecture using **Spring Boot WebFlux**, **Reactive MongoDB**, **Apache Kafka**, **Spring Security (JWT)**, and full containerization via **Docker**.

The system implements a **Microservices Architecture** where independent services communicate asynchronously, with all external traffic routed through a secure **API Gateway**.

---

## 1. Microservices Overview

The ecosystem consists of **7 distinct components** working in harmony. Each service is containerized, independently deployable, and communicates asynchronously.

### Core Business Services

#### Flight Service (`:8082`)
- **Responsibility:** Manages flight inventory, airlines, and seat availability.
- **Tech:** Spring WebFlux, Reactive MongoDB.
- **Key Features:**
  - Reactive CRUD operations for Flights and Airlines.
  - Handles real-time seat tracking.

#### Booking Service (`:8083`)
- **Responsibility:** Handles ticket reservation logic.
- **Tech:** Spring WebFlux, Reactive MongoDB, Spring Kafka.
- **Key Features:**
  - Validates booking requests against flight inventory.
  - **Producer:** Publishes `booking-created` and `booking-cancelled` events to Kafka.
  - Asynchronous non-blocking transaction management.

#### Email Service (`:8084`)
- **Responsibility:** Sends notifications to users based on system events.
- **Tech:** Spring Boot, Spring Kafka, JavaMailSender.
- **Key Features:**
  - **Consumer:** Listens to Kafka topics for booking events.
  - Decoupled from the main booking flow to ensure zero latency for the user.

#### Security Service (`:9091`)
- **Responsibility:** Centralized Authentication and Authorization.
- **Tech:** Spring Security, JWT (JSON Web Tokens).
- **Key Features:**
  - Manages User Registration (`/signup`) and Login (`/signin`).
  - Issues signed **JWT Access Tokens**.
  - Encrypts passwords using BCrypt.

---

### Infrastructure Services

#### API Gateway (`:9090`)
- **Responsibility:** Single entry point for all external traffic.
- **Tech:** Spring Cloud Gateway.
- **Key Features:**
  - **Security Filter:** Validates JWT tokens before routing requests.
  - Dynamic Routing based on service discovery.
  - Load Balancing.

#### Service Registry (`:8761`)
- **Responsibility:** Service Discovery.
- **Tech:** Spring Cloud Netflix Eureka.
- **Key Features:**
  - All microservices register themselves here upon startup.
  - Allows services to find each other by name (e.g., `flight-service`) instead of hardcoded IPs.

#### Config Server (`:8888`)
- **Responsibility:** Centralized Configuration Management.
- **Tech:** Spring Cloud Config.
- **Key Features:**
  - Stores properties for all microservices (DB URLs, Kafka ports, Secrets) in one place.
  - Enables configuration changes without redeploying the entire system.
## 2. Tech Stack

### Backend Technologies
* Java 17
* Spring Boot
* Spring WebFlux
* Spring Cloud Gateway
* Spring Cloud Netflix Eureka
* Spring Cloud Config
* Spring Security (Reactive)
* JSON Web Tokens (JWT)
* Spring Cloud Circuit Breaker (Resilience4j)
* Spring Cloud OpenFeign
* Spring Kafka
* Reactive MongoDB
* Lombok
* Maven

### Reactive Framework
* **Project Reactor**:
    * `Mono<T>`: async single value
    * `Flux<T>`: async stream

### Database & Persistence
* MongoDB (Reactive Driver)
    * Data Model: `flightdb`, `bookingdb`, `flight_security_db`

### Message Broker (Event-Driven)
* Apache Kafka
* Zookeeper


### DevOps & Infrastructure
* Docker
* Docker Compose

### Testing Tools
* JUnit 5
* Mockito
* WebTestClient
* Reactor Test
* JaCoCo(~90% coverage).
* SonarQube Cloud

### Performance Testing
* Apache JMeter
 
## 3. System Features

### Microservice Architecture
The application is decomposed into **7 independent, containerized microservices**. Each service has its own database and communicates via **REST APIs (HTTP)** or **Kafka Events**, ensuring loose coupling and high scalability.

### Reactive Non-Blocking Design
All core services (Gateway, Flight, Booking) are built on **Spring WebFlux** and **Project Reactor**, enabling high-throughput, non-blocking asynchronous processing ideal for handling thousands of concurrent users.

---

### Flight Service Features (`:8082`)
- **Inventory Management:** Register new flights and airlines (Admin role).
- **Real-Time Availability:** Update seat counts atomically upon booking.
- **Advanced Search:** Query flights by route (Origin -> Destination) and date.
- **Reactive Data Access:** Uses `ReactiveMongoRepository` for non-blocking DB interactions.

---

### Booking Service Features (`:8083`)
- **Booking Workflow:** Handles seat reservation and PNR generation.
- **Event Driven:** Publishes booking events to Kafka topics asynchronously.
- **Circuit Breaker (Resilience4j):** Wraps calls to the Flight Service to handle failures gracefully.
- **Asynchronous Processing:** Transactions are handled reactively without blocking threads.

---

### Security Service Features (`:9091`)
- **JWT Authentication:** Issues signed JSON Web Tokens (JWT) upon successful login.
- **Role-Based Access Control (RBAC):** Manages roles (e.g., `USER`, `ADMIN`).
- **Secure Password Storage:** Uses **BCrypt** hashing for storing user credentials.
- **Token Validation:** Provides logic for validating token signatures and claims.

---

### Email Service Features (`:8084`)
- **Kafka Consumer:** Listens to `booking-created` and `booking-cancelled` topics.
- **Notification System:** Sends automated emails to users with booking details.
- **Decoupled Architecture:** Failure in email sending does not impact the main booking transaction.

---

### API Gateway Features (`:9090`)
- **Central Entry Point:** Routes all external traffic to the appropriate microservice.
- **Security Filter:** Intercepts requests to validate the `Authorization: Bearer <token>` header before routing.
- **Load Balancing:** Distributes traffic across service instances.
- **Dynamic Routing:** Automatically discovers service locations via Eureka.

---

### Config Server Features (`:8888`)
- **Centralized Configuration:** Stores properties (`application.yml`) for all services in a single location.
- **Dynamic Updates:** Allows changing configuration without rebuilding service JARs.
- **Profile Management:** Simplifies switching between `dev`, `docker`, and `prod` environments.

---

### Service Registry (Eureka) (`:8761`)
- **Service Discovery:** Maintains a live registry of all active microservices.
- **Health Checks:** Monitors service heartbeats and removes unhealthy instances automatically.

---

### Kafka Event Streams
The system uses an event-driven architecture for critical side effects:

| Topic | Trigger | Consumer Action |
| :--- | :--- | :--- |
| `booking-created` | User confirms a booking | **Email Service** sends a "Confirmation" email. |
| `booking-cancelled` | User cancels a booking | **Email Service** sends a "Cancellation" email. |

---

### Circuit Breaker (Resilience4j)
Implemented in the **Booking Service** to protect against Flight Service failures.
- **Fail Fast:** Immediately returns an error if the downstream service is unresponsive.
- **Fallback:** Provides default responses or helpful error messages to the user instead of hanging.

### DTO Layer
- **Separation of Concerns:** Controllers accept/return DTOs (Data Transfer Objects), not Database Entities.
- **Security:** Prevents leaking internal database structures (like IDs or audit fields) to the client API.

---

## 4. Architecture Diagram

<img width="918" height="449" alt="Dockerised Architecture Diagram" src="https://github.com/user-attachments/assets/e8163c40-e599-46ea-9ed6-1f54ad54e80c" />
