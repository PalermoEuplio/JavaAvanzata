package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
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
import model.utility.Sessione;
import model.Main;
import model.connection.PacchettoRisposta;
import model.connection.ServerConnection;


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
        
         // Carico i giocatori
        List<Player> l = null;
        try {
            
            l = new DBConnector<Player>().elencaTuttiPlayer();
            
        } catch (Exception ex) {System.err.println("Errore caricamento giocatori: "+ex);}
        
        tableList = FXCollections.observableArrayList(l);
        
        
        
        // Definizione del server
         if (Sessione.getServer() == null) {
            
            ServerConnection server = new ServerConnection(
                
                // CALLBACK 1: MESSAGGI IN ARRIVO DAL CLIENT
                (pacchettoRicevuto, mittente) -> {
                    if (pacchettoRicevuto instanceof PacchettoRisposta) {
                        PacchettoRisposta pacchetto = (PacchettoRisposta) pacchettoRicevuto;
                        
                        switch (pacchetto.getComando()) {
                            case "PING":
                                break;
                                
                            case "LOGIN_REQUEST":
                                String[] cred = (String[]) pacchetto.getPayload();
                                try {
                                    Player pLog = new DBConnector<Player>().cerca(new Player(cred[0], 0, 0, 0, 0), cred[1]);
                                    
                                    // Salviamo l'identità nel Socket 
                                    mittente.setUsernameLoggato(pLog.getUsername());
                                    
                                    mittente.send(new PacchettoRisposta("LOGIN_OK", pLog));
                                    Platform.runLater(() -> aggiornaStatoOnline(pLog.getUsername(), true));
                                    
                                } catch (Exception e) {
                                    try { mittente.send(new PacchettoRisposta("LOGIN_ERR", e.getMessage())); } catch (IOException ex) {}
                                }
                                break;
                                
                            case "REGISTER_REQUEST":
                                String[] datiReg = (String[]) pacchetto.getPayload();
                                try {
                                    Player pReg = new DBConnector<Player>().registrazione(new Player(datiReg[0], 0, 0, 0, 0), datiReg[1]);
                                    
                                    mittente.setUsernameLoggato(pReg.getUsername());
                                    
                                    mittente.send(new PacchettoRisposta("REGISTER_OK", pReg));
                                    Platform.runLater(() -> {
                                        tableList.add(pReg);
                                        aggiornaStatoOnline(pReg.getUsername(), true);
                                    });
                                } catch (Exception e) {
                                    try { mittente.send(new PacchettoRisposta("REGISTER_ERR", e.getMessage())); } catch (IOException ex) {}
                                }
                                break;
                            case "LOGOUT_REQUEST":
                                String userSloggato = mittente.getUsernameLoggato();
                                if (userSloggato != null) {
                                    Platform.runLater(() -> aggiornaStatoOnline(userSloggato, false));
                                    // Rimuovo l'associazione utente-socket, 
                                    // così la disconnessione successiva (CALLBACK 2) non duplica l'aggiornamento
                                    mittente.setUsernameLoggato(null);
                                }
                                break;
                        }
                    }
                },
                
                // CALLBACK 2: CLIENT DISCONNESSO
                (mittenteDisconnesso) -> {
                    if (mittenteDisconnesso.getUsernameLoggato() != null) {
                        Platform.runLater(() -> aggiornaStatoOnline(mittenteDisconnesso.getUsernameLoggato(), false));
                    }
                }
            );

            server.startServer();
            // Salva il server così non si dealloca cambiando pagina!
            Sessione.setServer(server); 
        } else {
            // Se eravamo già connessi, calcola solo il numero attuale degli online
            aggiornaContatore();
        }
       
        
        // Preparo la struttura della tabella
        tabellaGiocatori.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        Username.setCellValueFactory(new PropertyValueFactory<>("username"));
        nPartite.setCellValueFactory(new PropertyValueFactory<>("nPartite"));
        nVittorie.setCellValueFactory(new PropertyValueFactory<>("nVittorie"));
        nVittorie.setSortType(TableColumn.SortType.DESCENDING);
        tempoRisposta.setCellValueFactory(new PropertyValueFactory<>("tempoRisposta"));
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
        // Ordinamento dei Giocatori per nVittorie (Simulazione della Classifica)
        tabellaGiocatori.getSortOrder().addAll(nVittorie,tempoRisposta);
        tabellaGiocatori.sort();
        
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
    
    
    
    private void aggiornaStatoOnline(String username, boolean isOnline) {
        for (Player p : tableList) {
            if (p.getUsername().equals(username)) {
                p.setOn(isOnline);
                break;
            }
        }
        tabellaGiocatori.refresh(); // Forza l'aggiornamento dei colori
        aggiornaContatore();
    }
    
    private void aggiornaContatore() {
        long onlineCount = tableList.stream().filter(Player::isOn).count();
        playerCount.setText(String.valueOf(onlineCount));
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
