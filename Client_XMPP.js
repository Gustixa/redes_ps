const { client, xml } = require('@xmpp/client');
const debug = require('@xmpp/debug');
const path = require('path');
const fs = require('fs');
const netClient = require('net').Socket(); // Required for user registration

process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0'; // Allows self-signed certificates

/**
 * Client_XMPP class for XMPP client functionalities
 */
class Client_XMPP {
  /**
   * Constructor for Client_XMPP
   * @param {string} username - Username for the XMPP client
   * @param {string} password - Password for the XMPP client
   * @param {string} service - XMPP service URL
   * @param {string} domain - XMPP domain
   */
  constructor(username, password, service = 'xmpp://alumchat.lol:5222', domain = 'alumchat.lol') {
    this.username = username;
    this.password = password;
    this.service = service;
    this.domain = domain;

    this.xmpp = null;
    this.loginState = false;

    this.messages = [];
    this.receivedSubscriptions = [];
    this.receivedGroupChatInvites = [];
  }

  /**
   * Connects to the XMPP server
   */
  async connect() {
    this.xmpp = client({
      service: this.service,
      domain: this.domain,
      username: this.username,
      password: this.password,
      terminal: true,
      tls: {
        rejectUnauthorized: false
      }
    });

    this.xmpp.on('error', (err) => {
      if (err.condition === 'not-authorized') {
        console.error('Error while logging in');
      }
    });

    this.xmpp.on('online', async () => {
      await this.xmpp.send(xml('presence', { type: 'online' }));

      this.xmpp.on('stanza', (stanza) => {
        if (stanza.is('message') && stanza.attrs.type === 'chat') {
          const from = stanza.attrs.from;
          const body = stanza.getChildText('body');
          const message = { from, body };

          if (body) {
            console.log(`Received message from ${from.split('@')[0]}:`, body);
          }
        } else if (stanza.is('presence') && stanza.attrs.type === 'subscribe') {
          const from = stanza.attrs.from;
          this.receivedSubscriptions.push(from);
          console.log('Received subscription request from:', from.split('@')[0]);
          console.log('Request message:', stanza.getChildText('status'));
        } else if (stanza.is('message') && stanza.attrs.from.includes('@conference.alumchat.lol')) {
          const groupchat = stanza.attrs.from;
          const to = stanza.attrs.to;

          this.receivedGroupChatInvites.push(groupchat);

          if (!to.includes('/')) {
            console.log('Group chat invitation from: ', groupchat);
          }
        }
      });
    });

    await this.xmpp.start().then(() => { this.loginState = true }).catch((err) => {
      if (err.condition === 'not-authorized') {
        console.error('This user may not exist in the server. Please try again.');
      }
    });
  }

  /**
   * Deletes the user account from the XMPP server
   */
  async deleteAccount() {
    const deleteRequest = xml('iq', { type: 'set', id: 'unreg1' }, xml('query', { xmlns: 'jabber:iq:register' }, xml('remove', {})));
    this.xmpp.send(deleteRequest).then(() => {
      console.log('Account deleted successfully');
      this.xmpp.stop();
    }).catch((err) => {
      console.error('Error when deleting account: ', err);
    });
  }

  /**
   * Adds a contact to the user's roster
   * @param {string} jid - JID of the contact to be added
   */
  async addContacts(jid) {
    const presence = xml('presence', { type: 'subscribe', to: jid + '@alumchat.lol' });
    this.xmpp.send(presence).then(() => {
      console.log('Contact request sent to: ', jid);
    }).catch((err) => {
      console.error('Error when adding contact: ', err);
    });
  }

  /**
   * Sends a file to a specified user
   * @param {string} sendTo - JID of the recipient
   * @param {string} filePath - Path to the file to be sent
   */
  async sendFile(sendTo, filePath) {
    const user = sendTo + '@alumchat.lol';

    const file = fs.readFileSync(filePath);
    const fileName = path.basename(filePath);
    const base64File = file.toString('base64');

    const message = xml('message', { type: 'chat', to: user }, xml('body', {}, ' File: ' + fileName + ' content: ' + base64File));
    await this.xmpp.send(message);

    console.log('File sent successfully');
  }

  /**
   * Sends a file to a group chat
   * @param {string} sendTo - JID of the group chat
   * @param {string} filePath - Path to the file to be sent
   */
  async sendFileGC(sendTo, filePath) {
    const user = sendTo;

    const file = fs.readFileSync(filePath);
    const fileName = path.basename(filePath);
    const base64File = file.toString('base64');

    const message = xml('message', { type: 'groupchat', to: user }, xml('body', {}, ' File: ' + fileName + ' content: ' + base64File));
    await this.xmpp.send(message);

    console.log('File sent successfully');
  }

  /**
   * Creates a group chat
   * @param {string} roomName - Name of the group chat room
   */
  async createGC(roomName) {
    const roomId = roomName + '@conference.alumchat.lol';

    await this.xmpp.send(xml('presence', { to: roomId + '/' + this.username }));
    console.log('Joined group chat successfully');
  }

  /**
   * Sets the presence message and status
   * @param {string} presenceState - State of the presence (e.g., 'away', 'dnd', 'online')
   * @param {string} message - Message to be set
   */
  async setPresenceMessage(presenceState, message) {
    const presence = xml('presence', {}, xml('show', {}, presenceState), xml('status', {}, message));
    await this.xmpp.send(presence);

    console.log('Presence status and message set to: ', presenceState, message);
  }

  /**
   * Displays the user's contacts
   */
  async mostrarUsuarios() {
    const requestContacts = xml(
      'iq',
      { type: 'get', id: 'roster' },
      xml('query', { xmlns: 'jabber:iq:roster' })
    );

    this.xmpp.send(requestContacts)
      .then(() => {
        console.log('Requesting Contacts...');
      }).catch((err) => {
        console.error('Error when requesting contacts: ', err);
      });

    this.xmpp.on('stanza', (stanza) => {
      if (stanza.is('iq') && stanza.attrs.type === 'result') {
        const contacts = stanza.getChild('query', 'jabber:iq:roster').getChildren('item');

        console.log('Contact list: ');
        contacts.forEach((contact) => {
          console.log('JID', contact.attrs.jid);
          console.log('Name', contact.attrs.name);
          console.log('Subscription', contact.attrs.subscription);
        });
      }
    });
  }

  /**
   * Shows details of a specified user
   * @param {string} jid - JID of the user to show details for
   */
  async showUserDetails(jid) {
    const username = jid + '@alumchat.lol';

    this.xmpp.on('stanza', (stanza) => {
      if (stanza.is('iq') && stanza.attrs.type === 'result') {
        const users = stanza.getChild('query', 'jabber:iq:roster').getChildren('item');
        const user = users.find((user) => user.attrs.jid === username);
        if (user) {
          const jid = user.attrs.jid;
          const name = user.attrs.name;
          const subscription = user.attrs.subscription;
          console.log('Contact:', jid, 'Name:', name, 'Subscription:', subscription);
        } else {
          console.log('User not found');
        }
      }
    });

    const requestContacts = xml(
      'iq',
      { type: 'get', id: 'roster' },
      xml('query', { xmlns: 'jabber:iq:roster' })
    );

    this.xmpp.send(requestContacts)
      .then(() => {
        console.log('Requesting Contacts...');
      }).catch((err) => {
        console.error('Error when requesting contacts: ', err);
      });
  }

  /**
   * Registers a new user on the XMPP server
   * @param {string} username - Username of the new user
   * @param {string} password - Password of the new user
   */
  async registerUser(username, password) {
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
          </iq>
        `;
        await netClient.write(register);
      } else if (data.toString().includes('<iq type="result"')) {
        console.log('User registered into server');
        this.registerState = true;
        await netClient.end();
      } else if (data.toString().includes('<iq type="error"')) {
        console.log('XMPP Server error');
      }
    });

    netClient.on('close', function () {
      console.log('Connection closed');
    });
  }

  /**
   * Sends a message to a specified user
   * @param {string} destinatario - JID of the recipient
   * @param {string} mensaje - Message content
   */
  async sendMessage(destinatario, mensaje) {
    const message = xml(
      'message',
      { type: 'chat', to: destinatario + '@alumchat.lol' },
      xml('body', {}, mensaje)
    );

    await this.xmpp.send(message);
    console.log(`Sent message to ${destinatario}: ${mensaje}`);
  }
}

module.exports = Client_XMPP;
