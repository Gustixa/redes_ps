package com.example;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

public class XmppClient {

    private XMPPTCPConnection connection;

    public void connect(String username, String password) throws XmppStringprepException, XMPPException, InterruptedException {
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain("alumchat.lol")
                .setHost("alumchat.lol")
                .setPort(5222)
                .setUsernameAndPassword("arg211024-test", "211024")
                .build();

        connection = new XMPPTCPConnection(config);
        connection.connect().login();
        
        System.out.println("Connected as: " + connection.getUser());
    }

    public void sendMessage(String toJid, String message) throws XmppStringprepException, InterruptedException {
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        EntityBareJid jid = JidCreate.entityBareFrom(toJid);
        chatManager.chatWith(jid).send(message);
        System.out.println("Message sent to " + toJid + ": " + message);
    }

    public void addIncomingMessageListener() {
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addIncomingListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid from, org.jivesoftware.smack.packet.Message message, org.jivesoftware.smack.chat2.Chat chat) {
                System.out.println("Received message from " + from + ": " + message.getBody());
            }
        });
    }

    public void disconnect() {
        if (connection != null && connection.isConnected()) {
            connection.disconnect();
            System.out.println("Disconnected");
        }
    }

    public static void main(String[] args) throws Exception {
        XmppClient client = new XmppClient();
        client.connect("username", "password");
        client.addIncomingMessageListener();
        client.sendMessage("someone@yourserver.com", "Hello from Smack!");
        Thread.sleep(10000); // Wait to receive messages
        client.disconnect();
    }
}
