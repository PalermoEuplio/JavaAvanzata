package model.utility;

import java.io.Serializable;

public class Testo implements Serializable{

   private String titolo; 
   private int txtId; 
   
   private static String gameText = "";    // Contenuto del testo selezionato

    public Testo(String titolo, int txtId) {
        this.titolo = titolo;
        this.txtId = txtId;
    }

    public String getTitolo() {
        return titolo;
    }

    public int getTxtId() {
        return txtId;
    }
    
    public static String getGameText() {
        return gameText;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public void setTxtId(int txtId) {
        this.txtId = txtId;
    }

    public static void setGameText(String gameText) {
        Testo.gameText = gameText;
    }

    @Override
    public String toString() {
        return titolo + ", " + txtId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.txtId;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Testo other = (Testo) obj;
        return this.txtId == other.txtId;
    }

   
    

}
