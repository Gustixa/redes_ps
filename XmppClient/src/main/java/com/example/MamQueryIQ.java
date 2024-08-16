package com.example;

import org.jivesoftware.smack.packet.IQ;

public class MamQueryIQ extends IQ {

    private static final String ELEMENT = "query";
    private static final String NAMESPACE = "urn:xmpp:mam:1";

    private final String startDate;
    private final String endDate;

    public MamQueryIQ(String startDate, String endDate) {
        super(ELEMENT, NAMESPACE);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        if (startDate != null) {
            xml.append("<start>").append(startDate).append("</start>");
        }
        if (endDate != null) {
            xml.append("<end>").append(endDate).append("</end>");
        }
        return xml;
    }
}
