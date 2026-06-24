package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import model.Player;
import model.DBConnector;
import model.Sessione;
import model.Main;


public class AdminDashboardController implements Initializable{
    
    
    @FXML
    private TableView<Player> tabellaGiocatori;
    @FXML
    private TableColumn<Player, Integer> id;
    @FXML
    private TableColumn<Player, String> Username;
    @FXML
    private TableColumn<Player, Integer> nPartite;
    @FXML
    private TableColumn<Player, Integer> nVittorie;
    @FXML
    private TableColumn<Player, Double> tempoRisposta;
    @FXML
    private TableColumn<Player, Boolean> state;
    
    
    
    @FXML
    private Button btnBanna;
    
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        // Carico i giocatori
        List<Player> l = null;
        try {
            
            l = new DBConnector<Player>().elencaTuttiPlayer();
            
        } catch (Exception ex) {System.err.println("Errore caricamento giocatori: "+ex);}
        
        ObservableList<Player> tableList = FXCollections.observableArrayList(l);
        
        // Preparo la struttura della tabella
        tabellaGiocatori.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        Username.setCellValueFactory(new PropertyValueFactory<>("Username"));
        nPartite.setCellValueFactory(new PropertyValueFactory<>("nPartite"));
        nVittorie.setCellValueFactory(new PropertyValueFactory<>("nVittorie"));
        tempoRisposta.setCellValueFactory(new PropertyValueFactory<>("tempoRisposta"));
        state.setCellValueFactory(new PropertyValueFactory<>("isOn"));
        
        state.setCellFactory(column -> new TableCell<Player, Boolean>() {
        @Override
        protected void updateItem(Boolean isOnline, boolean empty) {
            super.updateItem(isOnline, empty);

            if (empty || isOnline == null) {
                // Se la riga è vuota, non disegnare nulla
                setGraphic(null);
                setText(null);
            } else {
                // Creo un pallino con raggio di 6 pixel
                Circle pallino = new Circle(6); 

                // Decido il colore in base allo stato
                if (isOnline) {
                    pallino.setFill(Color.web("#2ecc71")); // Verde acceso
                } else {
                    pallino.setFill(Color.web("#e74c3c")); // Rosso acceso
                }

                // Inserisco il pallino nella cella
                setGraphic(pallino);
                setText(null);
                setAlignment(Pos.CENTER); // Centro il pallino nella colonna
                }
            }
        });
        
        
        // Effettu il collegamento dei dati con la tabella
        tabellaGiocatori.setItems(tableList);
        
        // Comportamento pulsante Ban
        btnBanna.disableProperty().bind(tabellaGiocatori.getSelectionModel().selectedItemProperty().isNull());
    }
    
    
    // Comportamento pulsante Inizia partita
    @FXML
    private void startGame() throws IOException {
        
    }
    
    
    // Comportamento pulsante banPlayer
    @FXML
    private void banPlayer() throws IOException {
        
        Player p = tabellaGiocatori.getSelectionModel().getSelectedItem();
        
        if(p==null){
            return;
        }
        
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma Ban Giocatore");
        alert.setHeaderText("Stai per bannare: " + p.getUsername());
        alert.setContentText("Sei sicuro di voler procedere? L'operazione non può essere annullata.");

        alert.getDialogPane().getStylesheets().add(getClass().getResource("/it/guesstheword/StyleSheet.css").toExternalForm());
        
        Optional<ButtonType> result = alert.showAndWait();
        
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
        try {
            
            DBConnector<Player> db = new DBConnector<>();
            db.rimuoviPlayer(p);
            tabellaGiocatori.getItems().setAll(db.elencaTuttiPlayer());
            
        } catch (SQLException e) {System.err.println("Errore durante la rimozione: "+e);}
    }
    }
    
    // Comportamento pulsante di logout
    @FXML
    private void logout() throws IOException {
        Sessione.logout();
        Main.setRoot("login");
    }
    
    
}
