# AI-flow

_The service provides an Intric proxy with predefined workflows for writing documents._

## Getting Started

### Prerequisites

- **Java 21 or higher**
- **Maven**
- **MariaDB**
- **Git**

### Installation

1. **Clone the repository:**

```bash
git clone https://github.com/Sundsvallskommun/api-service-ai-flow.git
cd api-service-ai-flow
```

2. **Configure the application:**

   Before running the application, you need to set up configuration settings.
   See [Configuration](#configuration)

   **Note:** Ensure all required configurations are set; otherwise, the application may fail to start.

3. **Ensure dependent services are running:**

   *Templating*

   - Purpose: Used to render templates.
   - Repository: https://github.com/Sundsvallskommun/api-service-templating
   - Setup Instructions: See documentation in repository above for installation and configuration steps.

   *Intric*

   - Purpose: Intric is an AI platform, a LLM is used to execute the predefined workflow.
4. **Build and run the application:**

- Using Maven:

```bash
mvn spring-boot:run
```

- Using Gradle:

```bash
gradle bootRun
```

## API Documentation

Access the API documentation via:

- **Swagger UI:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Usage

### API Endpoints

See the [API Documentation](#api-documentation) for detailed information on available endpoints.

### Example Request

```bash
curl -X 'GET' 'https://localhost:8080/2281/flow'
```

## Configuration

Configuration is crucial for the application to run successfully. Ensure all necessary settings are configured in
`application.yml`.

### Key Configuration Parameters

- **Server Port:**

```yaml
server:
  port: 8080
```

- **Database Settings**

```yaml
spring:
  datasource:
    driver-class-name: org.mariadb.jdbc.Driver
    username: <db_username>
    password: <db_password>
    url: jdbc:mariadb://<db_host>:<db_port>/<database>
  jpa:
    properties:
      jakarta:
        persistence:
          schema-generation:
            database:
              action: validate
  flyway:
    enabled: <true|false> # Enable if you want to run Flyway migrations
```

- **Integration Settings**

```yaml
integration:
  templating:
    base-url: <templating-url>
    connect-timeout-in-seconds: <connect-timeout-in-seconds>
    read-timeout-in-seconds: <read-timeout-in-seconds>
    oauth2:
      token-url: <oauth2-token-url>
      client-id: <oauth2-client-id>
      client-secret: <oauth2-client-secret>
      authorization-grant-type: <authorization-grant-type>
```

- **Scheduler Settings**

```yaml
scheduler:
  stale-session-reader:
    cron:
      expression: <cron-expression>
    shedlock-lock-at-most-for: <maximum-lock-duration>
    maximum-execution-time: <maximum-execution-time> 
```

### Database Initialization

The project is set up with [Flyway](https://github.com/flyway/flyway) for database migrations. Flyway is disabled by
default so you will have to enable it to automatically populate the database schema upon application startup.

```yaml
spring:
  flyway:
    enabled: true
```

- **No additional setup is required** for database initialization, as long as the database connection settings are
  correctly configured.

### Additional Notes

- **Application Profiles:**

  Use Spring profiles (`dev`, `prod`, etc.) to manage different configurations for different environments.

- **Logging Configuration:**

  Adjust logging levels if necessary.

- **Dynamic clients:**

  The application uses dynamic clients feign and rest clients to connect to Intric. This enables different
  municipalities to have their own instances of Intric. Each instance is stored in the database and a new instance can
  be added via the API.

## Contributing

Contributions are welcome! Please
see [CONTRIBUTING.md](https://github.com/Sundsvallskommun/.github/blob/main/.github/CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the [MIT License](LICENSE).

## Status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-ai-flow&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-ai-flow)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-ai-flow&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-ai-flow)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-ai-flow&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-ai-flow)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-ai-flow&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-ai-flow)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-ai-flow&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-ai-flow)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-ai-flow&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-ai-flow)

## 

Copyright (c) 2023 Sundsvalls kommun
