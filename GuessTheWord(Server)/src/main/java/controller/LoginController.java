package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.utility.Amministratore;
import model.db.DBConnector;
import model.connection.Sessione;
import model.Main;

public class LoginController implements Initializable{

    @FXML
    private TextField fieldUsername;
    
    @FXML
    private TextField fieldPassword;
    
    @FXML
    private Button enter;
    
    @FXML
    private Label errormsg;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        enter.disableProperty().bind(fieldUsername.textProperty().isEmpty().or(fieldPassword.textProperty().isEmpty()));
        
    }
    
    
    
    @FXML
    private void login() throws IOException {
        
        errormsg.setVisible(false);
        

        if(fieldUsername.getText().equals("ND")){
            errormsg.setText("* Errore: Utente non valido");
            errormsg.setVisible(true);
            return;
        }
           
        
        
        try {
            
            Amministratore adminlog = (Amministratore) new DBConnector().cerca(new Amministratore(fieldUsername.getText()), fieldPassword.getText());
            Sessione.setAdmin(adminlog);
            Main.setRoot("adminDashboard");
            
        }
        catch (SQLException e){
            errormsg.setText("* Errore: "+e.getMessage());
            errormsg.setVisible(true);
        }
        catch (IllegalArgumentException e){System.out.println("Parametri non gestiti: " + e.getMessage());}
        
    }
}
