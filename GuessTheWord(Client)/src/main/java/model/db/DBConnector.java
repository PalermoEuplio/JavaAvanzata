package model.db;

// Classe che contiene tutta la logica di connessione al db

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import model.utility.Player;
import model.utility.Sfida;
import model.utility.TextEditor;

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
             PreparedStatement ps = c.prepareStatement(" SELECT Username,Id_Utente,N_Vittorie,N_Partite,Tempo_Medio_Risposta FROM Player WHERE Id_Utente != 0 ORDER BY N_Vittorie DESC, Tempo_Medio_Risposta ASC");
                ) {
        
            ResultSet  rs = ps.executeQuery();


            while(rs.next()) {
                    elenco.add(new Player(rs.getString("Username"), rs.getInt("Id_Utente"), rs.getInt("N_Partite"), rs.getInt("N_Vittorie"), rs.getDouble("Tempo_Medio_Risposta")));
            }
        }
        
        return (List<T>) elenco;
    } 
    
    @Override
    public List<T> caricaSfide(int idPlayer) throws SQLException{
        
        List<Sfida> sfide = new ArrayList<>();
        
        try( Connection c = DriverManager.getConnection(dbURL, dbUsername, dbPassword)) {
        
            // Stesso nella query cerco le sfide effettuate dall'utente IdPlayer, raggruppo tutte le soluzioni in un'unica stringa e ricavo l'username dell'avversario
            // Inoltre Setto il P1, i suoi tempi e il risultato della partita in funzione dell'utente loggato (Che diventa P1)
            try (PreparedStatement ps = c.prepareStatement("SELECT S.Id_Documento, S.Durata, S.Id_P1, S.Id_P2, " +
                         "CASE WHEN S.Id_P1 = Me.MyId THEN S.Tempo_RispostaP1 ELSE S.Tempo_RispostaP2 END AS MioTempo, " +
                         "CASE WHEN S.Id_P1 = Me.MyId THEN S.Tempo_RispostaP2 ELSE S.Tempo_RispostaP1 END AS SuoTempo, " +
                         "CASE WHEN S.Id_P1 = Me.MyId THEN S.Risultato " +
                         "     WHEN S.Risultato = 0 THEN 1 WHEN S.Risultato = 1 THEN 0 ELSE S.Risultato END AS MioRisultato, " +
                         "GROUP_CONCAT(Sol.Parola, ', ') AS ElencoSoluzioni, " +
                         "Avversario.Username AS UsernameAvversario " +
                         "FROM (SELECT ? AS MyId) Me " +
                         "JOIN Sfida S ON S.Id_P1 = Me.MyId OR S.Id_P2 = Me.MyId " +
                         "JOIN Soluzione Sol ON S.Id_Sfida = Sol.Id_Sfida " +
                         "JOIN Player Avversario ON (Avversario.Id_Utente = S.Id_P1 OR Avversario.Id_Utente = S.Id_P2) AND Avversario.Id_Utente != Me.MyId " +
                         "GROUP BY S.Id_Sfida");) {
                
                
                ps.setInt(1, idPlayer);
                
                
                try (ResultSet rs = ps.executeQuery()){
                    
                    while(rs.next()) {
                        String soluzioni = rs.getString("ElencoSoluzioni");
                        
                        if(soluzioni==null)
                            soluzioni="";
                        
                        
                        TextEditor te = new TextEditor();
                        
                        te.leggiReport();
                        HashMap<Integer,String> x = te.getTitle();
                        
                        Sfida s = new Sfida(rs.getInt("Id_Documento"), rs.getDouble("Durata"), rs.getDouble("MioTempo"), rs.getDouble("SuoTempo"),
                                rs.getInt("id_P1"), rs.getInt("id_P2"), rs.getString("UsernameAvversario"), 
                                rs.getInt("MioRisultato")==1 ? "Vittoria":"Sconfitta",soluzioni);
                        
                        s.setTitoloTesto(x.get(rs.getInt("Id_Documento"))); 
                        
                        sfide.add(s);
                    }
                    
                } catch (Exception e) { System.err.println("Errore durante l'esecuzione della query: "+e);}
            }
        }
        
        return (List<T>) sfide;
    }
}
