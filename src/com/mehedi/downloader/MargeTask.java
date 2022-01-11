package com.mehedi.downloader;

import com.mehedi.utils.Constants;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class MargeTask implements Runnable {
    private String fileName;
    private String downloadDir;
    private CountDownLatch countDownLatch;
    private int totalParts;

    public MargeTask(String fileName, String downloadDir, CountDownLatch countDownLatch, int totalParts) {
        this.fileName = fileName;
        this.downloadDir = downloadDir;
        this.countDownLatch = countDownLatch;
        this.totalParts = totalParts;
    }

    @Override
    public void run() {
        try {
            countDownLatch.await();

            File[] files = findPartialFiles();
            Arrays.sort(files);
            File finalFile = createFinalFile();
            margeFiles(files, finalFile);
            deletePartials(files);
        } catch (InterruptedException | IOException e) {
            System.out.println("Failed to marge file: " + e.getMessage());
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    private void deletePartials(File[] files){
        for(File file : files){
            file.delete();
        }
    }

    private void margeFiles(File[] parts, File outputFileName){
        try(FileChannel outputChannel = new FileOutputStream(outputFileName).getChannel()){
            for(File file : parts){
                try(FileChannel inputChannel = new FileInputStream(file).getChannel()){
                    inputChannel.transferTo(0, inputChannel.size(), outputChannel);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("file not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Couldn't marge files, because " + e.getMessage());
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    private File createFinalFile() throws IOException {
        File dest = new File(getPathName());

        if(!dest.exists()){
            dest.createNewFile();
        }

        return dest;
    }

    private String getPathName() {
        return downloadDir + FileSystems.getDefault().getSeparator() + fileName;
    }

    private File[] findPartialFiles() {
        final File[] files = new File[totalParts];

        for(int i = 0; i < files.length; i++){
            files[i] = new File(getDownloadPartName(i));
        }

        return files;
    }

    private String getDownloadPartName(int partNumber) {
        return downloadDir + FileSystems.getDefault().getSeparator() + partNumber + Constants.PART_EXTENTION;
    }
}
