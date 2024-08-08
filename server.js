const express = require('express')
const bodyParser = require('body-parser')
const { client, xml } = require('@xmpp/client')
const cors = require('cors');

const app = express()
const port = 8000
const domain = 'alumchat.lol'

process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';

app.use(bodyParser.json())
app.use(cors());

let xmpp = null
let messages = {}
let message = []

const iniciarSesion = async (username, password) => {
  xmpp = client({
    service: `xmpp://${domain}:5222`,
    domain,
    username,
    password
  })

  xmpp.on('error', err => {
    console.error(err)
  })

  xmpp.on('stanza', stanza => {
    if (stanza.is('message')) {
      const body = stanza.getChildText('body')
      if (body) {
        console.log(`Mensaje recibido de ${stanza.attrs.from}: ${body}`)
      }
    }

    if (stanza.is('presence')) {
      const status = stanza.getChildText('status') || 'Sin mensaje de presencia'
      console.log(`Cambio en la presencia: ${stanza.attrs.from} - ${status}`)
    }
  })

  await xmpp.start().catch(console.error)
  return xmpp.jid.toString()
}

const cerrarSesion = async () => {
  if (xmpp) {
    await xmpp.stop()
    console.log('Sesión cerrada exitosamente.')
  } else {
    console.log('No hay ninguna sesión activa.')
  }
}

const enviarMensaje = async (destinatario, mensaje) => {
  const message = xml(
    'message',
    { type: 'chat', to: destinatario },
    xml('body', {}, mensaje)
  )
  await xmpp.send(message)
  console.log(`Mensaje enviado a ${destinatario}`)
}

const agregarContacto = async (contacto) => {
  const presence = xml('presence', { type: 'subscribe', to: contacto })
  await xmpp.send(presence)
  console.log(`Solicitud de suscripción enviada a ${contacto}`)
}

const definirMensajePresencia = async (mensaje) => {
  const presence = xml('presence', {}, xml('status', {}, mensaje))
  await xmpp.send(presence)
  console.log('Mensaje de presencia actualizado')
}

app.post('/login', async (req, res) => {
  const { username, password } = req.body
  try {
    const jid = await iniciarSesion(username, password)
    res.status(200).json({ message: 'Sesión iniciada', jid })
  } catch (error) {
    res.status(500).json({ message: 'Error al iniciar sesión', error: error.message })
  }
})

app.post('/logout', async (req, res) => {
  try {
    await cerrarSesion()
    res.status(200).json({ message: 'Sesión cerrada' })
  } catch (error) {
    res.status(500).json({ message: 'Error al cerrar sesión', error: error.message })
  }
})

app.post('/message', async (req, res) => {
  const { destinatario, mensaje } = req.body
  try {
    await enviarMensaje(destinatario, mensaje)
    res.status(200).json({ message: 'Mensaje enviado' })
  } catch (error) {
    res.status(500).json({ message: 'Error al enviar mensaje', error: error.message })
  }
})

app.post('/add-contact', async (req, res) => {
  const { contacto } = req.body
  try {
    await agregarContacto(contacto)
    res.status(200).json({ message: 'Contacto agregado' })
  } catch (error) {
    res.status(500).json({ message: 'Error al agregar contacto', error: error.message })
  }
})

app.post('/presence', async (req, res) => {
  const { mensaje } = req.body
  try {
    await definirMensajePresencia(mensaje)
    res.status(200).json({ message: 'Mensaje de presencia actualizado' })
  } catch (error) {
    res.status(500).json({ message: 'Error al definir mensaje de presencia', error: error.message })
  }
})

app.listen(port, () => {
  console.log(`API escuchando en http://localhost:${port}`)
})
