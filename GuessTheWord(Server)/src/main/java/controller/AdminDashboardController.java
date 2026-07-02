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
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import model.utility.Player;
import model.db.DBConnector;
import model.connection.Sessione;
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
    
    
    @FXML
    private Label playerCount;
    
    
    private ObservableList<Player> tableList;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        tableList = FXCollections.observableArrayList();    // Creo l'istanza della lista osservabile dalla tabella
        
        // Definizione del server
        if (Sessione.getServer() == null)
            Sessione.startServer();
        
        // Dico alla Sessione quale funzione svolgere ogni volta che rileva un cambiamento nei player 
        Sessione.setOnUserStatusChanged(() -> {
            aggiornaDatiGrafica();
        });
         
         aggiornaDatiGrafica(); // Primo richiamo della funzione per caricare i player nella tabella e aggiornare il playercount
       
        
        // Preparo la struttura della tabella
        tabellaGiocatori.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        Username.setCellValueFactory(new PropertyValueFactory<>("username"));
        nPartite.setCellValueFactory(new PropertyValueFactory<>("nPartite"));
        nVittorie.setCellValueFactory(new PropertyValueFactory<>("nVittorie"));
        nVittorie.setSortType(TableColumn.SortType.DESCENDING);
        tempoRisposta.setCellValueFactory(new PropertyValueFactory<>("tempoRisposta"));
        tempoRisposta.setCellFactory(column -> new TableCell<Player, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    int totalSeconds = item.intValue();
                    int min = totalSeconds / 60;
                    int sec = totalSeconds % 60;
                    setText(String.format("%02d:%02d", min, sec));
                }
            }
        });
        tempoRisposta.setSortType(TableColumn.SortType.ASCENDING);
        state.setCellValueFactory(new PropertyValueFactory<>("on"));
        
        // CellFactory dello stato per far comparire un cerchio verde per il loggato ed uno rosso per il non loggato
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
        
        // Effettuo il collegamento dei dati con la tabella
        tabellaGiocatori.setItems(tableList);
        
        // Impedisco lo spostamento delle colonne della tabella
        tabellaGiocatori.widthProperty().addListener((obs, oldVal, newVal) -> {
            javafx.scene.layout.Pane header = (javafx.scene.layout.Pane) tabellaGiocatori.lookup("TableHeaderRow");
            if (header != null) {
                header.setMouseTransparent(true); 
            }
        });
        
        // Comportamento pulsante Ban
        btnBanna.disableProperty().bind(tabellaGiocatori.getSelectionModel().selectedItemProperty().isNull());
    }
    
    
    // Funzione che aggiorna variabili grafiche (TabellaPlayer e PlayerCount) in base agli utenti loggati e non
    private void aggiornaDatiGrafica() {
        try {
            // 1. Ricarico i giocatori dal DB
            List<Player> aggiornatiDalDb = new DBConnector<Player>().elencaTuttiPlayer();
            
            // 2. Chiedo al server chi è loggato
            List<String> onlineUsers = Sessione.getServer().getUtentiOnline();
            
            // 3. Accendo i pallini appropriati
            for (Player p : aggiornatiDalDb) {
                p.setOn(onlineUsers.contains(p.getUsername()));
            }
            // 4. Aggiorno la tabella rimpiazzando i dati
            tableList.setAll(aggiornatiDalDb);
            tabellaGiocatori.refresh();
            
            // 5. Aggiorno il contatore
            long onlineCount = aggiornatiDalDb.stream().filter(Player::isOn).count();
            if (playerCount != null) {
                playerCount.setText(String.valueOf(onlineCount));
            }
            
        } catch (Exception e) {
            System.err.println("Errore nell'aggiornamento della grafica: " + e);
        }
    }
    
    
    // Comportamento pulsante Inizia partita
    @FXML
    private void startGame() throws IOException {
        Main.setRoot("gameSettings");
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
