package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class UploadFile {
    private String id;
    private String name;
    private String type;
    private long size;
    private byte[] data;
    private String to;

    public UploadFile(File file) throws IOException {
        this.name = file.getName();
        this.type = Files.probeContentType(file.toPath());
        this.size = file.length();
        this.data = Files.readAllBytes(file.toPath());
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public long getSize() {
        return size;
    }

    public byte[] getData() {
        return data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
