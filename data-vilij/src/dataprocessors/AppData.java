package dataprocessors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Stream;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor        processor;
    private ApplicationTemplate applicationTemplate;
    boolean hasError;
    
    int LineCounter; 
    Queue<String> dataQueue= new LinkedList<String>();
    Queue<String> hideDataQueue = new LinkedList<String>();
    ArrayList<String> labelList = new ArrayList<>();

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void loadData(Path dataFilePath) {
        // TODO: NOT A PART OF HW 1
        File input = new File(dataFilePath.toString());
        
        StringBuilder fullString = new StringBuilder();
        
        dataQueue= new LinkedList<String>();
        hideDataQueue = new LinkedList<String>();
        try {
            
            
            BufferedReader bufferedReader = new BufferedReader(new FileReader(input));
            String readLine;
            while ((readLine = bufferedReader.readLine()) != null) {
                fullString.append(readLine).append("\n");
                
            }
            
            if (checkError(fullString.toString()).isEmpty()) {
                String[] dataList = fullString.toString().split("\n");
                for (String a : dataList) {
                    dataQueue.add(a);
                }
                if (dataQueue.size() > 10) {
                    for (int i = 0; i < 10; i++) {
                        ((AppUI) applicationTemplate.getUIComponent()).addToTextArea(dataQueue.remove());
                    }
                    ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    PropertyManager manager = applicationTemplate.manager;
                    String errTitle = manager.getPropertyValue(AppPropertyTypes.MORE_THAN_TEN_LINES_TITLE.name());
                    String errMsg = "Loaded data consists of " + 
                            dataList.length + " lines," + "\n" + 
                            manager.getPropertyValue(AppPropertyTypes.MORE_THAN_TEN_LINES.name());
                    dialog.show(errTitle, errMsg);

                } else {
                    for (String a : dataList) {
                        ((AppUI) applicationTemplate.getUIComponent()).addToTextArea(a);
                    }
                    clearQueue();
                }
                hideDataQueue.addAll(dataQueue);

            }else{
                ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                PropertyManager manager = applicationTemplate.manager;
                String errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
                String errMsg   = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_MSG.name());
                String errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
                String reason = checkError(fullString.toString()).get(0);
                dialog.show(errTitle, errMsg + errInput + "\n" + reason);
            }
            
            
            
        } catch (Exception e) {
            
            
            
        }
            

    }

    public void loadData(String dataString) {
        try {
            if (checkError(dataString).isEmpty()) {
                processor.processString(dataString);
                ((AppUI) applicationTemplate.getUIComponent()).enableScrnshotButton();
            }else{
                ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                PropertyManager manager = applicationTemplate.manager;
                String errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
                String errMsg   = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_MSG.name());
                String errInput = manager.getPropertyValue(AppPropertyTypes.TEXT_AREA.name());
                String reason = checkError(dataString).get(0);
                dialog.show(errTitle, errMsg + errInput + "\n" + reason);
                ((AppUI) applicationTemplate.getUIComponent()).disableScrnshotButton();
            }
            
            
            
        } catch (Exception e) {
            ((AppUI) applicationTemplate.getUIComponent()).disableScrnshotButton();
            ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            PropertyManager manager  = applicationTemplate.manager;
            String          errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
            String          errMsg   = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_MSG.name());
            String          errInput = manager.getPropertyValue(AppPropertyTypes.TEXT_AREA.name());
            dialog.show(errTitle, errMsg + errInput);
        }
    }

    @Override
    public void saveData(Path dataFilePath) {
        // NOTE: completing this method was not a part of HW 1. You may have implemented file saving from the
        // confirmation dialog elsewhere in a different way.
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(dataFilePath))) {
            writer.write(((AppUI) applicationTemplate.getUIComponent()).getCurrentText());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void clear() {
        processor.clear();
    }

    public void displayData() {
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
    }
    
    public ArrayList<String> checkError(String input) {
        ArrayList errorMessages = new ArrayList();
        LineCounter = 1;
        ArrayList<String> nameList = new ArrayList<>();
        labelList=new ArrayList<>();

        Stream.of(input.split("\n"))
                .map(line -> Arrays.asList(line.split("\t")))
                .forEach(list -> {
                    try {
                        if (dataHasError(list.get(0), list.size(), LineCounter)) {

                        }
                        
                        if (!nameList.isEmpty()) {
                            if(hasDuplicate(list.get(0), nameList, LineCounter)){

                            }
                        }
                            nameList.add(list.get(0));
                            
                        if (!labelList.contains(list.get(1)) && !list.get(1).equals("null")) {
                            labelList.add(list.get(1));
                        }
                            
                            

                    } catch (InvalidDataException e) {
                        
                        errorMessages.add(e.getMessage());
                        
                    }catch(DuplicateNameException ex){
                        errorMessages.add(ex.getMessage());
                    }
                    LineCounter++;
                });
        
        if (errorMessages.size()!=0) {
            hasError=true;
        }
        else{
            hasError=false;
        }
        return errorMessages;
    }

    private boolean dataHasError(String name, int dataFormat, int lineNumber ) throws InvalidDataException {
        if (!name.startsWith("@")|| dataFormat!=3)
            throw new InvalidDataException(lineNumber);
        return true;
    }
    
    public static class InvalidDataException extends Exception {

        private static final String DATA_ERROR_MSG = "Data format error at line #";

        public InvalidDataException(int line) {
            super(String.format(DATA_ERROR_MSG + line));
        }
    }
    
    private boolean hasDuplicate(String name, ArrayList<String> nameList, int lineNumber ) throws DuplicateNameException {
        
        for(String a: nameList){
            if (name.equals(a)) {
                throw new DuplicateNameException(lineNumber, name);
            }
        }
        return true;
    }
    
    public static class DuplicateNameException extends Exception {

        private static final String DUPLICATE_ERROR_MSG = "Duplicate instance at line #";
        private static final String DUPLICATE_NAME_MSG = "Duplicate name: ";

        public DuplicateNameException(int line, String name) {
            super(String.format(DUPLICATE_ERROR_MSG + line + "\n"+DUPLICATE_NAME_MSG + name));
        }
    }
    
    public Queue<String> getQueue(){
        return hideDataQueue;
    }
    
    public void clearQueue(){
        dataQueue = new LinkedList<String>();
        hideDataQueue = new LinkedList<String>();
    }
    
    public String getRestData(){
        Queue<String> temp = new LinkedList<String>();
        temp.addAll(hideDataQueue);
        String restString= "";
        while(!temp.isEmpty()){
            restString+=temp.remove()+"\n";
        }
        return restString;
    }
    
    public boolean thereIsError(){
        return hasError;
    }
    
    public ArrayList<String> getLabelList(){
        return labelList;
    }
    
    public double getTheMax(){
        return processor.getMax();
    }
    
    public double getTheMin(){
        return processor.getMin();
    }
    
}
