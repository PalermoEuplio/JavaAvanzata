/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Main;
import model.db.DBConnector;
import model.utility.Player;
import model.utility.Sessione;
import model.utility.Sfida;

/**
 *
 * @author euppa
 */
public class DashboardController implements Initializable{
    
    
    
    
    @FXML
    private Label username;
    
    @FXML
    private Label nPartite;
    
    @FXML
    private Label nVittorie;
    
    @FXML
    private Label rispostaMedia;
    
    
    @FXML
    private TableView<Sfida> tabellaStorico;
    
    @FXML
    private TableColumn<Sfida, String> titolo;
    
    @FXML
    private TableColumn<Sfida, String> avversario;
    
    @FXML
    private TableColumn<Sfida, Double> durata;
    
    @FXML
    private TableColumn<Sfida, Double> tempo1;
    
    @FXML
    private TableColumn<Sfida, Double> tempo2;
    
    @FXML
    private TableColumn<Sfida, String> soluzione;
    
    @FXML
    private TableColumn<Sfida, String> risultato;
    
    
    
    
    public void initialize(URL location, ResourceBundle resources) {
        
        Player currentP = Sessione.getPlayer();
        
        username.setText(currentP.getUsername());   // Setto l'Username
        nPartite.setText(String.valueOf(currentP.getNPartite()));   // Setto il numero di Partite
        nVittorie.setText(String.valueOf(currentP.getNVittorie()));   // Setto il numero di Vittorie
        rispostaMedia.setText(String.valueOf(currentP.getTempoRisposta()));   // Setto il tempo di risposta medio
        
        
        List<Sfida> s = null;
        
        try {
            s = new DBConnector<Sfida>().caricaSfide(Sessione.getPlayer().getId());
        } catch (Exception e) { System.out.println("Errore durante il caricamento delle sfide: "+e);}
        
        tabellaStorico.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        titolo.setCellValueFactory(new PropertyValueFactory<>("titoloTesto"));
        avversario.setCellValueFactory(new PropertyValueFactory<>("oppUsername"));
        durata.setCellValueFactory(new PropertyValueFactory<>("durata"));
        tempo1.setCellValueFactory(new PropertyValueFactory<>("tRisposta1"));
        tempo2.setCellValueFactory(new PropertyValueFactory<>("tRisposta2"));
        soluzione.setCellValueFactory(new PropertyValueFactory<>("soluzione"));
        risultato.setCellValueFactory(new PropertyValueFactory<>("risultato"));
        
        tabellaStorico.setItems(FXCollections.observableArrayList(s));
        
        tabellaStorico.widthProperty().addListener((obs, oldVal, newVal) -> {
            javafx.scene.layout.Pane header = (javafx.scene.layout.Pane) tabellaStorico.lookup("TableHeaderRow");
            if (header != null) {
                header.setMouseTransparent(true); 
            }
        });
        
    }
    
    @FXML
    private void mostraClassifiche() throws IOException{
        Main.setRoot("rankings");
    }
    
    @FXML
    private void nuovaPartita(){
        
    }
    @FXML
    private void logout() throws IOException{
        Sessione.logout();
        Main.setRoot("login");
    }
    
    
    
}
