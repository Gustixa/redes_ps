package com.example;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.util.XmlStringBuilder;

public class FileUploadIQ extends IQ {

    private final String fileName;
    private final long fileSize;
    private final String contentType;

    public FileUploadIQ(String fileName, long fileSize, String contentType) {
        super("request", "urn:xmpp:http:upload:0");
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.setType(Type.get);
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.attribute("filename", fileName);
        xml.attribute("size", fileSize);
        xml.attribute("content-type", contentType);
        xml.rightAngleBracket();
        return xml;
    }
}