
package algorithms;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 * @author Ritwik Banerjee
 */
public abstract class Clusterer implements Algorithm {

    protected final int numberOfClusters;
    
    boolean finished;
    int iteration;
    
    private final ReadOnlyObjectWrapper resultWrapper = new ReadOnlyObjectWrapper(iteration);
    private final ReadOnlyObjectWrapper finishWrapper = new ReadOnlyObjectWrapper(finished);

    public int getNumberOfClusters() { return numberOfClusters; }

    public Clusterer(int k) {
        if (k < 2)
            k = 2;
        else if (k > 4)
            k = 4;
        numberOfClusters = k;
    }
    
    public ReadOnlyObjectProperty getResult(){
        return resultWrapper;
    }
    
    public ReadOnlyObjectProperty getFinish(){
        return finishWrapper;
    }
    
}