# Product Requirements Document (PRD)
## Project: Deadlock Detection in OS — Banker's Algorithm Simulator
**Subjects:** Java | DMGT | Operating Systems  
**Version:** 1.0  
**Status:** Draft

---

## 1. Project Overview

This project simulates an Operating System's process-resource allocation mechanism to **detect deadlocks** using the **Banker's Algorithm**. It is a Java-based console/GUI application that accepts real-time input of processes, resource types, allocation matrices, and request matrices, and determines whether the system is in a **safe state** or a **deadlock state**.

---

## 2. Objectives

- Simulate OS-level resource allocation between multiple processes.
- Implement Banker's Algorithm for **deadlock detection and avoidance**.
- Accept dynamic user input for all system dimensions and matrices.
- Display a **safe sequence** if the system is safe, or identify **deadlocked processes** if not.
- Visualize the step-by-step execution of the algorithm.

---

## 3. Scope

### In Scope
- Input of system dimensions (number of processes `n`, number of resource types `m`)
- Input of Total/Existing Resources vector
- Input of Allocation Matrix (`n × m`)
- Input of Request Matrix (`n × m`) — what each process is currently requesting
- Auto-calculation of Available Vector from Total Resources − Sum of Allocation
- Banker's Algorithm execution (Safety Algorithm)
- Output: Safe Sequence OR list of deadlocked processes
- Step-by-step trace of algorithm execution

### Out of Scope
- Real OS process scheduling
- Network/distributed deadlock detection
- GUI animation (Phase 1 — console only)
- Database persistence of inputs

---

## 4. Functional Requirements

| ID | Requirement |
|----|-------------|
| FR-01 | System shall accept `n` (number of processes) as integer input |
| FR-02 | System shall accept `m` (number of resource types) as integer input |
| FR-03 | System shall accept Total Resources vector of size `m` |
| FR-04 | System shall accept Allocation Matrix of size `n × m` |
| FR-05 | System shall accept Request Matrix of size `n × m` |
| FR-06 | System shall auto-compute Available Vector = Total − ColumnSum(Allocation) |
| FR-07 | System shall run the Banker's Safety Algorithm on given inputs |
| FR-08 | System shall display a safe execution sequence if system is in safe state |
| FR-09 | System shall identify and display deadlocked processes if unsafe |
| FR-10 | System shall show step-by-step trace (Work vector, Finish array per step) |
| FR-11 | System shall validate all inputs (no negative values, matrix dimensions match) |
| FR-12 | System shall allow re-running with new inputs without restarting |

---

## 5. Non-Functional Requirements

| ID | Requirement |
|----|-------------|
| NFR-01 | Application must run on standard JDK 8+ |
| NFR-02 | Console input must be intuitive with clear prompts |
| NFR-03 | Algorithm must execute in O(n² × m) time complexity |
| NFR-04 | Code must be modular — separate classes for Input, Algorithm, Output |
| NFR-05 | Code must be well-commented for academic submission |

---

## 6. Input Specification

### A. System Dimensions
```
Enter number of processes (n): 5
Enter number of resource types (m): 3
```

### B. Total Resources Vector
```
Enter total units for each resource type:
Resource 0 (e.g., CPU):    10
Resource 1 (e.g., Memory): 5
Resource 2 (e.g., Disk):   7
```

### C. Allocation Matrix (n × m)
```
Enter Allocation Matrix (units currently held by each process):
         R0  R1  R2
P0:       0   1   0
P1:       2   0   0
P2:       3   0   2
P3:       2   1   1
P4:       0   0   2
```

### D. Request Matrix (n × m)
```
Enter Request Matrix (units each process is currently requesting):
         R0  R1  R2
P0:       7   4   3
P1:       1   2   2
P2:       6   0   0
P3:       0   1   1
P4:       4   3   1
```

### E. Available Vector (Auto-Computed)
```
Available = Total − Sum of each column in Allocation Matrix
Available: [3, 3, 2]
```

---

## 7. Output Specification

### Safe State Output
```
=== BANKER'S ALGORITHM — DEADLOCK DETECTION ===

Available Resources: [3, 3, 2]

--- Safety Algorithm Trace ---
Step 1: P1 can execute. Work = [5, 3, 2]
Step 2: P3 can execute. Work = [7, 4, 3]
Step 3: P0 can execute. Work = [7, 5, 3]
Step 4: P2 can execute. Work = [10, 5, 5]
Step 5: P4 can execute. Work = [10, 5, 7]

✅ System is in a SAFE STATE.
Safe Sequence: P1 → P3 → P0 → P2 → P4
```

### Deadlock State Output
```
=== BANKER'S ALGORITHM — DEADLOCK DETECTION ===

Available Resources: [1, 0, 0]

--- Safety Algorithm Trace ---
Step 1: No process can proceed...

❌ DEADLOCK DETECTED!
Deadlocked Processes: P0, P2, P4
These processes are waiting for resources that will never be released.
```

---

## 8. Algorithm — Banker's Safety Algorithm

```
Input:  n, m, Allocation[n][m], Request[n][m], Available[m]
Output: Safe sequence OR deadlocked processes

1. Work[m]   = Available[m]
2. Finish[n] = { false, false, ..., false }

3. REPEAT:
   Find process Pi such that:
     a) Finish[Pi] == false
     b) Request[Pi][j] <= Work[j]  for all j
   
   If found:
     Work[j]    = Work[j] + Allocation[Pi][j]  for all j
     Finish[Pi] = true
     Add Pi to safe sequence
   
   If not found:
     BREAK

4. If all Finish[Pi] == true → SAFE STATE, return safe sequence
   Else → DEADLOCK, return { Pi | Finish[Pi] == false }
```

---

## 9. Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java (JDK 8+) |
| Input | Scanner (Console) |
| Output | System.out (Console) |
| Build | Manual (`javac` / IntelliJ / VS Code) |
| Testing | Manual test cases |

---

## 10. Deliverables

- [ ] `Main.java` — Entry point
- [ ] `InputHandler.java` — All user input logic
- [ ] `BankersAlgorithm.java` — Core algorithm
- [ ] `OutputPrinter.java` — Formatted output
- [ ] `DeadlockResult.java` — Result data model
- [ ] `prd.md` — This document
- [ ] `design.md` — UI/UX design document
- [ ] `structure.md` — File structure document
- [ ] Test case document (manual)

---

## 11. Constraints & Assumptions

- Maximum processes: 20, Maximum resource types: 10 (for console readability)
- All input values are non-negative integers
- The system does not consider process priority
- A process that has completed (Finish=true) releases all its allocated resources
- Available vector is derived — not entered manually by user

---

*End of PRD*
