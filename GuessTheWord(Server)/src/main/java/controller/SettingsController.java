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
import model.utility.Esito;
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
    
    
    
    private TextEditor te;  // Collegamento all'editor di testo per effettuare le diverse operazioni sul testo
    
    private String testoSelezionato = "";   // Variabile Contenente il testo selezionato 
    
    private final int durataPartita = 300;   // Variabile per cambiare la durata della partita in secondi
    
    private final int nParoleMinimo = 60;   // Variabile per cambiare il numero minimo di parole del testo per la partita per evitare l'utilizzo di testi troppo corti
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        te = new TextEditor();  // Inizializzo l'editor di testo che dovrà essere usato in tutta la pagina
        te.leggiReport();   // Leggo il report dei testi già analizzati e lo salvo all'interno del TextEditor
        
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
        
        calcolaDifficolta();    // Richiamo subito la funzione che calcola la difficoltà per modificare la label difficoltà

        List<Testo> titleList = te.getTitle();  // Mi salvo la lista dei titoli disponibili
        
        aggiornaReportVideo();  // Preparo la lista dei file disponibili e se questi sono già stati analizzati
        
        // Preparo il combo box per la selezione del testo da analizzare
        comboTesti.setItems(FXCollections.observableArrayList(titleList.stream().map(Testo::getTitolo).collect(Collectors.toList())));

        // Preparo la parte dell'analisi del testo mediante un Listner che aggiorna in tempo reale il contenuto dell'area di testo
        comboTesti.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            
            if (newValue != null) {
                
                // Ricavo l'intero oggetto Testo a partire dal nome selezionato nel combobox
                Testo testoSelezionato = titleList.stream().filter(t -> t.getTitolo().equals(newValue)).findFirst().orElse(null);

                if (testoSelezionato != null) {
                    
                    areaTesto.setText(te.caricaTesto(testoSelezionato.getTitolo()));    // Aggiungo il testo
                    
                    if (!testoSelezionato.isAnalized()) {   // Verifico se il testo selezionato sia già stato analizzato o meno
                        risultatoAnalisi.setText("Il testo selezionato non è ancora stato analizzato.\nPremi il tasto 'Analizza' per generare le statistiche.");
                    } else {
                        // Nel caso in cui il testo fosse già analizzato, procedo col caricamento a video dell'analisi
                        risultatoAnalisi.clear();
                        
                        te.caricaAnalisi(testoSelezionato.getTxtId());  // Carico l'analisi a partire dall'id del testo
                        Map<String, Integer> mappaRisultati = te.getFrequency();    // Ricavo la mappa con le statistiche dell'analisi

                        if (mappaRisultati != null && !mappaRisultati.isEmpty()) {
                            formattazioneRisultatoanalisi(mappaRisultati,risultatoAnalisi); // Richiamo il metodo che permette di mostrare in maniera schematizzata i risultati dell'analisi
                        } else { 
                            risultatoAnalisi.setText("Errore: Impossibile caricare l'analisi per questo file (File .dat mancante o corrotto).");    // Errore generico
                        }
                    }
                }
            }
        });
        
        //  Disabilito il pulsante di inizio partita se non sono ancora state generate delle parole e se il testo selezionato sia vuoto
        startGame.disableProperty().bind(paroleScelte.textProperty().isEmpty().and(areaTesto.selectedTextProperty().isEmpty()));    
        
    }
    
    
    // ------------- Metodi di utilità ------------------
    
    // Metodo specializzato nel caricamento della lista dei titoli disponibili e del loro stato attuale (Analizzato e non)
    private void aggiornaReportVideo() {
        
        // Prima di caricare a video la lista inserisco analizzato di fianco ai titoli che sono stati analizzati in precedenza
        List<String> listaFormattata = te.getTitle().stream().map(
                a -> a.isAnalized() ? a.getTitolo() + " (Analizzato)" : a.getTitolo()).collect(Collectors.toList());
            
        fileAnalizzati.setItems(FXCollections.observableArrayList(listaFormattata));
    }

    
    // Metodo specializzato nella formattazzione di una mappa all'interno di un textArea
    private void formattazioneRisultatoanalisi(Map<String, Integer> mappa, TextArea txa) {
        
        if (mappa.isEmpty()) {  // Iniziale controllo sulla Mappa
            txa.setText("Il testo non contiene parole valide.");
            return;
        }
        
        int paroleTotali = mappa.values().stream().mapToInt(Integer::intValue).sum();   // Sommo le parole trovate

        StringBuilder sb = new StringBuilder(); // StringBuilder per la costruzione dell'output sul textArea
        sb.append("Parole totali: ").append(paroleTotali).append("\n");
        sb.append("Frequenza:\n");

        // Ordinamento in ordine decrescente di occorrenze
        mappa.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) 
                .forEach(e -> sb.append("- ").append(e.getKey()).append(": ").append(e.getValue()).append("\n"));

        txa.setText(sb.toString());    // Collegamento alla textArea
    }
    
    // Metodo che si occupa del calcolo della difficoltà in base ai valori degli slider e spinner messi a video
    // In particolare viene attribuito un peso ad ogni impostazione per cambiare la label difficoltà in Facile,Media,Difficile
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
            difficoltà.setText("MEDIA");
            difficoltà.setStyle("-fx-text-fill: #e67e22;"); // Colore Arancione
        } else {
            difficoltà.setText("DIFFICILE");
            difficoltà.setStyle("-fx-text-fill: #c0392b;"); // Colore Rosso
        }
    }
    
    
    
    
    
    // ------------- Metodi per l'interfaccia grafica ------------------

    // Comportamento del pulsante per l'avvio della partita
    @FXML
    private void avviaPartita() throws IOException {
        
        String testoDaUsare = areaTesto.getSelectedText();  // Recupero il testo da utilizzare nella gara
        
        // Se è vuoto (l'utente ha perso la selezione), uso quello salvato in precedenza
        if (testoDaUsare == null || testoDaUsare.trim().isEmpty()) {
            testoDaUsare = testoSelezionato;
        }
        
        // Se l'Admin non ha selezionato del testo, prendo tutto il testo di default
        if (testoDaUsare == null || testoDaUsare.trim().isEmpty()) {
            testoDaUsare = areaTesto.getText();
        }
        
        te.setSelectedText(testoDaUsare);   // Salvo il testo da utilizzare per la gara
        
        // Recupero l'istanza di Testo dal titolo, per ricavarne il relativo id
        Testo t = te.getTitle().stream().filter(t0 -> t0.getTitolo().equals(comboTesti.getValue())).findFirst().orElse(null);
        
        // Inizializzo la variabile sfidaCorrente con solo l'id del testo da usare e la durata della partita
        Sessione.setCurrentGame(new Sfida(t.getTxtId(),durataPartita,0,0,0,0,"",Esito.None,""));
        Sessione.getCurrentGame().setTitoloTesto(t.getTitolo());
        
        // Richiamo il metodo per la cifratura del testo. Questo metodo salva anche le parole selezionate
        te.cifraTesto(shift.getValue(), paroleScelte.getText().split(",\\s*"));
        Main.setRoot("loading");    // Mi sposto alla pagina d'attesa
    }
     
    // Comportamento del pulsante per la generazione delle parole
    // In particolare questo metodo si occupa della generazione randomica delle parole da poter usare per la partita, insieme a delle statistiche locali
    // simili a quelle salvate nel report ma inerenti al solo testo selezionato. In questo modo è più facile per l'Admin tenere traccia di quante e quali
    // parole utili alla partita sono state selezionate
    @FXML
    private void generaParole() {
        
        // Prendo come base l'analisi globale, che ha già un primo filtro sulle stopwords
        Map<String, Integer> mappaGlobale = te.getFrequency();
        if (mappaGlobale == null || mappaGlobale.isEmpty()) {
            risultatoAnalisiLocale.setText("Seleziona prima un testo!");
            paroleScelte.clear();
            return;
        }

        // Prendo come testo di riferimento la sola sezione selezionata col mouse (oppure, nel caso fosse nullo, tutto il testo)
        String estratto = areaTesto.getSelectedText();
        if (estratto == null || estratto.trim().isEmpty()) {
            estratto = areaTesto.getText(); // Fallback se non evidenzia nulla
        }
        
        this.testoSelezionato = estratto;   // Salvo quale testo è stato selezionato

        // Prendo i parametri dalle impostazioni grafiche
        int targetLunghezza = (int) lunghezza.getValue();
        int livelloFrequenza = (int) frequenza.getValue();
        int numeroParoleVolute = nParole.getValue();

        // Calcolo le frequenze locali sulla base di quelle globali
        Map<String, Integer> frequenzeLocali = Arrays.stream(estratto.toLowerCase().split("\\W+"))
                .filter(w -> !w.isEmpty())
                .filter(w -> Math.abs(w.length() - targetLunghezza) <= 1)// Scarto le parole troppo corte o troppo lunghe (+/- 1 di tolleranza)
                .filter(w -> mappaGlobale.containsKey(w) && mappaGlobale.get(w) >= 3) // La parola deve esistere nell'analisi globale e comparire almeno 3 volte in tutto il testo (Filtro per parole anomale o proprie)
                .collect(Collectors.groupingBy(w -> w, Collectors.summingInt(w -> 1)));

        // Controllo sulla Mappa, se è vuota non genero nessuna parola
        if (frequenzeLocali.isEmpty()) {
            risultatoAnalisiLocale.setText("L'estratto non ha parole valide per questi filtri.");
            paroleScelte.clear();
            return;
        }
        
        // Mappa temporanea per la verifica della lunghezza del testo selezionato
        Map<String, Integer> temp = Arrays.stream(estratto.toLowerCase().split("\\W+"))
                .filter(w -> !w.isEmpty())
                .collect(Collectors.groupingBy(w -> w, Collectors.summingInt(w -> 1)));
        
        if(temp.values().stream().mapToInt(Integer::intValue).sum()<nParoleMinimo){    // Verifico che il testo selezionato abbia almeno nParoleMinimo parole
            risultatoAnalisiLocale.setText("L'estratto selezionato è troppo piccolo: Selezionare almeno "+nParoleMinimo+" parole.");
            paroleScelte.clear();
            return;
        }
        
        temp=null;   // Ripulisco la mappa inutile per risparmiare memoria
        
        formattazioneRisultatoanalisi(frequenzeLocali, risultatoAnalisiLocale); // Richiammo il metodo di formattazzione dei risultati analisi

        // Ordino le parole che si possono utilizzare per difficoltà locale
        List<Map.Entry<String, Integer>> paroleOrdinate = frequenzeLocali.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());

        // Calcolo le fette della difficoltà basandomi sulla classifica locale
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
            
            // Salvo la lista delle sole parole candidate all'utilizzo per la difficoltà individuata dalle impostazioni
            candidati = paroleOrdinate.subList(indiceInizio, indiceFine).stream()
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }

        // Estraggo le parole casualmente dalla fetta selezionata
        Collections.shuffle(candidati);
        int limite = Math.min(numeroParoleVolute, candidati.size());
        List<String> paroleFinali = candidati.subList(0, limite);
        
        // Stampo a video le parole
        String risultatoFinale = String.join(", ", paroleFinali).toUpperCase();
        paroleScelte.setText(risultatoFinale);
        
        if (paroleFinali.size() < numeroParoleVolute) { // Formato stampa nel caso in cui vengono trovate meno parole di quelle richieste
            paroleScelte.setText(risultatoFinale + " (Trovate " + paroleFinali.size() + "/" + numeroParoleVolute + ")");
        }
    }
    
    
    // Comportamento del pulsante per l'analisi del testo selezionato
    @FXML
    private void analisiTesto() { 
        
        String titolo = comboTesti.getValue();  // Ricavo il titolo del testo da analizzare
        Testo t = te.getTitle().stream().filter(t0 -> t0.getTitolo().equals(titolo)).findFirst().orElse(null);  // Creo l'istanza del testo a partire dal titolo
        
        if(titolo != null && t != null && !t.isAnalized()){ // Verifico che non sono stati passati valori nulli e che il testo non sia già stato analizzato
            
            analizzaTesto.setDisable(true); // Disabilito il bottone per evitare che l'admin lo spammi
            risultatoAnalisi.setText("Analisi in corso... L'operazione potrebbe richiedere alcuni secondi, attendere prego.");  // Eventuale messaggio d'attesa
            
            // Definizione della task del thread per effettuare l'analisi, così da non bloccare la grafica qual'ora l'analisi richiedesse più tempo
            Task<Boolean> taskAnalisi = new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    return te.analizzaTesto(t.getTxtId());  // Parto con l'analisi del testo e ne verifico l'esito
                }
            };
            
            // Caso di Successo
            taskAnalisi.setOnSucceeded(event -> {
                boolean successo = taskAnalisi.getValue();
                
                if (successo) {
                    
                    aggiornaReportVideo();  // Aggiorno lo stato dei Testi disponibili
                    risultatoAnalisi.clear();   // Pulisco l'area dell'Analisi
        
                    te.caricaAnalisi(t.getTxtId()); // Carico l'analisi appena effettuata
                    Map<String, Integer> mappaRisultati = te.getFrequency();

                    if (mappaRisultati != null) {
                        formattazioneRisultatoanalisi(mappaRisultati,risultatoAnalisi); // Inserisco l'analisi del TextArea dedicato
                    } else { risultatoAnalisi.setText("Errore: Impossibile caricare l'analisi per questo file."); } 
                    
                } else {
                    risultatoAnalisi.setText("Si è verificato un errore durante l'analisi del testo.");
                }
                analizzaTesto.setDisable(false); // Risblocco il bottone d'analisi
            });
            
            // Caso d'errore
            taskAnalisi.setOnFailed(event -> {
                risultatoAnalisi.setText("Errore Critico: L'analisi si è interrotta bruscamente.");
                analizzaTesto.setDisable(false);    // Risblocco il bottone d'analisi
            });
            
            // Creazione ed avvio del thread
            Thread threadBackground = new Thread(taskAnalisi);
            threadBackground.setDaemon(true); 
            threadBackground.start();
            
        }
    }
    
    
    // Comportamento del bottone per tornare alla dashboard
    @FXML
    private void backDashboard() throws IOException {
        Main.setRoot("adminDashboard"); // Cambio schermata a adminDashboard
    }
}