import React, { useState, useEffect } from 'react';
import { client } from '@xmpp/client';

const App = () => {
  const [xmppClient, setXmppClient] = useState(null);

  useEffect(() => {
    const xmppClient = client({
      service: 'wss://alumchat.lol:5280/xmpp-websocket',
      domain: 'alumchat.lol',
      resource: 'example',
      username: 'arg211024-test',
      password: '211024',
    });

    xmppClient.start().catch(console.error);

    xmppClient.on('online', (address) => {
      console.log(`Conectado como ${address.toString()}`);
      xmppClient.sendPresence();
    });

    xmppClient.on('error', (err) => {
      console.error('Error de conexión:', err);
    });

    xmppClient.on('stanza', (stanza) => {
      console.log('Mensaje recibido:', stanza.toString());
    });

    setXmppClient(xmppClient);

    return () => xmppClient.stop();
  }, []);

  const sendMessage = async (to, message) => {
    if (xmppClient) {
      const messageStanza = (
        <message to={to} type="chat">
          <body>{message}</body>
        </message>
      );
      await xmppClient.send(messageStanza);
    }
  };

  return (
    <div>
      <h1>Cliente XMPP</h1>
      <button onClick={() => sendMessage('echobot@alumchat.lol', 'Hola!')}>
        Enviar Mensaje
      </button>
    </div>
  );
};

export default App;
