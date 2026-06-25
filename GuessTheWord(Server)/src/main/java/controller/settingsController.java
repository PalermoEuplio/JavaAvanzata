/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
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
import model.Testo;
import model.TextEditor;
import model.Main;
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
        TextEditor te = new TextEditor();
        
        areaTesto.appendText(te.caricaTesto("I_Promessi_Sposi"));
        
        
        te.leggiReport();
        
        
        List<Testo> titleList = te.getTitle();
        
        // Preparo la lista dei file disponibili e se questi sono già stati analizzati
        fileAnalizzati.setItems(FXCollections.observableArrayList(titleList.stream().map(
                a -> {return (a.isAnalized()) ? a.getTitolo() + " (Analizzato)" : a.getTitolo(); }).collect(Collectors.toList())
        
        ));
        
        // Preparo il combo box per la selezione del testo da analizzare
        comboTesti.setItems(FXCollections.observableArrayList(titleList.stream().map(Testo::getTitolo).collect(Collectors.toList())));

        
        
        
        
        
        
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
    
    @FXML
    private void backDashboard() throws IOException {
        Main.setRoot("adminDashboard");
    }
    
}
