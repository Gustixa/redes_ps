package com.example;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.IQChildElementXmlStringBuilder;
import org.jxmpp.jid.EntityBareJid;

public class MamQueryIQ extends IQ {

    public static final String ELEMENT = "query";
    public static final String NAMESPACE = "urn:xmpp:mam:2";
    private final EntityBareJid withJid;

    public MamQueryIQ(EntityBareJid withJid) {
        super(ELEMENT, NAMESPACE);
        this.withJid = withJid;
        this.setType(Type.set); // Cambiado a 'set' porque es común para consultas MAM
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();

        xml.append("<x xmlns='jabber:x:data' type='submit'>");
        xml.append("<field var='FORM_TYPE' type='hidden'><value>").append(NAMESPACE).append("</value></field>");

        if (withJid != null) {
            xml.append("<field var='with'><value>").append(withJid.toString()).append("</value></field>");
        }

        xml.append("</x>");
        return xml;
    }
}
