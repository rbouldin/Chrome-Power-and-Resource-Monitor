/** 
 *  PowerShellController.java
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class PowerShellController {

    public static void runPowerShellScript(String scriptPath) throws IOException {

        //String command = "powershell.exe  your command";
        //Getting the version
        String command = "powershell.exe powershell -executionPolicy bypass . '" + scriptPath + "'";
        // String command = "powershell.exe " + scriptPath;

        // Executing the command
        Process powerShellProcess = Runtime.getRuntime().exec(command);
        
        // Getting the results
        powerShellProcess.getOutputStream().close();
        
        String line;
        // System.out.println("Standard Output:");
        BufferedReader stdout = new BufferedReader(new InputStreamReader(
            powerShellProcess.getInputStream()));
        
        while ((line = stdout.readLine()) != null) {
            System.out.println(line);
        }
        stdout.close();
        
        // System.out.println("Standard Error:");
        BufferedReader stderr = new BufferedReader(new InputStreamReader(
            powerShellProcess.getErrorStream()));
        
        while ((line = stderr.readLine()) != null) {
            System.out.println(line);
        }
        stderr.close();
        
        // System.out.println("Done");

    }

}