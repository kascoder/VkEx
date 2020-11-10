package org.kascoder.vkex.updater;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpDownloadUtils {
    private int contentLength;
    private InputStream inputStream;
    private HttpURLConnection httpURLConnection;

    public void downloadFile(URL url) throws Exception {
        this.httpURLConnection = (HttpURLConnection) url.openConnection();
        int responseCode = httpURLConnection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            String disposition = httpURLConnection.getHeaderField("Content-Disposition");
            String contentType = httpURLConnection.getContentType();
            this.contentLength = httpURLConnection.getContentLength();

            System.out.println("Content-Type = " + contentType);
            System.out.println("Content-Disposition = " + disposition);
            System.out.println("Content-Length = " + this.contentLength);

            this.inputStream = httpURLConnection.getInputStream();
        } else {
            throw new Exception("No file to download. Server replied HTTP code: " + responseCode);
        }
    }

    public void disconnect() throws IOException {
        this.inputStream.close();
        this.httpURLConnection.disconnect();
    }

    public int getContentLength() {
        return this.contentLength;
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }
}
