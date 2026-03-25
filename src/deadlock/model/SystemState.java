package deadlock.model;

/**
 * SystemState — Data model that holds all input data for one simulation run.
 * 
 * Fields:
 *   n              — number of processes
 *   m              — number of resource types
 *   totalResources — Total[m]
 *   allocation     — Allocation[n][m]
 *   request        — Request[n][m]
 *   available      — Available[m] (auto-computed)
 */
public class SystemState {

    private int n;                   // number of processes
    private int m;                   // number of resource types
    private int[] totalResources;    // Total[m]
    private int[][] allocation;      // Allocation[n][m]
    private int[][] request;         // Request[n][m]
    private int[] available;         // Available[m] — computed

    // ─── Constructors ───────────────────────────────────────────

    public SystemState() {}

    public SystemState(int n, int m, int[] totalResources, int[][] allocation, int[][] request, int[] available) {
        this.n = n;
        this.m = m;
        this.totalResources = totalResources;
        this.allocation = allocation;
        this.request = request;
        this.available = available;
    }

    // ─── Getters ────────────────────────────────────────────────

    public int getN() {
        return n;
    }

    public int getM() {
        return m;
    }

    public int[] getTotalResources() {
        return totalResources;
    }

    public int[][] getAllocation() {
        return allocation;
    }

    public int[][] getRequest() {
        return request;
    }

    public int[] getAvailable() {
        return available;
    }

    // ─── Setters ────────────────────────────────────────────────

    public void setN(int n) {
        this.n = n;
    }

    public void setM(int m) {
        this.m = m;
    }

    public void setTotalResources(int[] totalResources) {
        this.totalResources = totalResources;
    }

    public void setAllocation(int[][] allocation) {
        this.allocation = allocation;
    }

    public void setRequest(int[][] request) {
        this.request = request;
    }

    public void setAvailable(int[] available) {
        this.available = available;
    }

    // ─── toString (for debug printing) ──────────────────────────

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SystemState { n=").append(n).append(", m=").append(m).append(" }\n");

        sb.append("Total Resources: [");
        for (int j = 0; j < m; j++) {
            sb.append(totalResources[j]);
            if (j < m - 1) sb.append(", ");
        }
        sb.append("]\n");

        sb.append("Available: [");
        for (int j = 0; j < m; j++) {
            sb.append(available[j]);
            if (j < m - 1) sb.append(", ");
        }
        sb.append("]\n");

        sb.append("Allocation Matrix:\n");
        for (int i = 0; i < n; i++) {
            sb.append("  P").append(i).append(": [");
            for (int j = 0; j < m; j++) {
                sb.append(allocation[i][j]);
                if (j < m - 1) sb.append(", ");
            }
            sb.append("]\n");
        }

        sb.append("Request Matrix:\n");
        for (int i = 0; i < n; i++) {
            sb.append("  P").append(i).append(": [");
            for (int j = 0; j < m; j++) {
                sb.append(request[i][j]);
                if (j < m - 1) sb.append(", ");
            }
            sb.append("]\n");
        }

        return sb.toString();
    }
}
