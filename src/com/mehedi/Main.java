package com.mehedi;

import com.mehedi.service.DownloadManager;

public class Main {

    public static void main(String[] args) {
	    final String downloadDir = "./";
	    final String url = "https://file-examples-com.github.io/uploads/2017/04/file_example_MP4_480_1_5MG.mp4";

        DownloadManager manager = new DownloadManager();
        manager.download(url, downloadDir);
    }
}
