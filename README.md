# Proyecto 1 de redes. XMPP Chat Application

El proyecto desarrollado es un Chat utilizando XMPP. El servidor, en este caso, era alumchat.lol
Para el proyecto, se debian cumplir con las siguientes funcionalidades, divididaes en 2 categorias,
cada una teniendo ciertos puntos a cubrir. Estas son las siguientes:

Administración de cuentas (20% del funcionamiento, 5% cada funcionalidad)
1) Registrar una nueva cuenta en el servidor
2) Iniciar sesión con una cuenta
3) Cerrar sesión con una cuenta
4) Eliminar la cuenta del servidor

Comunicación (80% del funcionamiento, 10% cada funcionalidad)
1) Mostrar todos los usuarios/contactos y su estado
2) Agregar un usuario a los contactos
3) Mostrar detalles de contacto de un usuario
4) Comunicación 1 a 1 con cualquier usuario/contacto
5) Participar en conversaciones grupales
6) Definir mensaje de presencia
7) Enviar/recibir notificaciones
8) Enviar/recibir archivos


![Java](https://img.shields.io/badge/Java-21-orange)
![Maven](https://img.shields.io/badge/Maven-3.8.6-blue)
![Smack](https://img.shields.io/badge/Smack-4.4.4-green)
![License](https://img.shields.io/badge/License-MIT-yellow)

Una aplicación de chat basada en el protocolo XMPP (Extensible Messaging and Presence Protocol) implementada en Java utilizando la biblioteca Smack. Esta aplicación permite a los usuarios conectarse a un servidor XMPP, enviar y recibir mensajes, gestionar contactos, crear grupos, y más.

## Características

- **Autenticación XMPP:** Soporte para login y registro de usuarios.
- **Mensajería en tiempo real:** Envío y recepción de mensajes de texto.
- **Transferencia de archivos:** Envío de archivos a otros usuarios (por ahora, solo recepcion).
- **Historial de mensajes:** Almacenamiento local del historial de chat (Historial de manera temporal, es decir, mienetas la sesión este actva).

## Requisitos

- **Java 21 LTS** o superior.
- **Maven 3.8.6** o superior.
- **Servidor XMPP:** Cualquier servidor compatible con XMPP (Ej. ejabberd, Openfire).

## Instalación

1. **Clona el repositorio:**

    ```bash
    git clone https://github.com/Gustixa/redes_ps.git
    cd XmppClient
    ```

2. **Compila el proyecto con Maven:**

    ```bash
    mvn clean install
    ```

3. **Configura el servidor XMPP:**
   - Asegúrate de tener un servidor XMPP funcionando y configurado.
   - Actualizar alumchat.lol con el servidor a utilizar (revisar código donde este implementado)

4. **Ejecuta la aplicación:**

    ```bash
    mvn exec:java -Dexec.mainClass="com.example.App"
    ```

## Uso

1. **Inicio de sesión:**
   - Abre la aplicación y proporciona tus credenciales de usuario para iniciar sesión.

2. **Gestión de contactos:**
   - Agrega, elimina y administra tus contactos desde la interfaz.

3. **Mensajería:**
   - Selecciona un contacto y comienza a enviar y recibir mensajes.

4. **Transferencia de archivos:**
   - Haz clic en el botón "Enviar archivo" para transferir archivos a un contacto seleccionado.

5. **Creación de grupos:**
   - Crea grupos y añade contactos para iniciar conversaciones en grupo.

## Estructura del Proyecto

```plaintext
xmpp-chat-app/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/tuusuario/xmppchatapp/
│   │   │       ├── App.java
│   │   │       ├── Contact.java
│   │   │       ├── FileUploadHandler.java
│   │   │       ├── FileUploadIQ.java
│   │   │       ├── GroupData.java
│   │   │       ├── LoginWindow.java
│   │   │       ├── RegisterWindow.java
│   │   │       ├── UploadFile.java
│   │   │       ├── XmppChatApp.java
│   │   │       ├── XmppClient.java
│   └── test/
│       └── java/
│           └── com/tuusuario/xmppchatapp/
│               └── AppTest.java
│
└── pom.xml

