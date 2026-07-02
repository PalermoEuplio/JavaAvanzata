package controller;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.Button;
import javafx.fxml.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.Main;
import model.connection.PacchettoRisposta;
import model.connection.Sessione;
import model.utility.Player;

public class LoginController implements Initializable{

    // Serie di componenti grafiche
    
    @FXML
    private Label serverError;
    
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
    
    
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        btnLogin.disableProperty().bind(loginUsername.textProperty().isEmpty().or(loginPassword.textProperty().isEmpty()));
        
        btnRegister.disableProperty().bind(regUsername.textProperty().isEmpty().or(regPassword.textProperty().isEmpty().or(regConfirmPassword.textProperty().isEmpty())));
        
        
        Sessione.setOnServerResponse(this::gestisciRispostaServer);
        
        Sessione.avviaMonitoraggio();
        
    }
    
    
    private void nascondiErrori() {
        loginErrorMsg.setVisible(false);
        regErrorMsg.setVisible(false);
        serverError.setVisible(false);
    }
    
    
    
    private void gestisciRispostaServer(PacchettoRisposta pacchetto) {
        
        switch (pacchetto.getComando()) {
            case "CONNESSIONE_OK":
                nascondiErrori();
                break;
                
            case "CONNESSIONE_PERSA":
                nascondiErrori();
                serverError.setText("Server offline o non raggiungibile!");
                serverError.setVisible(true);
                break;
                
            case "LOGIN_OK":
            case "REGISTER_OK":
                // Login o Reg andata a buon fine!
                Sessione.setPlayer((Player) pacchetto.getPayload());
                try {
                    Main.setRoot("playerDashboard");
                } catch (IOException e) {
                    System.err.println("Errore caricamento Dashboard: " + e.getMessage());
                }
                break;
                
            case "LOGIN_ERR":
                nascondiErrori();
                String msgLogin = (String) pacchetto.getPayload();
                loginErrorMsg.setText("* Errore: " + (msgLogin != null ? msgLogin : "Credenziali errate"));
                loginErrorMsg.setVisible(true);
                break;
                
            case "REGISTER_ERR":
                nascondiErrori();
                String msgReg = (String) pacchetto.getPayload();
                regErrorMsg.setText("* Errore: " + (msgReg != null ? msgReg : "Impossibile registrare"));
                regErrorMsg.setVisible(true);
                break;
        }
    }
    
    
    @FXML
    public void login() throws IOException{
        
        loginErrorMsg.setVisible(false);
        
        // Verifico di essere connesso
        if (!Sessione.isConnected()) {
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
            Sessione.getClient().send(new PacchettoRisposta("LOGIN_REQUEST", credenziali));
            
        }
        catch (IOException e){}
        catch (IllegalArgumentException e){System.out.println("Parametri non gestiti: " + e.getMessage());}
    }
    
    @FXML
    public void register(){
        
        regErrorMsg.setVisible(false);
        
        if (!Sessione.isConnected()) {
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
            Sessione.getClient().send(richiesta);
            
        } catch (Exception e) {}
    }
    
}
