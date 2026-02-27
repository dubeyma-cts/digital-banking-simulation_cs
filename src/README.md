# ibn-enterprise-app

This is a multi-module Maven project structured around a 3-tier architecture, consisting of a front-end Angular application and back-end Spring Boot services. The project is designed to allow independent build and deployment of each module.

## Project Structure

- **ibn-ui**: Angular front-end application.
  - Contains all the necessary files for the Angular application, including components, services, and configuration files.

- **ibn-core-svc**: Core back-end service.
  - Implements the controller, service, and DAO layers using Spring MVC and Spring Data JPA with Hibernate.
  - Utilizes Spring AOP for cross-cutting concerns.

- **ibn-message-svc**: Middleware message broker service.
  - Implements a reliable messaging service using Apache ActiveMQ.

- **ibn-external-svc**: External service module.
  - Provides additional functionalities and integrations with external systems.

## Technologies Used

- **Front-end**: Angular
- **Back-end**: Spring Boot
- **Database**: Oracle
- **Messaging**: Apache ActiveMQ
- **Build Tool**: Maven

## Modules

### ibn-ui
- Angular application for user interface.

### ibn-core-svc
- Core service with:
  - Controller: `com.ibn.controller`
  - Service: `com.ibn.service`
  - DAO: `com.ibn.dao`
  - AOP: `com.ibn.aop`
  - Configuration: `com.ibn.config`

### ibn-message-svc
- Message broker service with:
  - Producer: `com.ibn.message.producer`
  - Listener: `com.ibn.message.listener`
  - Configuration: `com.ibn.message.config`

### ibn-external-svc
- External service with:
  - Controller: `com.ibn.external.controller`
  - Service: `com.ibn.external.service`
  - DAO: `com.ibn.external.dao`

## Build and Deployment

Each module can be built and deployed independently using Maven commands. Ensure that the necessary configurations are set in the respective `pom.xml` files for each module.

## Getting Started

1. Clone the repository.
2. Navigate to the root directory of the project.
3. Build the project using Maven:
   ```
   mvn clean install
   ```
4. Follow the instructions in each module's README for specific build and deployment steps.

## Contributing

Contributions are welcome! Please submit a pull request or open an issue for any enhancements or bug fixes.