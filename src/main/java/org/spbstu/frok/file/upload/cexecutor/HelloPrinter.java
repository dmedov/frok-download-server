package org.spbstu.frok.file.upload.cexecutor;

import java.io.PrintWriter;

public class HelloPrinter implements Runnable {
    public HelloPrinter() {
    }
    @Override
    public void run() {
        for(int j = 0; j < 10; j++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }
}
