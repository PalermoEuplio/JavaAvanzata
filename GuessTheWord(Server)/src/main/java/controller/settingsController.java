/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 *
 * @author euppa
 */
public class settingsController implements Initializable{
    
    
    // Collegamenti agli elementi della pagina
    @FXML
    private ComboBox<String> comboTesti;
    
    @FXML
    private Button analizzaTesto;
    
    @FXML 
    private TextArea areaTesto;
    
    @FXML
    private ListView<String> fileAnalizzati;
    
    @FXML
    private TextArea risultatiAnalisi;
    
    @FXML
    private Slider frequenza;
    
    @FXML
    private Slider lunghezza;
    
    @FXML 
    private Spinner<Integer> nParole;
    
    @FXML
    private Spinner<Integer> shift;
    
    @FXML
    private Label difficoltà;
    
    @FXML
    private TextField paroleScelte;
    
    
    
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @FXML
    private void avviaPartita() throws IOException {
         
    }
     
    @FXML
    private void generaParole() throws IOException {
          
    }
    
    @FXML
    private void analisiTesto() throws IOException {
          
    }
    
}
