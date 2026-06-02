# Producer-Consumer Simulation Using Semaphores and Mutex

## Project Description

This is a **JavaFX-based visual simulation** of the classic **Producer-Consumer synchronization problem** from Operating Systems. The application demonstrates how multiple producer and consumer threads coordinate access to a shared bounded buffer using **semaphores** and **mutex locks** to prevent race conditions.

**Producers** generate items and add them to a shared buffer with finite capacity. **Consumers** remove and process those items. Without proper synchronization, this scenario leads to race conditions where data can be corrupted or lost. This simulation shows how semaphores (`emptySlots` and `fullSlots`) and a mutex (`ReentrantLock`) solve the problem elegantly.

---

## Operating Systems Concepts Covered

| Concept | How It Is Demonstrated |
|---|---|
| **Threads** | Each producer and consumer runs in its own `java.lang.Thread`. |
| **Synchronization** | Threads coordinate via semaphores to avoid race conditions. |
| **Semaphore** | `Semaphore.emptySlots` and `Semaphore.fullSlots` control access to buffer slots. |
| **Mutex** | `ReentrantLock` ensures only one thread modifies the buffer at a time. |
| **Race Condition** | Without semaphores, producers/consumers would corrupt shared data. |
| **Critical Section** | The buffer enqueue/dequeue operations are protected by the mutex. |
| **Bounded Buffer** | A fixed-capacity queue shared between producers and consumers. |

---

## How the Simulation Works

### Producer Flow

1. **Wait for empty slot** — acquire the `emptySlots` semaphore (blocks if buffer is full).
2. **Enter critical section** — lock the mutex (`ReentrantLock`).
3. **Add item** — enqueue the item into the buffer queue.
4. **Exit critical section** — unlock the mutex.
5. **Signal consumers** — release the `fullSlots` semaphore.

### Consumer Flow

1. **Wait for filled slot** — acquire the `fullSlots` semaphore (blocks if buffer is empty).
2. **Enter critical section** — lock the mutex.
3. **Remove item** — dequeue the item from the buffer queue.
4. **Exit critical section** — unlock the mutex.
5. **Signal producers** — release the `emptySlots` semaphore.

### Simulation Stop Condition

The simulation stops automatically when:
- Total produced items reaches the configured target.
- Total consumed items reaches the configured target.
- The buffer becomes empty after all items are consumed.

The user can also click **Stop Simulation** at any time.

---

## How to Run

### Prerequisites

- Java 17 or later (JDK)
- Apache Maven 3.6+
- JavaFX 17+ (handled automatically by Maven dependencies)

### Steps

```bash
# Clone or navigate to the project directory
cd producer-consumer-fx

# Compile and run the application
mvn clean javafx:run
```

### Build a JAR

```bash
mvn clean package
```

---

## Screenshots

> Add screenshots here after running the application.

---

## Sample Output Logs

```
[09:15:00] === Simulation Started ===
[09:15:00] Buffer Size: 5, Producers: 2, Consumers: 2, Total Items: 30
[09:15:00] Producer 1 started
[09:15:00] Consumer 1 started
[09:15:00] Producer 2 started
[09:15:00] Consumer 2 started
[09:15:00] Producer 1 produced item 1
[09:15:00] Producer 2 produced item 2
[09:15:01] Consumer 1 consumed item 1
[09:15:01] Consumer 2 consumed item 2
[09:15:01] Producer 1 produced item 3
[09:15:01] Producer 2 produced item 4
[09:15:02] Consumer 1 consumed item 3
[09:15:03] Producer 1 is waiting because buffer is full
[09:15:03] Consumer 2 is waiting because buffer is empty
[09:15:04] Consumer 1 consumed item 4
[09:15:04] Producer 1 produced item 5
...
[09:18:30] === Simulation Completed ===
```

---

## Explanation for University Presentation

> "This project simulates the classic Producer-Consumer problem using Java threads and synchronization primitives. We have two types of threads — producers that generate data and consumers that process it — sharing a bounded buffer of fixed capacity. Without synchronization, producers might overwrite data or consumers might read garbage. We solve this using two semaphores: `emptySlots` tracks available space in the buffer, and `fullSlots` tracks available data. A `ReentrantLock` acts as a mutex to ensure that only one thread accesses the buffer at a time. The visual interface shows each slot of the buffer in real time, along with thread statuses and live statistics, making it easy to understand how semaphores and mutex prevent race conditions in concurrent systems."

---

## Project Structure

```
producer-consumer-fx/
├── pom.xml
├── README.md
└── src/main/java/com/os/producerconsumer/
    ├── MainApp.java                          # JavaFX entry point
    ├── controller/
    │   └── SimulationController.java         # UI layout and event handling
    ├── model/
    │   ├── BoundedBuffer.java                # Shared buffer with semaphores + mutex
    │   ├── BufferItem.java                   # Data item model
    │   └── SimulationStats.java              # Thread-safe statistics counters
    ├── service/
    │   ├── ProducerWorker.java               # Producer thread implementation
    │   ├── ConsumerWorker.java               # Consumer thread implementation
    │   └── SimulationService.java            # Simulation orchestration
    └── util/
        └── UiLogger.java                     # Timestamped logging utility
```

---

## Default Configuration

| Parameter             | Default Value |
|-----------------------|---------------|
| Buffer Size           | 5             |
| Number of Producers   | 2             |
| Number of Consumers   | 2             |
| Total Items to Produce| 30            |
| Producer Speed (ms)   | 700           |
| Consumer Speed (ms)   | 1000          |

---

## Technologies Used

- **Java 17** — Language features (records, lambdas, etc.)
- **JavaFX 17** — Graphical user interface
- **Maven** — Build and dependency management
- **java.util.concurrent.Semaphore** — Counting semaphores
- **java.util.concurrent.locks.ReentrantLock** — Mutex implementation
- **java.lang.Thread** — Concurrent execution

---

## Author

Created as a mini-project for the Operating Systems course.
