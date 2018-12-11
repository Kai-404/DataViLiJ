/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.clustering;

import algorithms.Clusterer;
import data.DataSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Point2D;

/**
 *
 * @author zhongkai
 */
public class RandomClusterer extends Clusterer{
    private DataSet       dataset;
    
    boolean finished;
    int iteration;
    
    private final int           maxIterations;
    private final int           updateInterval;
    private final AtomicBoolean continueRun;
    
    private final ReadOnlyObjectWrapper resultWrapper = new ReadOnlyObjectWrapper(iteration);
    private final ReadOnlyObjectWrapper finishWrapper = new ReadOnlyObjectWrapper(finished);

    public RandomClusterer(DataSet dataset, int maxIterations, int updateInterval, int numberOfClusters, boolean continueRun) {
        super(numberOfClusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.continueRun = new AtomicBoolean(continueRun);
    }

    @Override
    public int getMaxIterations() { return maxIterations; }

    @Override
    public int getUpdateInterval() { return updateInterval; }

    @Override
    public boolean tocontinue() { return continueRun.get(); }

    @Override
    public void run() {
        Platform.runLater(() -> {
                finished = false;
                finishWrapper.set(finished);
            });
        iteration = 0;
        
        while (iteration++ < maxIterations ) {
            
            randomAssignLabels();
            
            if (iteration % updateInterval == 0) {
                Platform.runLater(() -> {
                    resultWrapper.set(iteration);
                });

                synchronized (this) {
                    try {
                        if (continueRun.get()) {
                            wait(500);
                        } else {
                            wait();
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        
        Platform.runLater(() -> {
            finished=true;
            finishWrapper.set(finished);
        });

    }
    
    
    private void randomAssignLabels(){
        Random randomInt = new Random();
        
        dataset.getLocations().forEach((instanceName, location) -> {
            int randomLabel = randomInt.nextInt(numberOfClusters);
            while (randomLabel<0) {                
                randomLabel=randomInt.nextInt(numberOfClusters);
            }
            dataset.getLabels().put(instanceName,Integer.toString(randomLabel) );
        });
        
        
    }
    
    
    
    public ReadOnlyObjectProperty getResult(){
        return resultWrapper;
    }
    
    public DataSet getDataSet(){
        return dataset;
    }
    
    public ReadOnlyObjectProperty getFinish(){
        return finishWrapper;
    }
    
}
