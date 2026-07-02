package controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
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

public class ClassificheController implements Initializable {
    
    @FXML
    private Label username1;
    
    @FXML
    private Label username2;
    
    @FXML
    private Label username3;
    
    @FXML
    private Label vittorie1;
    
    @FXML
    private Label vittorie2;
    
    @FXML
    private Label vittorie3;
    
    
    
    @FXML
    private TableView<Player> tabellaClassifica;
    
    @FXML
    private TableColumn<Player, Integer> posizione;
    
    @FXML
    private TableColumn<Player, String> username;
    
    @FXML
    private TableColumn<Player, Integer> vittorie;
    
    @FXML
    private TableColumn<Player, Integer> partite;
    
    @FXML
    private TableColumn<Player, Double> tempoMedio;
    
    
    @FXML
    private Label miaPosizione;
    
    
    @Override
     public void initialize(URL location, ResourceBundle resources) {
         
        
         
         try{
            Sessione.getClient().send(new PacchettoRisposta("CLASSIFICA_REQUEST"));
        }catch (IOException e){}
         
         
         Sessione.setOnServerResponse(this::gestisciRispostaServer);
         
         
         
         
        
        // Formattazione speciale per la colonna contenente il numero di posizione in classifica
        posizione.setCellValueFactory(cellData -> 
            new ReadOnlyObjectWrapper<>(tabellaClassifica.getItems().indexOf(cellData.getValue()) + 1)
        );
         
         
        username.setCellValueFactory(new PropertyValueFactory<>("username"));
        vittorie.setCellValueFactory(new PropertyValueFactory<>("nVittorie"));
        partite.setCellValueFactory(new PropertyValueFactory<>("nPartite"));
        tempoMedio.setCellValueFactory(new PropertyValueFactory<>("tempoRisposta"));
        tempoMedio.setCellFactory(column -> new TableCell<Player, Double>() {
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
        
        tabellaClassifica.setSelectionModel(null);  // Impedisco la selezione della tabella
        
        // Listner che serve per impedire che l'utente cambi l'ordine delle colonne della tabella
        tabellaClassifica.widthProperty().addListener((obs, oldVal, newVal) -> {
            javafx.scene.layout.Pane header = (javafx.scene.layout.Pane) tabellaClassifica.lookup("TableHeaderRow");
            if (header != null) {
                header.setMouseTransparent(true); 
            }
        }); 
    }
    
     
     
     private void gestisciRispostaServer(PacchettoRisposta pacchetto) {
        
        switch(pacchetto.getComando()){
            case "CLASSIFICA_OK":
                    List<Player> p = (List<Player>) pacchetto.getPayload();
                    
                    tabellaClassifica.setItems(FXCollections.observableArrayList(p));   // Collego la lista di giocatori alla tabella
                    
                    // Imposto le Lable relative alla classifica
                    if (p != null && !p.isEmpty()) {
                        // TOP 1
                        username1.setText(p.get(0).getUsername());
                        vittorie1.setText(p.get(0).getNVittorie() + " Vittorie");

                        if (p.size() > 1) { // TOP 2
                            username2.setText(p.get(1).getUsername());
                            vittorie2.setText(p.get(1).getNVittorie() + " Vittorie");
                        }

                        if (p.size() > 2) { // TOP3
                            username3.setText(p.get(2).getUsername());
                            vittorie3.setText(p.get(2).getNVittorie() + " Vittorie");
                        }

                        int x = -1;

                        // Scorro la lista finché non trovo lo stesso Username
                        for (int i = 0; i < p.size(); i++) {
                            if (p.get(i).getUsername().equals(Sessione.getPlayer().getUsername())) {    // Mi salvo la posizione nella lista (Cioè nella tabella)
                                x = i + 1; 
                                break; 
                            }
                        }

                        // Aggiorno la grafica nel Footer
                        if (x != -1) {
                            miaPosizione.setText(x + "° Posto");
                            miaPosizione.setStyle("-fx-text-fill: #2ecc71;"); // Colore verde se sei in classifica
                        } else {
                            miaPosizione.setText("Non Classificato");
                            miaPosizione.setStyle("-fx-text-fill: #e74c3c;"); // Colore rosso se non hai mai giocato
                        }
                    }
                    
                    break;
            default: System.out.println("Errore: Impossibile caricare lo storico");
        }
        
    }
     
     
     
    @FXML
    private void back() throws IOException{
        Main.setRoot("playerDashboard");
    }
    
}
