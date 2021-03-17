/** 
 *  PowerMonitor.java
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

public class PowerMonitor {

    private final static String POWERSHELL_SCRIPT = "./powershell_scripts/sendPowerDataToCSV.ps1";
    private final static String POWER_DATA_FILEPATH = "./output_POW.csv";

    private final static int NAME_INDEX = 0;
    private final static int SENSORTYPE_INDEX = 1;
    private final static int VALUE_INDEX = 2;
    private final static int PROCESSID_INDEX = 3;


    public static void updatePowerData() throws IOException {

        PowerShellController.runPowerShellScript(POWERSHELL_SCRIPT);

    }

    public static String getPowerSensorName(String key) throws IOException {

        return getElementWithoutQuotes(key, NAME_INDEX);

    }

    public static String getPowerSensorType(String key) throws IOException {

        return getElementWithoutQuotes(key, SENSORTYPE_INDEX);

    }

    public static double getPowerSensorValue(String key) throws IOException {

        String val = getElementWithoutQuotes(key, VALUE_INDEX);
        if (val != null) {
            return Double.parseDouble(val);
        }
        return -1;

    }

    public static String getPowerSensorProcessID(String key) throws IOException {

        return getElementWithoutQuotes(key, PROCESSID_INDEX);

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