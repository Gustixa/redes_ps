package com.example;

import java.util.List;

public class GroupData {
    private final String groupName;
    private final List<String> contacts;
    private final boolean isPublic;

    public GroupData(String groupName, List<String> contacts, boolean isPublic) {
        this.groupName = groupName;
        this.contacts = contacts;
        this.isPublic = isPublic;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<String> getContacts() {
        return contacts;
    }

    public boolean isPublic() {
        return isPublic;
    }
}
