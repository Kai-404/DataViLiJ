package algorithms.classification;

import algorithms.Classifier;
import data.DataSet;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier{
    
    boolean finished;
    
    private final ReadOnlyObjectWrapper resultWrapper = new ReadOnlyObjectWrapper(getOutput());
    
    private final ReadOnlyObjectWrapper finishWrapper = new ReadOnlyObjectWrapper(finished);

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;

    private final int maxIterations;
    private final int updateInterval;

    // currently, this value does not change after instantiation
    private final AtomicBoolean tocontinue;

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    public RandomClassifier(DataSet dataset,
                            int maxIterations,
                            int updateInterval,
                            boolean tocontinue) {
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
    }


    public  void run() {
        for (int i = 1; i <= maxIterations; i++) {
            
            Platform.runLater(() -> {
                finished = false;
                finishWrapper.set(finished);
            });
            int xCoefficient = new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
            int yCoefficient = 10;
            int constant     = RAND.nextInt(11);

            // this is the real output of the classifier
            output = Arrays.asList(xCoefficient, yCoefficient, constant);

            // everything below is just for internal viewing of how the output is changing
            // in the final project, such changes will be dynamically visible in the UI
            if (i % updateInterval == 0) {
                //System.out.printf("Iteration number %d: ", i); //
                //flush();
                Platform.runLater(()->{
                    resultWrapper.set(output);
                });
                synchronized(this){
                    try {
                        if (tocontinue()) {
                            wait(500);
                        }else{
                            wait();
                        }
                        
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
            if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                //System.out.printf("Iteration number %d: ", i);
                //flush();
                Platform.runLater(() -> {
                    resultWrapper.set(output);
                });
                break;
            }
        }
        //System.out.println("i am done");
        
        Platform.runLater(()->{
            finished =true;
            finishWrapper.set(finished);
        });
        
    }

    // for internal viewing only
    protected void flush() {
        System.out.printf("%d\t%d\t%d%n", output.get(0), output.get(1), output.get(2));
    }
    
    public ReadOnlyObjectProperty getResult(){
        return resultWrapper;
    }
    
    public ReadOnlyObjectProperty getFinish(){
        return finishWrapper;
    }
    
    public boolean ifFinish(){
        return finished;
    }
    /** A placeholder main method to just make sure this code runs smoothly */
//    public static void main(String... args) throws IOException {
//
//
//
//        DataSet          dataset    = DataSet.fromTSDFile( Paths.get("/Users/zhongkai/Desktop/Untitled.tsd"));
//        RandomClassifier classifier = new RandomClassifier(dataset, 100, 5, true);
//        classifier.run(); // no multithreading yet
//        Thread t = new Thread( classifier );
//        t.run();
//
//
//    }
    
}