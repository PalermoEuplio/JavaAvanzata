/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.connection;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Properties;
import java.util.function.Consumer;

/**
 *
 * @author euppa
 */
public class ClientConnection {

    private String ip;
    private int port;
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    private Consumer<Serializable> onReceiveCallback;
    private Thread listenerThread;

    /**
     * Costruttore: carica le impostazioni e salva la callback per i messaggi in arrivo.
     */
    public ClientConnection(Consumer<Serializable> onReceiveCallback) {
        this.onReceiveCallback = onReceiveCallback;
        caricaConfigurazione();
    }

    /**
     * Metodo interno per leggere IP e Porta dal file .properties.
     */
    private void caricaConfigurazione() {
        Properties props = new Properties();
        
        // Cerca il file "config_client.properties" nella cartella principale del progetto
        try (InputStream input = new FileInputStream("properties/client.properties")) {
            props.load(input);
            
            // Legge i valori, se non li trova usa localhost e 55555 come fallback
            this.ip = props.getProperty("server.ip", "127.0.0.1");
            this.port = Integer.parseInt(props.getProperty("server.port", "55555"));
            
            System.out.println("Configurazione caricata: Connessione a " + ip + ":" + port);
            
        } catch (IOException e) {
            System.err.println("File 'config_client.properties' non trovato! Uso i parametri di default (127.0.0.1:55555).");
            this.ip = "127.0.0.1";
            this.port = 55555;
        } catch (NumberFormatException e) {
            System.err.println("Errore nel formato della porta nel file properties! Uso la 55555.");
            this.port = 55555;
        }
    }

    /**
     * Avvia fisicamente la connessione con il Server.
     */
    public void connect() throws IOException {
        // 1. Apro la socket verso il server
        socket = new Socket(ip, port);
        
        // 2. Inizializzo gli stream (SEMPRE prima l'output, poi l'input per evitare deadlock)
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        // 3. Creo un Thread in background per restare costantemente in ascolto
        listenerThread = new Thread(() -> {
            try {
                while (true) {
                    // Resta bloccato qui finché non arriva un messaggio dal Server
                    Serializable data = (Serializable) in.readObject();
                    
                    // Quando arriva, lo passo al Controller tramite il Consumer
                    onReceiveCallback.accept(data);
                }
            } catch (Exception e) {
                System.out.println("Connessione col server interrotta: " + e.getMessage());
                
                if (onReceiveCallback != null) {
                    onReceiveCallback.accept(new PacchettoRisposta("SERVER_DISCONNECTED"));
                }
            }
        });
        
        // Fondamentale: imposta il thread come demone così si chiude automaticamente se chiudi il gioco
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Invia un oggetto (Pacchetto) al Server.
     */
    public void send(Serializable data) throws IOException {
        if (out != null) {
            out.writeObject(data);
            out.flush(); // Assicura che i dati partano immediatamente
        }
    }

    /**
     * Chiude la connessione e pulisce le risorse.
     */
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Chiudendo la socket si chiudono anche in e out in automatico
            }
        } catch (IOException e) {
            System.err.println("Errore durante la chiusura della connessione: " + e.getMessage());
        }
    }
}
