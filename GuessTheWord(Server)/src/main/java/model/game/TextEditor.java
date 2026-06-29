/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.game;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author euppa
 */
public class TextEditor {
    
    private List<Testo> title; // Lista dei titoli disponibili e se sono già analizzati
    
    private HashMap<Integer,String> titleMap;
    
    private HashMap<String, Integer> frequency; // Parole disponibili nel testo e con che frequenza
    
    private String selectedText;    // Contenuto del testo selezionato

    
    public TextEditor(){
        title = new ArrayList<>();
        titleMap = new HashMap<>();
        frequency = new HashMap<>();
        selectedText = "";
    }
    
    
    
    // Metodi Get
    public List<Testo> getTitle() {
        return title;
    }
    
    public HashMap<Integer,String> getTitleMap() {
        return titleMap;
    }

    public HashMap<String, Integer> getFrequency() {
        return frequency;
    }

    public String getSelectedText() {
        return selectedText;
    }
    
    
    // Metodi di gestione Testo
    
    // Metodo per caricare tutto il testo nella stringa selectedText
    public String caricaTesto(String fileName){
        
        File file = new File("../testi/"+fileName+".txt");
        
        if (!file.exists()) {
            return "Attenzione: Il file non è stato trovato nel percorso specificato.";
        }
        
        // Forzo la lettura con UTF_8
        try (BufferedReader br = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)){
            selectedText = "";  // Cancello il testo precedente
            
            String line;
            while((line = br.readLine())!=null){
                selectedText = selectedText.concat(" "+line);   // Leggo il testo riga per riga
            }
                
            
            
            
            
        }catch (IOException io){System.err.println("Errore durante la lettura del file: "+io);}
        
        return  selectedText;
    }
    
    // Metodo per leggere il Report e caricare i dati nella lista title
    public void leggiReport() {
        
        File fileCsv = new File("../analisiTesti/report.csv"); 
        
        if (!fileCsv.exists()) {
            System.err.println("File CSV non trovato!");
            return;
        }
        
        try (BufferedReader br = Files.newBufferedReader(fileCsv.toPath(), StandardCharsets.UTF_8)) {
            
            String linea;
            br.readLine(); 
            
            while ((linea = br.readLine()) != null) {
                String[] campi = linea.split(";");
                
                if (campi.length >= 2) {
                    try {
                        Testo t = new Testo(campi[1].trim(), Integer.parseInt(campi[0].trim()), campi[2].trim().equals("1"));
                        this.titleMap.put(Integer.parseInt(campi[0].trim()),campi[1].trim()); 
                        this.title.add(t);
                    } catch (NumberFormatException e) {
                        System.err.println("Errore di conversione numeri alla riga: " + linea);
                    }
                }
            }
            
        } catch (Exception e) { System.err.println("Errore nel caricamento del report: "+e); }
    }
    
    // Metodo per il solo caricamento dell'analisi del testo specificato
    public void caricaAnalisi(Integer txtId){
        
        String filename = "../analisiTesti/"+title.stream().filter(a -> a.getTxtId() == txtId).map(a -> a.getTitolo()).findFirst().orElse("Testo Sconosciuto") + "-Analisi.dat";
        try (ObjectInputStream ob = new ObjectInputStream(
                new BufferedInputStream(
                        new FileInputStream(filename)))){
            
            frequency = (HashMap<String, Integer>)ob.readObject();
            ob.close();
        } catch (Exception e) {System.out.println("Errore durante la lettura dell'analisi: "+e);}
    }
    
    // Metodo per effettuare l'analisi del testo selezionato
    public boolean analizzaTesto(Integer txtId){
        
        // Salvo il nome che il file d'analisi dovrà avere
        String filename = "../analisiTesti/"+title.stream().filter(a -> a.getTxtId() == txtId).map(a -> a.getTitolo()).findFirst().orElse("Testo Sconosciuto") + "-Analisi.dat";
        
        
        
        try (ObjectOutputStream ob = new ObjectOutputStream(new BufferedOutputStream( new FileOutputStream(filename)))){
            
            
            List<String> stopwords = new ArrayList<>();
            
            // Leggo il file contenente le stopword
            try (BufferedReader br = new BufferedReader( new FileReader("../analisiTesti/stopwords.txt"))){
                
                String word;
                while((word = br.readLine())!=null)
                    stopwords.add(word);
                
            } catch (Exception e) { System.err.println("Errore durante la lettura delle stopword: "+e);}
            
            
            
            if (this.selectedText == null || this.selectedText.trim().isEmpty()) {
                System.err.println("Nessun testo da analizzare!");
                return false;
            }
            
            
            frequency = Arrays.stream(this.selectedText.toLowerCase().split("\\W+")) // minuscolo e divido per non-lettere (spazi, virgole, punti)
                .filter(w -> !w.isEmpty())                               // scarto eventuali stringhe vuote
                .filter(w -> !stopwords.contains(w))                     // scarto le stopword
                .collect(Collectors.groupingBy(                                // 4. Raggruppo le parole identiche e conto le occorrenze (Di base restituisce solo Map)
                        w -> w,
                        HashMap::new,           // Richiamo il costruttore di hashMap per ricavare la collezione specifica
                        Collectors.summingInt(w -> 1)
                ));
            
            ob.writeObject(frequency);
            
            ob.close();
        } catch (Exception e) { System.err.println("Errore durante l'analisi: "+e); return false;}
        
        aggiornaReport(txtId);
        return true;
    }
    
    // Metodo per aggiornare il file report con la nuova analisi effettuata (Aggiorna anche il valore isAnalized del testo nella lista title)
    public void aggiornaReport(Integer txtId){
        
        title.stream().filter(t -> t.getTxtId()==txtId).forEach(t -> t.setIsAnalized(true));
        
        File fileCsv = new File("../analisiTesti/report.csv");
        
       if (!fileCsv.exists()) {
            System.err.println("File CSV non trovato!");
            return;
        }
        
        try (BufferedWriter br = Files.newBufferedWriter(fileCsv.toPath())) {
            
            br.write("Id");
            br.write(";");
            br.write("Titolo");
            br.write(";");
            br.write("Stato");
            br.write("\n");
            
            for(Testo x : title){
                br.write(String.valueOf(x.getTxtId()));
                br.write(";");
                br.write(x.getTitolo());
                br.write(";");
                br.write(String.valueOf((x.isAnalized()) ? 1 : 0));
                br.write("\n");
            }
            br.close();
        } catch (Exception e) { System.err.println("Errore nella scrittura del report: "+e); }
        
    }
    /*
    
    public String cifraTesto(Integer txtId){
        
    }
    
    
    
    */
    
    
}
