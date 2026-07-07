package model.game;

/**
 * Classe che rappresenta un Testo.
 */
public class Testo{

   private String titolo; 
   private int txtId; 
   private boolean isAnalized;

    public Testo(String titolo, int txtId, boolean isAnalized) {
        this.titolo = titolo;
        this.txtId = txtId;
        this.isAnalized = isAnalized;
    }

    public String getTitolo() {
        return titolo;
    }

    public int getTxtId() {
        return txtId;
    }

    public boolean isAnalized() {
        return isAnalized;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public void setTxtId(int txtId) {
        this.txtId = txtId;
    }

    public void setIsAnalized(boolean isAnalized) {
        this.isAnalized = isAnalized;
    }

    @Override
    public String toString() {
        return titolo + ", " + txtId + ", " +isAnalized;
    }

   

}
