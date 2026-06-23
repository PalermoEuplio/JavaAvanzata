package model;

// Classe che contiene tutta la logica di connessione al db

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnector implements DAO<Amministratore>{
    
    private final String dbURL ="jdbc:sqlite:../GuessTheWord.db";
    private final String dbUsername = "";
    private final String dbPassword = "";
    
    @Override
    public Amministratore cerca(Amministratore admin, String password) throws SQLException{
        
        Amministratore s = null;
        
        try( Connection c = DriverManager.getConnection(dbURL, dbUsername,dbPassword);
                
             PreparedStatement ps = c.prepareStatement("SELECT Username, Password FROM Amministratore WHERE Username = ? AND Password = ?");
                
                ) {
            
            ps.setString(1, admin.getUsername()); 
            ps.setString(2, password);
            
            try (ResultSet rs = ps.executeQuery()) {
                
                if (rs.next()) {
                    s = new Amministratore(rs.getString("Username"));   // Inseriti dati corretti
                } else {
                    throw new SQLException("Credenziali non valide o utente inesistente");  // Accesso non effettuato
                }
            }
        }
        
        return s;
        
        
    }
    
}
