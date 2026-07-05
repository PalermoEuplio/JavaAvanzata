package model.connection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import javafx.application.Platform;
import model.connection.ServerConnection.ClientHandler;
import model.db.DBConnector;
import model.utility.Esito;
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
    
    private static Consumer<Integer> onAnswerReceived;  // Consumer utilizzato principalmente per l'aggiornamento in tempo reale del numero di player che hanno risposto 
    
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

    public static void setOnAnswerReceived(Consumer<Integer> onAnswerReceived) {
        Sessione.onAnswerReceived = onAnswerReceived;
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
                                    
                                    ClientHandler giaConnesso = server.trovaClientPerId(pLog.getId());
                                    if (giaConnesso != null && giaConnesso != mittente) {
                                        mittente.send(new PacchettoRisposta("LOGIN_ERR", "Account già connesso in un'altra sessione."));
                                        break;
                                    }

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
                                                
                                                Sfida sfidaP1 = new Sfida(currentGame.getIdDocumento(), currentGame.getDurata(), 0, 0, idGiocatore1, idGiocatore2, userP2, Esito.None, String.valueOf(nSoluzioni));
                                                Sfida sfidaP2 = new Sfida(currentGame.getIdDocumento(), currentGame.getDurata(), 0, 0, idGiocatore1, idGiocatore2, userP1, Esito.None, String.valueOf(nSoluzioni));

                                                sfidaP1.setTitoloTesto(currentGame.getTitoloTesto());
                                                sfidaP2.setTitoloTesto(currentGame.getTitoloTesto());
                                                
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
                                            socketP1.send(new PacchettoRisposta("RESIGN_OK",currentGame.getSoluzione()));
                                        } else socketP1.send(new PacchettoRisposta("OPP_RESIGN",currentGame.getSoluzione()));
                                    }

                                    if (socketP2 != null) {
                                        if( mittente.equals(socketP2) ){
                                            socketP2.send(new PacchettoRisposta("RESIGN_OK",currentGame.getSoluzione()));
                                        } else socketP2.send(new PacchettoRisposta("OPP_RESIGN",currentGame.getSoluzione()));
                                    }
                                    
                                    new DBConnector<Sfida>().aggiungiSfida(currentGame);
                                
                                    notifyAnswerReceived(2);
                                
                                } catch (IOException e) {}
                                    catch (SQLException ex) {}
                                
                                break;
                            
                            case "VALIDATION_REQUEST":
                                
                                
                                if(pacchetto.getPayload()!=null){
                                    
                                    if(answers==null)
                                        answers = new HashMap<>();
                                    
                                    answers.put(mittente, pacchetto);
                                    
                                    notifyAnswerReceived(answers.size());
                                    
                                    if(answers.keySet().size()==2){
                                        
                                        List<String>[] pachList = new List[2];
                                        int i=0;
                                        ClientHandler[] ch0 = new ClientHandler[2];
                                        
                                        for(ClientHandler ch : answers.keySet()){
                                            ch0[i] = ch;
                                            pachList[i] = (List<String>) answers.get(ch).getPayload();
                                            i++;
                                        }
                                        
                                        String[] risposteVere = currentGame.getSoluzione().split(",\\s*");
                                        
                                        int nCorrette1 = 0;
                                        int nCorrette2 = 0;
                                        int tempo1 = Integer.parseInt(pachList[0].get(pachList[0].size() - 1));
                                        int tempo2 = Integer.parseInt(pachList[1].get(pachList[1].size() - 1));
                                        
                                        // Controllo le risposte
                                        for (int j = 0; j < risposteVere.length; j++) {
                                            String rispostaVera = risposteVere[j].trim();

                                            // Verifichiamo che l'indice j sia valido (escludendo l'ultimo elemento che è il tempo)
                                            if (j < pachList[0].size() - 1 && pachList[0].get(j).trim().toUpperCase().equalsIgnoreCase(rispostaVera)) {
                                                nCorrette1++;
                                            }
                                            if (j < pachList[1].size() - 1 && pachList[1].get(j).trim().toUpperCase().equalsIgnoreCase(rispostaVera)) {
                                                nCorrette2++;
                                            }
                                        }
                                        
                                        try {
                                            
                                                boolean vinceCh0 = false;
                                                if (nCorrette1 > nCorrette2) {
                                                    vinceCh0 = true;
                                                } else if (nCorrette1 == nCorrette2) {
                                                    if (tempo1 < tempo2) vinceCh0 = true;
                                                }

                                                // 2. Capiamo chi è il P1 rispetto al DB
                                                boolean ch0EilP1 = (ch0[0].getIdLoggato() == currentGame.getId1());

                                                // 3. Compiliamo i campi del DB in base a chi ha vinto
                                                if (vinceCh0) { // Ha vinto ch0[0]
                                                    if (ch0EilP1) {
                                                        currentGame.settRisposta1(tempo1);
                                                        currentGame.settRisposta2(tempo2);
                                                        currentGame.setRisultato(Esito.Vittoria); // Vince P1
                                                    } else {
                                                        currentGame.settRisposta1(tempo2);
                                                        currentGame.settRisposta2(tempo1);
                                                        currentGame.setRisultato(Esito.Sconfitta); // Vince P2
                                                    }
                                                } else { // Ha vinto ch0[1]
                                                    if (ch0EilP1) {
                                                        currentGame.settRisposta1(tempo1);
                                                        currentGame.settRisposta2(tempo2);
                                                        currentGame.setRisultato(Esito.Sconfitta); // Vince P2
                                                    } else {
                                                        currentGame.settRisposta1(tempo2);
                                                        currentGame.settRisposta2(tempo1);
                                                        currentGame.setRisultato(Esito.Vittoria); // Vince P1
                                                    }
                                                }
                                                
                                                
                                            String strTempo1 = String.format("%02d:%02d", (int)tempo1 / 60, (int)tempo1 % 60);
                                            String strTempo2 = String.format("%02d:%02d", (int)tempo2 / 60, (int)tempo2 % 60);
                                
                                                
                                            // Preparo la stringa di oggetti da inviare ad entrambi i giocatori per far sapere il loro esito e quello avversario 
                                            // [soluzioni, mioTempo, oppTempo, mieRisposte, oppRisposte]
                                            Object[] payloadVincitore = {currentGame.getSoluzione(), (vinceCh0 ? strTempo1 : strTempo2), 
                                                (vinceCh0 ? strTempo2 : strTempo1), (vinceCh0 ? nCorrette1 : nCorrette2), (vinceCh0 ? nCorrette2 : nCorrette1)};
                                            Object[] payloadPerdente  = {currentGame.getSoluzione(), (!vinceCh0 ? strTempo1 : strTempo2), 
                                                (!vinceCh0 ? strTempo2 : strTempo1), (!vinceCh0 ? nCorrette1 : nCorrette2), (!vinceCh0 ? nCorrette2 : nCorrette1)};
                                            
                                            // Mando il messaggio dell'esito col payload calcolato
                                            if (vinceCh0) {
                                                ch0[0].send(new PacchettoRisposta("YOU_WON", payloadVincitore));
                                                ch0[1].send(new PacchettoRisposta("YOU_LOST", payloadPerdente));
                                            } else {
                                                ch0[1].send(new PacchettoRisposta("YOU_WON", payloadVincitore));
                                                ch0[0].send(new PacchettoRisposta("YOU_LOST", payloadPerdente));
                                            } 
                                            
                                            new DBConnector<Sfida>().aggiungiSfida(currentGame);
                                            
                                            answers.clear();    // Svuoto la mappa delle risposte per prepararla ad una nuova partita
                                            
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
                        notificaGrafica();
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
    
    public static void notifyAnswerReceived(int count) {
        if (onAnswerReceived != null) {
            // Assicuriamoci che la modifica grafica avvenga nel thread di JavaFX
            Platform.runLater(() -> onAnswerReceived.accept(count));
        }
    }

    // Metodo per fare il logout
    public static void logout() {
        adminLoggato = null;
        server.stopServer();
        server=null;
    }
    
}
