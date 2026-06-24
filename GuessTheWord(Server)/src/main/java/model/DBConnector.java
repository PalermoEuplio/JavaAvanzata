package model;

// Classe che contiene tutta la logica di connessione al db

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBConnector <T> implements DAO<T>{
    
    private final String dbURL ="jdbc:sqlite:../GuessTheWord.db";
    private final String dbUsername = "";
    private final String dbPassword = "";
    
    @Override
    public T cerca(T admin, String password) throws SQLException, IllegalArgumentException{
        
        T result = null;
        
        if(admin instanceof Amministratore){
            Amministratore s = (Amministratore) admin;
            try( Connection c = DriverManager.getConnection(dbURL, dbUsername,dbPassword);

                 PreparedStatement ps = c.prepareStatement("SELECT Username, Password FROM Amministratore WHERE Username = ? AND Password = ?");

                    ) {

                ps.setString(1, s.getUsername()); 
                ps.setString(2, password);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        result = (T) new Amministratore(rs.getString("Username"));   // Inseriti dati corretti
                    } else {
                        throw new SQLException("Credenziali non valide o utente inesistente");  // Accesso non effettuato
                    }
                }
            }
        } else throw new IllegalArgumentException();
        return result;
           
    }
    
    
    @Override
    public void rimuoviPlayer(T p1) throws Exception{
        if(p1 instanceof Player){
            
            Player p = (Player) p1;
            
            try( Connection c = DriverManager.getConnection(dbURL, dbUsername, dbPassword); 

                    Statement stmt = c.createStatement();

                ) {
                
                String removeStudent = String.format("DELETE FROM Player WHERE Id_Utente = '%s'", p.getId());

                stmt.executeUpdate(removeStudent);

                }
            }
    }
    
    
    
    @Override
    public List<T> elencaTuttiPlayer()throws Exception{
        
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
