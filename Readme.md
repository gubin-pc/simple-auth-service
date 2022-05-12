# Simple Auth Service

## What was done

1. ...

## How to run
### In docket container
```
mvn clean package docker:build docker:run
```
- To run application on a specific port use
  ```-Dservice.port=```

Example: 
```
mvn clean package docker:build docker:run -Dservice.port=9870
```