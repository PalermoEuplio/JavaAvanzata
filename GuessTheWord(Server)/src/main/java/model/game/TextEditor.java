package model.game;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Classe TextEditor che si occupa di gestire i testi da analizzare
 */
public class TextEditor {

    private List<Testo> title; // Lista dei titoli disponibili e se sono già analizzati

    private HashMap<String, Integer> frequency; // Parole disponibili nel testo e con che frequenza

    private String selectedText; // Contenuto del testo selezionato

    private static String modifiedText = "";

    private static String[] risposte = null;

    public TextEditor() {
        title = new ArrayList<>();
        frequency = new HashMap<>();
        selectedText = "";
    }

    // --------------- Metodi Getter e Setter ---------------
    public List<Testo> getTitle() {
        return title;
    }  

    public HashMap<String, Integer> getFrequency() {
        return frequency;
    }

    public String getSelectedText() {
        return selectedText;
    }

    public void setSelectedText(String selectedText) {
        this.selectedText = selectedText;
    }

    public static String getModifiedText() {
        return modifiedText;
    }

    public static void setModifiedText(String modifiedText) {
        TextEditor.modifiedText = modifiedText;
    }

    public static String[] getRisposte() {
        return risposte;
    }

    // --------------- Metodi di gestione Testo ---------------

    /**
     * Metodo per caricare tutto il testo nella stringa selectedText.
     * 
     * @param fileName Il nome del file.
     * @return Il testo caricato.
     */
    public String caricaTesto(String fileName) {

        Path file = Paths.get("documents", fileName + ".txt");

        if (!Files.exists(file)) {
            return "Attenzione: Il file non è stato trovato nel percorso specificato.";
        }

        /*     Altro modo per la lettura del testo usando le stream; In particolare ci sono due vie: 
                                    la Collectors.joining, che utilizza lo StringBuilder
                                    la funzione reduce delle stream che permette di ridurre tutta una stream in un solo elemento
            try{
                selectedText = Files.lines(file, StandardCharsets.UTF_8).collect(Collectors.joining(" "));
                selectedText = Files.lines(file, StandardCharsets.UTF_8).reduce("", (a,b) -> a+b);
            }catch(IOException e){}
        */
        
        
        
        // Forzo la lettura con UTF_8
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            selectedText = ""; // Cancello il testo precedente

            String line;
            while ((line = br.readLine()) != null) {
                selectedText = selectedText.concat(" " + line); // Leggo il testo riga per riga
            }
            br.close();
        } catch (IOException io) {
            System.err.println("Errore durante la lettura del file: " + io);
        }

        return selectedText;
    }

    /**
     * Metodo per leggere il Report e caricare i dati nella lista title.
     */
    public void leggiReport() {

        Path fileCsv = Paths.get("data", "report.csv");

        if (!Files.exists(fileCsv)) {
            System.err.println("File CSV non trovato!");
            return;
        }
       
        /*   Metodo alternativo per leggere da csv mediante stream API e Files.lines
            try {
                title = Files.lines(fileCsv).skip(1).map(t -> {
                    String[] temp = t.split(";");
                    return new Testo(temp[1],Integer.parseInt(temp[0]),temp[2].equals("1"));
                }).collect(Collectors.toList());
            } catch (IOException e) {System.out.println("Errore"+e);}
        */
        
        try (BufferedReader br = Files.newBufferedReader(fileCsv, StandardCharsets.UTF_8)) {

            String linea;
            br.readLine();

            while ((linea = br.readLine()) != null) {
                String[] campi = linea.split(";");

                if (campi.length >= 2) {
                    try {
                        Testo t = new Testo(campi[1].trim(), Integer.parseInt(campi[0].trim()),
                                campi[2].trim().equals("1"));
                        title.add(t);
                    } catch (NumberFormatException e) {
                        System.err.println("Errore di conversione numeri alla riga: " + linea);
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            System.err.println("Errore nel caricamento del report: " + e);
        }
    }

    /**
     * Metodo per il solo caricamento dell'analisi del testo specificato.
     * 
     * @param txtId L'id del testo.
     */
    public void caricaAnalisi(Integer txtId) {

        String filename = String.valueOf(txtId) + "_" + title.stream()
                .filter(a -> a.getTxtId() == txtId).map(a -> a.getTitolo()).findFirst().orElse("Testo Sconosciuto")
                + "-Analisi.dat";
        Path filepath = Paths.get("data", filename);
        
        try (ObjectInputStream ob = new ObjectInputStream(
                new BufferedInputStream(
                        Files.newInputStream(filepath)))) {

            frequency = (HashMap<String, Integer>) ob.readObject();
            ob.close();
        } catch (Exception e) {
            System.out.println("Errore durante la lettura dell'analisi: " + e);
        }
    }

    /**
     * Metodo per effettuare l'analisi del testo selezionato.
     * 
     * @param txtId L'id del testo.
     * @return true se l'analisi va a buon fine, false altrimenti.
     */
    public boolean analizzaTesto(Integer txtId) {

        // Salvo il nome che il file d'analisi dovrà avere
        String filename = String.valueOf(txtId) + "_" + title.stream()
                .filter(a -> a.getTxtId() == txtId).map(a -> a.getTitolo()).findFirst().orElse("Testo Sconosciuto")
                + "-Analisi.dat";
        Path filepath = Paths.get("data", filename);

        try (ObjectOutputStream ob = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(filepath)))) {

            List<String> stopwords = new ArrayList<>();

            // Leggo il file contenente le stopword
            Path stopwordsPath = Paths.get("analisiTesti", "stopwords.txt");
            try (BufferedReader br = Files.newBufferedReader(stopwordsPath)) {

                String word;
                while ((word = br.readLine()) != null)
                    stopwords.add(word);

            } catch (Exception e) {
                System.err.println("Errore durante la lettura delle stopword: " + e);
            }

            if (this.selectedText == null || this.selectedText.trim().isEmpty()) {
                System.err.println("Nessun testo da analizzare!");
                return false;
            }

            frequency = Arrays.stream(this.selectedText.toLowerCase().split("\\W+")) // minuscolo e divido per non-lettere (spazi, virgole, punti)
                    .filter(w -> !w.isEmpty()) // scarto eventuali stringhe vuote
                    .filter(w -> !stopwords.contains(w)) // scarto le stopword
                    .collect(Collectors.groupingBy( // Raggruppo le parole identiche e conto le occorrenze (Di base restituisce solo Map)
                            w -> w,
                            HashMap::new, // Richiamo il costruttore di hashMap per ricavare la collezione specifica
                            Collectors.summingInt(w -> 1)));

            ob.close();
        } catch (Exception e) {
            System.err.println("Errore durante l'analisi: " + e);
            return false;
        }

        aggiornaReport(txtId);
        return true;
    }

    /**
     * Metodo per aggiornare il file report con la nuova analisi effettuata
     * (Aggiorna anche il valore isAnalized del testo nella lista title).
     * 
     * @param txtId L'id del testo.
     */
    public void aggiornaReport(Integer txtId) {

        title.stream().filter(t -> t.getTxtId() == txtId).forEach(t -> t.setIsAnalized(true));

        Path fileCsv = Paths.get("data", "report.csv");

        if (!Files.exists(fileCsv)) {
            System.err.println("File CSV non trovato!");
            return;
        }

        try (BufferedWriter br = Files.newBufferedWriter(fileCsv)) {

            br.write("Id");
            br.write(";");
            br.write("Titolo");
            br.write(";");
            br.write("Stato");
            br.write("\n");

            for (Testo x : title) {
                br.write(String.valueOf(x.getTxtId()));
                br.write(";");
                br.write(x.getTitolo());
                br.write(";");
                br.write(String.valueOf((x.isAnalized()) ? 1 : 0));
                br.write("\n");
            }
            br.close();
        } catch (Exception e) {
            System.err.println("Errore nella scrittura del report: " + e);
        }

    }

    /**
     * Cifra il testo utilizzando la stream API. In particolare le parole cifrate
     * sono nel formato [[ parola ]],
     * in modo da essere facilmente riconoscibili dal Client.
     * 
     * @param shift  Lo shift per la cifratura.
     * @param parole Le parole da cifrare.
     */
    public void cifraTesto(int shift, String[] parole) {

        risposte = parole;

        // 1. Prepariamo la lista delle parole target in minuscolo per fare controlli
        // precisi (case-insensitive)
        List<String> paroleDaCifrare = Arrays.stream(parole)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        // Usiamo una Regex avanzata con Stream per "tagliare" il testo preservando gli
        // spazi e la punteggiatura.
        // (?U) attiva il supporto Unicode fondamentale per riconoscere lettere accentate 
        // Il pattern taglia il testo nel punto esatto di confine tra una parola e un simbolo/spazio.
        modifiedText = Pattern.compile("(?U)(?<=\\w)(?=\\W)|(?<=\\W)(?=\\w)")
                .splitAsStream(selectedText)
                .map(token -> {

                    // Verifico se la parola corrente è una di quelle da cifrare
                    if (paroleDaCifrare.contains(token.toLowerCase())) {

                        // Cifriamo la singola parola carattere per carattere con lo Stream
                        String parolaCifrata = token.chars()
                                .map(c -> {
                                    if (Character.isLetter(c)) {
                                        char base = Character.isLowerCase(c) ? 'a' : 'A';
                                        return ((c - base + shift) % 26) + base;
                                    }
                                    return c;
                                })
                                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                                .toString();

                        // MAGIA PER IL CLIENT: Aggiungiamo i delimitatori attorno alla parola cifrata
                        // così il TextFlow del Client saprà esattamente come colorarla di giallo!
                        return "[[" + parolaCifrata + "]]";
                    }

                    // Se non è nella lista, restituiamo il token originale (spazi, virgole, o
                    // parole da non toccare)
                    return token;

                })
                .collect(Collectors.joining()); // Riunisce tutti i frammenti in un'unica stringa
    }

}
