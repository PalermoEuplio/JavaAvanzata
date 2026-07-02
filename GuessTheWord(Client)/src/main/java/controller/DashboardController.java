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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Main;
import model.connection.PacchettoRisposta;
import model.utility.Player;
import model.connection.Sessione;
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
    private TableColumn<Sfida, Integer> durata;
    
    @FXML
    private TableColumn<Sfida, Integer> tempo1;
    
    @FXML
    private TableColumn<Sfida, Integer> tempo2;
    
    @FXML
    private TableColumn<Sfida, String> soluzione;
    
    @FXML
    private TableColumn<Sfida, String> risultato;
    
    
    
    
    public void initialize(URL location, ResourceBundle resources) {
        
        Player currentP = Sessione.getPlayer();
        
        username.setText(currentP.getUsername());   // Setto l'Username
        nPartite.setText(String.valueOf(currentP.getNPartite()));   // Setto il numero di Partite
        nVittorie.setText(String.valueOf(currentP.getNVittorie()));   // Setto il numero di Vittorie
        int mediaSecondi = (int) currentP.getTempoRisposta();
        rispostaMedia.setText(String.format("%02d:%02d", mediaSecondi / 60, mediaSecondi % 60));
        
        
        
        // Invio la richiesta dello storico al Server
        if (!Sessione.isConnected()) {
            System.out.println("Errore: Connessione col server persa");
            return;
        }
        
        try{
            Sessione.getClient().send(new PacchettoRisposta("STORICO_REQUEST"));
        }catch (IOException e){}
        
        
        Sessione.setOnServerResponse(this::gestisciRispostaServer);
        
       
        
        tabellaStorico.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        titolo.setCellValueFactory(new PropertyValueFactory<>("titoloTesto"));
        avversario.setCellValueFactory(new PropertyValueFactory<>("oppUsername"));
        soluzione.setCellValueFactory(new PropertyValueFactory<>("soluzione"));
        risultato.setCellValueFactory(new PropertyValueFactory<>("risultato"));
        
        
        // Formatto le colonne legate al tempo in modo da far comparire minuti:secondi
        durata.setCellValueFactory(new PropertyValueFactory<>("durata"));
        formattaColonnaTempo(durata);
        
        tempo1.setCellValueFactory(new PropertyValueFactory<>("tRisposta1"));
        formattaColonnaTempo(tempo1);
        
        tempo2.setCellValueFactory(new PropertyValueFactory<>("tRisposta2"));
        formattaColonnaTempo(tempo2);
        
        
        
        
        // Impedisco lo spostamento delle colonne della tabella
        tabellaStorico.widthProperty().addListener((obs, oldVal, newVal) -> {
            javafx.scene.layout.Pane header = (javafx.scene.layout.Pane) tabellaStorico.lookup("TableHeaderRow");
            if (header != null) {
                header.setMouseTransparent(true); 
            }
        });
        
    }
    
    
    private void formattaColonnaTempo(TableColumn<Sfida, Integer> colonna) {
        colonna.setCellFactory(column -> new TableCell<Sfida, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    int min = item / 60;
                    int sec = item % 60;
                    setText(String.format("%02d:%02d", min, sec));
                }
            }
        });
    }
    
    
    private void gestisciRispostaServer(PacchettoRisposta pacchetto) {
        
        switch(pacchetto.getComando()){
            case "STORICO_OK":
                    List<Sfida> s = (List<Sfida>) pacchetto.getPayload();
                    tabellaStorico.setItems(FXCollections.observableArrayList(s));
                    break;
            default: System.out.println("Errore: Impossibile caricare lo storico");
        }
        
    }
    
    
    
    @FXML
    private void mostraClassifiche() throws IOException{
        Main.setRoot("rankings");
    }
    
    @FXML
    private void nuovaPartita() throws IOException{
        Main.setRoot("loading");
    }
    @FXML
    private void logout() throws IOException{
        if (model.connection.Sessione.getClient() != null) {
            try {
                model.connection.Sessione.getClient().send(new PacchettoRisposta("LOGOUT_REQUEST"));
            } catch (java.io.IOException e) {
                System.err.println("Errore nell'invio del comando di logout al server.");
            }
        }
        
        // 2. Rimuoviamo il giocatore dalla sessione locale (senza uccidere il socket!)
        Sessione.logout(); 
        
        // 3. Torniamo alla pagina di login
        Main.setRoot("login");
    }
    
    
    
}
