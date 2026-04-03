package deadlock.model;

/**
 * Data class that stores everything about the current system state.
 * Holds process/resource counts, the allocation and request matrices,
 * total resources, and the computed available vector.
 */
public class SystemState {

    private int processCount;          // number of processes

    private int resourceTypeCount;     // number of resource types

    private int[] totalResources;      // total instances of each resource

    private int[][] allocation;        // what each process currently holds

    private int[][] request;           // what each process still needs

    private int[] available;           // free resources = total - sum(allocation)

    public SystemState() {}



    public SystemState(int processCount, int resourceTypeCount,
                       int[] totalResources, int[][] allocation,
                       int[][] request, int[] available) {
        this.processCount = processCount;
        this.resourceTypeCount = resourceTypeCount;
        this.totalResources = totalResources;
        this.allocation = allocation;
        this.request = request;
        this.available = available;
    }

    // getN() and getM() kept for backward compatibility with ApiHandler

    public int getN() {
        return processCount;
    }

    public int getM() {
        return resourceTypeCount;
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



    public void setN(int processCount) {
        this.processCount = processCount;
    }

    public void setM(int resourceTypeCount) {
        this.resourceTypeCount = resourceTypeCount;
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



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("SystemState { processes=").append(processCount)
          .append(", resources=").append(resourceTypeCount).append(" }\n");


        sb.append("  Total Resources : [");
        for (int j = 0; j < resourceTypeCount; j++) {
            if (j > 0) sb.append(", ");
            sb.append(totalResources[j]);
        }
        sb.append("]\n");

        sb.append("Available: [");
        for (int j = 0; j < resourceTypeCount; j++) {
            if (j > 0) sb.append(", ");
            sb.append(available[j]);
        }
        sb.append("]\n\n");

        sb.append("Allocation:\n");
        for (int i = 0; i < processCount; i++) {
            sb.append("  P").append(i).append(": [");
            for (int j = 0; j < resourceTypeCount; j++) {
                if (j > 0) sb.append(", ");
                sb.append(allocation[i][j]);
            }
            sb.append("]\n");
        }

        sb.append("Request:\n");
        for (int i = 0; i < processCount; i++) {
            sb.append("  P").append(i).append(": [");
            for (int j = 0; j < resourceTypeCount; j++) {
                if (j > 0) sb.append(", ");
                sb.append(request[i][j]);
            }
            sb.append("]\n");
        }

        return sb.toString();
    }
}
