package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import model.connection.Sessione;
import model.utility.TextEditor;

public class GameController implements Initializable {
    
    @FXML
    private Label timer;
    
    @FXML
    private ProgressBar timerBar;
    
    @FXML
    private TextFlow testo;
    
    @FXML
    private FlowPane contenitoreSlotRisposte;
    
    
    
    
    @FXML
    private VBox overlayFinePartita;
    
    @FXML
    private Label esito;
    
    @FXML
    private Label risposte;
    
    @FXML
    private Label mioTempo;
    
    @FXML
    private Label mieParole;
    
    @FXML
    private Label oppTempo;
    
    @FXML
    private Label oppParole;
    
    
    
    
    
    @Override
     public void initialize(URL location, ResourceBundle resources){
         testo.getChildren().addAll(new Text(TextEditor.getGameText()));
     }
     
     
     
     @FXML
     private void resign(){
         
     }
     
     @FXML
     private void confirmAnswer(){
         
     }
     
     
     @FXML
     private void back(){
         
     }
     
     @FXML
     private void newGame(){
         
     }
    
}
