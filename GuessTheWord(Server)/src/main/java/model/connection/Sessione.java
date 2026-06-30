/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.connection;

import java.io.IOException;
import java.util.List;
import javafx.application.Platform;
import model.db.DBConnector;
import model.utility.Sfida;
import model.utility.Amministratore;
import model.utility.Player;

/**
 *
 * @author euppa
 */
public class Sessione {
    
    private static Amministratore adminLoggato;
    
    private static ServerConnection server;
    
    private static Runnable onUserStatusChanged;    // Variabile che permette di specificare il comportamento da adottare in base a determinate richieste del client
    
    
    
    // Metodo da chiamare quando si effettua il login
    public static void setAdmin(Amministratore admin) {
        adminLoggato = admin;
    }

    
    // Metodo per recuperare l'amministratore nelle altre schermate
    public static Amministratore getAdmin() {
        return adminLoggato;
    }
    // Metodo set per il server
    public static void setServer(ServerConnection s) {
        server = s;
    }
    // Metodo per recuperare l'istanza del server in tutte le schermate e tenerlo sempre attivo
    public static ServerConnection getServer() {
        return server;
    }
    // Metodo per settare il comportamento da adottare in base a determinate richieste del server
    // (Questo metodo verrà implementato con delle lambdaFunction che ne specificano il comportamento)
    public static void setOnUserStatusChanged(Runnable callback) {
        onUserStatusChanged = callback;
    }
    
    
    
    // Metodo necessario all'avvio del server e alla definizione di alcuni dei suoi comportamenti principali
    public static ServerConnection startServer(){
        server = new ServerConnection(
                
                // CALLBACK 1: MESSAGGI IN ARRIVO DAL CLIENT
                (pacchettoRicevuto, mittente) -> {
                    if (pacchettoRicevuto instanceof PacchettoRisposta) {
                        PacchettoRisposta pacchetto = (PacchettoRisposta) pacchettoRicevuto;

                        switch (pacchetto.getComando()) {
                            case "PING":
                                break;

                            case "LOGIN_REQUEST":
                                String[] cred = (String[]) pacchetto.getPayload();
                                try {
                                    Player pLog = new DBConnector<Player>().cerca(new Player(cred[0], 0, 0, 0, 0), cred[1]);

                                    // Salviamo l'identità nel Socket 
                                    mittente.setUsernameLoggato(pLog.getUsername());
                                    mittente.setIdLoggato(pLog.getId());

                                    mittente.send(new PacchettoRisposta("LOGIN_OK", pLog));
                                    notificaGrafica();

                                } catch (Exception e) {
                                    try { mittente.send(new PacchettoRisposta("LOGIN_ERR", e.getMessage())); } catch (IOException ex) {}
                                }
                                break;

                            case "REGISTER_REQUEST":
                                String[] datiReg = (String[]) pacchetto.getPayload();
                                try {
                                    Player pReg = new DBConnector<Player>().registrazione(new Player(datiReg[0], 0, 0, 0, 0), datiReg[1]);

                                    mittente.setUsernameLoggato(pReg.getUsername());
                                    mittente.setIdLoggato(pReg.getId());

                                    mittente.send(new PacchettoRisposta("REGISTER_OK", pReg));

                                    notificaGrafica(); // Fa comparire in tabella il nuovo utente!

                                } catch (Exception e) {
                                    try { mittente.send(new PacchettoRisposta("REGISTER_ERR", e.getMessage())); } catch (IOException ex) {}
                                }
                                break;
                            case "LOGOUT_REQUEST":
                                String userSloggato = mittente.getUsernameLoggato();
                                if (userSloggato != null) {
                                    mittente.setUsernameLoggato(null);
                                    mittente.setIdLoggato(-1);
                                    notificaGrafica(); // Fa comparire in tabella il nuovo utente!
                                }
                                break;
                            case "STORICO_REQUEST":
                                
                                try {
                                    
                                    List<Sfida> s = new DBConnector<Sfida>().caricaSfide(mittente.getIdLoggato());
                                    
                                    mittente.send(new PacchettoRisposta("STORICO_OK",s));
                                    
                                } catch (Exception e) {}
                                
                                break;
                        }
                    }
                },

                // CALLBACK 2: CLIENT DISCONNESSO
                (mittenteDisconnesso) -> {
                    if (mittenteDisconnesso.getUsernameLoggato() != null) {
                        notificaGrafica(); // Fa comparire in tabella il nuovo utente!
                    }
                }
            );
        
        server.startServer();
        return server;
    }
    
    // Metodo usato per aggiornare la grafica quando avviene una richiesta legata all'accesso dell'utente
    private static void notificaGrafica() {
        if (onUserStatusChanged != null) {
            Platform.runLater(onUserStatusChanged);
        }
    }

    // Metodo per fare il logout
    public static void logout() {
        adminLoggato = null;
        server=null;
    }
    
}
