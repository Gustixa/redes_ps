package com.example;

/**
 * La clase {@code Contact} representa un contacto en la aplicación de chat XMPP.
 * <p>
 * Cada contacto tiene un JID (Jabber ID), un nombre y un estado que indican su presencia
 * o disponibilidad.
 * </p>
 */
public class Contact {
    private String jid;
    private String name;
    private String status;

    /**
     * Crea un nuevo contacto con el JID, nombre y estado especificados.
     *
     * @param jid    El Jabber ID (JID) del contacto.
     * @param name   El nombre del contacto.
     * @param status El estado de presencia del contacto (por ejemplo, "online", "away").
     */
    public Contact(String jid, String name, String status) {
        this.jid = jid;
        this.name = name;
        this.status = status;
    }

    /**
     * Obtiene el Jabber ID (JID) del contacto.
     *
     * @return El JID del contacto.
     */
    public String getJid() {
        return jid;
    }

    /**
     * Obtiene el nombre del contacto.
     *
     * @return El nombre del contacto.
     */
    public String getName() {
        return name;
    }

    /**
     * Obtiene el estado de presencia del contacto.
     *
     * @return El estado de presencia del contacto.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Devuelve una representación en cadena del contacto, que incluye su nombre y estado.
     *
     * @return Una cadena que representa al contacto en el formato "nombre (estado)".
     */
    @Override
    public String toString() {
        return name + " (" + status + ")";
    }
}
