/** 
 *  ResourceMonitor.java
 *
 *  VERSION: 2021.03.17
 *  AUTHORS: Rae Bouldin
 *
 *  DESCRIPTION:
 *    Contains methods to create, read from, and delete an output file that 
 *    holds data about the System resources.
 * 
 *  (Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech)
 */
package src;

import java.io.File;
import java.io.IOException;

public class ResourceMonitor {

    private final static String POWERSHELL_SCRIPT = "./powershell_scripts/sendResourceDataToCSV.ps1";
    private final static String DATA_FILEPATH = "./output_RES.csv";


    /**
     *  Calls the POWERSHELL_SCRIPT to update the contents of the file at the 
     *  DATA_FILEPATH.
     */
    public static void updateResourceData() throws IOException {

        PowerShellController.runPowerShellScript(POWERSHELL_SCRIPT);

    }


    /**
     *  Deletes any output files generated by updateResourceData().
     */
    public static boolean deleteOutputFiles() {
        File dataFile = new File(DATA_FILEPATH);
        return dataFile.delete();
    }


    // ...
    // ...
    // ...


    /** 
     *  Looks for the row in DATA_FILEPATH with the specified key, "Memory 
     *  Total" then returns its Value (in ?). Returns NaN if the Value couldn't
     *  be found or if something else goes wrong.
     * 
     *  Note: You should call updateResourceData() at least once before calling 
     *  this method.
     */
    public static double getMemoryTotal() throws IOException {

        String val = getElementWithoutQuotes("Memory Total", 1);
        if (val != null) {
            return Double.parseDouble(val);
        }
        return Double.NaN;

    }

    /** 
     *  Looks for the row in DATA_FILEPATH with the specified key, "CPU Total" 
     *  then returns its Value (represented as a percent %). Returns NaN if the
     *  Value couldn't be found or if something else goes wrong.
     * 
     *  Note: You should call updateResourceData() at least once before calling 
     *  this method.
     */
    public static double getCPUTotal() throws IOException {

        String val = getElementWithoutQuotes("CPU Total", 1);
        if (val != null) {
            return Double.parseDouble(val);
        }
        return Double.NaN;

    }

    /** 
     *  Looks for the row in DATA_FILEPATH with the specified key, "GPU Memory"
     *  then returns its Value (represented as a percent %). Returns NaN if the
     *  Value couldn't be found or if something else goes wrong.
     * 
     *  Note: You should call updateResourceData() at least once before calling 
     *  this method.
     */
    public static double getGPUMemory() throws IOException {

        String val = getElementWithoutQuotes("GPU Memory", 1);
        if (val != null) {
            return Double.parseDouble(val);
        }
        return Double.NaN;

    }


    /** 
     *  Looks for the row in DATA_FILEPATH with the specified key, then returns
     *  the element in that row at the specified index without any quotation 
     *  marks. Returns null if no such element could be found.
     */
    private static String getElementWithoutQuotes(String key, int index) throws IOException {
        
        File dataFile = new File(DATA_FILEPATH);
        String[] row = null;
        String elem = null;
        
        // Exit if: invalid method arguments.
        if (key == null || index < 0) { return null; }
        // Exit if: dataFile doesn't exist.
        if (!dataFile.isFile()) { 
            System.err.printf("FileNotFound at \"%s\"", DATA_FILEPATH);
            System.err.println();
            return null; 
        }

        // Find the first row in the CSV file containing the key.
        row = CSVHandler.getRow(DATA_FILEPATH, key);

        // Exit if: no row containing the key could be found.
        if (row == null) { return null; }
        // Exit if: index out of bounds.
        if (index >= row.length) { return null; }

        // Remove all the quotes in the row[index] element if it isn't null.
        if (row[index] != null) {
            elem = row[index].replaceAll("\"", "");
        }

        return elem;

    }

}