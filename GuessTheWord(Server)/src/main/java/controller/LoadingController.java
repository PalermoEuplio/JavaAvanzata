package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import model.Main;

public class LoadingController implements Initializable{
    
    @FXML private Circle dot1;
    @FXML private Circle dot2;
    @FXML private Circle dot3;
    @FXML private Circle dot4;
    
    
    
    @Override
    public void initialize(URL location, ResourceBundle resources){
        avviaAnimazionePallini();
        
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
    }
    
}
