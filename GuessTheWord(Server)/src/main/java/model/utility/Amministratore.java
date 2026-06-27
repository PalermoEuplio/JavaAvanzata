package model.utility;

// Classe che tiene traccia dell'Username dell'amministratore loggato
public class Amministratore {
    
    private String username;

    public Amministratore(String username) {
        this.username = username;
    }
    

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return username;
    }
}
