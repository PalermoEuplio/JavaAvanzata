package controller;


import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.scene.control.Button;
import javafx.fxml.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.Main;
import model.db.DBConnector;
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
    
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        btnLogin.disableProperty().bind(loginUsername.textProperty().isEmpty().or(loginPassword.textProperty().isEmpty()));
        
        btnRegister.disableProperty().bind(regUsername.textProperty().isEmpty().or(regPassword.textProperty().isEmpty().or(regConfirmPassword.textProperty().isEmpty())));
        
    }
    
    
    @FXML
    public void login() throws IOException{
        
        loginErrorMsg.setVisible(false);
        

        if(loginUsername.getText().equals("ND")){
            loginErrorMsg.setText("* Errore: Utente non valido");
            loginErrorMsg.setVisible(true);
            return;
        }
        
        try {
            
            Player plogin = new DBConnector<Player>().cerca(new Player(loginUsername.getText(), 0, 0, 0, 0), loginPassword.getText());
            Sessione.setPlayer(plogin);
            Main.setRoot("playerDashboard");
            
        }
        catch (SQLException e){
            loginErrorMsg.setText("* Errore: "+e.getMessage());
            loginErrorMsg.setVisible(true);
        }
        catch (IllegalArgumentException e){System.out.println("Parametri non gestiti: " + e.getMessage());}
    }
    
    @FXML
    public void register(){
        
        regErrorMsg.setVisible(false);
        
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
            
            Player p = new DBConnector<Player>().registrazione(new Player(regUsername.getText(),0,0,0,0), regConfirmPassword.getText());
            Sessione.setPlayer(p);
            Main.setRoot("playerDashboard");
            
        } catch (Exception e) {
            regErrorMsg.setText("* Errore: "+e.getMessage());
            regErrorMsg.setVisible(true);
        }
        
        
        
        
        
    }
    
}
