package org.spbstu.frok.classifier;

import java.io.*;
import java.util.List;

public class Classifier
{
    private static final String PROCESS_DIRECTORY = "/tmp/";

    private static String executeCommand(List<String> command) throws IOException, InterruptedException
    {
        String output = "";

        ProcessBuilder procBuilder = new ProcessBuilder(command);
        procBuilder.directory(new File(PROCESS_DIRECTORY));
        procBuilder.redirectErrorStream(true);

        Process proc = procBuilder.start();

        InputStream processOutput = proc.getInputStream();
        InputStreamReader outputReader = new InputStreamReader(processOutput);
        BufferedReader bufferReader = new BufferedReader(outputReader);

        proc.waitFor();

        // Fill output variable with process output
        String tmp = null;
        StringBuilder stringBuilder = new StringBuilder(output);
        while((tmp = bufferReader.readLine()) != null)
        {
            stringBuilder.append(tmp);
            stringBuilder.append("\n");
        }
        output = stringBuilder.toString();

        return output;
    }
}

