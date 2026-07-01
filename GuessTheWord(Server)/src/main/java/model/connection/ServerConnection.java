package model.connection;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Classe che gestisce la connessione di rete lato SERVER.
 * Legge la porta dal file config_server.properties e gestisce client multipli.
 */
public class ServerConnection {

    private int port;
    private ServerSocket serverSocket;
    private boolean isRunning;
    
    // Lista per tenere traccia di tutti i client connessi al momento
    private List<ClientHandler> connectedClients = new ArrayList<>();
    
    // Il BiConsumer ci passa il pacchetto ricevuto E il riferimento al client che lo ha inviato
    private BiConsumer<Serializable, ClientHandler> onReceiveCallback;
    
    private Consumer<ClientHandler> onDisconnectCallback; 

    /**
     * Costruttore: carica la porta dal file e imposta la callback per i messaggi.
     */
    public ServerConnection(BiConsumer<Serializable, ClientHandler> onReceiveCallback, Consumer<ClientHandler> onDisconnectCallback) {
        this.onReceiveCallback = onReceiveCallback;
        this.onDisconnectCallback = onDisconnectCallback;
        caricaConfigurazione();
    }

    /**
     * Metodo interno per leggere la Porta dal file .properties.
     */
    private void caricaConfigurazione() {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("config_server.properties")) {
            props.load(input);
            this.port = Integer.parseInt(props.getProperty("server.port", "55555"));
        } catch (Exception e) {
            this.port = 55555;
        }
    }

    /**
     * Avvia il Server su un Thread separato per non bloccare l'interfaccia dell'Admin.
     */
    public void startServer() {
        isRunning = true;
        
        Thread acceptorThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("Server avviato correttamente!");
                
                // Ciclo infinito: il server aspetta sempre nuovi giocatori
                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Nuovo giocatore connesso: " + clientSocket.getInetAddress());
                    
                    // Creiamo un gestore indipendente per questo specifico giocatore e lo avviamo
                    ClientHandler handler = new ClientHandler(clientSocket);
                    connectedClients.add(handler);
                    handler.start();
                }
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Errore del Server: " + e.getMessage());
                } else {
                    System.out.println("Server arrestato in modo pulito.");
                }
            }
        });
        
        acceptorThread.setDaemon(true); // Se l'Admin chiude l'app, il server si spegne in automatico
        acceptorThread.start();
    }

    /**
     * Spegne il Server e scollega tutti i client.
     */
    public void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            // Disconnette gentilmente tutti i client attivi
            for (ClientHandler client : connectedClients) {
                client.disconnect();
            }
            connectedClients.clear();
        } catch (IOException e) {
            System.err.println("Errore durante l'arresto del server: " + e.getMessage());
        }
    }

    /**
     * Invia lo stesso messaggio a TUTTI i client connessi contemporaneamente (es. "Partita Iniziata!").
     */
    public void broadcast(Serializable data) {
        for (ClientHandler client : connectedClients) {
            try {
                client.send(data);
            } catch (IOException e) {
                System.err.println("Errore invio broadcast a un client.");
            }
        }
    }
    
    public List<String> getUtentiOnline() {
        return connectedClients.stream()
            .map(ClientHandler::getUsernameLoggato)
            .filter(nome -> nome != null)
            .collect(java.util.stream.Collectors.toList());
    }
    
    public ClientHandler trovaClientPerId(int idTarget) {
        for (ClientHandler client : connectedClients) {
            if (client.getIdLoggato() == idTarget) {
                return client;
            }
        }
        return null;
    }
    

    /**
     * Classe Interna (Inner Class): Gestisce la singola connessione di un singolo Giocatore.
     */
    public class ClientHandler extends Thread {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        
        private String usernameLoggato = null;
        private int idLoggato = -1;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.setDaemon(true);
        }

        
        public String getUsernameLoggato() { 
            return usernameLoggato; 
        }
        public void setUsernameLoggato(String username) { 
            this.usernameLoggato = username; 
        }
        
        
        public int getIdLoggato() { 
            return idLoggato; 
        }
        public void setIdLoggato(int id) { 
            this.idLoggato = id; 
        }
        
        @Override
        public void run() {
            try {
                // Inizializza gli stream (SEMPRE prima output, poi input)
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                // Ciclo di lettura per questo specifico client
                while (isRunning) {
                    Serializable data = (Serializable) in.readObject();
                    
                    // Passo i dati al controller, allegando anche ME STESSO (questo handler)
                    if (onReceiveCallback != null) {
                        onReceiveCallback.accept(data, this);
                    }
                }
            } catch (EOFException e) {
                System.out.println("Un giocatore si è disconnesso normalmente.");
            } catch (Exception e) {
                System.out.println("Connessione persa con un giocatore.");
            } finally {
                disconnect();
                connectedClients.remove(this); // Rimuovo il client dalla lista dei connessi
                if (onDisconnectCallback != null && usernameLoggato != null) {
                    onDisconnectCallback.accept(this);
                }
            }
        }

        /**
         * Invia un pacchetto di risposta SOLO a questo giocatore.
         */
        public void send(Serializable data) throws IOException {
            if (out != null) {
                out.writeObject(data);
                out.flush();
            }
        }

        public void disconnect() {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Errore chiusura socket client: " + e.getMessage());
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.socket);
            hash = 97 * hash + this.idLoggato;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ClientHandler other = (ClientHandler) obj;
            if (this.idLoggato != other.idLoggato) {
                return false;
            }
            return Objects.equals(this.socket, other.socket);
        }
        
    }
}