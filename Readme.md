# Simple Auth Service

## How to run
### In docker container

- To run application on 8080 port use
```
./mvnw clean package docker:build docker:run
```
- To run application on a specific port use
  ```-Dservice.port=```

Example: 
```
./mvnw clean package docker:build docker:run -Dservice.port=9870
```

## Implementation note

1. H2 is used as a database.
2. Written plugin for ktor to validate roles.
3. Interfaces for the service layer are not used, since there are no other implementations, and they are not expected
4. Kotest used for testing API part only
5. UI part is written in simple HTML + CSS + JS