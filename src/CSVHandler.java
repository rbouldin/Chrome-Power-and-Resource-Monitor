/** 
 *  CSVHandler.java
 *
 *  VERSION: 2021.03.17
 *  AUTHORS: Rae Bouldin
 */
package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class CSVHandler {

    /**
     *  Returns the row in a CSV file which contains the specified key.
     */
    public static String[] getRow(String csvFilePath, String key) throws IOException {

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                for (int i = 0; i < values.length; i++) {
                    if (values[i].contains(key)) {
                        return values;
                    }
                }
            }

        }

        return null;

    }

    /**
     *  Returns the contents of a CSV file as an ArrayList.
     */
    public static List<List<String>> toArrayList(String csvFilePath) throws IOException {

        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                records.add(Arrays.asList(values));
            }
        }

        return records;

    }

}