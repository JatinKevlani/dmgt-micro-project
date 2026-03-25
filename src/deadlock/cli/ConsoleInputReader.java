package deadlock.cli;

import deadlock.model.SystemState;
import java.util.Scanner;

/**
 * InputHandler — Handles ALL user input from the console.
 * 
 * Responsibilities:
 *   - Reads n (processes) and m (resource types)
 *   - Reads Total Resources vector [m]
 *   - Reads Allocation Matrix [n][m]
 *   - Reads Request Matrix [n][m]
 *   - Computes Available Vector = Total − ColumnSum(Allocation)
 *   - Validates all inputs (non-negative, correct dimensions)
 *   - Returns a populated SystemState object
 */
public class ConsoleInputReader {

    private Scanner scanner;

    // ANSI color codes
    private static final String CYAN   = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN  = "\u001B[32m";
    private static final String RED    = "\u001B[31m";
    private static final String WHITE  = "\u001B[37m";
    private static final String RESET  = "\u001B[0m";

    public ConsoleInputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Collects all inputs from the user and returns a populated SystemState.
     * Screens 2–6 from the Design Document.
     */
    public SystemState collectInput() {

        // ═══════════════════════════════════════════════════════════
        // SCREEN 2 — System Dimensions Input
        // ═══════════════════════════════════════════════════════════
        System.out.println();
        printBoxHeader("STEP 1: System Dimensions");
        System.out.println();

        int n = readBoundedInt("  Enter number of Processes      (n): ", 1, 20);
        int m = readBoundedInt("  Enter number of Resource Types (m): ", 1, 10);

        System.out.println();
        System.out.println("  [Processes will be labeled: P0, P1, P2" + (n > 3 ? " ..." : "") + "]");
        System.out.println("  [Resources will be labeled: R0, R1, R2" + (m > 3 ? " ..." : "") + "]");

        // ═══════════════════════════════════════════════════════════
        // SCREEN 3 — Total Resources Input
        // ═══════════════════════════════════════════════════════════
        System.out.println();
        printBoxHeader("STEP 2: Total Resources Vector");
        System.out.println();
        System.out.println("  Enter the TOTAL units available in the system for each resource:");
        System.out.println();

        int[] totalResources = new int[m];
        for (int j = 0; j < m; j++) {
            totalResources[j] = readNonNegativeInt("  R" + j + " (total): ");
        }

        // Display Total Resources summary
        System.out.println();
        System.out.print("  Total Resources = [");
        for (int j = 0; j < m; j++) {
            System.out.print(" R" + j + " ");
        }
        System.out.println("]");
        System.out.print("                    [");
        for (int j = 0; j < m; j++) {
            System.out.printf("%3d ", totalResources[j]);
        }
        System.out.println("]");

        // ═══════════════════════════════════════════════════════════
        // SCREEN 4 — Allocation Matrix Input
        // ═══════════════════════════════════════════════════════════
        System.out.println();
        printBoxHeader("STEP 3: Allocation Matrix (" + n + " \u00D7 " + m + ")");
        System.out.println();
        System.out.println("  Enter units CURRENTLY HELD by each process:");
        System.out.println("  (How many units of each resource does each process already have?)");
        System.out.println();

        int[][] allocation = readMatrix("Allocation", n, m);

        // ═══════════════════════════════════════════════════════════
        // SCREEN 5 — Request Matrix Input
        // ═══════════════════════════════════════════════════════════
        System.out.println();
        printBoxHeader("STEP 4: Request Matrix (" + n + " \u00D7 " + m + ")");
        System.out.println();
        System.out.println("  Enter units CURRENTLY REQUESTED by each process:");
        System.out.println("  (How many additional units does each process need right now?)");
        System.out.println();

        int[][] request = readMatrix("Request", n, m);

        // ═══════════════════════════════════════════════════════════
        // Compute Available Vector
        // ═══════════════════════════════════════════════════════════
        int[] available = computeAvailable(totalResources, allocation, n, m);

        // Validate that Available vector has no negative values
        boolean valid = true;
        for (int j = 0; j < m; j++) {
            if (available[j] < 0) {
                System.out.println(RED + "  \u26A0 Error: Allocation exceeds Total Resources for R" + j + ". Check inputs." + RESET);
                valid = false;
            }
        }

        if (!valid) {
            System.out.println(RED + "  \u26A0 Available vector has negative values. System state is invalid." + RESET);
            System.out.println(RED + "  Please restart and enter correct values." + RESET);
            return null;
        }

        // ═══════════════════════════════════════════════════════════
        // SCREEN 6 — Computed Available Vector
        // ═══════════════════════════════════════════════════════════
        System.out.println();
        printBoxHeader("Computed: Available Resources Vector");
        System.out.println();
        System.out.println("  Formula: Available[j] = Total[j] \u2212 \u03A3 Allocation[i][j]");
        System.out.println();

        // Sum of Allocation per column
        int[] sumAlloc = new int[m];
        for (int j = 0; j < m; j++) {
            for (int i = 0; i < n; i++) {
                sumAlloc[j] += allocation[i][j];
            }
        }

        System.out.print("  Total Resources   :  [");
        for (int j = 0; j < m; j++) {
            System.out.printf("%4d", totalResources[j]);
        }
        System.out.println("  ]");

        System.out.print("  Sum of Allocation :  [");
        for (int j = 0; j < m; j++) {
            System.out.printf("%4d", sumAlloc[j]);
        }
        System.out.println("  ]");

        System.out.println("                        " + "\u2500".repeat(m * 4 + 3));

        System.out.print("  Available         :  [");
        for (int j = 0; j < m; j++) {
            System.out.printf("%4d", available[j]);
        }
        System.out.println("  ]");

        System.out.println();
        System.out.println(GREEN + "  \u2714 Available vector computed successfully." + RESET);

        // Build and return SystemState
        SystemState state = new SystemState(n, m, totalResources, allocation, request, available);
        return state;
    }

    // ═══════════════════════════════════════════════════════════════
    // Private Helpers
    // ═══════════════════════════════════════════════════════════════

    /**
     * Prints a box header using ╔═╗ ║ ╚═╝ characters (design.md Section 5).
     */
    private void printBoxHeader(String title) {
        int width = title.length() + 6;
        System.out.println(CYAN + "  \u2554" + "\u2550".repeat(width) + "\u2557" + RESET);
        System.out.println(CYAN + "  \u2551   " + title + "   \u2551" + RESET);
        System.out.println(CYAN + "  \u255A" + "\u2550".repeat(width) + "\u255D" + RESET);
    }

    /**
     * Reads a bounded integer with validation (design.md Section 3).
     */
    private int readBoundedInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    System.out.println(YELLOW + "  \u26A0  Value must be between " + min + " and " + max + "." + RESET);
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println(YELLOW + "  \u26A0 Error: Only integers are accepted." + RESET);
            }
        }
    }

    /**
     * Reads a non-negative integer with validation (design.md Section 3).
     */
    private int readNonNegativeInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                int value = Integer.parseInt(input);
                if (value < 0) {
                    System.out.println(YELLOW + "  \u26A0 Error: Value cannot be negative. Please re-enter." + RESET);
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println(YELLOW + "  \u26A0 Error: Only integers are accepted." + RESET);
            }
        }
    }

    /**
     * Reads a matrix (n × m) with per-process/per-resource prompts,
     * then displays it in a bordered table (design.md Screens 4 & 5).
     */
    private int[][] readMatrix(String label, int n, int m) {
        int[][] matrix = new int[n][m];
        for (int i = 0; i < n; i++) {
            System.out.println("  Process P" + i + ":");

            // Collect all resource values for this process on separate lines
            for (int j = 0; j < m; j++) {
                matrix[i][j] = readNonNegativeInt("    R" + j + ": ");
            }
            System.out.println();
        }

        // Display the entered matrix in a bordered table
        // ┌─ Label Matrix ──────────────────────┐
        int tableWidth = 7 + m * 5 + 15;  // fixed width for alignment
        System.out.println("  \u250C\u2500 " + label + " Matrix " + "\u2500".repeat(Math.max(1, tableWidth - label.length() - 10)) + "\u2510");

        // Header row with resource labels
        System.out.print("  \u2502       ");
        for (int j = 0; j < m; j++) {
            System.out.printf("  R%-2d", j);
        }
        // pad to fill table width
        int headerUsed = 7 + m * 5;
        System.out.print(" ".repeat(Math.max(1, tableWidth - headerUsed)));
        System.out.println("\u2502");

        // Data rows
        for (int i = 0; i < n; i++) {
            System.out.printf("  \u2502  P%-2d [", i);
            for (int j = 0; j < m; j++) {
                System.out.printf("%4d ", matrix[i][j]);
            }
            System.out.print("]");
            int rowUsed = 7 + m * 5 + 1;
            System.out.print(" ".repeat(Math.max(1, tableWidth - rowUsed)));
            System.out.println("\u2502");
        }

        System.out.println("  \u2514" + "\u2500".repeat(tableWidth) + "\u2518");

        return matrix;
    }

    /**
     * Computes Available Vector = Total − ColumnSum(Allocation).
     */
    private int[] computeAvailable(int[] total, int[][] allocation, int n, int m) {
        int[] available = new int[m];
        for (int j = 0; j < m; j++) {
            int sumAlloc = 0;
            for (int i = 0; i < n; i++) {
                sumAlloc += allocation[i][j];
            }
            available[j] = total[j] - sumAlloc;
        }
        return available;
    }
}
