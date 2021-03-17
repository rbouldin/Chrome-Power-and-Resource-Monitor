/** 
 *  CSVParser.java
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

import java.io.BufferedReader;
// import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class CSVParser {

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

}