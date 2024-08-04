const express = require('express');
const bodyParser = require('body-parser');
const Client_XMPP = require('./Client_XMPP');

const app = express();

app.use(bodyParser.json());

app.post('/login', async (req, res) => {
    const { username, password } = req.body;
    const client = new Client_XMPP(username, password);
    try {
        await client.connect();

        if (client.loginState) {
        res.status(200).json({ message: 'Logged in successfully' });
        } else {
        res.status(401).json({ message: 'Login failed' });
        }
    } catch (err) {
        res.status(500).json({ message: 'Error while logging in', error: err.message });
    }
});
app.listen(3000)