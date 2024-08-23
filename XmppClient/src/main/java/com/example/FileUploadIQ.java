package com.example;

import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.Jid;

public class FileUploadIQ extends IQ {
    
    public static final String ELEMENT = "request";
    public static final String NAMESPACE = "urn:xmpp:http:upload:0";
    
    private final String fileName;
    private final long size;
    private final String contentType;

    public FileUploadIQ(String fileName, long size, String contentType) {
        super(ELEMENT, NAMESPACE);
        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.append("<filename>").append(fileName).append("</filename>");
        xml.append("<size>").append(String.valueOf(size)).append("</size>");
        xml.append("<content-type>").append(contentType).append("</content-type>");
        return xml;
    }

    public String getFileName() {
        return fileName;
    }

    public long getSize() {
        return size;
    }

    public String getContentType() {
        return contentType;
    }
}
