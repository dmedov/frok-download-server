package org.spbstu.frok.file.upload.scheduler;

import java.util.LinkedList;

public class Scheduler {
    private final int nThreads;
    private final PoolWorker[] threads;
    private final Object monitor = new Object();

    public Scheduler(int nThreads)
    {
        this.nThreads = nThreads;
        threads = new PoolWorker[nThreads];

        for (int i=0; i<nThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].start();
        }
    }

    public int execute(Runnable r) {
        synchronized(monitor) {
            for(int i = 0; i < nThreads; i++) {
                if(threads[i].getTaskStatus()) {
                    threads[i].setRunnable(r);
                    monitor.notify();
                    return i;
                }
            }
            return -1;
        }
    }

    public boolean getTaskStatus(int index) {
        return threads[index].getTaskStatus();
    }

    public void join(int index) {
        while(!threads[index].getTaskStatus()) {}
        return;
    }

    private class PoolWorker extends Thread {
        private boolean isFree = true;
        Runnable r;

        public boolean getTaskStatus() {
            return isFree;
        }
        public void setRunnable(Runnable r) {
            this.r = r;
        }
        public void run() {


            while (true) {
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException ignore) {}
                }
                isFree = false;
                // If we don't catch RuntimeException,
                // the pool could leak threads
                try {
                    r.run();
                }
                catch (RuntimeException e) {
                    isFree = true;
                    continue;
                }
                isFree = true;
            }
        }
    }
}
