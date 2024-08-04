"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.XmppService = void 0;
const common_1 = require("@nestjs/common");
const client_1 = require("@xmpp/client");
const net_1 = require("net");
const fs = require("fs");
const path = require("path");
let XmppService = class XmppService {
    constructor() {
        this.clients = {};
    }
    async connect(username, password) {
        const xmppClient = (0, client_1.client)({
            service: "xmpp://alumchat.lol:5222",
            domain: "alumchat.lol",
            username,
            password,
            tls: { rejectUnauthorized: false },
        });
        xmppClient.on('error', (err) => {
            if (err.condition == 'not-authorized') {
                console.error('Error while logging in');
            }
        });
        xmppClient.on('online', async () => {
            await xmppClient.send((0, client_1.xml)('presence', { type: 'online' }));
        });
        await xmppClient.start();
        this.clients[username] = xmppClient;
        return xmppClient;
    }
    async sendMessage(username, to, message) {
        const xmppClient = this.clients[username];
        const msg = (0, client_1.xml)('message', { type: 'chat', to: `${to}@alumchat.lol` }, (0, client_1.xml)('body', {}, message));
        await xmppClient.send(msg);
    }
    async sendFile(username, to, filePath) {
        const xmppClient = this.clients[username];
        const file = fs.readFileSync(filePath);
        const fileName = path.basename(filePath);
        const base64File = file.toString('base64');
        const msg = (0, client_1.xml)('message', { type: 'chat', to: `${to}@alumchat.lol` }, (0, client_1.xml)('body', {}, `File: ${fileName} content: ${base64File}`));
        await xmppClient.send(msg);
    }
    async registerUser(username, password) {
        const netClient = new net_1.Socket();
        netClient.connect(5222, 'alumchat.lol', function () {
            netClient.write("<stream:stream to='alumchat.lol' xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' version='1.0'>");
        });
        netClient.on('data', async (data) => {
            if (data.toString().includes('<stream:features>')) {
                const register = `
            <iq type="set" id="reg_1" mechanism='PLAIN'>
            <query xmlns="jabber:iq:register">
              <username>${username}</username>
              <password>${password}</password>
            </query>
          </iq>`;
                netClient.write(register);
            }
            else if (data.toString().includes('<iq type="result"')) {
                console.log('User registered into server');
                netClient.end();
            }
            else if (data.toString().includes('<iq type="error"')) {
                console.log('XMPP Server error');
            }
        });
        netClient.on('close', function () {
            console.log('Connection closed');
        });
    }
};
exports.XmppService = XmppService;
exports.XmppService = XmppService = __decorate([
    (0, common_1.Injectable)()
], XmppService);
//# sourceMappingURL=xmpp.service.js.map