package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
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
import model.connection.Sessione;
import model.game.TextEditor;


/**
 * Classe che specifica il comportamento della pagina d'attesa fra la selezione delle impostazioni e l'inizio della partita.
 */
public class LoadingController implements Initializable{
    
    // Collegamenti agli elementi della pagina
    @FXML private Circle dot1;
    @FXML private Circle dot2;
    @FXML private Circle dot3;
    @FXML private Circle dot4;
    @FXML private Label status;
    
    private Timeline monitor;   // Timeline per l'animazione d'attesa
    
    /**
     * Inizializza il controller.
     * 
     * @param location L'URL di location.
     * @param resources Le risorse.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources){
        
        avviaAnimazionePallini();   // Richiamo l'avvio dell'animazione d'attesa
        
        Sessione.setClientAttesa(new CopyOnWriteArrayList<>()); // Inizializzo la lista che conterrà le socket dei player collegati alla partita
        
        Sessione.setOnGameReady(() -> { // Specifico il comportamento dell'oggetto Runnable che permetterà di far cambiare l'interfaccia all'Admin quando il server rileva i 2 giocatori 
            
            if (monitor != null) monitor.stop();    // Fermo la timeline
            try {
                Main.setRoot("gameWait");   // Mi sposto alla pagina d'attesa fine gioco
            } catch (IOException ex) { System.out.println("Pagina non trovata"); }
            
        });
        
        // Inizializzo la timeline
        monitor = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            
            if (Sessione.getClientInAttesa() == null) // Se non è stata allocata la lista d'attesa non far nulla
                return;
            
            // Stampo a video il numero di giocatori attualmente connessi per la partita
            int n = Sessione.getClientInAttesa().size();
            status.setText("Giocatori Connessi: " + n + "/2");
            
        }));
        
        monitor.setCycleCount(Timeline.INDEFINITE); // La timeline si ripete all'infinito fino a quando non cambio la pagina
        monitor.play(); // Faccio partire la timeline
    }
    
    
    // ------------- Metodi di utilità ------------------
    
    /**
     * Metodo che specifica come deve comportarsi l'animazione.
     */
    private void avviaAnimazionePallini() {
        
        Circle[] dots = {dot1, dot2, dot3, dot4};   // Seleziono i 4 pallini a video
        SequentialTransition sequenza = new SequentialTransition(); // Creo una nuova transizione sequenziale

        // Creiamo una transizione (Scomparsa/Comparsa) per ogni pallino
        for (Circle dot : dots) {
            dot.setOpacity(0.2); // Partono tutti semitrasparenti
            
            FadeTransition ft = new FadeTransition(Duration.millis(250), dot);
            ft.setFromValue(0.2);
            ft.setToValue(1.0);
            ft.setAutoReverse(true); // Dopo essersi illuminato, torna trasparente
            ft.setCycleCount(2);     // Un ciclo di andata e uno di ritorno
            
            sequenza.getChildren().add(ft);
        }

        // L'animazione si ripete all'infinito
        sequenza.setCycleCount(Animation.INDEFINITE);
        sequenza.play();    // Faccio partire l'animazione
        
    }
    
    // ------------- Metodi per l'interfaccia grafica ------------------
    
    /**
     * Comportamento del pulsante per tornare indietro.
     * 
     * @throws IOException In caso di errori I/O.
     */
    @FXML
    private void back() throws IOException{
        Main.setRoot("gameSettings");   // Ritorno alla pagina d'impostazioni partita
        TextEditor.setModifiedText("");   // Cancello il testo preparato per questa partita
        Sessione.setClientAttesa(null); // Dealloco la lista d'attesa giocatori per la partita
    }
    
}
