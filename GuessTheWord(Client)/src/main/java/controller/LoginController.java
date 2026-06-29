package controller;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.fxml.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.Main;
import model.connection.ClientConnection;
import model.connection.PacchettoRisposta;
import model.utility.Player;
import model.utility.Sessione;

public class LoginController implements Initializable{

    // Serie di componenti grafiche
    
    @FXML
    private TextField loginUsername;
    
    @FXML
    private TextField loginPassword;
    
    @FXML
    private Button btnLogin;
    
    @FXML
    private Label loginErrorMsg;
    
    
    @FXML
    private TextField regUsername;
    
    @FXML
    private TextField regPassword;
    
    @FXML
    private TextField regConfirmPassword;
    
    @FXML
    private Button btnRegister;
    
    @FXML
    private Label regErrorMsg;
    
    
    
    private ClientConnection client;
    private boolean isConnected = false;
    private volatile boolean stopMonitor = false;
    
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        btnLogin.disableProperty().bind(loginUsername.textProperty().isEmpty().or(loginPassword.textProperty().isEmpty()));
        
        btnRegister.disableProperty().bind(regUsername.textProperty().isEmpty().or(regPassword.textProperty().isEmpty().or(regConfirmPassword.textProperty().isEmpty())));
        
        
        if (Sessione.getClient() != null) {
            this.client = Sessione.getClient();
            this.isConnected = true;
        }
        
        
        avviaMonitoraggioServer();
        
    }
    
    
    private void nascondiErrori() {
        loginErrorMsg.setVisible(false);
        regErrorMsg.setVisible(false);
    }
    
    
    
    private void avviaMonitoraggioServer() {
        Thread monitorThread = new Thread(() -> {
            // Il ciclo si ferma se stopMonitor diventa true (cioè se abbiamo fatto il login e cambiato pagina)
            while (!stopMonitor) { 
                if (!isConnected) {
                    try {
                        client = new ClientConnection(messaggioRicevuto -> {
                            Platform.runLater(() -> {
                                if (messaggioRicevuto instanceof PacchettoRisposta) {
                                    gestisciRispostaServer((PacchettoRisposta) messaggioRicevuto);
                                }
                            });
                        });
                        
                        client.connect(); 
                        isConnected = true; 
                        
                        Platform.runLater(this::nascondiErrori);
                        
                    } catch (IOException e) {
                        Platform.runLater(() -> {
                            nascondiErrori();
                            if (loginErrorMsg != null) {
                                loginErrorMsg.setText("Server offline o non raggiungibile!");
                                loginErrorMsg.setVisible(true);
                            }
                            if (regErrorMsg != null) {
                                regErrorMsg.setText("Server offline o non raggiungibile!");
                                regErrorMsg.setVisible(true);
                            }
                        });
                    }
                } else {
                    try {
                        // Ping di controllo vita
                        client.send(new PacchettoRisposta("PING"));
                    } catch (IOException e) {
                        isConnected = false; 
                        Platform.runLater(() -> {
                            nascondiErrori();
                            if (loginErrorMsg != null) {
                                loginErrorMsg.setText("Connessione col server interrotta!");
                                loginErrorMsg.setVisible(true);
                            }
                            if (regErrorMsg != null) {
                                regErrorMsg.setText("Connessione col server interrotta!");
                                regErrorMsg.setVisible(true);
                            }
                        });
                    }
                }
                
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
        });
        
        monitorThread.setDaemon(true); 
        monitorThread.start();
    }
    
    private void gestisciRispostaServer(PacchettoRisposta pacchetto) {
        System.out.println("Dal Server è arrivato: " + pacchetto);
        
        switch (pacchetto.getComando()) {
            
            case "LOGIN_OK":
                // 1. Fermiamo il ciclo while(true) del Thread!
                stopMonitor = true; 
                
                // 2. Salviamo l'utente e il client a livello Globale!
                Player pLogin = (Player) pacchetto.getPayload();
                Sessione.setPlayer(pLogin);
                Sessione.setClient(client); 
                
                try {
                    Main.setRoot("playerDashboard");
                } catch (IOException e) {
                    System.err.println("Errore caricamento Dashboard: " + e.getMessage());
                }
                break;
                
            case "REGISTER_OK":
                // Stesse operazioni del Login
                stopMonitor = true; 
                
                Player pReg = (Player) pacchetto.getPayload();
                Sessione.setPlayer(pReg);
                Sessione.setClient(client); 
                
                try {
                    Main.setRoot("playerDashboard");
                } catch (IOException e) {
                    System.err.println("Errore caricamento Dashboard: " + e.getMessage());
                }
                break;
                
            case "LOGIN_ERR":
                nascondiErrori();
                String msgLogin = (String) pacchetto.getPayload();
                if (msgLogin == null || msgLogin.isEmpty()) msgLogin = "Credenziali errate";
                    loginErrorMsg.setText("* Errore: " + msgLogin);
                    loginErrorMsg.setVisible(true);
                
                break;
                
            case "REGISTER_ERR":
                nascondiErrori();
                String msgReg = (String) pacchetto.getPayload();
                if (msgReg == null || msgReg.isEmpty()) msgReg = "Impossibile completare la registrazione";
                
                    regErrorMsg.setText("* Errore: " + msgReg);
                    regErrorMsg.setVisible(true);
                    
              
                break;
        }
    }
    
    
    @FXML
    public void login() throws IOException{
        
        loginErrorMsg.setVisible(false);
        
        // Verifico di essere connesso
        if (!isConnected) {
            loginErrorMsg.setText("* Errore: In attesa del Server...");
            loginErrorMsg.setVisible(true);
            return;
        }
        
        // Caso Username non valido
        if(loginUsername.getText().equals("ND")){
            loginErrorMsg.setText("* Errore: Utente non valido");
            loginErrorMsg.setVisible(true);
            return;
        }
        
        try {
            
            String[] credenziali = {loginUsername.getText(), loginPassword.getText()};
            PacchettoRisposta richiesta = new PacchettoRisposta("LOGIN_REQUEST", credenziali);
            client.send(richiesta);
            
        }
        catch (IOException e){
             isConnected = false;
        }
        catch (IllegalArgumentException e){System.out.println("Parametri non gestiti: " + e.getMessage());}
    }
    
    @FXML
    public void register(){
        
        regErrorMsg.setVisible(false);
        
        if (!isConnected) {
            regErrorMsg.setText("* Errore: In attesa del Server...");
            regErrorMsg.setVisible(true);
            return;
        }
        
        if(!regPassword.getText().equals(regConfirmPassword.getText())){
            regErrorMsg.setText("* Errore: Le password non coincidono");
            regErrorMsg.setVisible(true);
            return;
        }
        
        if(regUsername.getText().equals("ND")){
            regErrorMsg.setText("* Errore: Utente non valido");
            regErrorMsg.setVisible(true);
            return;
        }
        
        if(regConfirmPassword.getText().length()<8){
            regErrorMsg.setText("* Errore: La Password deve essere di almeno 8 caratteri");
            regErrorMsg.setVisible(true);
            return;
        }
        
        
        try {
            // Creo l'Array di credenziali da inviare per la registrazione
            String[] datiRegistrazione = {regUsername.getText(), regConfirmPassword.getText()};
            
            // Impacchetto e invio!
            PacchettoRisposta richiesta = new PacchettoRisposta("REGISTER_REQUEST", datiRegistrazione);
            client.send(richiesta);
            
        } catch (Exception e) {
            isConnected = false;
        }
    }
    
}
