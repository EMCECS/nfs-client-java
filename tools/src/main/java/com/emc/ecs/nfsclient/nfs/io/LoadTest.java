/**
 * Copyright 2016 EMC Corporation. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.emc.ecs.nfsclient.nfs.io;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.emc.ecs.nfsclient.nfs.nfs3.Nfs3;
import com.emc.ecs.nfsclient.rpc.CredentialUnix;
import com.emc.ecs.nfsclient.rpc.RpcRequest;

/**
 * @author seibed
 *
 */
public class LoadTest {

    public static void main(String[] args) {
        String export = args[0];
        String fileName = args[1];
        int fileSizeInKiloBytes = Integer.parseInt(args[2]);
        int maximumNumberOfThreads = Integer.parseInt(args[3]);
        int megaBytesToRead = Integer.parseInt(args[4]);
        try {
            testThreadedReadingPerformance(export, fileName, fileSizeInKiloBytes, maximumNumberOfThreads, megaBytesToRead);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testThreadedReadingPerformance(String export, String fileName, int fileSizeInKiloBytes, int maximumNumberOfThreads, int megaBytesToRead)
            throws Exception {
        Nfs3 nfs3 = new Nfs3(export, new CredentialUnix(0, 0, null), 3);
        int fileLength = writeFile(nfs3, fileName, fileSizeInKiloBytes);
        for (int numberOfThreads = 1; numberOfThreads <= maximumNumberOfThreads; numberOfThreads = 2 * numberOfThreads) {
            testThreadedReadingPerformance(nfs3, fileName, fileLength, numberOfThreads, megaBytesToRead);
        }
        new Nfs3File(nfs3, fileName).delete();
    }

    /**
     * @param string
     * @param fileSizeInKiloBytes
     * @return
     * @throws IOException 
     */
    private static int writeFile(Nfs3 nfs3, String fileName, int fileSizeInKiloBytes) throws IOException {
        Nfs3File test = new Nfs3File(nfs3, fileName);

        byte[] dataChunk = "another chunk of data!".getBytes(RpcRequest.CHARSET);
        int longerSize = (1000 * fileSizeInKiloBytes) + dataChunk.length;
        NfsFileOutputStream outputStream = new NfsFileOutputStream(test);
        byte[] expectedData = new byte[longerSize];
        int chunkStart = 0;
        while (chunkStart + dataChunk.length < longerSize) {
            System.arraycopy(dataChunk, 0, expectedData, chunkStart, dataChunk.length);
            chunkStart += dataChunk.length;
        }
        outputStream.write(expectedData);
        outputStream.close();
        return longerSize;
    }

    private static void testThreadedReadingPerformance(final Nfs3 nfs3, final String fileName, final int fileLength, final int numberOfThreadsToUse, int megaBytesToRead)
            throws Exception {
        final ThreadCounter threadCounter = new ThreadCounter();
        Future<?>[] futures = new Future<?>[numberOfThreadsToUse];
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreadsToUse);
        long timeInMillis = Calendar.getInstance().getTimeInMillis();
        while (((long) fileLength) * threadCounter.getNumberOfReadingThreadsCompleted() < (1000000L * megaBytesToRead)) {
            for (int i = 0; i < numberOfThreadsToUse; ++i) {
                if ((futures[i] == null) || futures[i].isDone()) {
                    Thread thread = new Thread() {

                        @Override
                        public void run() {
                            NfsFileInputStream inputStream = null;
                            try {
                                    inputStream = new NfsFileInputStream(new Nfs3File(nfs3, fileName));
                                    byte[] buffer = new byte[fileLength + 5];
                                    int bytesRead = inputStream.read(buffer);
                                    if (NfsFileInputStream.EOF != inputStream.read()) {
                                        throw new Exception("Reading error - should have been at the end of the file");
                                    }
                                    if (fileLength != bytesRead) {
                                        throw new Exception("Reading error - read " + bytesRead
                                                + " bytes, should have been " + fileLength);
                                    }
                                    threadCounter.incrementNumberOfReadingThreadsCompleted();
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            } finally {
                                if (inputStream != null) {
                                    try {
                                        inputStream.close();
                                    } catch (IOException e) {
                                        // e.printStackTrace();
                                    }
                                }
                            }
                        }

                    };
                    thread.setDaemon(true);
                    futures[i] = executorService.submit(thread);
                }
            }
            Thread.sleep(1);
        }

        timeInMillis = Calendar.getInstance().getTimeInMillis() - timeInMillis;
        System.out.println("Milliseconds to read " + megaBytesToRead + " MB of " + (fileLength / 1000) + " kilobyte files with "
                + numberOfThreadsToUse + " threads: " + timeInMillis);

        // Wait until all threads have completed.
        for (int i = 0; i < numberOfThreadsToUse; ++i) {
            try {
                futures[i].get();
            } catch (Exception e) {
                // do nothing, the thread is done either way.
            }
        }
    }

    /**
     * @author seibed
     *
     */
    private static final class ThreadCounter {

        int numberOfReadingThreadsCompleted = 0;

        public int getNumberOfReadingThreadsCompleted() {
            return numberOfReadingThreadsCompleted;
        }

        public synchronized void incrementNumberOfReadingThreadsCompleted() {
            ++numberOfReadingThreadsCompleted;
        }
    }

}
