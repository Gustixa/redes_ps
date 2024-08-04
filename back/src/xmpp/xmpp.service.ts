import { Injectable } from '@nestjs/common'
import { client, xml } from '@xmpp/client'
import { Socket } from 'net'
import * as fs from 'fs'
import * as path from 'path'

@Injectable()
export class XmppService {
    private clients: { [key: string]: any } = {};

    async connect(username: string, password: string) {
      const xmppClient = client({
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
        await xmppClient.send(xml('presence', { type: 'online' }));
      });
  
      await xmppClient.start();
      this.clients[username] = xmppClient;
      return xmppClient;
    }
  
    async sendMessage(username: string, to: string, message: string) {
      const xmppClient = this.clients[username];
      const msg = xml('message', { type: 'chat', to: `${to}@alumchat.lol` }, xml('body', {}, message));
      await xmppClient.send(msg);
    }
  
    async sendFile(username: string, to: string, filePath: string) {
      const xmppClient = this.clients[username];
      const file = fs.readFileSync(filePath);
      const fileName = path.basename(filePath);
      const base64File = file.toString('base64');
      const msg = xml('message', { type: 'chat', to: `${to}@alumchat.lol` }, xml('body', {}, `File: ${fileName} content: ${base64File}`));
      await xmppClient.send(msg);
    }
  
    async registerUser(username: string, password: string) {
      const netClient = new Socket();
      netClient.connect(5222, 'alumchat.lol', function() {
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
        } else if (data.toString().includes('<iq type="result"')) {
          console.log('User registered into server');
          netClient.end();
        } else if (data.toString().includes('<iq type="error"')) {
          console.log('XMPP Server error');
        }
      });
  
      netClient.on('close', function() {
        console.log('Connection closed');
      });
    }
}
