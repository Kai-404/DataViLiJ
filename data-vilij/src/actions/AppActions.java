package actions;

import dataprocessors.AppData;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import settings.AppPropertyTypes;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import static java.io.File.separator;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import ui.AppUI;
import static vilij.settings.PropertyTypes.SAVE_WORK_TITLE;

/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;

    /** Path to the data file currently active. */
    Path dataFilePath;
    Path loadFilePath;
    String fileName;
    Boolean fileLoaded = false;

    /** The boolean property marking whether or not there are any unsaved changes. */
    SimpleBooleanProperty isUnsaved;

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
        this.isUnsaved = new SimpleBooleanProperty(false);
    }

    public void setIsUnsavedProperty(boolean property) { isUnsaved.set(property); }
    
    @Override
    public void handleNewRequest() {
        dataFilePath = null;
        try {
            if (!isUnsaved.get() || promptToSave()) {
                ((AppData)applicationTemplate.getDataComponent()).clearQueue();
                applicationTemplate.getDataComponent().clear();
                applicationTemplate.getUIComponent().clear();
                ((AppUI)applicationTemplate.getUIComponent()).disableScrnshotButton();
                isUnsaved.set(false);
                dataFilePath = null;
            }
        } catch (IOException e) { errorHandlingHelper(); }
    }

    @Override
    public void handleSaveRequest() {
        // TODO: NOT A PART OF HW 1
        try {
            if (!isUnsaved.get() || promptToSave()) {
                isUnsaved.set(false);
                ((AppUI) applicationTemplate.getUIComponent()).disableSaveButton();
            }
            
        } catch (Exception e) {
            errorHandlingHelper();
        }
    }

    @Override
    public void handleLoadRequest() {
        // TODO: NOT A PART OF HW 1
        
        
        PropertyManager manager = applicationTemplate.manager;
        FileChooser fileChooser = new FileChooser();

        String description = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
        String extension = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
        ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (.*%s)", description, extension),
                String.format("*%s", extension));

        fileChooser.getExtensionFilters().add(extFilter);
        File selected = fileChooser.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        if (selected != null) {
            loadFilePath = selected.toPath();
            ((AppData)applicationTemplate.getDataComponent()).clearQueue();
            applicationTemplate.getDataComponent().clear();
            applicationTemplate.getUIComponent().clear();
            applicationTemplate.getDataComponent().loadData(loadFilePath);
            fileName =selected.getName();
            fileLoaded=true;
        }
        else{
            fileLoaded=false;
        }
        
        
    }

    @Override
    public void handleExitRequest() {
        try {
            if (!isUnsaved.get() || promptToSave())
                System.exit(0);
        } catch (IOException e) { errorHandlingHelper(); }
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        // TODO: NOT A PART OF HW 1
        PropertyManager manager = applicationTemplate.manager;
        
        WritableImage chartSnap = 
                ((AppUI)applicationTemplate.getUIComponent()).getChart()
                        .snapshot(new SnapshotParameters(), null);
        FileChooser fileChooser = new FileChooser();
        
        String description = manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_FILE_EXT_DESC.name());
        String extension = manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_FILE_EXT.name());
        ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (*.%s)", description, extension),
        String.format("*.%s", extension));
        
       // ExtensionFilter extFilter = new ExtensionFilter(".PNG Image File","*.png");
        fileChooser.getExtensionFilters().add(extFilter);
        File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        if(selected!=null){
            ImageIO.write(SwingFXUtils.fromFXImage(chartSnap, null),extension, selected);
        }
        
    }

    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. The user will be presented with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to continue with the action, but also does not
     * want to save the work at this point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and <code>true</code> otherwise.
     */
    private boolean promptToSave() throws IOException {
        PropertyManager    manager = applicationTemplate.manager;
        ConfirmationDialog dialog  = ConfirmationDialog.getDialog();
        dialog.show(manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK_TITLE.name()),
                    manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()));

        if (dialog.getSelectedOption() == null) return false; // if user closes dialog using the window's close button

        if (dialog.getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
            
            String dataToSave = ((AppUI) applicationTemplate.getUIComponent()).getCurrentText();
            if (!((AppData) applicationTemplate.getDataComponent()).checkError(dataToSave).isEmpty()) {
                ErrorDialog saveError = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                String errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
                String errMsg   = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_MSG.name());
                String errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
                String reason = ((AppData) applicationTemplate.getDataComponent()).checkError(dataToSave).get(0);
                saveError.show(errTitle, errMsg + errInput + "\n" + reason);
                return false;
            }
            else{
                if (dataFilePath == null) {
                FileChooser fileChooser = new FileChooser();
//                String      dataDirPath = separator + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
//                URL         dataDirURL  = getClass().getResource(dataDirPath);
//
//                if (dataDirURL == null)
//                    throw new FileNotFoundException(manager.getPropertyValue(AppPropertyTypes.RESOURCE_SUBDIR_NOT_FOUND.name()));
//
//                fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
                fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));

                String description = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
                String extension   = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
                ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (.*%s)", description, extension),
                                                                String.format("*%s", extension));

                fileChooser.getExtensionFilters().add(extFilter);
                File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                if (selected != null) {
                    dataFilePath = selected.toPath();
                    save();
                } else return false; // if user presses escape after initially selecting 'yes'
            } else
                save();
            }
            
        }

        return !dialog.getSelectedOption().equals(ConfirmationDialog.Option.CANCEL);
    }

    private void save() throws IOException {
        applicationTemplate.getDataComponent().saveData(dataFilePath);
        isUnsaved.set(false);
    }

    private void errorHandlingHelper() {
        ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
        String          errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
        String          errMsg   = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
        String          errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
        dialog.show(errTitle, errMsg + errInput);
    }
    
    
    public String getFileName(){
        return fileName;
    }
    
    public boolean fileIsLoaded() {
        return fileLoaded;
    }
    
}
