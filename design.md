# Design Document (UI/UX)
## Project: Deadlock Detection in OS — Banker's Algorithm Simulator
**Version:** 1.0

---

## 1. Design Philosophy

Since this is a **Java console application**, the "UI" is the terminal interface. The design goals are:

- **Clarity** — Every prompt tells the user exactly what to enter
- **Structure** — Inputs are grouped in logical sections with clear headers
- **Readability** — Matrix outputs use aligned columns and borders
- **Feedback** — Every step of the algorithm is narrated to the user
- **Color (Optional)** — ANSI escape codes for colored output (✅ green = safe, ❌ red = deadlock)

---

## 2. Console Screen Flow (Screen-by-Screen)

---

### SCREEN 1 — Welcome Banner

```
╔══════════════════════════════════════════════════════════════╗
║        DEADLOCK DETECTION SIMULATOR — BANKER'S ALGORITHM     ║
║              Java | OS | DMGT  |  Microproject               ║
╚══════════════════════════════════════════════════════════════╝

  Subjects  : Operating Systems, DMGT, Java
  Algorithm : Banker's Safety Algorithm
  Purpose   : Detect if system is in safe or deadlock state

──────────────────────────────────────────────────────────────
Press ENTER to begin...
```

---

### SCREEN 2 — System Dimensions Input

```
╔══════════════════════════════╗
║   STEP 1: System Dimensions  ║
╚══════════════════════════════╝

  Enter number of Processes      (n): _
  Enter number of Resource Types (m): _

  [Processes will be labeled: P0, P1, P2 ...]
  [Resources will be labeled: R0, R1, R2 ...]
```

**Validation rules shown inline:**
```
  ⚠  Value must be between 1 and 20.
```

---

### SCREEN 3 — Total Resources Input

```
╔══════════════════════════════════╗
║   STEP 2: Total Resources Vector ║
╚══════════════════════════════════╝

  Enter the TOTAL units available in the system for each resource:

  R0 (total): _
  R1 (total): _
  R2 (total): _

  Total Resources = [ R0  R1  R2 ]
                    [  _   _   _ ]
```

---

### SCREEN 4 — Allocation Matrix Input

```
╔═══════════════════════════════════════╗
║   STEP 3: Allocation Matrix (n × m)   ║
╚═══════════════════════════════════════╝

  Enter units CURRENTLY HELD by each process:
  (How many units of each resource does each process already have?)

  Process P0:
    R0: _   R1: _   R2: _

  Process P1:
    R0: _   R1: _   R2: _

  Process P2:
    R0: _   R1: _   R2: _

  ...

  ┌─ Allocation Matrix ──────────────────┐
  │         R0    R1    R2               │
  │  P0  [   0     1     0  ]            │
  │  P1  [   2     0     0  ]            │
  │  P2  [   3     0     2  ]            │
  └──────────────────────────────────────┘
```

---

### SCREEN 5 — Request Matrix Input

```
╔════════════════════════════════════════╗
║   STEP 4: Request Matrix (n × m)       ║
╚════════════════════════════════════════╝

  Enter units CURRENTLY REQUESTED by each process:
  (How many additional units does each process need right now?)

  Process P0:
    R0: _   R1: _   R2: _

  ...

  ┌─ Request Matrix ─────────────────────┐
  │         R0    R1    R2               │
  │  P0  [   7     4     3  ]            │
  │  P1  [   1     2     2  ]            │
  │  P2  [   6     0     0  ]            │
  └──────────────────────────────────────┘
```

---

### SCREEN 6 — Computed Available Vector

```
╔══════════════════════════════════════════════╗
║   Computed: Available Resources Vector        ║
╚══════════════════════════════════════════════╝

  Formula: Available[j] = Total[j] − Σ Allocation[i][j]

  Total Resources   :  [ 10   5   7 ]
  Sum of Allocation :  [  7   2   5 ]
                         ─────────────
  Available         :  [  3   3   2 ]

  ✔ Available vector computed successfully.
```

---

### SCREEN 7 — Full Input Summary Table

```
╔══════════════════════════════════════════════════════════╗
║                  INPUT SUMMARY                           ║
╚══════════════════════════════════════════════════════════╝

  Processes : 5 (P0 – P4)     Resource Types : 3 (R0 – R2)

  ┌──────────────────────────────────────────────────────┐
  │         ALLOCATION          │         REQUEST         │
  │  Proc   R0   R1   R2        │   R0   R1   R2          │
  ├─────────────────────────────┼─────────────────────────┤
  │  P0      0    1    0        │    7    4    3           │
  │  P1      2    0    0        │    1    2    2           │
  │  P2      3    0    2        │    6    0    0           │
  │  P3      2    1    1        │    0    1    1           │
  │  P4      0    0    2        │    4    3    1           │
  └──────────────────────────────────────────────────────┘

  Total Resources :  [ 10   5   7 ]
  Available       :  [  3   3   2 ]

  Confirm and Run Algorithm? (Y/N): _
```

---

### SCREEN 8 — Algorithm Execution Trace

```
╔══════════════════════════════════════════════════════════╗
║            BANKER'S ALGORITHM — EXECUTION TRACE          ║
╚══════════════════════════════════════════════════════════╝

  Initial Work (Available) : [ 3   3   2 ]
  Finish Array             : [ F   F   F   F   F ]

  ──────────────────────────────────────────────────────────
  ITERATION 1:
    Checking P0: Request[0] = [7,4,3] > Work [3,3,2] → ✗ SKIP
    Checking P1: Request[1] = [1,2,2] ≤ Work [3,3,2] → ✔ EXECUTE
    → Work  = Work + Allocation[P1] = [3,3,2] + [2,0,0] = [5,3,2]
    → Finish[P1] = true
    → Safe Sequence so far: < P1 >

  ITERATION 2:
    Checking P0: Request[0] = [7,4,3] > Work [5,3,2] → ✗ SKIP
    Checking P2: Request[2] = [6,0,0] > Work [5,3,2] → ✗ SKIP
    Checking P3: Request[3] = [0,1,1] ≤ Work [5,3,2] → ✔ EXECUTE
    → Work  = [5,3,2] + [2,1,1] = [7,4,3]
    → Finish[P3] = true
    → Safe Sequence so far: < P1, P3 >

  ... (continues for all iterations)
  ──────────────────────────────────────────────────────────
```

---

### SCREEN 9A — Safe State Result

```
╔══════════════════════════════════════════════════════════╗
║                      RESULT                              ║
╚══════════════════════════════════════════════════════════╝

  ✅  SYSTEM IS IN A SAFE STATE

  Safe Execution Sequence:
  ┌────────────────────────────────────────────┐
  │   P1  →  P3  →  P0  →  P2  →  P4          │
  └────────────────────────────────────────────┘

  All processes can complete without deadlock.
  No process will be left waiting indefinitely.

  ──────────────────────────────────────────────
  Run again with new inputs? (Y/N): _
```

---

### SCREEN 9B — Deadlock State Result

```
╔══════════════════════════════════════════════════════════╗
║                      RESULT                              ║
╚══════════════════════════════════════════════════════════╝

  ❌  DEADLOCK DETECTED!

  The following processes are in DEADLOCK:
  ┌────────────────────────────────────────────┐
  │   P0,  P2,  P4                             │
  └────────────────────────────────────────────┘

  These processes are permanently blocked.
  Their resource requests cannot be fulfilled.
  They will wait INDEFINITELY → DEADLOCK.

  ──────────────────────────────────────────────
  Run again with new inputs? (Y/N): _
```

---

## 3. Input Validation Messages

| Scenario | Message Shown |
|----------|---------------|
| Negative value entered | `⚠ Error: Value cannot be negative. Please re-enter.` |
| Non-integer input | `⚠ Error: Only integers are accepted.` |
| n or m = 0 | `⚠ Error: Must have at least 1 process and 1 resource type.` |
| Available goes negative | `⚠ Error: Allocation exceeds Total Resources for R[j]. Check inputs.` |
| Dimension mismatch | `⚠ Error: Matrix row/column count does not match n or m.` |

---

## 4. ANSI Color Scheme (Optional Enhancement)

| Element | ANSI Code | Color |
|---------|-----------|-------|
| Section Headers | `\u001B[36m` | Cyan |
| ✅ Safe State | `\u001B[32m` | Green |
| ❌ Deadlock | `\u001B[31m` | Red |
| ⚠ Warnings | `\u001B[33m` | Yellow |
| Matrix Data | `\u001B[37m` | White |
| Reset | `\u001B[0m` | Default |

Usage in Java:
```java
System.out.println("\u001B[32m✅ SYSTEM IS IN A SAFE STATE\u001B[0m");
System.out.println("\u001B[31m❌ DEADLOCK DETECTED!\u001B[0m");
```

---

## 5. Typography / Formatting Rules

- **Headers**: Box-drawn borders `╔═╗ ║ ╚═╝`
- **Tables**: ASCII table borders `┌─┐ │ └─┘`
- **Section dividers**: `──────────────`
- **Spacing**: One blank line between each section
- **Alignment**: All matrix columns right-aligned with fixed width (4 chars per cell)
- **Labels**: Processes as P0–Pn, Resources as R0–Rm throughout

---

*End of Design Document*
