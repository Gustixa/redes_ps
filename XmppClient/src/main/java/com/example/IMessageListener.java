package com.example;

public interface IMessageListener {
    void onNewMessage(String fromJid, String message);
}