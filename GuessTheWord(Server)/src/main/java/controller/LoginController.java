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
import model.Amministratore;
import model.DBConnector;
import model.Sessione;
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
    
    @FXML
    private Label errormsg1;
    
    
    public void initialize(URL location, ResourceBundle resources) {
        
        enter.disableProperty().bind(fieldUsername.textProperty().isEmpty().or(fieldPassword.textProperty().isEmpty()));
        
    }
    
    
    
    @FXML
    private void login() throws IOException {
        
        errormsg.setVisible(false);
        errormsg1.setVisible(false);
        

        if(fieldUsername.getText().equals("ND")){
            errormsg1.setVisible(true);
            return;
        }
           
        
        
        try {
            
            Amministratore adminlog = (Amministratore) new DBConnector().cerca(new Amministratore(fieldUsername.getText()), fieldPassword.getText());
            Sessione.setAdmin(adminlog);
            Main.setRoot("adminDashboard");
            
        }
        catch (SQLException e){
            System.out.println("Errore di login: " + e.getMessage());
            errormsg.setVisible(true);
        }
        catch (IllegalArgumentException e){System.out.println("Parametri non gestiti: " + e.getMessage());}
        
    }
}
