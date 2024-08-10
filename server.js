const express = require('express')
const bodyParser = require('body-parser')
const { client, xml } = require('@xmpp/client')
const cors = require('cors');

const PORT = 8000


process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';

app.use(bodyParser.json())
app.use(cors());

const express = require('express');
const bodyParser = require('body-parser');
const Client_XMPP = require('./xmppClient'); // Importa la lógica XMPP

const app = express();
app.use(bodyParser.json());

// Endpoint para iniciar sesión
app.post('/login', async (req, res) => {
    const { username, password } = req.body;
    const client = new Client_XMPP(username, password);
    
    try {
        await client.connect();
        if (client.loginState) {
            res.status(200).json({ message: "Logged in successfully" });
        } else {
            res.status(401).json({ message: "Login failed" });
        }
    } catch (error) {
        res.status(500).json({ message: "Error logging in", error });
    }
});

// Endpoint para registrar un nuevo usuario
app.post('/register', async (req, res) => {
    const { username, password } = req.body;
    const client = new Client_XMPP(username, password);
    
    try {
        await client.registerUser(username, password);
        res.status(201).json({ message: "User registered successfully" });
    } catch (error) {
        res.status(500).json({ message: "Error registering user", error });
    }
});

// Endpoint para enviar un mensaje
app.post('/sendMessage', async (req, res) => {
    const { username, password, destinatario, mensaje } = req.body;
    const client = new Client_XMPP(username, password);

    try {
        await client.connect();
        await client.sendMessage(destinatario, mensaje);
        res.status(200).json({ message: "Message sent successfully" });
    } catch (error) {
        res.status(500).json({ message: "Error sending message", error });
    }
});

// Endpoint para agregar un contacto
app.post('/addContact', async (req, res) => {
    const { username, password, contact } = req.body;
    const client = new Client_XMPP(username, password);

    try {
        await client.connect();
        await client.addContacts(contact);
        res.status(200).json({ message: "Contact added successfully" });
    } catch (error) {
        res.status(500).json({ message: "Error adding contact", error });
    }
});

// Iniciar el servidor
app.listen(PORT, () => {
    console.log($`Server running on port ${PORT}`);
});

