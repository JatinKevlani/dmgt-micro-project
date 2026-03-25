# File Structure Document
## Project: Deadlock Detection in OS — Banker's Algorithm Simulator
**Version:** 1.0

---

## 1. Root Directory Structure

```
DeadlockDetector/
│
├── src/
│   └── deadlock/
│       ├── Main.java
│       ├── InputHandler.java
│       ├── BankersAlgorithm.java
│       ├── OutputPrinter.java
│       └── model/
│           ├── SystemState.java
│           └── DeadlockResult.java
│
├── docs/
│   ├── prd.md
│   ├── design.md
│   └── structure.md
│
├── test/
│   ├── TestCase1_SafeState.txt
│   ├── TestCase2_Deadlock.txt
│   └── TestCase3_EdgeCase.txt
│
├── out/                        ← compiled .class files go here
│
└── README.md
```

---

## 2. File-by-File Breakdown

---

### `src/deadlock/Main.java`
**Role:** Entry point of the application.

**Responsibilities:**
- Displays the welcome banner
- Creates instances of `InputHandler`, `BankersAlgorithm`, `OutputPrinter`
- Drives the main program loop (run again? Y/N)
- Handles top-level try-catch for unexpected crashes

**Key method:**
```java
public static void main(String[] args)
```

---

### `src/deadlock/InputHandler.java`
**Role:** Handles ALL user input from the console.

**Responsibilities:**
- Reads `n` (processes) and `m` (resource types)
- Reads Total Resources vector `[m]`
- Reads Allocation Matrix `[n][m]`
- Reads Request Matrix `[n][m]`
- Computes Available Vector = Total − ColumnSum(Allocation)
- Validates all inputs (non-negative, correct dimensions)
- Returns a populated `SystemState` object

**Key methods:**
```java
public SystemState collectInput()
private int readPositiveInt(String prompt)
private int[][] readMatrix(String label, int n, int m)
private int[] computeAvailable(int[] total, int[][] allocation, int m)
```

---

### `src/deadlock/BankersAlgorithm.java`
**Role:** Core algorithm implementation.

**Responsibilities:**
- Accepts a `SystemState` object
- Runs the Banker's Safety Algorithm
- Tracks Work vector and Finish array at each step
- Builds safe sequence or identifies deadlocked processes
- Returns a `DeadlockResult` object

**Key methods:**
```java
public DeadlockResult runSafetyAlgorithm(SystemState state)
private boolean canExecute(int process, int[] work, SystemState state)
```

**Algorithm Steps (implemented here):**
```
1. Work = Available.copy()
2. Finish[i] = false for all i
3. Loop: find Pi where Finish[Pi]=false AND Request[Pi] ≤ Work
4. If found: Work += Allocation[Pi], Finish[Pi] = true
5. Repeat until no more found
6. If all Finish = true → SAFE; else → DEADLOCK
```

---

### `src/deadlock/OutputPrinter.java`
**Role:** Handles ALL output formatting and printing.

**Responsibilities:**
- Prints the welcome banner (Screen 1)
- Prints section headers with box borders
- Prints the input summary table (Screen 7)
- Prints step-by-step algorithm trace (Screen 8)
- Prints final result — safe or deadlock (Screen 9A / 9B)
- Applies ANSI color codes (green/red/cyan/yellow)

**Key methods:**
```java
public void printWelcomeBanner()
public void printInputSummary(SystemState state)
public void printTrace(List<String> traceLog)
public void printSafeResult(List<Integer> safeSequence)
public void printDeadlockResult(List<Integer> deadlockedProcs)
private String formatRow(int[] row, int width)
private void printBoxHeader(String title)
```

---

### `src/deadlock/model/SystemState.java`
**Role:** Data model — holds all input data for one simulation run.

**Fields:**
```java
int n;                  // number of processes
int m;                  // number of resource types
int[] totalResources;   // Total[m]
int[][] allocation;     // Allocation[n][m]
int[][] request;        // Request[n][m]
int[] available;        // Available[m] — computed
```

**Methods:**
```java
// Getters and setters for all fields
// toString() for debug printing
```

---

### `src/deadlock/model/DeadlockResult.java`
**Role:** Data model — holds the result of the algorithm.

**Fields:**
```java
boolean isSafe;                  // true = safe state, false = deadlock
List<Integer> safeSequence;      // order of process execution (if safe)
List<Integer> deadlockedProcs;   // deadlocked process IDs (if unsafe)
List<String> traceLog;           // step-by-step trace messages
```

**Methods:**
```java
// Getters and setters
// isSafe() convenience method
```

---

### `docs/prd.md`
Product Requirements Document — defines what the system does, inputs, outputs, constraints, algorithm, and deliverables.

---

### `docs/design.md`
UI/UX Design Document — defines how every screen looks in the console, formatting rules, color scheme, and validation messages.

---

### `docs/structure.md`
This file — defines the complete file/folder structure and responsibility of each file.

---

### `test/TestCase1_SafeState.txt`
Sample input that results in a **safe state**.
```
n=5, m=3
Total: [10, 5, 7]
Allocation:
  P0: 0 1 0
  P1: 2 0 0
  P2: 3 0 2
  P3: 2 1 1
  P4: 0 0 2
Request:
  P0: 7 4 3
  P1: 1 2 2
  P2: 6 0 0
  P3: 0 1 1
  P4: 4 3 1
Expected Output: SAFE — Sequence: P1 → P3 → P0 → P2 → P4
```

---

### `test/TestCase2_Deadlock.txt`
Sample input that results in a **deadlock**.
```
n=3, m=2
Total: [2, 2]
Allocation:
  P0: 1 1
  P1: 1 0
  P2: 0 1
Request:
  P0: 1 1
  P1: 1 1
  P2: 1 0
Expected Output: DEADLOCK — Deadlocked: P0, P1, P2
```

---

### `test/TestCase3_EdgeCase.txt`
Edge case — single process, single resource.
```
n=1, m=1
Total: [3]
Allocation: P0: [2]
Request:    P0: [1]
Available:  [1]
Expected Output: SAFE — Sequence: P0
```

---

### `README.md`
```
# Deadlock Detection Simulator

## How to Compile
  javac -d out src/deadlock/**/*.java src/deadlock/*.java

## How to Run
  java -cp out deadlock.Main

## Project Structure
  See docs/structure.md

## Algorithm
  Banker's Safety Algorithm — See docs/prd.md Section 8
```

---

## 3. Class Dependency Diagram

```
Main.java
  │
  ├──► InputHandler.java
  │         └──► model/SystemState.java
  │
  ├──► BankersAlgorithm.java
  │         ├──► model/SystemState.java  (reads)
  │         └──► model/DeadlockResult.java  (writes)
  │
  └──► OutputPrinter.java
            └──► model/DeadlockResult.java  (reads)
```

---

## 4. Data Flow

```
User Input
    │
    ▼
InputHandler.collectInput()
    │ returns SystemState
    ▼
BankersAlgorithm.runSafetyAlgorithm(state)
    │ returns DeadlockResult
    ▼
OutputPrinter.printResult(result)
    │
    ▼
Console Output (Safe Sequence or Deadlock List)
```

---

## 5. Compilation & Run Commands

```bash
# From project root

# Compile all Java files
javac -d out src/deadlock/model/SystemState.java \
             src/deadlock/model/DeadlockResult.java \
             src/deadlock/InputHandler.java \
             src/deadlock/BankersAlgorithm.java \
             src/deadlock/OutputPrinter.java \
             src/deadlock/Main.java

# Run the program
java -cp out deadlock.Main
```

Or using an IDE (IntelliJ IDEA / Eclipse / VS Code with Java Extension Pack) — simply open the project root and run `Main.java`.

---

*End of Structure Document*
