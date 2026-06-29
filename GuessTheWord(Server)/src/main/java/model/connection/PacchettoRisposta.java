package model.connection;

import java.io.Serializable;

/**
 * Classe che rappresenta il pacchetto di dati scambiato tra Client e Server.
 * Implementa Serializable per poter essere inviata attraverso gli ObjectOutputStream.
 */
public class PacchettoRisposta implements Serializable {

    // È buona norma inserire il serialVersionUID quando si implementa Serializable
    private static final long serialVersionUID = 1L;

    private String comando; // L'azione da eseguire (es. "LOGIN_OK", "ERRORE", "AGGIORNA_CLASSIFICA")
    private Object payload; // I dati effettivi da trasportare (può essere una String, una List, ecc.)

    /**
     * Costruttore per messaggi semplici senza dati allegati.
     * @param comando Il comando da inviare.
     */
    public PacchettoRisposta(String comando) {
        this.comando = comando;
        this.payload = null;
    }

    /**
     * Costruttore per messaggi complessi con dati allegati.
     * @param comando Il comando da inviare.
     * @param payload I dati allegati al messaggio.
     */
    public PacchettoRisposta(String comando, Object payload) {
        this.comando = comando;
        this.payload = payload;
    }

    // --- GETTER E SETTER ---

    public String getComando() {
        return comando;
    }

    public void setComando(String comando) {
        this.comando = comando;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
    
    @Override
    public String toString() {
        if (payload != null) {
            return "Comando: [" + comando + "] - Payload: " + payload.toString();
        } else {
            return "Comando: [" + comando + "]";
        }
    }
}