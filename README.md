# Producer-Consumer Problem - Java Concurrency Assignment

## Overview

This project demonstrates the **Producer-Consumer Problem**, a classic synchronization problem in concurrent programming.

```
┌──────────────────────────────────────────────────────────────────┐
│                    PRODUCER-CONSUMER PATTERN                      │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│   ┌──────────┐      ┌─────────────────┐      ┌──────────┐       │
│   │ PRODUCER │ ───► │  SHARED BUFFER  │ ───► │ CONSUMER │       │
│   │  Thread  │      │   (Bounded)     │      │  Thread  │       │
│   └──────────┘      └─────────────────┘      └──────────┘       │
│        │                    │                      │             │
│        │                    │                      │             │
│   Generates           Synchronized            Processes          │
│     data              access to               received           │
│                      shared data                data             │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

## The Problem

- **Producers** generate data and put it into a shared buffer
- **Consumers** take data from the buffer and process it
- The buffer has a **fixed capacity** (bounded buffer)

### Synchronization Challenges:
1. **Mutual Exclusion**: Only one thread should access the buffer at a time
2. **Buffer Full**: Producers must wait when buffer is full
3. **Buffer Empty**: Consumers must wait when buffer is empty
4. **No Deadlock**: The system should never get permanently stuck

## Project Structure

```
src/main/java/com/concurrency/
├── ProducerConsumerDemo.java     # Main entry point
├── buffer/
│   ├── Buffer.java               # Interface for buffer operations
│   ├── SynchronizedBuffer.java   # Classic wait/notify implementation
│   └── BlockingQueueBuffer.java  # Modern BlockingQueue implementation
├── producer/
│   └── Producer.java             # Producer thread implementation
└── consumer/
    └── Consumer.java             # Consumer thread implementation
```

## Two Implementations

### 1. SynchronizedBuffer (Classic Approach)
Uses Java's intrinsic locks with `synchronized`, `wait()`, and `notifyAll()`.

```java
public synchronized void put(T item) throws InterruptedException {
    while (queue.size() == capacity) {
        wait();  // Wait if buffer is full
    }
    queue.add(item);
    notifyAll();  // Wake up consumers
}
```

### 2. BlockingQueueBuffer (Modern Approach)
Uses `java.util.concurrent.ArrayBlockingQueue` which handles synchronization internally.

```java
public void put(T item) throws InterruptedException {
    queue.put(item);  // Blocks automatically if full
}
```

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## How to Build and Run

### Using Maven

```bash
# Compile the project
mvn compile

# Run the demo
mvn exec:java

# Package as JAR
mvn package

# Run the JAR
java -jar target/producer-consumer-1.0.0.jar
```

### Using javac directly

```bash
# Compile
javac -d out src/main/java/com/concurrency/**/*.java

# Run
java -cp out com.concurrency.ProducerConsumerDemo
```

## Sample Output

```
======================================================================
        PRODUCER-CONSUMER PROBLEM DEMONSTRATION
            Java Concurrency Assignment
======================================================================

  DEMO 1: SYNCHRONIZED BUFFER (wait/notifyAll)
----------------------------------------------------------------------
[Producer-1] Starting production of 5 items
[Producer-1] Producing item #1: 45001
[BUFFER] Item added. New size: 1/5
[Consumer-1] Waiting to consume item #1...
[BUFFER] Item removed. New size: 0/5
[Consumer-1] Processing item #1: 45001 (Item value squared: 2025090001)
...
```

## Key Concurrency Concepts Demonstrated

| Concept | Description |
|---------|-------------|
| **Thread** | Independent path of execution |
| **Synchronization** | Coordinating access to shared resources |
| **Mutual Exclusion** | Only one thread in critical section |
| **Wait/Notify** | Thread communication mechanism |
| **BlockingQueue** | Thread-safe queue with blocking operations |
| **Bounded Buffer** | Fixed-size buffer preventing memory overflow |

## Why Use BlockingQueue in Production?

1. **Thread-safe by design** - No need for explicit synchronization
2. **Well-tested** - Part of Java's standard library since Java 5
3. **Optimized** - Uses efficient locking mechanisms internally
4. **Less error-prone** - Reduces chance of deadlocks and race conditions

## Authors

Group 5 - Java Concurrency Assignment

## License

This is an educational project for learning purposes.

