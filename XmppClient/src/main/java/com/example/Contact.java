package com.example;

public class Contact {
    private String jid;
    private String name;
    private String status;

    public Contact(String jid, String name, String status) {
        this.jid = jid;
        this.name = name;
        this.status = status;
    }

    public String getJid() {
        return jid;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return name + " (" + status + ")";
    }
}
