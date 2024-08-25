# Proyecto 1 for Networks. XMPP Chat Application

The developed project is a Chat application using XMPP. The server used in this case was alumchat.lol.

For the project, the following functionalities needed to be implemented, divided into two categories, each with specific points to cover:

Account Management (20% of the functionality, 5% each)

    Register a new account on the server.
    Log in with an account.
    Log out of an account.
    Delete the account from the server.

Communication (80% of the functionality, 10% each)

    Display all users/contacts and their status.
    Add a user to contacts.
    Show contact details of a user.
    One-on-one communication with any user/contact.
    Participate in group conversations.
    Set a presence message.
    Send/receive notifications.
    Send/receive files.


![Java](https://img.shields.io/badge/Java-21-orange)
![Maven](https://img.shields.io/badge/Maven-3.8.6-blue)
![Smack](https://img.shields.io/badge/Smack-4.4.4-green)
![License](https://img.shields.io/badge/License-MIT-yellow)

An XMPP (Extensible Messaging and Presence Protocol) chat application implemented in Java using the Smack library. This application allows users to connect to an XMPP server, send and receive messages, manage contacts, create groups, and more.

## Features

- **XMPP Authentication:** Support for login user and registration.
- **Real-time Messaging:** Send and receive text messages.
- **File transfer:** Send files to other users (sending files it's via Base64, just for small files).
- **Messages history:** Local storage of chat history (Temporarily, while the session is active).

## Requirements

- **Java 21 LTS** or higher.
- **Maven 3.8.6** or higher.
- **Servidor XMPP:** Any XMPP-compatible server (e.g, ejabberd, Openfire).

## Installation

1. **Clone the respository:**

    ```bash
    git clone https://github.com/Gustixa/redes_ps.git
    cd XmppClient
    ```

2. **Build the project with Maven:**

    ```bash
    mvn clean install
    ```

3. **Configure the XMPP server:**
   - Asegúrate de tener un servidor XMPP funcionando y configurado.
   - Actualizar alumchat.lol con el servidor a utilizar (revisar código donde este implementado)

4. **Run the application:**

    ```bash
    mvn exec:java -Dexec.mainClass="com.example.App"
    ```

## Uso

1. **Log in:**
   - Open the application and provide your user credentials to log in.
   
2. **Manage contacts:**
   - Add contacts via specifying the JID (user@server).

3. **Messaging:**
   - Select a contact and start sending and receiving messages.
   
4. **File transfer:**
   - Click the "Send file" button to transfer files to a selected contact.
   
5. **Creating chatrooms:**
   - Create groups and add contacts to start group conversations. (not implemented at all)

## Project Structure

```plaintext
xmpp-chat-app/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/
│   │   │       ├── App.java
│   │   │       ├── Contact.java
│   │   │       ├── GroupData.java
│   │   │       ├── LoginWindow.java
│   │   │       ├── RegisterWindow.java
│   │   │       ├── XmppChatApp.java
│   │   │       ├── XmppClient.java
│   └── test/
│       └── java/
│           └── com/tuusuario/xmppchatapp/
│               └── AppTest.java
│
└── pom.xml
```
## App.java

This is the main class, where the program start.

## Contact.java
## GroupData.java
## LoginWindow.java
## RegisterWindow.java
## XmppChatApp.java
## XmppClient.java

