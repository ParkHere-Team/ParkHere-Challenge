# How to run the project?

1. Clone the repository
2. Run `docker-compose up` in the root directory of the project
3. This project uses configuration-service to fetch the available parking lot.
   - Install Prism using `npm install -g @stoplight/prism-cli`
   - Run `prism mock api-specs/configuration-service-api-specs.yaml` in the root directory of the project
   - it should run on `http://localhost:4010`.
4. Run the project using `./gradlew bootRun` in the root directory of the project. Alternatively, you can run the project using your preferred IDE. e.g: IntelliJ IDEA
5. The project will be running on `http://localhost:8080`
6. Now you can create a reservation using the following curl command:
   ```shell
   curl --location 'http://localhost:8080/api/parking-lots/1/reservations' \
   --header 'Content-Type: application/json' \
   --data-raw '{
       "userId": "test-user@parkhere.eu",
       "startTimestamp": 1741927800000,
       "endTimestamp": 1741934800000 
   }'
   ```
   
# System Design
 - The system design can be found in the `system-design` directory. The design is created using draw.io and can be found in `docs/architechture/system_architechture_components.png` file.
 - The terraform scripts to deploy the system can be found in the `docs/terraform` directory.

# Optional tasks added to the project
 - This project uses API-first approach and generate in api interface from api-specs found in `api-specs` directory.
 - After the project compilation, the generated API interface can be found in `build/generated-sources/openapi` directory.
 - The gradle build scripts then copies the generated API interface to the `src/main/kotlin` directory.
 - This project uses `utils/Logger.kt` as the logging solution.
 - This project uses `utils/ExceptionHandler.kt` as the exception handling solution.
 - Custom reservation exception classes are created in `reservation/exception` package.
 - A unit test is added for the `ReservationService` class and can be found under test as `ReservationServiceTest`.
 - For easy access, the database credentials are in application-yaml file (Not recommended for production).


