package ui;

import actions.AppActions;
import algorithms.Classifier;
import algorithms.Clusterer;
import data.DataSet;
import dataprocessors.AppData;
import dataprocessors.TSDProcessor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import static java.io.File.separator;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.settings.PropertyTypes;
import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /** The application to which this class of actions belongs. */
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Button                       scrnshotButton; // toolbar button to take a screenshot of the data
    private LineChart<Number, Number>    chart;          // the chart where data will be displayed
    private Button                       displayButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display
    private CheckBox                     checkBox;
    private Text                         leftPanelTitle;
    private Label                        dataInfo;
    private Button                       doneOrEditBtn;
    private Button                       algAconfigBtn;
    private Button                       algBconfigBtn;
    private Button                       algCconfigBtn;
    private Button                       classificationConfigBtn;
    private Button                       runBtn;
    private Button                       continueBtn;
    private ComboBox                     algorithmTypeSelect;
    private VBox                         leftPanel;
    private boolean                      classificationSelected;
    private boolean                      clusteringSelected;
    private VBox                         algsBox;
    private VBox                         classificationBox;
    private HBox                         runBox;
    private String[]                     classificationSetting = {"100","5","true"};
    private String[][]                   clusteringSettings = {{"100","2","3","true"},{"100","10","3","true"}};
    private ToggleGroup                  algSelect = new ToggleGroup();
    RadioButton                          algorithmA;
    RadioButton                          algorithmB;
    NumberAxis                           xAxis;
    NumberAxis                           yAxis;
    
    
    
    Constructor[] classifierConstructors = new Constructor[1];
    
    Constructor[] clusterConstructors = new Constructor[2];
    
    
    //public RandomClassifier              classifier;
    //public KMeansClusterer               kMeansClusterer;
    
    public Thread                        algTheard = new Thread();
    public boolean                       isContinue;

    public LineChart<Number, Number> getChart() { return chart; }
    
    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        super.setToolBar(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = "/" + String.join(separator,
                                             manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                                             manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String scrnshoticonPath = String.join(separator,
                                              iconsPath,
                                              manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_ICON.name()));
        scrnshotButton = setToolbarButton(scrnshoticonPath,
                                          manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_TOOLTIP.name()),
                                          true);
        toolBar.getItems().add(scrnshotButton);
        newButton.setDisable(false);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> {
                applicationTemplate.getActionComponent().handleNewRequest();
                textArea.setVisible(true);
                textArea.setDisable(false);
                leftPanelTitle.setVisible(true);
                doneOrEditBtn.setVisible(true);
                doneOrEditBtn.setText("Done");
                dataInfo.setVisible(false);
                leftPanel.getChildren().remove(algorithmTypeSelect);
                leftPanel.getChildren().remove(algsBox);
                leftPanel.getChildren().remove(classificationBox);
                algSelect.selectToggle(null);
        });
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> {
            applicationTemplate.getActionComponent().handleLoadRequest();
            doneOrEditBtn.setVisible(false);
            dataInfo.setText("");
            leftPanel.getChildren().remove(algorithmTypeSelect);
            leftPanel.getChildren().remove(algsBox);
            leftPanel.getChildren().remove(classificationBox);
            algSelect.selectToggle(null);
            if (!((AppData) applicationTemplate.getDataComponent()).thereIsError() && textArea.getText()!=null
                    && ((AppActions) applicationTemplate.getActionComponent()).fileIsLoaded()) {
                textArea.setVisible(true);
                textArea.setDisable(true);
                leftPanelTitle.setVisible(true);
                dataInfo.setVisible(true);
                
                dataInfo.setText(numberOfInstances()+
                        " Instances with "
                        +((AppData) applicationTemplate.getDataComponent()).getLabelList().size()
                        +" Labels loaded from "+'\n'
                        +((AppActions) applicationTemplate.getActionComponent()).getFileName()+'\n'
                        +"The Labels are:"+'\n'
                        +labelString(((AppData) applicationTemplate.getDataComponent()).getLabelList()));
                
                algorithmTypeSelect = new ComboBox();
                algorithmTypeSelect.setPromptText("Algorithm Type");
                if (((AppData) applicationTemplate.getDataComponent()).getLabelList().size()!=2) {
                    algorithmTypeSelect.getItems().addAll("clustering");
                }
                else{
                    algorithmTypeSelect.getItems().addAll("classification", "clustering");
                }
                algorithmTypeSelect.valueProperty().addListener((observable, oldValue, newValue) ->{
                    if (newValue.equals("classification")) {
                        leftPanel.getChildren().remove(algorithmTypeSelect);
                        classificationSelected=true;
                        clusteringSelected=false;
                        leftPanel.getChildren().add(classificationBox);
                        
                        classificationConfigBtn.setOnAction(a ->{
                            showClassificationConfigWindow(classificationSetting);
                        });
                        
                    }
                    else if (newValue.equals("clustering")) {
                        leftPanel.getChildren().remove(algorithmTypeSelect);
                        classificationSelected=false;
                        clusteringSelected=true;
                        leftPanel.getChildren().add(algsBox);
                        
                        algAconfigBtn.setOnAction(a ->{
                            showClusteringConfigWindow(clusteringSettings[0]);
                        });
                        
                        algBconfigBtn.setOnAction(a ->{
                            showClusteringConfigWindow(clusteringSettings[1]);
                        });
                    }
                    
                });
                
                leftPanel.getChildren().add(algorithmTypeSelect);

            }else{
                textArea.setVisible(false);
                leftPanelTitle.setVisible(false);
                dataInfo.setVisible(false);
            }
            
            textArea.textProperty().addListener((observable, oldValue, newValue) -> {

                if (!((AppData) applicationTemplate.getDataComponent()).getQueue().isEmpty()) {
                    if (getHowManyLines(newValue) < 10) {
                        addToTextArea(((AppData) applicationTemplate.getDataComponent()).getQueue().remove());
                    }
                }
            });
        });
        exitButton.setOnAction(e -> {
            
            PropertyManager manager = applicationTemplate.manager;
            if (algTheard.isAlive()) {

                ConfirmationDialog dialog = (ConfirmationDialog) applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
                String warnString = manager.getPropertyValue(AppPropertyTypes.EXIT_WHILE_RUNNING_WARNING.name());
                
                dialog.show(" ",warnString);
                if (dialog.getSelectedOption() == null) {

                    dialog.close();

                }else if (dialog.getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
                    System.exit(0);
                }
                else{
                    
                    dialog.close();
                }
                
                        
            }else{
                applicationTemplate.getActionComponent().handleExitRequest();
            }});
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
        scrnshotButton.setOnAction(e -> {
            try {
                ((AppActions)applicationTemplate.getActionComponent()).handleScreenshotRequest();
            } catch (IOException ex) {
                
            }
        });
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
        try {
            loadAlgorithms();
        } catch (ClassNotFoundException ex) {
            
        } catch (NoSuchMethodException ex) {
           
        }
    }

    @Override
    public void clear() {
        textArea.clear();
        chart.getData().clear();
    }

    public String getCurrentText() { return textArea.getText(); }

    private void layout() {
        PropertyManager manager = applicationTemplate.manager;
        xAxis   = new NumberAxis();
        yAxis   = new NumberAxis();
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(manager.getPropertyValue(AppPropertyTypes.CHART_TITLE.name()));
        chart.getStylesheets().add(manager.getPropertyValue(AppPropertyTypes.CSS_FILE_PATH.name()));
        chart.animatedProperty().set(false);
        
        chart.legendVisibleProperty().set(true);
        
        leftPanel = new VBox(8);
        //leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPadding(new Insets(10));

        leftPanelTitle = new Text(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLE.name()));
        String fontname       = manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLEFONT.name());
        Double fontsize       = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLESIZE.name()));
        leftPanelTitle.setFont(Font.font(fontname, fontsize));

        textArea = new TextArea();
        textArea.setPrefHeight(200);      
        checkBox = new CheckBox(manager.getPropertyValue(AppPropertyTypes.READONLY_CHECKBOX_TEXT.name()));

        HBox processButtonsBox = new HBox();
        displayButton = new Button(manager.getPropertyValue(AppPropertyTypes.DISPLAY_BUTTON_TEXT.name()));
        HBox.setHgrow(processButtonsBox, Priority.ALWAYS);
        processButtonsBox.getChildren().add(displayButton);
        
        dataInfo=new Label();
        dataInfo.setText("");
        dataInfo.setWrapText(true);
        
        doneOrEditBtn = new Button("Done");
        doneOrEditBtn.setVisible(false);
        
        algorithmTypeSelect = new ComboBox();
        leftPanel.getChildren().addAll(leftPanelTitle, textArea, doneOrEditBtn, dataInfo);
        
        
        leftPanelTitle.setVisible(false);
        textArea.setVisible(false);
        processButtonsBox.setVisible(false);
        checkBox.setVisible(false);
        
        StackPane rightPanel = new StackPane(chart);
        rightPanel.setMaxSize(windowWidth * 0.69, windowHeight * 0.69);
        rightPanel.setMinSize(windowWidth * 0.69, windowHeight * 0.69);
        StackPane.setAlignment(rightPanel, Pos.CENTER);
        
        workspace = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(workspace, Priority.ALWAYS);

        appPane.getChildren().add(workspace);
        VBox.setVgrow(appPane, Priority.ALWAYS);
        
        algAconfigBtn = new Button("Configuration");
        algBconfigBtn = new Button("Configuration");
        //
        classificationConfigBtn = new Button("Configuration");
        //
        algorithmA = new RadioButton(manager.getPropertyValue(AppPropertyTypes.CLUSTERER_KMEANS_NAME.name()));
        algorithmA.setToggleGroup(algSelect);
        algorithmB = new RadioButton(manager.getPropertyValue(AppPropertyTypes.CLUSTERER_RANDOM_NAME.name()));
        algorithmB.setToggleGroup(algSelect);
        //
        RadioButton classificationAlg = new RadioButton(manager.getPropertyValue(AppPropertyTypes.CLASSIFIER_RANDOM_NAME.name()));
        classificationAlg.setToggleGroup(algSelect);
        //
        HBox algAbox = new HBox();
        algAbox.setSpacing(10);
        algAbox.getChildren().addAll(algorithmA,algAconfigBtn);
        
        HBox algBbox = new HBox();
        algBbox.setSpacing(10);
        algBbox.getChildren().addAll(algorithmB,algBconfigBtn);
        //
        HBox RandomClassificationBox = new HBox();
        RandomClassificationBox.setSpacing(10);
        RandomClassificationBox.getChildren().addAll(classificationAlg,classificationConfigBtn);
        //
        algsBox = new VBox();
        algsBox.setSpacing(10);
        algsBox.getChildren().addAll(algAbox, algBbox);
        
        
        //
        classificationBox = new VBox();
        classificationBox.setSpacing(10);
        classificationBox.getChildren().add(RandomClassificationBox);
        //
        
        runBtn = new Button("Run");
        continueBtn = new Button("Continue");
        runBox = new HBox();
        runBox.setSpacing(10);
        runBox.getChildren().addAll(runBtn,continueBtn);
        continueBtn.setVisible(false);
        
        
    }

    private void setWorkspaceActions() {
        setTextAreaActions();
        setDisplayButtonActions();
        setCheckBoxActions();
        setDoneOrEditBtnActions();
        setToggleGroupActions();
        setRunButtonActions();    
    }

    private void setTextAreaActions() {
        



        
        
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.equals(oldValue)) {
                    if (!newValue.isEmpty()) {
                        ((AppActions) applicationTemplate.getActionComponent()).setIsUnsavedProperty(true);
                        if (newValue.charAt(newValue.length() - 1) == '\n') {
                            hasNewText = true;
                        }
                        //newButton.setDisable(false);
                        saveButton.setDisable(false);
                    } else {
                        hasNewText = true;
                       // newButton.setDisable(true);
                        saveButton.setDisable(true);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println(newValue);
            }
        });

        
        
    }

    private void setDisplayButtonActions() {
        displayButton.setOnAction(event -> {
            if (hasNewText) {
                try {
                    chart.getData().clear();
                    AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
                    dataComponent.clear();
                    if (((AppData) applicationTemplate.getDataComponent()).getQueue().isEmpty()) {
                        dataComponent.loadData(textArea.getText());

                    } else {
                        dataComponent.loadData(textArea.getText() + ((AppData) applicationTemplate.getDataComponent()).getRestData());

                    }
                    dataComponent.displayData();
                    if (chart.getData().isEmpty()) {
                        disableScrnshotButton();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        });
    }
    
    private void setCheckBoxActions(){
        checkBox.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue.equals(Boolean.TRUE)) {
                textArea.setDisable(true);
            }
            else{
                textArea.setDisable(false);
            }
        }));
    }
    
    private void setDoneOrEditBtnActions(){
        PropertyManager manager = applicationTemplate.manager;
        doneOrEditBtn.setOnAction(e ->{
            
            if (!textArea.isDisabled()) {
                
                
                String dataToCheck = textArea.getText();
                if (((AppData) applicationTemplate.getDataComponent()).checkError(dataToCheck).isEmpty()) {
                    doneOrEditBtn.setText("Edit");
                    textArea.setDisable(true);
                    dataInfo.setVisible(true);
                    dataInfo.setText(numberOfInstances()
                            + " Instances with "
                            + ((AppData) applicationTemplate.getDataComponent()).getLabelList().size()
                            + " Labels loaded from " + '\n'
                            + "Text Area" + '\n'
                            + "The Labels are:" + '\n'
                            + labelString(((AppData) applicationTemplate.getDataComponent()).getLabelList()));

                    algorithmTypeSelect = new ComboBox();
                    algorithmTypeSelect.setPromptText("Algorithm Type");
                    if (((AppData) applicationTemplate.getDataComponent()).getLabelList().size() != 2) {
                        algorithmTypeSelect.getItems().addAll("clustering");
                    } else {
                        algorithmTypeSelect.getItems().addAll("classification", "clustering");
                    }
                    algorithmTypeSelect.valueProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue.equals("classification")) {

                            leftPanel.getChildren().remove(algorithmTypeSelect);
                            classificationSelected = true;
                            clusteringSelected = false;
                            
                            leftPanel.getChildren().add(classificationBox);
                            
                            classificationConfigBtn.setOnAction(a -> {
                                showClassificationConfigWindow(classificationSetting);
                            });

                        } else if (newValue.equals("clustering")) {
                            leftPanel.getChildren().remove(algorithmTypeSelect);
                            classificationSelected = false;
                            clusteringSelected = true;
                            leftPanel.getChildren().add(algsBox);

                            algAconfigBtn.setOnAction(a -> {
                                showClusteringConfigWindow(clusteringSettings[0]);
                            });

                            algBconfigBtn.setOnAction(a -> {
                                showClusteringConfigWindow(clusteringSettings[1]);
                            });

                          
                        }

                    });
                    leftPanel.getChildren().add(algorithmTypeSelect);

                }
                else{
                    ErrorDialog loadError = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    String errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
                    String errMsg   = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_MSG.name());
                    String errInput = manager.getPropertyValue(AppPropertyTypes.TEXT_AREA.name());
                    String reason = ((AppData) applicationTemplate.getDataComponent()).checkError(dataToCheck).get(0);
                    loadError.show(errTitle, errMsg + errInput + "\n" + reason);
                }
                
                
            }
            else{
                doneOrEditBtn.setText("Done");
                textArea.setDisable(false);
                dataInfo.setVisible(false);
                leftPanel.getChildren().remove(algorithmTypeSelect);
                leftPanel.getChildren().remove(algsBox);
                leftPanel.getChildren().remove(classificationBox);
                algSelect.selectToggle(null);
            }
            
        });
        
        
        
    }
    
    private void setToggleGroupActions(){
        algSelect.selectedToggleProperty().addListener(((observable, oldValue, newValue) -> {
            
            if (algSelect.getSelectedToggle()!=null) {
                if (leftPanel.getChildren().contains(runBox)) {
                    leftPanel.getChildren().remove(runBox);
                }
                leftPanel.getChildren().add(runBox);
            }else{
                leftPanel.getChildren().remove(runBox);
            }
            
        }));
  
    }
    
    
    private void setRunButtonActions() {
        

        
        runBtn.setOnAction((ActionEvent e) -> {
            
            if (classificationSelected) {
                
                //DataSet dataset = new DataSet();
                
                DataSet dataset = DataSet.fromTSDString(textArea.getText() + ((AppData) applicationTemplate.getDataComponent()).getRestData());
                
                TSDProcessor tsd = new TSDProcessor(dataset.getLabels(), dataset.getLocations());
                
                tsd.calculateBound();
                
                
                isContinue = true;
                if (classificationSetting[2].equals("true")) {
                    isContinue = true;
                    continueBtn.setVisible(false);
                } else if (classificationSetting[2].equals("false")) {
                    isContinue = false;
                    continueBtn.setVisible(true);
                }
                
                try {
                    Classifier classifier = (Classifier) classifierConstructors[0].newInstance(dataset,
                            Integer.parseInt(classificationSetting[0]),
                            Integer.parseInt(classificationSetting[1]),
                            isContinue);
                
                
                continueBtn.setOnAction(ex -> {
                    synchronized (classifier) {
                        classifier.notify();
                    }
                });
                classifier.getResult().addListener((obs, ov, nv) -> {
                    classificationConfigBtn.setDisable(true);
                    runBtn.setDisable(true);    
                    newButton.setDisable(true);
                    saveButton.setDisable(true);
                    loadButton.setDisable(true);
                    doneOrEditBtn.setDisable(true);
                    
                    chart.getData().clear();
                    AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
                    dataComponent.clear();
                    if (((AppData) applicationTemplate.getDataComponent()).getQueue().isEmpty()) {
                        dataComponent.loadData(textArea.getText());

                    } else {
                        dataComponent.loadData(textArea.getText() + ((AppData) applicationTemplate.getDataComponent()).getRestData());

                    }
                    dataComponent.displayData();
                    if (chart.getData().isEmpty()||isContinue) {
                        disableScrnshotButton();
                    }
                    
                    double xMin = dataComponent.getTheMin();
                    double xMax = dataComponent.getTheMax();

                    double a = classifier.getOutput().get(0);
                    double b = classifier.getOutput().get(1);
                    double c = classifier.getOutput().get(2);
                    
//                    if (b==0) {
//                        b=1;
//                    }

                    double y1 = (a * xMin - c) / b;
                    double y2 = (a * xMax - c) / b;
                    
                    
                    
                    xAxis.autoRangingProperty().set(false);
                    
                    xAxis.setLowerBound(tsd.getMin()-1);
                    xAxis.setUpperBound(tsd.getMax()+1);

                    
                    XYChart.Series<Number, Number> classifierLine = new XYChart.Series<>();
                    classifierLine.setName("Random Classifier Line");
                    classifierLine.getData().add(new XYChart.Data<>(xMax, y1));
                    classifierLine.getData().add(new XYChart.Data<>(xMin, y2));
                    chart.getData().add(classifierLine);
                    classifierLine.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: green");
                    classifierLine.getData().get(0).getNode().lookup(".chart-line-symbol").setStyle("-fx-background-color: transparent, transparent");
                    classifierLine.getData().get(1).getNode().lookup(".chart-line-symbol").setStyle("-fx-background-color: transparent, transparent");  
                });
                
                classifier.getFinish().addListener((obs, ov, nv) -> {
                    

                   // System.out.println(nv.toString());
                    if (((boolean) nv) == true) {
                        runBtn.setDisable(false);
                        classificationConfigBtn.setDisable(false);
                        scrnshotButton.setDisable(false);
                        continueBtn.setVisible(false);
                        newButton.setDisable(false);
                        saveButton.setDisable(false);
                        loadButton.setDisable(false);
                        doneOrEditBtn.setDisable(false);
                    }
                    
                });
                
                algTheard = new Thread(classifier);
                algTheard.start();   
                
                } catch (InstantiationException ex) {
                    
                } catch (IllegalAccessException ex) {
                    
                } catch (IllegalArgumentException ex) {
                    
                } catch (InvocationTargetException ex) {
                    
                }
            }
            
            
            
            if (clusteringSelected) {
                
                DataSet dataset = DataSet.fromTSDString(textArea.getText() + ((AppData) applicationTemplate.getDataComponent()).getRestData());
                
                TSDProcessor tsd = new TSDProcessor(dataset.getLabels(), dataset.getLocations());
                
                tsd.calculateBound();
                
                
                try {

                    Clusterer clusterer;
                    if (algSelect.getSelectedToggle().equals(algorithmA)) {
                        isContinue = true;
                        if (clusteringSettings[0][3].equals("true")) {
                            isContinue = true;
                            continueBtn.setVisible(false);
                        } else if (clusteringSettings[0][3].equals("false")) {
                            isContinue = false;
                            continueBtn.setVisible(true);
                        }

                        clusterer = (Clusterer) clusterConstructors[1].newInstance(dataset,
                                Integer.parseInt(clusteringSettings[0][0]),
                                Integer.parseInt(clusteringSettings[0][1]),
                                Integer.parseInt(clusteringSettings[0][2]),
                                isContinue);

                    } else {
                        isContinue = true;
                        if (clusteringSettings[1][3].equals("true")) {
                            isContinue = true;
                            continueBtn.setVisible(false);
                        } else if (clusteringSettings[1][3].equals("false")) {
                            isContinue = false;
                            continueBtn.setVisible(true);
                        }
                        clusterer = (Clusterer) clusterConstructors[0].newInstance(dataset,
                                Integer.parseInt(clusteringSettings[1][0]),
                                Integer.parseInt(clusteringSettings[1][1]),
                                Integer.parseInt(clusteringSettings[1][2]),
                                isContinue);

                    }

                    if (chart.getData().isEmpty() || isContinue) {
                        disableScrnshotButton();
                    }
                    continueBtn.setOnAction(ex -> {
                        synchronized (clusterer) {
                            clusterer.notify();
                        }
                    });

                    clusterer.getResult().addListener((obs, ov, nv) -> {
                        algAconfigBtn.setDisable(true);
                        algBconfigBtn.setDisable(true);
                        runBtn.setDisable(true);
                        newButton.setDisable(true);
                        saveButton.setDisable(true);
                        loadButton.setDisable(true);
                        doneOrEditBtn.setDisable(true);
                        algorithmA.setDisable(true);
                        algorithmB.setDisable(true);

                        chart.getData().clear();

                        xAxis.autoRangingProperty().set(false);

                        xAxis.setLowerBound(tsd.getMin() - 0.5);
                        xAxis.setUpperBound(tsd.getMax() + 0.5);
                        tsd.toChartData(chart);

                    });

                    clusterer.getFinish().addListener((obs, ov, nv) -> {

                        // System.out.println(nv.toString());
                        if (((boolean) nv) == true) {
                            runBtn.setDisable(false);
                            algAconfigBtn.setDisable(false);
                            algBconfigBtn.setDisable(false);
                            scrnshotButton.setDisable(false);
                            continueBtn.setVisible(false);
                            newButton.setDisable(false);
                            saveButton.setDisable(false);
                            loadButton.setDisable(false);
                            doneOrEditBtn.setDisable(false);
                            algorithmA.setDisable(false);
                            algorithmB.setDisable(false);
                        }

                    });

                    algTheard = new Thread(clusterer);
                    algTheard.start();

                } catch (InstantiationException ex) {

                } catch (IllegalAccessException ex) {

                } catch (IllegalArgumentException ex) {

                } catch (InvocationTargetException ex) {

                }
            }

        });
        
    }
    
    
    public void disableScrnshotButton(){
        scrnshotButton.setDisable(true);
    }
    
    public void enableScrnshotButton(){
        scrnshotButton.setDisable(false);
    }
    
    public void disableSaveButton(){
        saveButton.setDisable(true);
    }
    
    public void addToTextArea(String input){
        textArea.appendText(input+"\n");
    }
    
    public int getHowManyLines(String text){
        return text.split("\n").length;
    }
    
    
    
    public int numberOfInstances(){
        return getHowManyLines(textArea.getText())+((AppData) applicationTemplate.getDataComponent()).getQueue().size();
    
    }
    
    public String labelString(ArrayList<String> a) {

        String printString = "";

        for (String s : a) {
            printString += ("-" + s + '\t');
        }

        return printString;
    }
    
    
    public void showClassificationConfigWindow(String[] setting){
        Stage ClassificationConfigWindow = new Stage();
        ClassificationConfigWindow.setTitle("Algorithm Run Configuration");
        
        VBox mainBox = new VBox();
        mainBox.setSpacing(15);
        
        HBox iterationsBox = new HBox();
        HBox intervalBox = new HBox();
        HBox continuousBox = new HBox();
        
        Label iterations = new Label("Max Iterations:        ");
        Label interval = new Label("Update Interval:      ");
        Label continuous = new Label("Continuous Run?     ");
        
        TextField setIterations = new TextField(setting[0]);
        TextField setInterval = new TextField(setting[1]);
        CheckBox setContinuous = new CheckBox();
        
        if (setting[2].equals("true")) {
            setContinuous.setSelected(true);
        }
        else{
            setContinuous.setSelected(false);
        }
        
        Button saveConfig = new Button("Save Config");
        
        
        saveConfig.setOnAction(e ->{
            
            int a;
            int b;

            try {
                a = Integer.parseInt(setIterations.getText());
                if (a < 1) {
                    a = 1;
                }
            } catch (NumberFormatException ex) {
                a = 1;
            }

            try {
                b = Integer.parseInt(setInterval.getText());
                if (b < 1||b>a) {
                    b = 1;
                }
            } catch (NumberFormatException ex) {
                b = 1;
            }
            
            setting[0]=String.valueOf(a);
            setting[1]=String.valueOf(b);
            if (setContinuous.isSelected()) {
                setting[2]="true";
            }else{
                setting[2]="false";
            }
            ClassificationConfigWindow.close();
        });
        
        iterationsBox.getChildren().addAll(iterations, setIterations);
        intervalBox.getChildren().addAll(interval, setInterval);
        continuousBox.getChildren().addAll(continuous, setContinuous);
        
        mainBox.getChildren().addAll(iterationsBox, intervalBox, continuousBox,saveConfig);
        
        ClassificationConfigWindow.setScene(new Scene(mainBox));
        
        ClassificationConfigWindow.showAndWait();
        
    }
    
    
    public void showClusteringConfigWindow(String[] setting){
        Stage ClusteringConfigWindow = new Stage();
        ClusteringConfigWindow.setTitle("Algorithm Run Configuration");
        
        VBox mainBox2 = new VBox();
        mainBox2.setSpacing(15);
        
        HBox iterationsBox = new HBox();
        HBox intervalBox = new HBox();
        HBox numberOfLabelsBox = new HBox();
        HBox continuousBox = new HBox();
        
        Label iterations = new Label("Max Iterations:        ");
        Label interval = new Label("Update Interval:      ");
        Label numberOfLabels = new Label("Number of Labels   ");
        Label continuous = new Label("Continuous Run?     ");
        
        TextField setIterations = new TextField(setting[0]);
        TextField setInterval = new TextField(setting[1]);
        TextField setNumberOfLabels = new TextField(setting[2]);
        CheckBox setContinuous = new CheckBox();
        
        if (setting[3].equals("true")) {
            setContinuous.setSelected(true);
        }
        else{
            setContinuous.setSelected(false);
        }
        
        Button saveConfig = new Button("Save Config");
        
        
        saveConfig.setOnAction(e ->{
            
            int a;
            int b;
            int c;

            try {
                a = Integer.parseInt(setIterations.getText());
                if (a < 1) {
                    a = 1;
                }
            } catch (NumberFormatException ex) {
                a = 1;
            }

            try {
                b = Integer.parseInt(setInterval.getText());
                if (b < 1||b>a) {
                    b = 1;
                }
            } catch (NumberFormatException ex) {
                b = 1;
            }
            try {
                c = Integer.parseInt(setNumberOfLabels.getText());
                if (c<2) {
                    c=2;
                }
            } catch (NumberFormatException ex) {
                c = 2;
            }
            
            
            
            setting[0]=String.valueOf(a);
            setting[1]=String.valueOf(b);
            setting[2]=String.valueOf(c);
            if (setContinuous.isSelected()) {
                setting[3]="true";
            }else{
                setting[3]="false";
            }
            ClusteringConfigWindow.close();
            

        });
        
        iterationsBox.getChildren().addAll(iterations, setIterations);
        intervalBox.getChildren().addAll(interval, setInterval);
        numberOfLabelsBox.getChildren().addAll(numberOfLabels, setNumberOfLabels);
        continuousBox.getChildren().addAll(continuous, setContinuous);
        
        mainBox2.getChildren().addAll(iterationsBox, intervalBox, numberOfLabelsBox, continuousBox, saveConfig);
        
        ClusteringConfigWindow.setScene(new Scene(mainBox2));
        
        ClusteringConfigWindow.showAndWait();
        
    }
    
    
    public void loadAlgorithms() throws ClassNotFoundException, NoSuchMethodException{
        
        PropertyManager manager = applicationTemplate.manager; 
        Class[] classificationParameters = {DataSet.class, int.class, int.class, boolean.class};
        Class[] clusterParameters = {DataSet.class, int.class, int.class, int.class, boolean.class};
        
        Class randomClassifier = Class.forName(manager.getPropertyValue(AppPropertyTypes.CLASSIFIER_RANDOM_PATH.name()));
        classifierConstructors[0] =randomClassifier.getConstructor(classificationParameters);
        
        Class randomClusterer = Class.forName(manager.getPropertyValue(AppPropertyTypes.CLUSTERER_RANDOM_PATH.name()));
        clusterConstructors[0] = randomClusterer.getConstructor(clusterParameters);
        
        Class kmeansClassifier = Class.forName(manager.getPropertyValue(AppPropertyTypes.CLUSTERER_KMEANS_PATH.name()));
        clusterConstructors[1] = kmeansClassifier.getConstructor(clusterParameters);
    }
    
    
}
