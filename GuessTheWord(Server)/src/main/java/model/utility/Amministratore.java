package model.utility;

/**
 * Classe che tiene traccia dell'Username dell'amministratore loggato.
 */
public class Amministratore {

    private String username;
    private int id;

    public Amministratore(String username, int id) {
        this.username = username;
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return username;
    }
}
