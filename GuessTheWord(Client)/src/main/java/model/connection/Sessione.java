/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.connection;

import java.io.IOException;
import java.util.function.Consumer;
import javafx.application.Platform;
import model.utility.Player;

/**
 *
 * @author euppa
 */
public class Sessione {
    
    private static Player playerLoggato;
    
    private static ClientConnection client;
    
    private static Consumer<PacchettoRisposta> onServerResponse;    // Variabile che permette di specificare il comportamento da adottare in base a determinate risposte del server
    
    private static volatile boolean stopMonitor = false;
    private static boolean isConnected = false;
    private static Thread monitorThread;
    
    
    
    // Metodo da chiamare quando si effettua il login
    public static void setPlayer(Player pl) {
        playerLoggato = pl;
    }
    // Metodo per recuperare l'amministratore nelle altre schermate
    public static Player getPlayer() {
        return playerLoggato;
    }
    
    
    // Metodi set e get per il client
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
    
    
    
    public static void avviaConnessione() {
        
        if (monitorThread != null && monitorThread.isAlive()) 
            return; // Evita thread doppi
        
        stopMonitor = false;
        
        monitorThread = new Thread(() -> {
            // Il ciclo si ferma se stopMonitor diventa true (cioè se abbiamo fatto il login e cambiato pagina)
            while (!stopMonitor) { 
                if (!isConnected) {
                    try {
                        client = new ClientConnection(messaggioRicevuto -> {
                                if (messaggioRicevuto instanceof PacchettoRisposta) {
                                    notificaGrafica((PacchettoRisposta) messaggioRicevuto);
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
                        // Ping di controllo vita
                        client.send(new PacchettoRisposta("PING"));
                    } catch (IOException e) {
                        isConnected = false; 
                        notificaGrafica(new PacchettoRisposta("CONNESSIONE_PERSA", null));
                    }
                }
                
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
        });
        
        monitorThread.setDaemon(true); 
        monitorThread.start();
    }
    
    
    
    public static void fermaConnessione() {
        stopMonitor = true;
    }
    
    
    // Metodo usato per aggiornare la grafica quando avviene una risposta dal server
    private static void notificaGrafica(PacchettoRisposta pacchetto) {
        if (onServerResponse != null) {
            Platform.runLater(() -> onServerResponse.accept(pacchetto));
        }
    }
    

    // Metodo per fare il logout
    public static void logout() {
        playerLoggato = null;
    }
    
}
