const { client, xml } = require("@xmpp/client");
const debug = require("@xmpp/debug");
const fs = require('fs');
const path = require('path');
const netClient = require("net").Socket();

const DOMAIN = 'alumchat.lol'

class Client_XMPP {
    constructor(username, password, service = "xmpp://alumchat.lol:5222", domain = DOMAIN) {
        this.username = username;
        this.password = password;
        this.service = service;
        this.domain = domain;
        this.xmpp = null;
        this.loginState = false;
        this.receivedSubscriptions = [];
        this.receivedGroupChatInvites = [];
    }

    async connect() {
        this.xmpp = client({
            service: this.service,
            domain: this.domain,
            username: this.username,
            password: this.password,
            terminal: true,
            tls: { rejectUnauthorized: false },
        });

        this.xmpp.on("error", (err) => {
            if (err.condition === "not-authorized") {
                console.error("Error while logging in");
            }
        });

        this.xmpp.on("online", async () => {
            await this.xmpp.send(xml("presence", { type: "online" }));
            this.loginState = true;

            this.xmpp.on("stanza", (stanza) => {
                if (stanza.is('message') && stanza.attrs.type === 'chat') {
                    const from = stanza.attrs.from;
                    const body = stanza.getChildText("body");
                    if (body) {
                        console.log(`Received message from ${from.split('@')[0]}:`, body);
                    }
                } else if (stanza.is('presence') && stanza.attrs.type === 'subscribe') {
                    const from = stanza.attrs.from;
                    this.receivedSubscriptions.push(from);
                    console.log("Received subscription request from:", from.split('@')[0]);
                } else if (stanza.is('message') && stanza.attrs.from.includes('@conference.alumchat.lol')) {
                    const groupchat = stanza.attrs.from;
                    this.receivedGroupChatInvites.push(groupchat);
                    console.log("Group chat invitation from:", groupchat);
                }
            });
        });

        await this.xmpp.start().catch((err) => {
            if (err.condition === "not-authorized") {
                console.error("This user may not exist on the server. Please try again.");
            }
        });
    }

    async registerUser(username, password) {
        return new Promise((resolve, reject) => {
            netClient.connect(5222, 'alumchat.lol', function() {
                netClient.write("<stream:stream to='alumchat.lol' xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' version='1.0'>");
            });

            netClient.on('data', async(data) => {
                if(data.toString().includes("<stream:features>")) {
                    const register = `
                    <iq type="set" id="reg_1" mechanism='PLAIN'>
                    <query xmlns="jabber:iq:register">
                      <username>${username}</username>
                      <password>${password}</password>
                    </query>
                  </iq>
                  `;
                    await netClient.write(register);
                } else if(data.toString().includes('<iq type="result"')) {
                    console.log("User registered into server");
                    resolve(true);
                    await netClient.end();
                } else if(data.toString().includes('<iq type="error"')) {
                    console.log("XMPP Server error");
                    reject(new Error("XMPP Server error"));
                }
            });

            netClient.on('close', function() {
                console.log('Connection closed');
            });
        });
    }

    async sendMessage(destinatario, mensaje) {
        const message = xml(
            "message",
            { type: "chat", to: destinatario + "@alumchat.lol" },
            xml("body", {}, mensaje)
        );
        await this.xmpp.send(message);
    }

    async addContacts(jid) {
        const presence = xml("presence", { type: "subscribe", to: jid + "@alumchat.lol" });
        await this.xmpp.send(presence);
    }
}

module.exports = Client_XMPP;
