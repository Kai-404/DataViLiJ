package vilij.components;

import java.nio.file.Path;

/**
 * This interface defines the minimal functionality of the data management component of Vilij application, providing the
 * method definitions for saving and loading the required data.
 *
 * @author Ritwik Banerjee
 */
public interface DataComponent {

    void loadData(Path dataFilePath);

    void saveData(Path dataFilePath);

    void clear();
}