# Restaurant Kitchen

**Software Design Patterns — Assignment 4**

## Description

Restaurant Kitchen is a simple Java console application using the Facade Design Pattern.

The system manages restaurant orders, kitchen stations, payments, and delivery.

## Design Pattern

I used the **Facade Pattern** to make the system easier to use.

The project has three subsystems:

- **A — Order:** Checks orders and ingredients.
- **B — Kitchen:** Finds a free kitchen station and checks readiness.
- **C — Billing:** Processes payment and registers delivery.

`RestaurantFacade` connects these three subsystems.

`AdminFacade` helps manage and check the system.

## Features

- Create orders for Burger, Pizza, and Pasta.
- Check available ingredients.
- Assign kitchen stations.
- Process test payments.
- Save data in JSON files.
- Cancel orders when an error happens.
- Recover unfinished orders after restarting.

## Technologies

- Java 17
- Maven
- Jackson JSON
- JUnit 5
- JaCoCo

## How to Run

Open the terminal in the project folder.

Run the application:

```bash
mvn clean compile exec:java
```

Run tests:

```bash
mvn clean verify
```

## Error Handling

The system handles three main errors:

1. **Not enough ingredients:** The order is rejected.
2. **Kitchen station is busy:** The order is cancelled and ingredients are released.
3. **Payment declined:** The kitchen task is cancelled first, then the ingredient reservation is cancelled.

## Facade vs Adapter

Facade makes a complex system easier to use.

Adapter helps two incompatible interfaces work together.

I chose Facade because my application needs to connect three subsystems in one simple workflow.

## Principle of Least Knowledge

Without Facade, the client needs to work with Order, Kitchen, and Billing separately.

With Facade, the client only needs to call one method:

```java
restaurant.start("Burger", 2, "Astana", true, 3);
```

This makes the code simpler and reduces dependencies.

## UML Diagram

The UML class diagram is in the `docs` folder.

## Conclusion

In this assignment, I learned how the Facade Pattern works in Java. I also learned how to connect independent subsystems, save data in JSON files, handle errors, and write unit tests.