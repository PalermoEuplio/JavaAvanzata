package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import model.Main;
import model.connection.PacchettoRisposta;
import model.connection.Sessione;
import model.utility.Sfida;
import model.utility.Testo;

public class GameController implements Initializable {
    
    @FXML
    private Label timer;
    
    @FXML
    private ProgressBar timerBar;
    
    @FXML
    private Label titoloTesto;
    
    @FXML
    private Label oppUsername;
    
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
    
    
    @FXML
    private VBox overlayAttesa;
    
    @FXML private Circle dot1;
    @FXML private Circle dot2;
    @FXML private Circle dot3;
    @FXML private Circle dot4;
    
    
    private Timeline timerPing; // Per i pallini d'attesa
    
    private Sfida currentGame;
    
    private Timeline timelineTimer;
    private int secondiRimanenti;
    private int durataIniziale;
    private int nRisposte;
    
    private List<TextField> listaInput = new ArrayList<>(); // Lista per leggere le risposte alla fine
    
    
    @Override
     public void initialize(URL location, ResourceBundle resources){
         caricaTesto();
         
         Sessione.setOnServerResponse(this::gestisciRispostaServer);
         
     }
     
     
     
    private void gestisciRispostaServer(PacchettoRisposta pacchetto){
        
        switch(pacchetto.getComando()){
            case "GAME_INFO":
                Platform.runLater(() -> {
                    
                    currentGame = (Sfida) pacchetto.getPayload();
                    
                    oppUsername.setText("Avversario: " + currentGame.getOppUsername());
                    titoloTesto.setText("Titolo: " + currentGame.getTitoloTesto());
                    
                    // 1. Creiamo dinamicamente i TextField in base al numero di parole cifrate
                    nRisposte = Integer.parseInt(currentGame.getSoluzione());
                    creaSlotRisposte(nRisposte);
                    
                    // 2. Avviamo il timer convertendo la durata in interi (secondi)
                    avviaTimer((int) currentGame.getDurata());
                });
                
                break;
            case "RESIGN_OK":
            case "OPP_RESIGN":  
                
                    if (timerPing != null) {
                        timerPing.stop();
                        overlayAttesa.setVisible(false);
                    }
                    
                    overlayFinePartita.setVisible(true);
                    esito.setText((pacchetto.getComando().equals("OPP_RESIGN")) ? "Vittoria" : "Sconfitta");

                    mioTempo.setText("0");
                    oppTempo.setText("0");

                    risposte.setText((pacchetto.getComando().equals("OPP_RESIGN")) ? 
                            "L'Avversario si è arreso; Parole da Indovinare: "+ (String) pacchetto.getPayload() :
                            "Ti sei arreso; Parole da Indovinare: "+ (String) pacchetto.getPayload());
                    mieParole.setText("0/"+nRisposte);
                    oppParole.setText("0/"+nRisposte);
                
               
                break;
                
            case "YOU_WON":
            case "YOU_LOST":
                
                    if (timerPing != null) {
                        timerPing.stop();
                        overlayAttesa.setVisible(false);
                    }
                
                    Object[] ob = (Object[]) pacchetto.getPayload();
                    
                    String paroleVere = (String) ob[0];
                    int mieR = (int) ob[3];
                    int oppR = (int) ob[4];
                    
                    
                    
                    overlayFinePartita.setVisible(true);
                    
                    esito.setText((pacchetto.getComando().equals("YOU_WON")) ? "Vittoria" : "Sconfitta");
                    
                    risposte.setText( (mieR == nRisposte) ? "Hai indovinato tutte le parole cifrate!" : 
                            "Parole da Indovinare: "+paroleVere );
                    
                    
                    mioTempo.setText((String)ob[1]);
                    oppTempo.setText((String) ob[2]);
                    
                    mieParole.setText(mieR+"/"+nRisposte);
                    oppParole.setText(oppR+"/"+nRisposte);
                
                break;
            default: System.out.println("Errore: Messaggio Sconosciuto");
        }
        
    }
     
     
     
     
    private void creaSlotRisposte(int nSoluzioni) {
        contenitoreSlotRisposte.getChildren().clear();
        listaInput.clear();
        
        for (int i = 0; i < nSoluzioni; i++) {
            TextField tf = new TextField();
            tf.setPromptText("Parola " + (i + 1));
            tf.setStyle("-fx-font-size: 16px; -fx-alignment: center; -fx-pref-width: 150px;");
            
            // Salviamo il TextField in una lista per leggerlo quando preme "Conferma"
            listaInput.add(tf);
            
            // Lo aggiungiamo alla grafica (nel FlowPane)
            contenitoreSlotRisposte.getChildren().add(tf);
        }
    }

    /**
     * Metodo per avviare il countdown e aggiornare barra e testi in tempo reale.
     */
    private void avviaTimer(int durataSecondi) {
        this.durataIniziale = durataSecondi;
        this.secondiRimanenti = durataSecondi;
        
        if (timelineTimer != null) timelineTimer.stop(); // Ferma eventuali timer precedenti
        
        timelineTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            secondiRimanenti--;
            
            // Aggiorna l'etichetta testuale del timer
            int min = secondiRimanenti / 60;
            int sec = secondiRimanenti % 60;
            timer.setText(String.format("%02d:%02d", min, sec));
            
            // Aggiorna la barra di progresso (valore da 1.0 a 0.0)
            timerBar.setProgress((double) secondiRimanenti / durataIniziale);
            
            // Effetto visivo: se manca 1 minuto o meno, diventa tutto rosso
            if (secondiRimanenti <= 60) {
                timer.setStyle("-fx-text-fill: #e74c3c;");
                timerBar.setStyle("-fx-accent: #e74c3c;");
            }
            
            // Se il tempo scade
            if (secondiRimanenti <= 0) {
                timelineTimer.stop();
                System.out.println("Tempo scaduto!");
                confirmAnswer(); // Simula la pressione del tasto conferma automaticamente!
            }
        }));
        
        timelineTimer.setCycleCount(Timeline.INDEFINITE);
        timelineTimer.play();
    } 
     
     
     
     
    private void caricaTesto() {
        
        testo.getChildren().clear(); // Pulisce eventuali testi precedenti
        
        // Il metodo split taglia la stringa ogni volta che trova [[ oppure ]]
        // In questo modo, l'array risultante alternerà SEMPRE: Testo Normale -> Parola Cifrata -> Testo Normale...
        String[] frammenti = Testo.getGameText().split("\\[\\[|\\]\\]");
        
        boolean isCifrata = false; // Il primo frammento (all'indice 0) è SEMPRE testo normale, anche se vuoto.
        
        for (String frammento : frammenti) {
            
            if (isCifrata) {
                
                // MAGIA JAVAFX: Usiamo una Label invece di un Text per poter colorare lo sfondo!
                Label parolaEvidenziata = new Label(frammento);
                parolaEvidenziata.setStyle(
                    "-fx-background-color: #f1c40f; " + // Sfondo Giallo accesso
                    "-fx-text-fill: #2c3e50; " +        // Testo scuro
                    "-fx-padding: 0 4 0 4; " +          // Piccolo margine interno per far respirare il testo
                    "-fx-background-radius: 4; " +      // Bordi leggermente arrotondati
                    "-fx-font-weight: bold;"            // Grassetto
                );
                parolaEvidenziata.setFont(Font.font("System", 16));
                
                testo.getChildren().add(parolaEvidenziata);
                
            } else {
                
                // Per il testo normale usiamo il classico oggetto Text
                Text testoNormale = new Text(frammento);
                testoNormale.setFont(Font.font("System", 16));
                
                testo.getChildren().add(testoNormale);
            }
            
            // Invertiamo lo stato per il prossimo frammento!
            isCifrata = !isCifrata;
        }
    }
     
     
    private void avviaAnimazionePallini() {
        Circle[] dots = {dot1, dot2, dot3, dot4};
        SequentialTransition sequenza = new SequentialTransition();

        // Creiamo una transizione di "Fade" (Scomparsa/Comparsa) per ogni pallino
        for (Circle dot : dots) {
            dot.setOpacity(0.2); // Partono tutti semitrasparenti
            
            FadeTransition ft = new FadeTransition(Duration.millis(250), dot);
            ft.setFromValue(0.2);
            ft.setToValue(1.0);
            ft.setAutoReverse(true); // Dopo essersi illuminato, torna trasparente
            ft.setCycleCount(2);     // Un ciclo di andata e uno di ritorno
            
            sequenza.getChildren().add(ft);
        }

        // Diciamo all'animazione intera di ripetersi all'infinito
        sequenza.setCycleCount(Animation.INDEFINITE);
        sequenza.play();
    } 
     
     
     @FXML
     private void resign() throws IOException{
         
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Bandiera bianca");
        alert.setHeaderText("stai per abbandonare la partita");
        alert.setContentText("Sei sicuro di volerti arrendere? La vittoria andrà al tuo avversario.");

        alert.getDialogPane().getStylesheets().add(getClass().getResource("/it/guesstheword/StyleSheet.css").toExternalForm());
        
        Optional<ButtonType> result = alert.showAndWait();
        
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            timelineTimer.stop();
            Sessione.getClient().send(new PacchettoRisposta("RESIGN_REQUEST"));
        }
         
     }
     
     @FXML
     private void confirmAnswer(){
         
        List<String> ss = new ArrayList<>();
        for(TextField tf : listaInput){
            ss.add(tf.getText().isEmpty() ? "" : tf.getText());
        }

        try {
            
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Conferma Risposta");
            alert.setHeaderText("Stai per confermare la tua risposta");
            alert.setContentText("Sei sicuro di voler inviare la tua risposta? Una volta inviata non sarà possibile modificarla");

            alert.getDialogPane().getStylesheets().add(getClass().getResource("/it/guesstheword/StyleSheet.css").toExternalForm());

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                
                timelineTimer.stop();   // Fermo lo scorrere del tempo
                ss.add(String.valueOf(durataIniziale-secondiRimanenti));    // Salvo il tempo impiegato ad inviare la risposta
                
                Sessione.getClient().send(new PacchettoRisposta("VALIDATION_REQUEST",ss));  // Mando il messaggio di validazione al server
                
                //Avvio l'animazione dei pallini d'attesa
                avviaAnimazionePallini();
                overlayAttesa.setVisible(true);
                timerPing = new Timeline(new KeyFrame(Duration.seconds(0.5)));
         
                timerPing.setCycleCount(Timeline.INDEFINITE);
                timerPing.play();
                
            }
            
        } catch (IOException ex) {System.out.println("Errore nella convalida delle risposte");}
     }
     
     
     @FXML
     private void back() throws IOException{
        Main.setRoot("playerDashboard");
     }
     
     @FXML
     private void newGame() throws IOException{
         Main.setRoot("loading");
     }
    
}
