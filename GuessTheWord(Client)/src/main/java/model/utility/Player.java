package model.utility;
import java.io.Serializable;

/**
 * Classe contenente le inforamzioni relative ai player.
 */

public class Player implements Serializable{
    
    private String username;
    private int id;
    private int nPartite;
    private int nVittorie;
    private double tempoRisposta;
    private boolean isOn;

    public Player(String Username, int id, int nPartite, int nVittorie, double tempoRisposta) {
        this.username = Username;
        this.id = id;
        this.nPartite = nPartite;
        this.nVittorie = nVittorie;
        this.tempoRisposta = tempoRisposta;
        this.isOn=false;
    }

    public String getUsername() {
        return username;
    }

    public int getId() {
        return id;
    }

    public int getNPartite() {
        return nPartite;
    }

    public int getNVittorie() {
        return nVittorie;
    }

    public double getTempoRisposta() {
        return tempoRisposta;
    }

    public boolean isOn() {
        return isOn;
    }
    
    public void setOn(boolean status){
        this.isOn=status;
    }

    @Override
    public String toString() {
        return username+", " + id + ", " + nPartite + ", " + nVittorie + ", " + tempoRisposta;
    }
}
