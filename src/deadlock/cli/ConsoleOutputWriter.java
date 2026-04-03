package deadlock.cli;

import deadlock.model.DeadlockResult;
import deadlock.model.SystemState;

import java.util.List;

/**
 * Handles terminal output formatting for the deadlock simulator.
 */
public class ConsoleOutputWriter {

    // ANSI color codes for different types of information
    private static final String CYAN   = "\u001B[36m";   // headers and section borders
    private static final String GREEN  = "\u001B[32m";   // good news (safe state)
    private static final String RED    = "\u001B[31m";   // bad news (deadlock)
    private static final String YELLOW = "\u001B[33m";   // warnings and skipped processes
    private static final String WHITE  = "\u001B[37m";   // general data
    private static final String RESET  = "\u001B[0m";    // back to default

    // --- Welcome Banner ---

    /**
     * Displays the opening banner with the project title and subject info.
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

    // --- Input Summary ---

    /**
     * Prints a formatted summary of the input matrices and resources.
     */
    public void printInputSummary(SystemState state) {
        int n = state.getN();
        int m = state.getM();

        System.out.println();
        System.out.println(CYAN + "\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557" + RESET);
        System.out.println(CYAN + "\u2551            INPUT SUMMARY                     \u2551" + RESET);
        System.out.println(CYAN + "\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255D" + RESET);
        System.out.println();
        System.out.println("  Processes : " + n + " (P0 \u2013 P" + (n - 1) + ")     Resource Types : " + m + " (R0 \u2013 R" + (m - 1) + ")");
        System.out.println();

        // Side-by-side Allocation | Request table
        int colBlock = m * 5;
        int labelW = 7;
        int halfWidth = labelW + colBlock + 3;

        // Top border
        System.out.println("  \u250C" + "\u2500".repeat(halfWidth) + "\u252C" + "\u2500".repeat(halfWidth) + "\u2510");

        // Header row
        System.out.printf("  \u2502  %-" + (halfWidth - 2) + "s", "ALLOCATION");
        System.out.printf("\u2502  %-" + (halfWidth - 2) + "s", "REQUEST");
        System.out.println("\u2502");

        // Resource labels
        System.out.print("  \u2502  Proc");
        for (int j = 0; j < m; j++) System.out.printf("  R%-2d", j);
        System.out.print(" ".repeat(Math.max(0, halfWidth - labelW - colBlock + 1)));
        System.out.print("\u2502  ");
        for (int j = 0; j < m; j++) System.out.printf("  R%-2d", j);
        System.out.print(" ".repeat(Math.max(0, halfWidth - 2 - colBlock)));
        System.out.println("\u2502");

        // Separator
        System.out.println("  \u251C" + "\u2500".repeat(halfWidth) + "\u253C" + "\u2500".repeat(halfWidth) + "\u2524");

        // Data rows — one per process
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

        // Bottom border
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



    // --- Safe State Result ---

    /**
     * Prints the safe sequence when the system is not deadlocked.
     */
    public void printSafeResult(List<Integer> safeSequence) {
        System.out.println();
        System.out.println(CYAN + "\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557" + RESET);
        System.out.println(CYAN + "\u2551                      RESULT                          \u2551" + RESET);
        System.out.println(CYAN + "\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255D" + RESET);
        System.out.println();
        System.out.println(GREEN + "  SYSTEM IS IN A SAFE STATE" + RESET);
        System.out.println();
        System.out.println("  Safe Execution Sequence:");

        // Build the sequence string: P1  →  P3  →  P0  →  P2  →  P4
        StringBuilder sequenceString = new StringBuilder();
        for (int i = 0; i < safeSequence.size(); i++) {
            sequenceString.append("P").append(safeSequence.get(i));
            if (i < safeSequence.size() - 1) sequenceString.append("  \u2192  ");
        }

        System.out.println("  \u250C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510");
        System.out.println(GREEN + "  \u2502   " + sequenceString.toString() + RESET);
        System.out.println("  \u2514\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518");
        System.out.println();

        System.out.println();
        System.out.println("\u2500".repeat(46));
    }

    // --- Deadlock Result ---

    /**
     * Prints the processes involved in a detected deadlock.
     */
    public void printDeadlockResult(List<Integer> deadlockedProcesses) {
        System.out.println();
        System.out.println(CYAN + "\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557" + RESET);
        System.out.println(CYAN + "\u2551                      RESULT                          \u2551" + RESET);
        System.out.println(CYAN + "\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255D" + RESET);
        System.out.println();
        System.out.println(RED + "  DEADLOCK DETECTED!" + RESET);
        System.out.println();
        System.out.println("  The following processes are stuck in a DEADLOCK:");

        // Build the list: P0,  P2,  P4
        StringBuilder processListString = new StringBuilder();
        for (int i = 0; i < deadlockedProcesses.size(); i++) {
            processListString.append("P").append(deadlockedProcesses.get(i));
            if (i < deadlockedProcesses.size() - 1) processListString.append(",  ");
        }

        System.out.println("  \u250C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510");
        System.out.println(RED + "  \u2502   " + processListString.toString() + RESET);
        System.out.println("  \u2514\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518");
        System.out.println();

        System.out.println();
        System.out.println("\u2500".repeat(46));
    }

    /**
     * Prints the complete result components.
     */
    public void printResult(DeadlockResult result) {        if (result.isSafe()) {
            printSafeResult(result.getSafeSequence());
        } else {
            printDeadlockResult(result.getDeadlockedProcs());
        }
    }
}
