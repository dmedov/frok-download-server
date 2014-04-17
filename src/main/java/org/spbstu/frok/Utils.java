package org.spbstu.frok;

import java.io.*;
import java.util.List;

/**
 * Created by den on 17/04/14.
 */
public class Utils {
    public static String executeCommand(String processDirectory, List<String> command) {
        String output = "";
        try {

            ProcessBuilder procBuilder = new ProcessBuilder(command);
            procBuilder.directory(new File(processDirectory));
            procBuilder.redirectErrorStream(true);

            Process proc = procBuilder.start();

            InputStream processOutput = proc.getInputStream();
            InputStreamReader outputReader = new InputStreamReader(processOutput);
            BufferedReader bufferReader = new BufferedReader(outputReader);

            proc.waitFor();

            // Fill output variable with process output
            String tmp = null;
            StringBuilder stringBuilder = new StringBuilder(output);
            while ((tmp = bufferReader.readLine()) != null) {
                stringBuilder.append(tmp);
                stringBuilder.append("\n");
            }
            output = stringBuilder.toString();

        } catch (IOException e) {

        } catch (InterruptedException e) {

        }

        return output;
    }

}
