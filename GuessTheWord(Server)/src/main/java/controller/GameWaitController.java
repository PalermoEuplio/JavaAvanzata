package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;
import model.Main;
import model.connection.Sessione;

/**
 * Classe che gestisce il comportamento della pagina d'attesa fine Partita.
 */
public class GameWaitController implements Initializable {
    
    // Collegamenti agli elementi della pagina
    @FXML
    private Label timer;
    
    @FXML
    private ProgressBar timerBar;
    
    @FXML
    private Label playerCount;
    
    
    private Timeline timelineTimer;
    private int secondiRimanenti;
    private int durataIniziale;
    
    
    /**
     * Inizializza il controller.
     * 
     * @param location L'URL di location.
     * @param resources Le risorse.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        if (Sessione.getCurrentGame() != null) {    // Se currentGame è già stato allocato
            avviaTimer((int) Sessione.getCurrentGame().getDurata());    // Avvio il timer a video
        } else {
            // Timeline che si mette in attesa della creazione effettiva del gioco prima di far partire il timer
            Timeline attesa = new Timeline(new KeyFrame(Duration.millis(200), e -> {
                
                if (Sessione.getCurrentGame() != null) {
                    avviaTimer((int) Sessione.getCurrentGame().getDurata());    // Avvio il timer a video
                    ((Timeline) e.getSource()).stop();
                }
                
            }));
            
            attesa.setCycleCount(Timeline.INDEFINITE);  // Attesa indefinita
            attesa.play();  // Faccio partire il timer
        }
        
        // Modifico il consimer per specificarne il comportamento
        // In questo caso lo specializzo per fargli cambiare pagina quando la partita termina
        Sessione.setOnAnswerReceived((x) -> {   
            
            if (playerCount != null) {
                playerCount.setText(String.valueOf(x)); // Specifico a video il numero di giocatori che ha già dato la risposta
            }
            
            if (x == 2) {   // Se tutti e due hanno risposto la sfida è terminata
                if (timelineTimer != null) timelineTimer.stop();    // Fermo il timer
                try {
                    Main.setRoot("adminDashboard"); // Ritorno alla dashboard
                } catch (IOException ex) { 
                    System.out.println("Pagina non trovata"); 
                }
            }
            
        });
        
    }
    
    // ------------- Metodi di utilità ------------------
    
    /**
     * Metodo che permette di avviare il timer in base alla dua durata in secondi.
     * In particolare viene messo a video nel formato min:sec.
     * 
     * @param durataSecondi La durata in secondi.
     */
    private void avviaTimer(int durataSecondi) {
        
        this.durataIniziale = durataSecondi;     // Mi salvo la durata iniziale
        this.secondiRimanenti = durataSecondi;  // Salvo i secondi rimanenti
        
        if (timelineTimer != null) timelineTimer.stop(); // Ferma eventuali timer precedenti
        
        timelineTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {   // Comportamneto del timer
            
            secondiRimanenti--; // Abbassa di 1 i secondi rimanenti
            
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
                timelineTimer.stop();   // Fermo il timer
                System.out.println("Tempo scaduto!");
                try {
                    Main.setRoot("adminDashboard"); // Vengo reindirizzato alla dashboard
                } catch (IOException ex) {System.err.println("Impossibile trovare la pagina: "+ex);}
            }
        }));
        
        timelineTimer.setCycleCount(Timeline.INDEFINITE);   // Timer perenne
        timelineTimer.play();   // Avvio del timer
    }
}
