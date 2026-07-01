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

public class LoadingController implements Initializable{
    
    @FXML private Circle dot1;
    @FXML private Circle dot2;
    @FXML private Circle dot3;
    @FXML private Circle dot4;
    @FXML private Label status;
    
    private Timeline monitor;
    
    @Override
    public void initialize(URL location, ResourceBundle resources){
        avviaAnimazionePallini();
        
        Sessione.setClientAttesa(new CopyOnWriteArrayList<>());
        
        Sessione.setOnGameReady(() -> {
            if (monitor != null) monitor.stop();
            try {
                Main.setRoot("gameWait");
            } catch (IOException ex) { System.out.println("Pagina non trovata"); }
        });
        
        // Monitoraggio del numero di utenti pronti a giocare
        monitor = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            if (Sessione.getClientInAttesa() == null) 
                return;
            int n = Sessione.getClientInAttesa().size();
            status.setText("Giocatori Connessi: " + n + "/2");
        }));
        
        
        
        
        monitor.setCycleCount(Timeline.INDEFINITE);
        monitor.play();
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
    private void back() throws IOException{
        Main.setRoot("gameSettings");
        new TextEditor().setModifiedText("");
        Sessione.setClientAttesa(null);
    }
    
}
