package dataprocessors;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Tooltip;

/**
 * The data files used by this data visualization applications follow a tab-separated format, where each data point is
 * named, labeled, and has a specific location in the 2-dimensional X-Y plane. This class handles the parsing and
 * processing of such data. It also handles exporting the data to a 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @see XYChart
 */
public final class TSDProcessor {

    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character.";

        public InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s'." + NAME_ERROR_MSG, name));
        }
    }

    private Map<String, String>  dataLabels;
    private Map<String, Point2D> dataPoints;
    static double sum;
    static double average;
    static int yCounter;
    static double min;
    static double max;
    

    public TSDProcessor() {
        dataLabels = new HashMap<>();
        dataPoints = new HashMap<>();
    }
    
    public TSDProcessor(Map<String, String>  dataLabels, Map<String, Point2D> dataPoints){
        this.dataLabels=dataLabels;
        this.dataPoints=dataPoints;
    }
    
    
    
    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws Exception {
        AtomicBoolean hadAnError   = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();
        Stream.of(tsdString.split("\n"))
              .map(line -> Arrays.asList(line.split("\t")))
              .forEach(list -> {
                  try {
                      String   name  = checkedname(list.get(0));
                      String   label = list.get(1);
                      String[] pair  = list.get(2).split(",");
                      Point2D  point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                      dataLabels.put(name, label);
                      dataPoints.put(name, point);
                  } catch (Exception e) {
                      errorMessage.setLength(0);
                      errorMessage.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
                      hadAnError.set(true);
                  }
              });
        if (errorMessage.length() > 0)
            throw new Exception(errorMessage.toString());
    }

    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */
    public void toChartData(XYChart<Number, Number> chart) {
        sum = 0;
        yCounter = 0;
        min = 0;
        max = 0;
        Set<String> labels = new HashSet<>(dataLabels.values());
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                series.getData().add(new XYChart.Data<>(point.getX(), point.getY()));
                
                if (max==0) {
                    max= point.getX();
                    min= point.getX();
                }else if (point.getX()>max) {
                    max=point.getX();
                }else if (point.getX()<min) {
                    min=point.getX();
                }
                
                sum +=point.getY();
                yCounter++;
                average = sum/yCounter;
            });
            
            chart.getData().add(series);
        }
        
        for( Series<Number,Number> series : chart.getData()){
            for (XYChart.Data<Number,Number> data : series.getData()) {
                
                Point2D temp = new Point2D((double)data.getXValue(), (double)data.getYValue());
                
                for(String s:dataPoints.keySet()){
                    
                    if(dataPoints.get(s).equals(temp)){
                        
                        Tooltip dataName = new Tooltip(s);
                        Tooltip.install(data.getNode(), dataName);  
                    } 
                }
                
                data.getNode().setOnMouseEntered(e -> {
                    ((Node) (e.getSource())).setCursor(Cursor.HAND);
                });
                
            }
        }
        
            
//            XYChart.Series<Number, Number> averageLine = new XYChart.Series<>();
//            averageLine.getData().add(new XYChart.Data<>(min, average));
//            averageLine.getData().add(new XYChart.Data<>(max, average));
//            chart.getData().add(averageLine);
//            averageLine.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: gold");
//            averageLine.getData().get(0).getNode().lookup(".chart-line-symbol").setStyle("-fx-background-color: transparent, transparent");
//            averageLine.getData().get(1).getNode().lookup(".chart-line-symbol").setStyle("-fx-background-color: transparent, transparent");
        
    }

    void clear() {
        dataPoints.clear();
        dataLabels.clear();
    }

    private String checkedname(String name) throws InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new InvalidDataNameException(name);
        return name;
    }
    
    public double getMax(){
        return max;
    }
    
    public double getMin(){
        return min;
    }
    
    
    
    
    public void calculateBound(){
        min =0;
        max =0;
        dataPoints.forEach((name, point)->{
            if (max==0) {
                    max= point.getX();
                    min= point.getX();
                }else if (point.getX()>max) {
                    max=point.getX();
                }else if (point.getX()<min) {
                    min=point.getX();
                }
        });
        
    }
}
