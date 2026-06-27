package model.db;

// Classe che contiene tutta la logica di connessione al db

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.utility.Player;

public class DBConnector <T> implements DAO<T>{
    
    private final String dbURL ="jdbc:sqlite:../GuessTheWord.db";
    private final String dbUsername = "";
    private final String dbPassword = "";
    
    
    // Metodo per la selezione di un Utente dal DB. 
    // In particolare può essere usato per fare il login come amministratore e cercare un singolo utente a partire dal suo id
    @Override
    public T cerca(T user, String password) throws SQLException, IllegalArgumentException{
        
        T result = null;
        
        if(user instanceof Player){  // Caso Player (Login o registrazione)
            Player p = (Player) user;
            try( Connection c = DriverManager.getConnection(dbURL, dbUsername,dbPassword);

                PreparedStatement ps = c.prepareStatement("SELECT Username,Password,Id_Utente,N_Vittorie,N_Partite,Tempo_Medio_Risposta FROM Player WHERE Username = ?");

                ) {
                
                ps.setString(1, p.getUsername());   
                
                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {    // L'utente Esiste nel DB
                        
                        if(password==null)  // Fase di Registrazione
                            result = (T) new Player(rs.getString("Username"), rs.getInt("Id_Utente"), rs.getInt("N_Partite"), rs.getInt("N_Vittorie"), rs.getDouble("Tempo_Medio_Risposta"));
                        else {
                            // Fase di Login
                            if(rs.getString("Password").equals(password))
                                result = (T) new Player(rs.getString("Username"), rs.getInt("Id_Utente"), rs.getInt("N_Partite"), rs.getInt("N_Vittorie"), rs.getDouble("Tempo_Medio_Risposta"));
                            else throw new SQLException("Password Errata");  // Accesso non effettuato
                        }
                    } else {  // Utente non trovato nel DB
                        if(password!=null)
                            throw new SQLException("Username Errato");  // Login non effettuato
                    }
                }
            }
        
        } else throw new IllegalArgumentException();
        
        return result;
           
    }
    
    
    @Override
    public T registrazione(T user, String password) throws SQLException, Exception{
        
        if(user instanceof Player){
            
            Player p = (Player) user;
            
            try(Connection c = DriverManager.getConnection(dbURL, dbUsername, dbPassword)){
                
                String insertStatement = String.format("INSERT INTO Player (Username,Password) VALUES ('%s','%s')", p.getUsername(),password);
                
                // Verifico che l'utente non sia già presente nel DB
                if(cerca(user, null)!=null){
                    throw new Exception("Username non disponibile");
                }
                
                try(Statement stmt = c.createStatement()){
                        stmt.executeUpdate(insertStatement);
                }
            }
            
            return cerca(user, password);   // Ritorno tutte le informazioni del nuovo utente loggato
        }
        
        return null;
    }
    
    @Override
    public List<T> elencaTuttiPlayer() throws SQLException{
        
        List<Player> elenco = new ArrayList<>();
        
        try( Connection c = DriverManager.getConnection(dbURL, dbUsername, dbPassword); 
             PreparedStatement ps = c.prepareStatement(" SELECT Username,Id_Utente,N_Vittorie,N_Partite,Tempo_Medio_Risposta FROM Player WHERE Id_Utente != 0");
                ) {
        
            ResultSet  rs = ps.executeQuery();


            while(rs.next()) {
                    elenco.add(new Player(rs.getString("Username"), rs.getInt("Id_Utente"), rs.getInt("N_Partite"), rs.getInt("N_Vittorie"), rs.getDouble("Tempo_Medio_Risposta")));
            }
        }
        
        return (List<T>) elenco;
        
    } 
}
