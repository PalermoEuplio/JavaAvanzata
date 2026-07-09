package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import model.Main;
import model.connection.PacchettoRisposta;
import model.connection.Sessione;
import model.utility.Testo;

/**
 * Controller per la pagina di caricamento.
 */
public class LoadingController implements Initializable{
    
    @FXML private Circle dot1;
    @FXML private Circle dot2;
    @FXML private Circle dot3;
    @FXML private Circle dot4;
    @FXML private Label status;
    
    private Timeline timerPing;
    private SequentialTransition sequenza;
    
    /**
     * Inizializza il controller.
     * 
     * @param location L'URL di location.
     * @param resources Le risorse.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources){
        avviaAnimazionePallini();
      
        Sessione.setOnServerResponse(this::gestisciRispostaServer);
        
        timerPing = new Timeline(new KeyFrame(Duration.seconds(0.5), event -> {
            // Inviamo il comando PING al server tramite la sessione
            try {
                Sessione.getClient().send(new PacchettoRisposta("GAME_PING"));
            } catch (Exception e) {}
        }));

        // Impostiamo la ripetizione infinita
        timerPing.setCycleCount(Timeline.INDEFINITE);
        timerPing.play();

        
    }
    
    /**
     * Gestisce la risposta ricevuta dal server.
     * 
     * @param pacchetto Il pacchetto di risposta.
     */
    private void gestisciRispostaServer(PacchettoRisposta pacchetto){
        
        switch(pacchetto.getComando()){
            case "START_GAME":
                if (timerPing != null)
                    timerPing.stop();
                if (sequenza != null)
                    sequenza.stop();
                try {
                    System.out.println("Inizio partita");
                    
                    Testo.setGameText((String) pacchetto.getPayload());
                    Main.setRoot("game");
                    
                } catch (Exception e) {e.printStackTrace();}
                break;
            case "NO_ADMIN":    
                status.setText("In attesa di un amministratore...");
                break;
            case "NO_OPPONENT":
                status.setText("Ricerca avversario...");
                break;
            default: System.out.println("Errore: Impossibile iniziare la partita");
        }
        
    }
        
    
    
    /**
     * Avvia l'animazione dei pallini di caricamento.
     */
    private void avviaAnimazionePallini() {
        Circle[] dots = {dot1, dot2, dot3, dot4};
        sequenza = new SequentialTransition();

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
    
    
    
    /**
     * Comportamento del pulsante per tornare indietro.
     * 
     * @throws IOException In caso di errori I/O.
     */
    @FXML
    private void back() throws IOException{
        if (timerPing != null)
            timerPing.stop();
        if (sequenza != null)
            sequenza.stop();
        Main.setRoot("playerDashboard");
    }
    
}
