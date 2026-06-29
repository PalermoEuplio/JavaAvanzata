/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.utility;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;

/**
 *
 * @author euppa
 */
public class TextEditor {
    
    private HashMap<Integer,String> title; // Lista dei titoli disponibili mappati per id
    
    private String selectedText;    // Contenuto del testo selezionato

    
    public TextEditor(){
        title = new HashMap<>();
        selectedText = "";
    }
    
    
    
    // Metodi Get
    public HashMap<Integer,String> getTitle() {
        return title;
    }

    public String getSelectedText() {
        return selectedText;
    }
    
    
    // Metodi di gestione Testo

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
                        this.title.put(Integer.parseInt(campi[0].trim()),campi[1].trim()); 
                    } catch (NumberFormatException e) {
                        System.err.println("Errore di conversione numeri alla riga: " + linea);
                    }
                }
            }
            
        } catch (Exception e) { System.err.println("Errore nel caricamento del report: "+e); }
    }
    
    
}
