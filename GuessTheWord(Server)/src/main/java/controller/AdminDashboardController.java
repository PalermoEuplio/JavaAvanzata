package controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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
    
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        
        
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        Username.setCellValueFactory(new PropertyValueFactory<>("Username"));
        nPartite.setCellValueFactory(new PropertyValueFactory<>("nPartite"));
        nVittorie.setCellValueFactory(new PropertyValueFactory<>("nVittorie"));
        tempoRisposta.setCellValueFactory(new PropertyValueFactory<>("tempoRisposta"));
        state.setCellValueFactory(new PropertyValueFactory<>("isOn"));
        
        List<Player> l = null;
        try {
            
            l = new DBConnector<Player>().elencaTuttiPlayer();
            
        } catch (Exception ex) {System.err.println("Errore tipo non gestito"+ex);}
        
        ObservableList<Player> tableList = FXCollections.observableArrayList(l);
        
        tabellaGiocatori.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        
        state.setCellFactory(column -> new TableCell<Player, Boolean>() {
        @Override
        protected void updateItem(Boolean isOnline, boolean empty) {
            super.updateItem(isOnline, empty);

            if (empty || isOnline == null) {
                // Se la riga è vuota, non disegnare nulla
                setGraphic(null);
                setText(null);
            } else {
                // Creiamo un pallino con raggio di 6 pixel
                Circle pallino = new Circle(6); 

                // Decidiamo il colore in base allo stato
                if (isOnline) {
                    pallino.setFill(Color.web("#2ecc71")); // Verde acceso
                } else {
                    pallino.setFill(Color.web("#e74c3c")); // Rosso acceso
                }

                // Inseriamo il pallino nella cella
                setGraphic(pallino);
                setText(null); // Nascondiamo l'eventuale testo
                setAlignment(Pos.CENTER); // Centriamo il pallino nella colonna
            }
        }
    });
        
        
        
        tabellaGiocatori.setItems(tableList);
        
        
    }
    
    @FXML
    private void startGame() throws IOException {
        
    }
    
    @FXML
    private void showSpecs() throws IOException {
        
    }
    
    @FXML
    private void banPlayer() throws IOException {
        
    }
    
    @FXML
    private void logout() throws IOException {
        Sessione.logout();
        Main.setRoot("login");
    }
    
    
}
