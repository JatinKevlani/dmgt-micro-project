package deadlock.cli;

import deadlock.core.BankersAlgorithm;
import deadlock.model.DeadlockResult;
import deadlock.model.SystemState;

import java.util.Scanner;

/**
 * Main — Entry point of the Deadlock Detection Simulator.
 * 
 * Displays the welcome banner, drives the main program loop,
 * and coordinates InputHandler, BankersAlgorithm, and OutputPrinter.
 */
public class CliMain {

    private static final String CYAN   = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RESET  = "\u001B[0m";

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        ConsoleInputReader inputHandler = new ConsoleInputReader(scanner);
        BankersAlgorithm algorithm = new BankersAlgorithm();
        ConsoleOutputWriter printer = new ConsoleOutputWriter();

        try {
            boolean runAgain = true;

            while (runAgain) {
                // ═══ Screen 1: Welcome Banner ═══════════════════
                printer.printWelcomeBanner();
                System.out.print("Press ENTER to begin...");
                scanner.nextLine();

                // ═══ Screens 2–6: Collect Input ═════════════════
                SystemState state = inputHandler.collectInput();

                if (state == null) {
                    // Invalid input — ask to retry
                    System.out.println();
                    System.out.print(YELLOW + "  Try again? (Y/N): " + RESET);
                    String retry = scanner.nextLine().trim();
                    if (!retry.equalsIgnoreCase("Y")) {
                        runAgain = false;
                    }
                    continue;
                }

                // ═══ Screen 7: Input Summary ════════════════════
                printer.printInputSummary(state);

                System.out.print("  Confirm and Run Algorithm? (Y/N): ");
                String confirm = scanner.nextLine().trim();
                if (!confirm.equalsIgnoreCase("Y")) {
                    System.out.println(YELLOW + "  Algorithm execution cancelled. Starting over..." + RESET);
                    continue;
                }

                // ═══ Screens 8–9: Run Algorithm & Print Result ══
                DeadlockResult result = algorithm.runSafetyAlgorithm(state);
                printer.printResult(result);

                // ═══ Ask to Run Again (design.md Screen 9A/9B) ══
                System.out.println();
                System.out.print("  Run again with new inputs? (Y/N): ");
                String again = scanner.nextLine().trim();
                runAgain = again.equalsIgnoreCase("Y");
            }

            System.out.println();
            System.out.println(CYAN + "  Thank you for using the Deadlock Detection Simulator!" + RESET);
            System.out.println(CYAN + "  " + "\u2500".repeat(58) + RESET);
            System.out.println();

        } catch (Exception e) {
            System.out.println();
            System.out.println("\u001B[31m  Unexpected error: " + e.getMessage() + RESET);
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
