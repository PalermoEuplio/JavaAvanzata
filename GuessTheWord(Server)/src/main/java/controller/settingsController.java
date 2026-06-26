/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.Testo;
import model.TextEditor;
import model.Main;
/**
 *
 * @author euppa
 */
public class settingsController implements Initializable{
    
    
    // Collegamenti agli elementi della pagina
    @FXML
    private ComboBox<String> comboTesti;
    
    @FXML
    private Button analizzaTesto;
    
    @FXML 
    private TextArea areaTesto;
    
    @FXML
    private ListView<String> fileAnalizzati;
    
    @FXML
    private TextArea risultatoAnalisi;
    
    @FXML
    private Slider frequenza;
    
    @FXML
    private Slider lunghezza;
    
    @FXML 
    private Spinner<Integer> nParole;
    
    @FXML
    private Spinner<Integer> shift;
    
    @FXML
    private Label difficoltà;
    
    @FXML
    private TextField paroleScelte;
    
    
    private TextEditor te;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        te = new TextEditor();
        
        te.leggiReport();
        
        
        List<Testo> titleList = te.getTitle();
        
        // Preparo la lista dei file disponibili e se questi sono già stati analizzati
        aggiornaReportVideo();
        
        // Preparo il combo box per la selezione del testo da analizzare
        comboTesti.setItems(FXCollections.observableArrayList(titleList.stream().map(Testo::getTitolo).collect(Collectors.toList())));

        
        // Preparo la parte dell'analisi del testo e carico il testo in base a quale file è stato selezionato nel comboBox
        comboTesti.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            
            if (newValue != null) {
                
                Testo testoSelezionato = te.getTitle().stream().filter(t -> t.getTitolo().equals(newValue)).findFirst().orElse(null);

                if (testoSelezionato != null) {
                    
                    int id = testoSelezionato.getTxtId();
                    // Carico il testo del relativo file nel textArea
                    areaTesto.clear();  // Pulisco l'area di Testo
                    System.out.println("TESTO:::::"+areaTesto.getText());
                    areaTesto.setText(te.caricaTesto(testoSelezionato.getTitolo()));
                    
                    if (!testoSelezionato.isAnalized()) {
                        
                        risultatoAnalisi.setText("Il testo selezionato non è ancora stato analizzato.\nPremi il tasto 'Analizza' per generare le statistiche.");
                        
                    } else aggiornaAnalisiVideo(id);   // Aggiorno il contenuto del textArea
                        
                    
                }
            }
        });
        
        
        
    }
    
    
    // Metodo per l'aggiornamento grafico della lista contenente il report dei testi
    private void aggiornaReportVideo() {
        List<String> listaFormattata = te.getTitle().stream().map(
                a -> a.isAnalized() ? a.getTitolo() + " (Analizzato)" : a.getTitolo()).collect(Collectors.toList());
            
        fileAnalizzati.setItems(FXCollections.observableArrayList(listaFormattata));
    }
    
    
    
    
    private void aggiornaAnalisiVideo(int id){
        risultatoAnalisi.clear();   // Pulisco l'area dell'Analisi
        
        te.caricaAnalisi(id);
        Map<String, Integer> mappaRisultati = te.getFrequency();
        
        if (mappaRisultati != null) {
            formattazioneRisultatoanalisi(mappaRisultati);
        } else { risultatoAnalisi.setText("Errore: Impossibile caricare l'analisi per questo file."); }
        
    }
    
    
    
    
    private void formattazioneRisultatoanalisi(Map<String, Integer> mappa) {
        
        if (mappa.isEmpty()) {
            risultatoAnalisi.setText("Il testo non contiene parole valide.");
            return;
        }
        
        int paroleTotali = mappa.values().stream().mapToInt(Integer::intValue).sum();   // Somma parole trovate

        StringBuilder sb = new StringBuilder();
        sb.append("Parole totali: ").append(paroleTotali).append("\n");
        sb.append("Frequenza:\n");

        // Ordinamento in ordine crescente
        mappa.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) 
                .forEach(e -> sb.append("- ").append(e.getKey()).append(": ").append(e.getValue()).append("\n"));

        risultatoAnalisi.setText(sb.toString());    // Collegamento alla textArea
    }
    
    
    
    
    
    
    @FXML
    private void avviaPartita() throws IOException {
         
    }
     
    @FXML
    private void generaParole() throws IOException {
          
    }
    
    @FXML
    private void analisiTesto() throws IOException {
        
        String titolo = comboTesti.getValue();
        Testo t = te.getTitle().stream().filter(t0 -> t0.getTitolo().equals(titolo)).findFirst().orElse(null);
        
        if(titolo!=null && t.isAnalized()==false){
            
            te.analizzaTesto(t.getTxtId());    // Analizzo il testo e aggiorno la lista
            aggiornaReportVideo();  // Aggiorno la lista a video
            aggiornaAnalisiVideo(t.getTxtId()); // Aggiorno l'area d'analisi a video
            
        }
    }
    
    @FXML
    private void backDashboard() throws IOException {
        Main.setRoot("adminDashboard");
    }
    
}
