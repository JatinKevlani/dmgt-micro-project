package deadlock.core;

import deadlock.model.DeadlockResult;
import deadlock.model.SystemState;

import java.util.Arrays;

/**
 * BankersAlgorithm — Core algorithm implementation.
 * 
 * Implements the Banker's Safety Algorithm:
 *   1. Work = Available.copy()
 *   2. Finish[i] = false for all i
 *   3. Loop: find Pi where Finish[Pi]=false AND Request[Pi] <= Work
 *   4. If found: Work += Allocation[Pi], Finish[Pi] = true
 *   5. Repeat until no more found
 *   6. If all Finish = true → SAFE; else → DEADLOCK
 * 
 * Trace output matches design.md Screen 8 format exactly.
 */
public class BankersAlgorithm {

    /**
     * Runs the Banker's Safety Algorithm on the given SystemState.
     * 
     * @param state The system state containing all matrices and vectors
     * @return DeadlockResult containing safe sequence or deadlocked processes
     */
    public DeadlockResult runSafetyAlgorithm(SystemState state) {

        int n = state.getN();
        int m = state.getM();
        int[][] allocation = state.getAllocation();
        int[][] request = state.getRequest();

        DeadlockResult result = new DeadlockResult();

        // Step 1: Work = copy of Available
        int[] work = Arrays.copyOf(state.getAvailable(), m);

        // Step 2: Finish[i] = false for all i
        boolean[] finish = new boolean[n];

        // ── Trace: Initial state (matches Screen 8 header) ──
        result.addTrace("Initial Work (Available) : " + arrayToString(work));
        result.addTrace("Finish Array             : " + finishToString(finish));
        result.addTrace("");

        // Step 3–5: Repeat until no process can be found
        int iteration = 0;
        boolean found;

        do {
            found = false;
            iteration++;
            result.addTrace("\u2500".repeat(58));
            result.addTrace("ITERATION " + iteration + ":");

            for (int i = 0; i < n; i++) {
                if (finish[i]) continue;

                if (canExecute(i, work, state)) {
                    // ── Trace: Process Pi can execute ──
                    result.addTrace("  Checking P" + i + ": Request[" + i + "] = " 
                        + arrayToString(request[i]) + " \u2264 Work " + arrayToString(work) + " \u2192 \u2714 EXECUTE");

                    // Work = Work + Allocation[Pi]
                    int[] oldWork = Arrays.copyOf(work, m);
                    for (int j = 0; j < m; j++) {
                        work[j] += allocation[i][j];
                    }

                    result.addTrace("  \u2192 Work  = Work + Allocation[P" + i + "] = " 
                        + arrayToString(oldWork) + " + " 
                        + arrayToString(allocation[i]) + " = " + arrayToString(work));

                    // Finish[Pi] = true
                    finish[i] = true;
                    result.addTrace("  \u2192 Finish[P" + i + "] = true");

                    // Add to safe sequence
                    result.addToSafeSequence(i);
                    result.addTrace("  \u2192 Safe Sequence so far: " + sequenceToString(result.getSafeSequence()));
                    result.addTrace("");

                    found = true;
                } else {
                    // ── Trace: Process Pi cannot execute ──
                    result.addTrace("  Checking P" + i + ": Request[" + i + "] = " 
                        + arrayToString(request[i]) + " > Work " + arrayToString(work) + " \u2192 \u2717 SKIP");
                }
            }

            if (!found) {
                result.addTrace("  No process can proceed...");
            }

        } while (found);

        // Step 6: Check if all processes finished
        boolean allFinished = true;
        for (int i = 0; i < n; i++) {
            if (!finish[i]) {
                allFinished = false;
                result.addDeadlockedProcess(i);
            }
        }

        result.setSafe(allFinished);
        return result;
    }

    /**
     * Checks whether process Pi can execute with the current Work vector.
     * Condition: Request[Pi][j] <= Work[j] for all j
     */
    private boolean canExecute(int process, int[] work, SystemState state) {
        int[][] request = state.getRequest();
        int m = state.getM();

        for (int j = 0; j < m; j++) {
            if (request[process][j] > work[j]) {
                return false;
            }
        }
        return true;
    }

    // ─── Helper: Convert int[] to string like [3, 3, 2] ────────

    private String arrayToString(int[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    // ─── Helper: Convert finish array to string like [F, F, T] ─

    private String finishToString(boolean[] finish) {
        StringBuilder sb = new StringBuilder("[ ");
        for (int i = 0; i < finish.length; i++) {
            sb.append(finish[i] ? "T" : "F");
            if (i < finish.length - 1) sb.append("   ");
        }
        sb.append(" ]");
        return sb.toString();
    }

    // ─── Helper: Convert safe sequence to string like < P1, P3 > 

    private String sequenceToString(java.util.List<Integer> seq) {
        StringBuilder sb = new StringBuilder("< ");
        for (int i = 0; i < seq.size(); i++) {
            sb.append("P").append(seq.get(i));
            if (i < seq.size() - 1) sb.append(", ");
        }
        sb.append(" >");
        return sb.toString();
    }
}
