/** 
 *  CSVHandler.java
 *
 *  VERSION: 2021.03.30
 *  AUTHORS: Rae Bouldin
 */
package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class CSVHandler {

	/**
	 * Returns the row in a CSV file which contains the specified key.
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
	 * Returns the contents of a CSV file as an ArrayList.
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
	
	/** 
     *  Looks for the row in csvFilePath with the specified key, then returns
     *  the element in that row at the specified index without any quotation 
     *  marks. Returns null if no such element could be found.
     */
    public static String getElementWithoutQuotes(String csvFilePath, String key, int index) throws IOException {
        
        File dataFile = new File(csvFilePath);
        String[] row = null;
        String elem = null;
        
        // Exit if: invalid method arguments.
        if (key == null || index < 0) { return null; }
        // Exit if: dataFile doesn't exist.
        if (!dataFile.isFile()) { 
            System.err.printf("FileNotFound at \"%s\"", csvFilePath);
            System.err.println();
            return null; 
        }

        // Find the first row in the CSV file containing the key.
        row = CSVHandler.getRow(csvFilePath, key);

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