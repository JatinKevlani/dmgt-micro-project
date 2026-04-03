package deadlock.cli;

import deadlock.core.BankersAlgorithm;
import deadlock.model.DeadlockResult;
import deadlock.model.SystemState;

import java.util.Scanner;

/**
 * Main entry point for the CLI version of the Deadlock Detection Simulator.
 */
public class CliMain {

    private static final String CYAN   = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RESET  = "\u001B[0m";

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        ConsoleInputReader inputReader = new ConsoleInputReader(scanner);
        BankersAlgorithm algorithm = new BankersAlgorithm();
        ConsoleOutputWriter outputWriter = new ConsoleOutputWriter();

        try {
            boolean keepRunning = true;

            while (keepRunning) {

                outputWriter.printWelcomeBanner();
                System.out.print("Press ENTER to begin...");
                scanner.nextLine();


                SystemState state = inputReader.collectInput();

                if (state == null) {

                    System.out.println();
                    System.out.print(YELLOW + "  Would you like to try again? (Y/N): " + RESET);
                    String retry = scanner.nextLine().trim();
                    if (!retry.equalsIgnoreCase("Y")) {
                        keepRunning = false;
                    }
                    continue;
                }


                outputWriter.printInputSummary(state);

                System.out.print("  Everything look right? Run the algorithm? (Y/N): ");
                String confirm = scanner.nextLine().trim();
                if (!confirm.equalsIgnoreCase("Y")) {
                    System.out.println(YELLOW + "  Restarting..." + RESET);
                    continue;
                }


                DeadlockResult result = algorithm.runSafetyAlgorithm(state);
                outputWriter.printResult(result);


                System.out.println();
                System.out.print("  Want to try again with new inputs? (Y/N): ");
                String again = scanner.nextLine().trim();
                keepRunning = again.equalsIgnoreCase("Y");
            }


            System.out.println();
            System.out.println(CYAN + "  Exiting Simulator." + RESET);
            System.out.println(CYAN + "  " + "\u2500".repeat(58) + RESET);
            System.out.println();

        } catch (Exception e) {
            System.out.println();
            System.out.println("\u001B[31m  An error occurred: " + e.getMessage() + RESET);
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
