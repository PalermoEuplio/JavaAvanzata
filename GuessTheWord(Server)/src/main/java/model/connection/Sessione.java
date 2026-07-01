/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.connection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javafx.application.Platform;
import model.connection.ServerConnection.ClientHandler;
import model.db.DBConnector;
import model.game.TextEditor;
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
    
    private static Runnable onGameReady;    // Runnable per il reindirizzamento dell'admin alla pagina d'attesa fine partita
    
    private static CopyOnWriteArrayList<Integer> clientInAttesa;   // ArrayList threadSafe che permette l'inserimento degli id Utenti pronti a giocare
    
    private static Sfida currentGame;   // Variabile contenente i dati della sfida attualmente preparata/in corso
    
    private static HashMap<ClientHandler, PacchettoRisposta> answers;
    
    
    
    
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

    public static void setClientAttesa(CopyOnWriteArrayList<Integer> cia) {
        Sessione.clientInAttesa = cia;
    }

    public static CopyOnWriteArrayList<Integer> getClientInAttesa() {
        return clientInAttesa;
    }

    public static void setCurrentGame(Sfida currentGame) {
        Sessione.currentGame = currentGame;
    }

    public static Sfida getCurrentGame() {
        return currentGame;
    }
    
    public static void setOnGameReady(Runnable callback) {
        onGameReady = callback;
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
                            case "CLASSIFICA_REQUEST":
                                
                                try {
                                    
                                    List<Player> p = new DBConnector<Player>().elencaTuttiPlayer();
                                    mittente.send(new PacchettoRisposta("CLASSIFICA_OK",p));
                                    
                                } catch (Exception e) {}
                                
                                
                                break;
                                
                            case "GAME_PING":
                                try {
                                    
                                    if(clientInAttesa==null)
                                        mittente.send(new PacchettoRisposta("NO_ADMIN"));
                                    else { 
                                        if(!clientInAttesa.contains(mittente.getIdLoggato()))   // Evito di aggiungere doppioni
                                            clientInAttesa.add(mittente.getIdLoggato());
                                        
                                        if(clientInAttesa.size()==2){
                                            
                                            try {
                                                int idGiocatore1 = clientInAttesa.get(0);
                                                int idGiocatore2 = clientInAttesa.get(1);


                                                clientInAttesa = null;

                                                ClientHandler socketP1 = server.trovaClientPerId(idGiocatore1);
                                                ClientHandler socketP2 = server.trovaClientPerId(idGiocatore2);


                                                String userP1 = new DBConnector<Player>().cerca(new Player("", idGiocatore1, 0, 0, 0), null).getUsername();
                                                String userP2 = new DBConnector<Player>().cerca(new Player("", idGiocatore2, 0, 0, 0), null).getUsername();

                                                int nSoluzioni = TextEditor.getRisposte().length;   // Agli utenti arriva solo il numero di parole da indovinare
                                                String testoModificato = TextEditor.getModifiedText();
                                                
                                                Sfida sfidaP1 = new Sfida(currentGame.getIdDocumento(), currentGame.getDurata(), 0.0, 0.0, idGiocatore1, idGiocatore2, userP2, "", String.valueOf(nSoluzioni));
                                                Sfida sfidaP2 = new Sfida(currentGame.getIdDocumento(), currentGame.getDurata(), 0.0, 0.0, idGiocatore1, idGiocatore2, userP1, "", String.valueOf(nSoluzioni));

                                                // 5. INVIAMO I DATI A ENTRAMBI CONTEMPORANEAMENTE (Zero Delay!)
                                                if (socketP1 != null) {
                                                    socketP1.send(new PacchettoRisposta("START_GAME", testoModificato));
                                                    socketP1.send(new PacchettoRisposta("GAME_INFO", sfidaP1));
                                                }

                                                if (socketP2 != null) {
                                                    socketP2.send(new PacchettoRisposta("START_GAME", testoModificato));
                                                    socketP2.send(new PacchettoRisposta("GAME_INFO", sfidaP2));
                                                }
                                                
                                                sfidaP1.setSoluzione(Arrays.stream(TextEditor.getRisposte()).collect(java.util.stream.Collectors.joining(", ")));
                                                currentGame = sfidaP1;
                                                notificaAvvioPartita();
                                                
                                            } catch (Exception e) {System.out.println("Errore nella ricerca dell'username Avversario");}
                                            
                                        } else mittente.send(new PacchettoRisposta("NO_OPPONENT"));
                                    }
                                
                                } catch (IOException ex) {}
                                
                                break;
                                
                            case "RESIGN_REQUEST":
                                
                                ClientHandler socketP1 = server.trovaClientPerId(currentGame.getId1());
                                ClientHandler socketP2 = server.trovaClientPerId(currentGame.getId2());
                                
                                try {
                                
                                    if (socketP1 != null) {
                                        if( mittente.equals(socketP1) ){
                                            socketP1.send(new PacchettoRisposta("RESIGN_OK"));
                                        } else socketP1.send(new PacchettoRisposta("OPP_RESIGN"));
                                    }

                                    if (socketP2 != null) {
                                        if( mittente.equals(socketP2) ){
                                            socketP2.send(new PacchettoRisposta("RESIGN_OK"));
                                        } else socketP2.send(new PacchettoRisposta("OPP_RESIGN"));
                                    }
                                    
                                    new DBConnector<Sfida>().aggiungiSfida(currentGame);
                                
                                    notificaAvvioPartita();
                                
                                } catch (IOException e) {}
                                    catch (SQLException ex) {}
                                
                                break;
                            
                            case "VALIDATION_REQUEST":
                                
                                
                                if(pacchetto.getPayload()!=null){
                                    
                                    List<String> answer = (List<String>) pacchetto.getPayload();
                                    
                                    if(answers==null)
                                        answers = new HashMap<>();
                                    
                                    answers.put(mittente, pacchetto);
                                    
                                    if(answers.keySet().size()==2){
                                        
                                        PacchettoRisposta[] pachList = new PacchettoRisposta[2];
                                        int i=0;
                                        
                                        ClientHandler[] ch0 = new ClientHandler[2];
                                        
                                        for(ClientHandler ch : answers.keySet()){
                                            ch0[i] = ch;
                                            pachList[i] = answers.get(ch);
                                            i++;
                                        }
                                        
                                        String[] risposteVere = currentGame.getSoluzione().split(",\\s*");
                                        
                                        int nCorrette1 = 0;
                                        int nCorrette2 = 0;
                                        int tempo1 = Integer.parseInt( ((List<String>)pachList[0].getPayload()).get(risposteVere.length) );
                                        int tempo2 = Integer.parseInt( ((List<String>)pachList[1].getPayload()).get(risposteVere.length) );
                                        
                                        // Controllo le risposte
                                        for(String temp : risposteVere){
                                            if( ((List<String>)pachList[0].getPayload()).contains(temp) )
                                                nCorrette1++;
                                            if( ((List<String>)pachList[1].getPayload()).contains(temp) )
                                                nCorrette2++;
                                        }
                                        
                                        try {
                                            
                                            if(nCorrette1>nCorrette2){    
                                                ch0[0].send(new PacchettoRisposta("YOU_WON",currentGame.getSoluzione()));
                                                ch0[1].send(new PacchettoRisposta("YOU_LOST",currentGame.getSoluzione()));
                                                
                                                if(ch0[0].getIdLoggato() == currentGame.getId1()){
                                                    currentGame.settRisposta1(tempo1);
                                                    currentGame.settRisposta2(tempo2);
                                                    currentGame.setRisultato("Vittoria");
                                                }
                                                    
                                                else {
                                                    currentGame.setRisultato("Sconfitta");
                                                    currentGame.settRisposta1(tempo2);
                                                    currentGame.settRisposta2(tempo1);
                                                }
                                                
                                            }else if(nCorrette1==nCorrette2){
                                                //  Se hanno indovinato lo stesso numero di risposte, controllo il tempo impiegato
                                                if(tempo1<tempo2){
                                                    
                                                    ch0[0].send(new PacchettoRisposta("YOU_WON",currentGame.getSoluzione()));
                                                    ch0[1].send(new PacchettoRisposta("YOU_LOST",currentGame.getSoluzione()));
                                                    
                                                    if(ch0[0].getIdLoggato() == currentGame.getId1()){
                                                        currentGame.settRisposta1(tempo1);
                                                        currentGame.settRisposta2(tempo2);
                                                        currentGame.setRisultato("Vittoria");
                                                    }
                                                    else currentGame.setRisultato("Sconfitta");

                                                }else {
                                                    ch0[1].send(new PacchettoRisposta("YOU_WON",currentGame.getSoluzione()));
                                                    ch0[0].send(new PacchettoRisposta("YOU_LOST",currentGame.getSoluzione()));
                                                    
                                                    if(ch0[1].getIdLoggato() == currentGame.getId1())
                                                        currentGame.setRisultato("Vittoria");
                                                    else currentGame.setRisultato("Sconfitta");
                                                }
                                            }else {
                                                ch0[1].send(new PacchettoRisposta("YOU_WON",currentGame.getSoluzione()));
                                                ch0[0].send(new PacchettoRisposta("YOU_LOST",currentGame.getSoluzione()));
                                                
                                                if(ch0[1].getIdLoggato() == currentGame.getId1())
                                                        currentGame.setRisultato("Vittoria");
                                                    else currentGame.setRisultato("Sconfitta");
                                            }
                                            
                                            new DBConnector<Sfida>().aggiungiSfida(currentGame);
                                            
                                        } catch (IOException e) {System.out.println("Errore durante l'invio dei messaggi");}
                                            catch(SQLException ex) {System.out.println("Errore: "+ex);}
                                        
                                    }
                                }
                                
                                
                                
                                
                                break;
                                
                            default:    
                                try{ 
                                mittente.send((new PacchettoRisposta("Messaggio Sconosciuto")));
                            }
                            catch(Exception e){}
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
    
    private static void notificaAvvioPartita() {
        if (onGameReady != null) {
            Platform.runLater(onGameReady);
        }
    }

    // Metodo per fare il logout
    public static void logout() {
        adminLoggato = null;
        server=null;
    }
    
}
