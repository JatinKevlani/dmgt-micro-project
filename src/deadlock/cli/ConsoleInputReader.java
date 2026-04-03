package deadlock.cli;

import deadlock.model.SystemState;
import java.util.Scanner;

/**
 * Handles reading user input from the console to build a SystemState.
 */
public class ConsoleInputReader {

    private Scanner scanner;

    // Colors to make the console output easier to read
    private static final String CYAN   = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED    = "\u001B[31m";
    private static final String WHITE  = "\u001B[37m";
    private static final String RESET  = "\u001B[0m";

    public ConsoleInputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Walks the user through all the input steps and returns a complete SystemState.
     * Returns null if the input turns out to be invalid (e.g., allocation > total).
     */
    public SystemState collectInput() {


        System.out.println();
        printBoxHeader("STEP 1: System Dimensions");
        System.out.println();

        int numberOfProcesses = askForNumberInRange("  How many processes?       (1–20): ", 1, 20);
        int numberOfResourceTypes = askForNumberInRange("  How many resource types?  (1–10): ", 1, 10);

        System.out.println();
        System.out.println("  [Processes: P0 to P" + (numberOfProcesses - 1) + "]");
        System.out.println("  [Resources: R0 to R" + (numberOfResourceTypes - 1) + "]");


        System.out.println();
        printBoxHeader("STEP 2: Total Resources Vector");
        System.out.println();
        System.out.println("  Enter the total units of each resource type:");
        System.out.println();

        int[] totalResources = new int[numberOfResourceTypes];
        for (int j = 0; j < numberOfResourceTypes; j++) {
            totalResources[j] = askForNonNegativeInt("  R" + j + " (total): ");
        }


        System.out.println();
        System.out.print("  Total Resources = [");
        for (int j = 0; j < numberOfResourceTypes; j++) {
            System.out.print(" R" + j + " ");
        }
        System.out.println("]");
        System.out.print("                    [");
        for (int j = 0; j < numberOfResourceTypes; j++) {
            System.out.printf("%3d ", totalResources[j]);
        }
        System.out.println("]");


        System.out.println();
        printBoxHeader("STEP 3: Allocation Matrix (" + numberOfProcesses + " \u00D7 " + numberOfResourceTypes + ")");
        System.out.println();
        System.out.println("  Enter units currently allocated to each process:");
        System.out.println();

        int[][] allocation = readMatrix("Allocation", numberOfProcesses, numberOfResourceTypes);


        System.out.println();
        printBoxHeader("STEP 4: Request Matrix (" + numberOfProcesses + " \u00D7 " + numberOfResourceTypes + ")");
        System.out.println();
        System.out.println("  Enter additional units requested by each process:");
        System.out.println();

        int[][] request = readMatrix("Request", numberOfProcesses, numberOfResourceTypes);


        int[] available = computeAvailable(totalResources, allocation, numberOfProcesses, numberOfResourceTypes);


        boolean inputsAreValid = true;
        for (int j = 0; j < numberOfResourceTypes; j++) {
            if (available[j] < 0) {
                System.out.println(RED + "  Error: Available resources cannot be negative for R" + j + "." + RESET);
                inputsAreValid = false;
            }
        }

        if (!inputsAreValid) {
            System.out.println(RED + "  Error: Invalid inputs. System cannot be initialized." + RESET);
            return null;
        }


        System.out.println();
        printBoxHeader("Computed: Available Resources Vector");
        System.out.println();
        System.out.println("  Formula: Available[j] = Total[j] \u2212 \u03A3 Allocation[i][j]");
        System.out.println();


        int[] allocationColumnSums = new int[numberOfResourceTypes];
        for (int j = 0; j < numberOfResourceTypes; j++) {
            for (int i = 0; i < numberOfProcesses; i++) {
                allocationColumnSums[j] += allocation[i][j];
            }
        }

        System.out.print("  Total Resources   :  [");
        for (int j = 0; j < numberOfResourceTypes; j++) {
            System.out.printf("%4d", totalResources[j]);
        }
        System.out.println("  ]");

        System.out.print("  Sum of Allocation :  [");
        for (int j = 0; j < numberOfResourceTypes; j++) {
            System.out.printf("%4d", allocationColumnSums[j]);
        }
        System.out.println("  ]");

        System.out.println("                        " + "\u2500".repeat(numberOfResourceTypes * 4 + 3));

        System.out.print("  Available         :  [");
        for (int j = 0; j < numberOfResourceTypes; j++) {
            System.out.printf("%4d", available[j]);
        }
        System.out.println("  ]");

        System.out.println();
        System.out.println();


        return new SystemState(numberOfProcesses, numberOfResourceTypes, totalResources, allocation, request, available);
    }



    private void printBoxHeader(String title) {
        int width = title.length() + 6;
        System.out.println(CYAN + "  \u2554" + "\u2550".repeat(width) + "\u2557" + RESET);
        System.out.println(CYAN + "  \u2551   " + title + "   \u2551" + RESET);
        System.out.println(CYAN + "  \u255A" + "\u2550".repeat(width) + "\u255D" + RESET);
    }

    private int askForNumberInRange(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    System.out.println(YELLOW + "  Value must be between " + min + " and " + max + "." + RESET);
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println(YELLOW + "  Invalid number format." + RESET);
            }
        }
    }

    private int askForNonNegativeInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                int value = Integer.parseInt(input);
                if (value < 0) {
                    System.out.println(YELLOW + "  Value cannot be negative." + RESET);
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println(YELLOW + "  Invalid number format." + RESET);
            }
        }
    }

    private int[][] readMatrix(String label, int rows, int cols) {
        int[][] matrix = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.out.println("  Process P" + i + ":");

            for (int j = 0; j < cols; j++) {
                matrix[i][j] = askForNonNegativeInt("    R" + j + ": ");
            }
            System.out.println();
        }


        int tableWidth = 7 + cols * 5 + 15;
        System.out.println("  \u250C\u2500 " + label + " Matrix " + "\u2500".repeat(Math.max(1, tableWidth - label.length() - 10)) + "\u2510");


        System.out.print("  \u2502       ");
        for (int j = 0; j < cols; j++) {
            System.out.printf("  R%-2d", j);
        }
        int headerUsed = 7 + cols * 5;
        System.out.print(" ".repeat(Math.max(1, tableWidth - headerUsed)));
        System.out.println("\u2502");


        for (int i = 0; i < rows; i++) {
            System.out.printf("  \u2502  P%-2d [", i);
            for (int j = 0; j < cols; j++) {
                System.out.printf("%4d ", matrix[i][j]);
            }
            System.out.print("]");
            int rowUsed = 7 + cols * 5 + 1;
            System.out.print(" ".repeat(Math.max(1, tableWidth - rowUsed)));
            System.out.println("\u2502");
        }

        System.out.println("  \u2514" + "\u2500".repeat(tableWidth) + "\u2518");

        return matrix;
    }

    private int[] computeAvailable(int[] total, int[][] allocation, int numberOfProcesses, int numberOfResourceTypes) {
        int[] available = new int[numberOfResourceTypes];
        for (int j = 0; j < numberOfResourceTypes; j++) {
            int totalAllocated = 0;
            for (int i = 0; i < numberOfProcesses; i++) {
                totalAllocated += allocation[i][j];
            }
            available[j] = total[j] - totalAllocated;
        }
        return available;
    }
}
