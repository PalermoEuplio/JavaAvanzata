package model;

// Classe contenente le inforamzioni relative ai player
public class Player {
    
    private String Username;
    private int id;
    private int nPartite;
    private int nVittorie;
    private double tempoRisposta;
    private boolean isOn = false;

    public Player(String Username, int id, int nPartite, int nVittorie, double tempoRisposta) {
        this.Username = Username;
        this.id = id;
        this.nPartite = nPartite;
        this.nVittorie = nVittorie;
        this.tempoRisposta = tempoRisposta;
    }

    public String getUsername() {
        return Username;
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

    public boolean isIsOn() {
        return isOn;
    }

    @Override
    public String toString() {
        return Username+", " + id + ", " + nPartite + ", " + nVittorie + ", " + tempoRisposta;
    }
}
