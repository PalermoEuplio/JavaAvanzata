package model.connection;

import java.io.IOException;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import model.Main;
import model.utility.Player;

/**
 * Classe che gestisce la sessione corrente, inclusi player loggato e client.
 */
public class Sessione {
    
    private static Player playerLoggato;
    private static ClientConnection client;
    
    // Variabile che permette al Controller corrente di ascoltare i pacchetti in arrivo
    private static Consumer<PacchettoRisposta> onServerResponse;    
    
    // Variabili per il monitoraggio vitale del Server
    private static boolean isConnected = false;
    private static Thread monitorThread;
    
    // --- GESTIONE PLAYER E CONNESSIONE ---
    
    public static void setPlayer(Player pl) {
        playerLoggato = pl;
    }
    
    public static Player getPlayer() {
        return playerLoggato;
    }
    
    public static void setClient(ClientConnection c) {
        client = c;
    }
    
    public static ClientConnection getClient() {
        return client;
    }
    
    public static void setOnServerResponse(Consumer<PacchettoRisposta> callback) {
        onServerResponse = callback;
    }

    public static boolean isConnected() {
        return isConnected;
    }
    
    
    
    // --- MONITORAGGIO COSTANTE DEL SERVER ---
    
    /**
     * Questo metodo va chiamato UNA SOLA VOLTA all'avvio dell'applicazione (es. nel LoginController).
     * Fa girare un thread perenne che pinga il server. Se il server muore, sbatte fuori l'utente.
     */
    public static void avviaMonitoraggio() {
        
        if (monitorThread != null && monitorThread.isAlive()) 
            return; // Evita di creare thread doppi
        
        monitorThread = new Thread(() -> {
            while (true) { 
                
                if (!isConnected) {
                    try {
                        // Tenta di connettersi silenziosamente
                        client = new ClientConnection(messaggioRicevuto -> {
                            if (messaggioRicevuto instanceof PacchettoRisposta) {
                                PacchettoRisposta pacchetto = (PacchettoRisposta) messaggioRicevuto;
                                
                                // Intercetto la caduta istantanea del server
                                if (pacchetto.getComando().equals("SERVER_DISCONNECTED")) {
                                    isConnected = false;
                                    if (playerLoggato != null) {
                                        forzaDisconnessione();
                                    } else {
                                        notificaGrafica(new PacchettoRisposta("CONNESSIONE_PERSA", null));
                                    }
                                }
                                
                                else if (pacchetto.getComando().equals("BAN")) {
                                    isConnected = false;
                                    if(playerLoggato != null){
                                        forzaDisconnessione("Ban");
                                    }
                                    else {
                                        notificaGrafica(new PacchettoRisposta("CONNESSIONE_PERSA", null));
                                    }
                                    
                                }
                                else {
                                    notificaGrafica(pacchetto);
                                }
                            }
                        });
                        
                        client.connect(); 
                        isConnected = true; 
                        notificaGrafica(new PacchettoRisposta("CONNESSIONE_OK", null));
                        
                    } catch (IOException e) {
                        isConnected = false;
                        notificaGrafica(new PacchettoRisposta("CONNESSIONE_PERSA", null));
                    }
                } else {
                    try {
                        // Se siamo connessi, mandiamo il Ping per verificare che il server sia vivo
                        client.send(new PacchettoRisposta("PING"));
                    } catch (IOException e) {
                        // IL SERVER E' CADUTO O LA CONNESSIONE E' SALTATA!
                        isConnected = false; 
                        
                        // Se c'è un giocatore loggato, lo disconnettiamo a forza
                        if (playerLoggato != null) {
                            forzaDisconnessione();
                        } else {
                            // Se eravamo solo nella schermata di login, notifichiamo l'errore visivo
                            notificaGrafica(new PacchettoRisposta("CONNESSIONE_PERSA", null));
                        }
                    }
                }
                
                // Attende 2 secondi prima del prossimo controllo
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
        });
        
        monitorThread.setDaemon(true); // Termina in automatico quando chiudiamo la finestra
        monitorThread.start();
    }
    
    // --- GESTIONE DISCONNESSIONI E LOGOUT ---
    
    /**
     * Metodo interno chiamato in automatico se la connessione col server cade mentre stiamo giocando.
     */
    private static void forzaDisconnessione() {
        playerLoggato = null;
        if (client != null) {
            client.disconnect();
        }
        
        Platform.runLater(() -> {
            try {
                // Mostriamo un messaggio di errore all'utente
                Alert alert = new Alert(Alert.AlertType.ERROR, "Connessione col Server persa. Sei stato disconnesso.", ButtonType.OK);
                alert.setTitle("Errore di Rete");
                alert.setHeaderText("Server Offline");
                alert.showAndWait();
                
                // Lo riportiamo alla pagina di Login
                Main.setRoot("login"); 
            } catch (IOException ex) {
                System.err.println("Impossibile caricare la pagina di Login.");
            }
        });
    }
    
    /**
     * Metodo interno chiamato in automatico se la connessione col server cade o si viene bannati.
     * 
     * @param ban La stringa di ban.
     */
    private static void forzaDisconnessione(String ban) {
        playerLoggato = null;
        if (client != null) {
            client.disconnect();
        }
        
        Platform.runLater(() -> {
            try {
                // Mostriamo un messaggio di errore all'utente
                Alert alert = new Alert(Alert.AlertType.ERROR, "Hai ricevuto un ban da un Admin. Le tue credenziali sono state eliminate. Sei stato disconnesso.", ButtonType.OK);
                alert.setTitle("Utente Eliminato");
                alert.setHeaderText("Eliminato da un Amministratore");
                alert.showAndWait();
                
                // Lo riportiamo alla pagina di Login
                Main.setRoot("login"); 
            } catch (IOException ex) {
                System.err.println("Impossibile caricare la pagina di Login.");
            }
        });
    }

    /**
     * Metodo manuale per fare il logout (chiamato volontariamente dal bottone "Esci")
     */
    public static void logout() {
        playerLoggato = null;
        if (client != null) {
            client.disconnect();
            isConnected = false;
        }
    }
    
    // --- UTILITY ---
    
    /**
     * Notifica la grafica dell'arrivo di un pacchetto.
     * 
     * @param pacchetto Il pacchetto arrivato.
     */
    private static void notificaGrafica(PacchettoRisposta pacchetto) {
        if (onServerResponse != null) {
            Platform.runLater(() -> onServerResponse.accept(pacchetto));
        }
    }
}