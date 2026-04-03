package deadlock.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the output of running the Banker's Algorithm on a SystemState.
 * Contains the safe/deadlock verdict, the process execution order (if safe),
 * the list of deadlocked processes (if not), and a trace log.
 */
public class DeadlockResult {

    private boolean isSafe;
    private List<Integer> safeSequence;
    private List<Integer> deadlockedProcs;

    public DeadlockResult() {
        this.isSafe = false;
        this.safeSequence = new ArrayList<>();
        this.deadlockedProcs = new ArrayList<>();

    }



    public boolean isSafe() {
        return isSafe;
    }

    public List<Integer> getSafeSequence() {
        return safeSequence;
    }

    public List<Integer> getDeadlockedProcs() {
        return deadlockedProcs;
    }





    public void setSafe(boolean safe) {
        isSafe = safe;
    }

    public void setSafeSequence(List<Integer> safeSequence) {
        this.safeSequence = safeSequence;
    }

    public void setDeadlockedProcs(List<Integer> deadlockedProcs) {
        this.deadlockedProcs = deadlockedProcs;
    }



    // convenience methods for building up the result
    public void addToSafeSequence(int processId) {
        this.safeSequence.add(processId);
    }


    public void addDeadlockedProcess(int processId) {
        this.deadlockedProcs.add(processId);
    }


    public void addTrace(String message) {

    }
}
