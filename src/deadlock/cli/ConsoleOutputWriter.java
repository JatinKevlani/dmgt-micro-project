package deadlock.cli;

import deadlock.model.DeadlockResult;
import deadlock.model.SystemState;

import java.util.List;

/**
 * OutputPrinter — Handles ALL output formatting and printing.
 * 
 * Uses ANSI color codes and box-drawn characters for a polished console UI.
 * Matches design.md Screens 1, 7, 8, 9A, 9B exactly.
 */
public class ConsoleOutputWriter {

    // ANSI color codes (design.md Section 4)
    private static final String CYAN   = "\u001B[36m";  // Section Headers
    private static final String GREEN  = "\u001B[32m";  // ✅ Safe State
    private static final String RED    = "\u001B[31m";  // ❌ Deadlock
    private static final String YELLOW = "\u001B[33m";  // ⚠ Warnings
    private static final String WHITE  = "\u001B[37m";  // Matrix Data
    private static final String RESET  = "\u001B[0m";   // Default

    // ═══════════════════════════════════════════════════════════════
    // SCREEN 1 — Welcome Banner
    // ═══════════════════════════════════════════════════════════════
    /**
     * Prints the welcome banner exactly as specified in design.md Screen 1.
     */
    public void printWelcomeBanner() {
        System.out.println();
        System.out.println(CYAN + "\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557" + RESET);
        System.out.println(CYAN + "\u2551        DEADLOCK DETECTION SIMULATOR \u2014 BANKER'S ALGORITHM     \u2551" + RESET);
        System.out.println(CYAN + "\u2551              Java | OS | DMGT  |  Microproject               \u2551" + RESET);
        System.out.println(CYAN + "\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255D" + RESET);
        System.out.println();
        System.out.println("  Subjects  : Operating Systems, DMGT, Java");
        System.out.println("  Algorithm : Banker's Safety Algorithm");
        System.out.println("  Purpose   : Detect if system is in safe or deadlock state");
        System.out.println();
        System.out.println("\u2500".repeat(62));
    }

    // ═══════════════════════════════════════════════════════════════
    // SCREEN 7 — Full Input Summary Table
    // ═══════════════════════════════════════════════════════════════
    /**
     * Prints the input summary table exactly as specified in design.md Screen 7.
     */
    public void printInputSummary(SystemState state) {
        int n = state.getN();
        int m = state.getM();

        System.out.println();
        System.out.println(CYAN + "\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557" + RESET);
        System.out.println(CYAN + "\u2551                  INPUT SUMMARY                        \u2551" + RESET);
        System.out.println(CYAN + "\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255D" + RESET);
        System.out.println();
        System.out.println("  Processes : " + n + " (P0 \u2013 P" + (n - 1) + ")     Resource Types : " + m + " (R0 \u2013 R" + (m - 1) + ")");
        System.out.println();

        // ── Combined Allocation | Request Table ──
        // Calculate column widths: each resource column is 5 chars wide
        int colBlock = m * 5;  // width for one set of resource columns
        int labelW = 7;        // "  Proc " or similar
        int halfWidth = labelW + colBlock + 3; // + padding

        // Top border
        System.out.println("  \u250C" + "\u2500".repeat(halfWidth) + "\u252C" + "\u2500".repeat(halfWidth) + "\u2510");

        // Header: ALLOCATION | REQUEST
        System.out.printf("  \u2502  %-" + (halfWidth - 2) + "s", "ALLOCATION");
        System.out.printf("\u2502  %-" + (halfWidth - 2) + "s", "REQUEST");
        System.out.println("\u2502");

        // Sub-header with resource labels
        System.out.print("  \u2502  Proc");
        for (int j = 0; j < m; j++) System.out.printf("  R%-2d", j);
        System.out.print(" ".repeat(Math.max(0, halfWidth - labelW - colBlock + 1)));
        System.out.print("\u2502  ");
        for (int j = 0; j < m; j++) System.out.printf("  R%-2d", j);
        System.out.print(" ".repeat(Math.max(0, halfWidth - 2 - colBlock)));
        System.out.println("\u2502");

        // Separator ├───┼───┤
        System.out.println("  \u251C" + "\u2500".repeat(halfWidth) + "\u253C" + "\u2500".repeat(halfWidth) + "\u2524");

        // Data rows
        for (int i = 0; i < n; i++) {
            System.out.printf("  \u2502  P%-2d", i);
            for (int j = 0; j < m; j++) {
                System.out.printf("%5d", state.getAllocation()[i][j]);
            }
            System.out.print(" ".repeat(Math.max(0, halfWidth - labelW - colBlock + 1)));
            System.out.print("\u2502  ");
            for (int j = 0; j < m; j++) {
                System.out.printf("%5d", state.getRequest()[i][j]);
            }
            System.out.print(" ".repeat(Math.max(0, halfWidth - 2 - colBlock)));
            System.out.println("\u2502");
        }

        // Bottom border └───┴───┘
        System.out.println("  \u2514" + "\u2500".repeat(halfWidth) + "\u2534" + "\u2500".repeat(halfWidth) + "\u2518");

        System.out.println();
        System.out.print("  Total Resources :  [");
        for (int j = 0; j < m; j++) System.out.printf("%4d", state.getTotalResources()[j]);
        System.out.println("  ]");

        System.out.print("  Available       :  [");
        for (int j = 0; j < m; j++) System.out.printf("%4d", state.getAvailable()[j]);
        System.out.println("  ]");
        System.out.println();
    }

    // ═══════════════════════════════════════════════════════════════
    // SCREEN 8 — Algorithm Execution Trace
    // ═══════════════════════════════════════════════════════════════
    /**
     * Prints the algorithm execution trace as per design.md Screen 8.
     * Color coding: green=execute, yellow=skip, red=deadlock, cyan=iteration headers.
     */
    public void printTrace(List<String> traceLog) {
        System.out.println();
        System.out.println(CYAN + "\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557" + RESET);
        System.out.println(CYAN + "\u2551            BANKER'S ALGORITHM \u2014 EXECUTION TRACE          \u2551" + RESET);
        System.out.println(CYAN + "\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255D" + RESET);
        System.out.println();

        for (String line : traceLog) {
            if (line.contains("\u2714 EXECUTE")) {
                System.out.println(GREEN + "  " + line + RESET);
            } else if (line.contains("\u2717 SKIP")) {
                System.out.println(YELLOW + "  " + line + RESET);
            } else if (line.contains("No process can proceed")) {
                System.out.println(RED + "  " + line + RESET);
            } else if (line.contains("ITERATION")) {
                System.out.println(CYAN + "  " + line + RESET);
            } else {
                System.out.println(WHITE + "  " + line + RESET);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // SCREEN 9A — Safe State Result
    // ═══════════════════════════════════════════════════════════════
    /**
     * Prints the safe state result exactly as specified in design.md Screen 9A.
     */
    public void printSafeResult(List<Integer> safeSequence) {
        System.out.println();
        System.out.println(CYAN + "\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557" + RESET);
        System.out.println(CYAN + "\u2551                      RESULT                          \u2551" + RESET);
        System.out.println(CYAN + "\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255D" + RESET);
        System.out.println();
        System.out.println(GREEN + "  \u2705  SYSTEM IS IN A SAFE STATE" + RESET);
        System.out.println();
        System.out.println("  Safe Execution Sequence:");

        // Build sequence string: P1  →  P3  →  P0  →  P2  →  P4
        StringBuilder seqStr = new StringBuilder();
        for (int i = 0; i < safeSequence.size(); i++) {
            seqStr.append("P").append(safeSequence.get(i));
            if (i < safeSequence.size() - 1) seqStr.append("  \u2192  ");
        }

        System.out.println("  \u250C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510");
        System.out.println(GREEN + "  \u2502   " + seqStr.toString() + RESET);
        System.out.println("  \u2514\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518");
        System.out.println();
        System.out.println("  All processes can complete without deadlock.");
        System.out.println("  No process will be left waiting indefinitely.");
        System.out.println();
        System.out.println("\u2500".repeat(46));
    }

    // ═══════════════════════════════════════════════════════════════
    // SCREEN 9B — Deadlock State Result
    // ═══════════════════════════════════════════════════════════════
    /**
     * Prints the deadlock state result exactly as specified in design.md Screen 9B.
     */
    public void printDeadlockResult(List<Integer> deadlockedProcs) {
        System.out.println();
        System.out.println(CYAN + "\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557" + RESET);
        System.out.println(CYAN + "\u2551                      RESULT                          \u2551" + RESET);
        System.out.println(CYAN + "\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255D" + RESET);
        System.out.println();
        System.out.println(RED + "  \u274C  DEADLOCK DETECTED!" + RESET);
        System.out.println();
        System.out.println("  The following processes are in DEADLOCK:");

        // Build deadlocked processes string: P0,  P2,  P4
        StringBuilder procStr = new StringBuilder();
        for (int i = 0; i < deadlockedProcs.size(); i++) {
            procStr.append("P").append(deadlockedProcs.get(i));
            if (i < deadlockedProcs.size() - 1) procStr.append(",  ");
        }

        System.out.println("  \u250C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510");
        System.out.println(RED + "  \u2502   " + procStr.toString() + RESET);
        System.out.println("  \u2514\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518");
        System.out.println();
        System.out.println("  These processes are permanently blocked.");
        System.out.println("  Their resource requests cannot be fulfilled.");
        System.out.println("  They will wait INDEFINITELY \u2192 DEADLOCK.");
        System.out.println();
        System.out.println("\u2500".repeat(46));
    }

    /**
     * Prints the complete result based on DeadlockResult.
     */
    public void printResult(DeadlockResult result) {
        printTrace(result.getTraceLog());

        if (result.isSafe()) {
            printSafeResult(result.getSafeSequence());
        } else {
            printDeadlockResult(result.getDeadlockedProcs());
        }
    }
}
