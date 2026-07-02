package controller;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.concurrent.Task; 
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.game.Testo;
import model.game.TextEditor;
import model.Main;
import model.connection.Sessione;
import model.utility.Sfida;

public class SettingsController implements Initializable{
    
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
    private TextArea risultatoAnalisiLocale;
    
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
    
    @FXML
    private Button startGame;
    
    private TextEditor te;
    
    private String testoSelezionato = "";
    
    private final int durataPartita = 300;   // Variabile per cambiare la durata della partita in secondi
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        te = new TextEditor();
        te.leggiReport();
        
        // Forzo gli spinner a restituire solo numeri interi per evitare problemi
        if(lunghezza != null) {
            lunghezza.valueProperty().addListener((obs, oldV, newV) -> {lunghezza.setValue(newV.intValue()); 
            calcolaDifficolta();
            });
        }
        if(frequenza != null) {
            frequenza.valueProperty().addListener((obs, oldV, newV) -> {frequenza.setValue(newV.intValue());
            calcolaDifficolta();
            });
        }
        
        // Formato degli spinner, altrimenti non permettono l'inserimento di valori
        if(nParole != null) {
            nParole.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 3, 1));
            nParole.valueProperty().addListener((obs, oldV, newV) -> calcolaDifficolta());
        }
        if(shift != null) {
            shift.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 25, 3));
            shift.valueProperty().addListener((obs, oldV, newV) -> calcolaDifficolta());
        }
        
        calcolaDifficolta();

        List<Testo> titleList = te.getTitle();
        
        // Preparo la lista dei file disponibili e se questi sono già stati analizzati
        aggiornaReportVideo();
        
        // Preparo il combo box per la selezione del testo da analizzare
        comboTesti.setItems(FXCollections.observableArrayList(titleList.stream().map(Testo::getTitolo).collect(Collectors.toList())));

        // Preparo la parte dell'analisi del testo
        comboTesti.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            
            if (newValue != null) {
                
                Testo testoSelezionato = te.getTitle().stream().filter(t -> t.getTitolo().equals(newValue)).findFirst().orElse(null);

                if (testoSelezionato != null) {
                    
                    int id = testoSelezionato.getTxtId();
                    
                    areaTesto.setText(te.caricaTesto(testoSelezionato.getTitolo()));
                    
                    // Nota: chiamo isAnalized() o isIsAnalized() a seconda di come l'hai dichiarato nel tuo Testo.java
                    if (!testoSelezionato.isAnalized()) {
                        risultatoAnalisi.setText("Il testo selezionato non è ancora stato analizzato.\nPremi il tasto 'Analizza' per generare le statistiche.");
                    } else {
                        risultatoAnalisi.clear();
                        
                        te.caricaAnalisi(id);
                        Map<String, Integer> mappaRisultati = te.getFrequency();

                        if (mappaRisultati != null && !mappaRisultati.isEmpty()) {
                            formattazioneRisultatoanalisi(mappaRisultati,risultatoAnalisi);
                        } else { 
                            risultatoAnalisi.setText("Errore: Impossibile caricare l'analisi per questo file (File .dat mancante o corrotto)."); 
                        }
                    }
                }
            }
        });
        
        
        startGame.disableProperty().bind(paroleScelte.textProperty().isEmpty().and(areaTesto.selectedTextProperty().isEmpty()));
        
        
    }
    
    
    // ------------- Metodi di utilità ------------------
    
    private void aggiornaReportVideo() {
        List<String> listaFormattata = te.getTitle().stream().map(
                a -> a.isAnalized() ? a.getTitolo() + " (Analizzato)" : a.getTitolo()).collect(Collectors.toList());
            
        fileAnalizzati.setItems(FXCollections.observableArrayList(listaFormattata));
    }

    private void formattazioneRisultatoanalisi(Map<String, Integer> mappa, TextArea txa) {
        
        if (mappa.isEmpty()) {
            txa.setText("Il testo non contiene parole valide.");
            return;
        }
        
        int paroleTotali = mappa.values().stream().mapToInt(Integer::intValue).sum();   // Somma parole trovate

        StringBuilder sb = new StringBuilder();
        sb.append("Parole totali: ").append(paroleTotali).append("\n");
        sb.append("Frequenza:\n");

        // Ordinamento in ordine decrescente di occorrenze
        mappa.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) 
                .forEach(e -> sb.append("- ").append(e.getKey()).append(": ").append(e.getValue()).append("\n"));

        txa.setText(sb.toString());    // Collegamento alla textArea
    }
    
    private void calcolaDifficolta() {
        
        int numParoleVal = nParole.getValue(); // Range: 1 - 5
        int freqVal = (int) frequenza.getValue(); // Range: 1 - 3 (1=Rara, 3=Comune)
        int shiftVal = shift.getValue(); // Range: 1 - 25
        int lungVal = (int) lunghezza.getValue(); // Range: 4 - 15

        // Normalizzazione dei valori su una scala da 0.0 (Più Facile) a 1.0 (Più Difficile)
        double normN = (numParoleVal - 1.0) / 4.0;
        
        // Per la frequenza la logica è invertita: 1 (Rara) dà punteggio 1.0 (massima difficoltà), 3 (Comune) dà 0.0
        double normF = (3.0 - freqVal) / 2.0; 
        
        double normS = (shiftVal - 1.0) / 24.0;
        double normL = (lungVal - 4.0) / 11.0;

        // Moltiplicazione per i pesi decrescenti
        double punteggio = (normN * 0.31) + (normF * 0.27) + (normS * 0.23) + (normL * 0.19);

        // Divisione matematica in 3 scaglioni esatti (1/3)
        if (punteggio < 0.333) {
            difficoltà.setText("FACILE");
            difficoltà.setStyle("-fx-text-fill: #27ae60;"); // Colore Verde
        } else if (punteggio < 0.666) {
            difficoltà.setText("MEDIO");
            difficoltà.setStyle("-fx-text-fill: #e67e22;"); // Colore Arancione
        } else {
            difficoltà.setText("DIFFICILE");
            difficoltà.setStyle("-fx-text-fill: #c0392b;"); // Colore Rosso
        }
    }
    
    
    
    
    
    // ------------- Metodi dei Bottoni Grafici --------------

    @FXML
    private void avviaPartita() throws IOException {
        String testoDaUsare = areaTesto.getSelectedText();
        
        // 2. Se è vuoto (l'utente ha perso la selezione), uso quello salvato
        if (testoDaUsare == null || testoDaUsare.trim().isEmpty()) {
            testoDaUsare = testoSelezionato;
        }
        
        // 3. Se è ancora vuoto, prendo tutto il testo di default
        if (testoDaUsare == null || testoDaUsare.trim().isEmpty()) {
            testoDaUsare = areaTesto.getText();
        }
        
        te.setSelectedText(testoDaUsare);
        
        Testo t = te.getTitle().stream().filter(t0 -> t0.getTitolo().equals(comboTesti.getValue())).findFirst().orElse(null);
        
        Sessione.setCurrentGame(new Sfida(t.getTxtId(),durataPartita,0,0,0,0,"","",""));
        
        // La cifratura salva anche il testo modificato in maniera statica
        te.cifraTesto(shift.getValue(), paroleScelte.getText().split(",\\s*"));
        Main.setRoot("loading");
    }
     
    @FXML
    private void generaParole() {
        
        // 1. Prendo l'Analisi GLOBALE (Filtro Qualità)
        Map<String, Integer> mappaGlobale = te.getFrequency();
        if (mappaGlobale == null || mappaGlobale.isEmpty()) {
            risultatoAnalisiLocale.setText("Seleziona prima un testo!");
            paroleScelte.clear();
            return;
        }

        // 2. Prendo l'estratto selezionato col MOUSE
        String estratto = areaTesto.getSelectedText();
        if (estratto == null || estratto.trim().isEmpty()) {
            estratto = areaTesto.getText(); // Fallback se non evidenzia nulla
        }
        
        this.testoSelezionato = estratto;

        int targetLunghezza = (int) lunghezza.getValue();
        int livelloFrequenza = (int) frequenza.getValue();
        int numeroParoleVolute = nParole.getValue();

        // 3. Calcolo le frequenze LOCALI (sull'estratto) ma accetto solo parole approvate dal Globale
        Map<String, Integer> frequenzeLocali = Arrays.stream(estratto.toLowerCase().split("\\W+"))
                .filter(w -> !w.isEmpty())
                .filter(w -> Math.abs(w.length() - targetLunghezza) <= 1)// Scarto le parole troppo corte o troppo lunghe (+/- 1 di tolleranza)
                .filter(w -> mappaGlobale.containsKey(w) && mappaGlobale.get(w) >= 3) // La parola deve esistere nell'analisi globale e comparire almeno 3 volte in tutto il testo (Filtro per parole anomale o proprie)
                .collect(Collectors.groupingBy(w -> w, Collectors.summingInt(w -> 1)));

        // Controllo se la Mappa è vuota, non genero nessuna parola
        if (frequenzeLocali.isEmpty()) {
            risultatoAnalisiLocale.setText("L'estratto non ha parole valide per questi filtri.");
            paroleScelte.clear();
            return;
        }
        // Mappa temporanea per la verifica della lunghezza del testo selezionato
        Map<String, Integer> temp = Arrays.stream(estratto.toLowerCase().split("\\W+"))
                .filter(w -> !w.isEmpty())
                .collect(Collectors.groupingBy(w -> w, Collectors.summingInt(w -> 1)));
        
        if(temp.values().stream().mapToInt(Integer::intValue).sum()<60){    // Verifico che il testo selezionato abbia almeno 60 parole
            risultatoAnalisiLocale.setText("L'estratto selezionato è troppo piccolo: Selezionare almeno 60 parole.");
            paroleScelte.clear();
            return;
        }
        temp.clear();
        
        formattazioneRisultatoanalisi(frequenzeLocali, risultatoAnalisiLocale);

        // 4. Ordino le parole per DIFFICOLTÀ LOCALE (Verifico i valori delle singole parole della Mappa)
        List<Map.Entry<String, Integer>> paroleOrdinate = frequenzeLocali.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());

        // 5. Calcolo le "Fette" della difficoltà basandomi sulla classifica locale
        int totaleParole = paroleOrdinate.size();
        int dimensioneFetta = Math.max(1, totaleParole / 3);
        List<String> candidati;

        if (totaleParole < 3) {
            candidati = paroleOrdinate.stream().map(Map.Entry::getKey).collect(Collectors.toList());
        } else {
            int indiceInizio = 0;
            int indiceFine = totaleParole;
            
            switch (livelloFrequenza) {
                case 1: // Bottom 33% locale
                    indiceInizio = dimensioneFetta * 2;
                    indiceFine = totaleParole;
                    break;
                case 2: // Mid 33% locale
                    indiceInizio = dimensioneFetta;
                    indiceFine = dimensioneFetta * 2;
                    break;
                case 3: // Top 33% locale
                    indiceInizio = 0;
                    indiceFine = dimensioneFetta;
                    break;
                default:
                    throw new AssertionError();
            }
            
            candidati = paroleOrdinate.subList(indiceInizio, indiceFine).stream()
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }

        // 6. Estraggo le parole casualmente dalla fetta selezionata
        Collections.shuffle(candidati);
        int limite = Math.min(numeroParoleVolute, candidati.size());
        List<String> paroleFinali = candidati.subList(0, limite);
        
        // 7. Stampo a video
        String risultatoFinale = String.join(", ", paroleFinali).toUpperCase();
        paroleScelte.setText(risultatoFinale);
        
        if (paroleFinali.size() < numeroParoleVolute) {
            paroleScelte.setText(risultatoFinale + " (Trovate " + paroleFinali.size() + "/" + numeroParoleVolute + ")");
        }
    }

    private void aggiornaAnalisiVideo(int id){
        risultatoAnalisi.clear();   // Pulisco l'area dell'Analisi
        
        te.caricaAnalisi(id);
        Map<String, Integer> mappaRisultati = te.getFrequency();
        
        if (mappaRisultati != null) {
            formattazioneRisultatoanalisi(mappaRisultati,risultatoAnalisi);
        } else { risultatoAnalisi.setText("Errore: Impossibile caricare l'analisi per questo file."); }
        
    }
    
    @FXML
    private void analisiTesto() { 
        
        String titolo = comboTesti.getValue();
        Testo t = te.getTitle().stream().filter(t0 -> t0.getTitolo().equals(titolo)).findFirst().orElse(null);
        
        if(titolo != null && t != null && !t.isAnalized()){
            
            analizzaTesto.setDisable(true); // Disabilito il bottone per evitare che l'admin lo spammi
            risultatoAnalisi.setText("Analisi in corso... L'operazione potrebbe richiedere alcuni secondi, attendere prego.");
            
            // Definizione della task del thread
            Task<Boolean> taskAnalisi = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return te.analizzaTesto(t.getTxtId());
                }
            };
            
            // Caso di Successo
            taskAnalisi.setOnSucceeded(event -> {
                boolean successo = taskAnalisi.getValue();
                
                if (successo) {
                    aggiornaReportVideo();  
                    aggiornaAnalisiVideo(t.getTxtId()); 
                } else {
                    risultatoAnalisi.setText("Si è verificato un errore durante l'analisi del testo.");
                }
                
                analizzaTesto.setDisable(false); 
            });
            
            // Caso d'errore
            taskAnalisi.setOnFailed(event -> {
                risultatoAnalisi.setText("Errore Critico: L'analisi si è interrotta bruscamente.");
                analizzaTesto.setDisable(false); 
                taskAnalisi.getException().printStackTrace(); 
            });
            
            // Creazione ed avvio del thread
            Thread threadBackground = new Thread(taskAnalisi);
            threadBackground.setDaemon(true); 
            threadBackground.start();
            
        }
    }
    
    @FXML
    private void backDashboard() throws IOException {
        Main.setRoot("adminDashboard");
    }
}