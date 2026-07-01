package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;
import model.Main;
import model.connection.Sessione;

public class GameWaitController implements Initializable {
    
    
    
    @FXML
    private Label timer;
    
    @FXML
    private ProgressBar timerBar;
    
    @FXML
    private Label playerCount;
    
    
    
    private Timeline timelineTimer;
    private int secondiRimanenti;
    private int durataIniziale;
    
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        if (Sessione.getCurrentGame() != null) {
            avviaTimer((int) Sessione.getCurrentGame().getDurata() * 60);
        } else {
            // Attendi che il server valorizzi la sessione
            Timeline attesa = new Timeline(new KeyFrame(Duration.millis(200), e -> {
                if (Sessione.getCurrentGame() != null) {
                    avviaTimer((int) Sessione.getCurrentGame().getDurata() * 60);
                    ((Timeline) e.getSource()).stop();
                }
            }));
            attesa.setCycleCount(Timeline.INDEFINITE);
            attesa.play();
        }
        
        
        Sessione.setOnGameReady(() -> {
            if (timelineTimer != null) timelineTimer.stop();
            try {
                Main.setRoot("adminDashboard");
            } catch (IOException ex) { System.out.println("Pagina non trovata"); }
        });
        
        
        
        
    }
    
    
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
                try {
                    Main.setRoot("adminDashboard");
                } catch (IOException ex) {
                    Logger.getLogger(GameWaitController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }));
        
        timelineTimer.setCycleCount(Timeline.INDEFINITE);
        timelineTimer.play();
    } 
    
    
    
    
    
    
    
    
}
