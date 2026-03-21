# Docker Project Setup Plan

This plan outlines the steps to build the Java microservices and run the entire project using Docker Compose.

## Proposed Changes

### Build Microservices
The Java microservices in `commercetools-wrapper/` need to be compiled and packaged into JAR files before Docker can build the images.

- Run `mvn clean package -DskipTests` in the `commercetools-wrapper/` directory.

### Run Docker Compose
Start all services defined in the `docker-compose.yml` file.

- Run `docker-compose up --build -d` in the `commercetools-wrapper/` directory.

## Verification Plan

### Automated Verification
- Check the status of all containers: `docker-compose ps`
- Verify the API Gateway is reachable: `curl http://localhost:8085/actuator/health` (assuming it has actuator) or a basic endpoint.
- Verify the Frontend is reachable: `curl http://localhost:3001` or check via browser.

### Manual Verification
1. Access the frontend at `http://localhost:3001` in the browser.
2. Verify that products are loaded from the `product-service`.
3. Test a basic user flow (e.g., adding an item to the cart).
