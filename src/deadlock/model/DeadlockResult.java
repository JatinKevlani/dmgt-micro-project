package deadlock.model;

import java.util.ArrayList;
import java.util.List;

/**
 * DeadlockResult — Data model that holds the result of the Banker's Algorithm.
 * 
 * Fields:
 *   isSafe          — true if system is in safe state, false if deadlock
 *   safeSequence    — order of process execution (if safe)
 *   deadlockedProcs — deadlocked process IDs (if unsafe)
 *   traceLog        — step-by-step trace messages
 */
public class DeadlockResult {

    private boolean isSafe;
    private List<Integer> safeSequence;
    private List<Integer> deadlockedProcs;
    private List<String> traceLog;

    // ─── Constructor ────────────────────────────────────────────

    public DeadlockResult() {
        this.isSafe = false;
        this.safeSequence = new ArrayList<>();
        this.deadlockedProcs = new ArrayList<>();
        this.traceLog = new ArrayList<>();
    }

    // ─── Getters ────────────────────────────────────────────────

    public boolean isSafe() {
        return isSafe;
    }

    public List<Integer> getSafeSequence() {
        return safeSequence;
    }

    public List<Integer> getDeadlockedProcs() {
        return deadlockedProcs;
    }

    public List<String> getTraceLog() {
        return traceLog;
    }

    // ─── Setters ────────────────────────────────────────────────

    public void setSafe(boolean safe) {
        isSafe = safe;
    }

    public void setSafeSequence(List<Integer> safeSequence) {
        this.safeSequence = safeSequence;
    }

    public void setDeadlockedProcs(List<Integer> deadlockedProcs) {
        this.deadlockedProcs = deadlockedProcs;
    }

    public void setTraceLog(List<String> traceLog) {
        this.traceLog = traceLog;
    }

    // ─── Convenience Methods ────────────────────────────────────

    public void addToSafeSequence(int processId) {
        this.safeSequence.add(processId);
    }

    public void addDeadlockedProcess(int processId) {
        this.deadlockedProcs.add(processId);
    }

    public void addTrace(String message) {
        this.traceLog.add(message);
    }
}
