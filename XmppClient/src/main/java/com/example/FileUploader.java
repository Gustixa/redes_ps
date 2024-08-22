package com.example;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FileUploader {

    private HttpClient httpClient;

    public FileUploader(String pemFilePath) throws Exception {
        this.httpClient = SSLUtilities.createHttpClientWithPem(pemFilePath);
    }

    public String uploadFile(File file) throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(new URI("https://alumchat.lol:7443/httpfileupload/"))
            .POST(HttpRequest.BodyPublishers.ofFile(file.toPath()))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new IOException("Failed to upload file. Server responded with code: " + response.statusCode());
        }
    }
}
