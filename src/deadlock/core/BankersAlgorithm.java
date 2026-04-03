package deadlock.core;

import deadlock.model.DeadlockResult;
import deadlock.model.SystemState;

import java.util.Arrays;

/**
 * Implements the Banker's Safety Algorithm to determine if a system state
 * is safe or will lead to a deadlock.
 */
public class BankersAlgorithm {


    public DeadlockResult runSafetyAlgorithm(SystemState state) {

        int numberOfProcesses = state.getN();
        int numberOfResourceTypes = state.getM();
        int[][] allocation = state.getAllocation();
        int[][] request = state.getRequest();

        DeadlockResult result = new DeadlockResult();

        int[] work = Arrays.copyOf(state.getAvailable(), numberOfResourceTypes);

        boolean[] finished = new boolean[numberOfProcesses];


        result.addTrace("Initial Work (Available) : " + formatArray(work));
        result.addTrace("Finish Array             : " + formatFinishArray(finished));
        result.addTrace("");


        int iterationNumber = 0;
        boolean madeProgress;

        do {
            madeProgress = false;
            iterationNumber++;
            result.addTrace("\u2500".repeat(58));
            result.addTrace("ITERATION " + iterationNumber + ":");

            for (int processIndex = 0; processIndex < numberOfProcesses; processIndex++) {
                if (finished[processIndex]) continue;

                if (canProcessRun(processIndex, work, state)) {

                    result.addTrace("  Checking P" + processIndex + ": Request[" + processIndex + "] = "
                        + formatArray(request[processIndex]) + " \u2264 Work " + formatArray(work) + " \u2192 \u2714 EXECUTE");


                    int[] workBeforeRelease = Arrays.copyOf(work, numberOfResourceTypes);
                    for (int resourceIndex = 0; resourceIndex < numberOfResourceTypes; resourceIndex++) {
                        work[resourceIndex] += allocation[processIndex][resourceIndex];
                    }

                    result.addTrace("  \u2192 Work  = Work + Allocation[P" + processIndex + "] = "
                        + formatArray(workBeforeRelease) + " + "
                        + formatArray(allocation[processIndex]) + " = " + formatArray(work));

                    finished[processIndex] = true;
                    result.addTrace("  \u2192 Finish[P" + processIndex + "] = true");

                    result.addToSafeSequence(processIndex);
                    result.addTrace("  \u2192 Safe Sequence so far: " + formatSequence(result.getSafeSequence()));
                    result.addTrace("");

                    madeProgress = true;
                } else {

                    result.addTrace("  Checking P" + processIndex + ": Request[" + processIndex + "] = "
                        + formatArray(request[processIndex]) + " > Work " + formatArray(work) + " \u2192 \u2717 SKIP");
                }
            }

            if (!madeProgress) {
                result.addTrace("  No process can proceed...");
            }

        } while (madeProgress);


        boolean everyoneFinished = true;
        for (int processIndex = 0; processIndex < numberOfProcesses; processIndex++) {
            if (!finished[processIndex]) {
                everyoneFinished = false;
                result.addDeadlockedProcess(processIndex);
            }
        }

        result.setSafe(everyoneFinished);
        return result;
    }


    private boolean canProcessRun(int processIndex, int[] work, SystemState state) {
        int[][] request = state.getRequest();
        int numberOfResourceTypes = state.getM();

        for (int resourceIndex = 0; resourceIndex < numberOfResourceTypes; resourceIndex++) {
            if (request[processIndex][resourceIndex] > work[resourceIndex]) {
                return false;
            }
        }
        return true;
    }


    private String formatArray(int[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }


    private String formatFinishArray(boolean[] finished) {
        StringBuilder sb = new StringBuilder("[ ");
        for (int i = 0; i < finished.length; i++) {
            sb.append(finished[i] ? "T" : "F");
            if (i < finished.length - 1) sb.append("   ");
        }
        sb.append(" ]");
        return sb.toString();
    }


    private String formatSequence(java.util.List<Integer> seq) {
        StringBuilder sb = new StringBuilder("< ");
        for (int i = 0; i < seq.size(); i++) {
            sb.append("P").append(seq.get(i));
            if (i < seq.size() - 1) sb.append(", ");
        }
        sb.append(" >");
        return sb.toString();
    }
}
