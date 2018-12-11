package algorithms;

import java.util.List;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 * An abstract class for classification algorithms. The output
 * for these algorithms is a straight line, as described in
 * Appendix C of the software requirements specification
 * (SRS). The {@link #output} is defined with extensibility
 * in mind.
 *
 * @author Ritwik Banerjee
 */
public abstract class Classifier implements Algorithm {

    /**
     * See Appendix C of the SRS. Defining the output as a
     * list instead of a triple allows for future extension
     * into polynomial curves instead of just straight lines.
     * See 3.4.4 of the SRS.
     */
    
    
    boolean finished;
    
    private final ReadOnlyObjectWrapper resultWrapper = new ReadOnlyObjectWrapper(getOutput());
    
    private final ReadOnlyObjectWrapper finishWrapper = new ReadOnlyObjectWrapper(finished);
    
    
    protected List<Integer> output;

    public List<Integer> getOutput() { return output; }
    
     public ReadOnlyObjectProperty getResult(){
        return resultWrapper;
    }
    
    public ReadOnlyObjectProperty getFinish(){
        return finishWrapper;
    }

}