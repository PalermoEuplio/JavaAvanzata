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
    
    
    // Metodo per la selezione di un Utente dal DB. 
    // In particolare può essere usato per fare il login come amministratore e cercare un singolo utente a partire dal suo id
    @Override
    public T cerca(T user, String password) throws SQLException, IllegalArgumentException{
        
        T result = null;
        
        if(user instanceof Amministratore){ // Caso Amministratore (Accesso)
            Amministratore s = (Amministratore) user;
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
        } else if(user instanceof Player){  // Caso Player (Richiesta di informazioni)
            Player s = (Player) user;
            try( Connection c = DriverManager.getConnection(dbURL, dbUsername,dbPassword);

                 PreparedStatement ps = c.prepareStatement("SELECT Username,Id_Utente,N_Vittorie,N_Partite,Tempo_Medio_Risposta FROM Player WHERE Id_Utente = ? ");

                    ) {

                ResultSet  rs = ps.executeQuery();

                if (rs.next()) {
                    result = (T) new Player(rs.getString("Username"), rs.getInt("Id_Utente"), rs.getInt("N_Partite"), rs.getInt("N_Vittorie"), rs.getDouble("Tempo_Medio_Risposta"));
                }
            }
        
        } else throw new IllegalArgumentException();
        
        return result;
           
    }
    
    
    @Override
    public void rimuoviPlayer(T p1) throws SQLException, IllegalArgumentException{
        if(p1 instanceof Player){
            
            Player p = (Player) p1;
            
            try( Connection c = DriverManager.getConnection(dbURL, dbUsername, dbPassword)){ 

                    c.setAutoCommit(false);
                    
                    String removeStudent = String.format("DELETE FROM Player WHERE Id_Utente = '%s'", p.getId());
                    
                    try(Statement stmt = c.createStatement()){
                        
                        stmt.executeUpdate(removeStudent);
                        
                        c.commit();
                    
                } catch (SQLException e) {
                    c.rollback();
                    System.err.println("Errore durante l'eliminazione. Effettuo il rollback.  "+e);
                    throw e;
                }
            }
        }else throw new IllegalArgumentException();
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
    
    
    @Override
    public void aggiungiSfida(T sfida) throws SQLException{
        
    }    
}
