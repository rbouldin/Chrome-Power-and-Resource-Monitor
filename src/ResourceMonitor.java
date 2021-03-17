/** 
 *  ResourceMonitor.java
 *
 *  VERSION: 2021.03.16
 *  AUTHORS: Rae Bouldin
 *
 *  DESCRIPTION:
 *    ...
 *    ...
 *    ...
 * 
 *  (Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech)
 */
package src;

import java.io.IOException;

public class ResourceMonitor {

    private final static String POWERSHELL_SCRIPT = "./powershell_scripts/sendResourceDataToCSV.ps1";
    private final static String POWER_DATA_FILEPATH = "./output_RES.csv";


    public static void updateResourceData() throws IOException {

        PowerShellController.runPowerShellScript(POWERSHELL_SCRIPT);

    }

    // ...
    // ...
    // ...

    public static String getMemoryTotal() throws IOException {

        return getElementWithoutQuotes("Memory Total", 1);

    }

    public static String getCPUTotal() throws IOException {

        return getElementWithoutQuotes("CPU Total", 1);

    }

    public static String getGPUMemory() throws IOException {

        return getElementWithoutQuotes("GPU Memory", 1);

    }


    private static String getElementWithoutQuotes(String key, int index) throws IOException {

        String[] row = CSVParser.getRow(POWER_DATA_FILEPATH, key);
        String elem = null;

        if (index >= row.length) { 
            return null; 
        }

        if (row[index] != null) {
            elem = row[index].replaceAll("\"", "");
        }

        return elem;

    }

}