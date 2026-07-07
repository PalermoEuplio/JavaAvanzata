package model.db;

/**
 * Classe che contiene tutta la logica di connessione al db.
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import model.connection.Sessione;
import model.utility.Esito;
import model.utility.Sfida;
import model.game.TextEditor;
import model.utility.Amministratore;
import model.utility.Player;

public class DBConnector<T> implements DAO<T> {

    private final String dbURL = "jdbc:sqlite:GuessTheWord.db";
    private final String dbUsername = "";
    private final String dbPassword = "";

    /**
     * Metodo per la selezione di un Utente dal DB.
     * In particolare può essere usato per fare il login come amministratore e
     * cercare un singolo utente a partire dal suo id.
     * 
     * @param user     L'utente da cercare.
     * @param password La password dell'utente.
     * @return Il risultato della ricerca.
     * @throws SQLException             Se c'è un errore SQL.
     * @throws IllegalArgumentException Se gli argomenti non sono validi.
     */
    @Override
    public T cerca(T user, String password) throws SQLException, IllegalArgumentException {

        T result = null;

        // CASO 1: AMMINISTRATORE (Login)
        if (user instanceof Amministratore) {
            Amministratore s = (Amministratore) user;
            try (Connection c = DriverManager.getConnection(dbURL, dbUsername, dbPassword);
                    PreparedStatement ps = c.prepareStatement("SELECT * FROM Amministratore WHERE Username = ?")) {

                ps.setString(1, s.getUsername());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        if (password != null && !rs.getString("Password").equals(password)) {
                            throw new SQLException("Password Errata");
                        }
                        result = (T) new Amministratore(rs.getString("Username"), rs.getInt("Id_utente"));
                    } else {
                        throw new SQLException("Username Errato");
                    }
                }
            }

            // CASO 2: PLAYER (Login o Controllo)
        } else if (user instanceof Player) {
            Player p = (Player) user;
            try (Connection c = DriverManager.getConnection(dbURL, dbUsername, dbPassword);
                    PreparedStatement ps = c
                            .prepareStatement("SELECT * FROM Player WHERE Username = ? or Id_Utente = ?")) {

                ps.setString(1, p.getUsername());
                ps.setInt(2, p.getId() > 0 ? p.getId() : -1);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {

                        if (password == null) {
                            result = (T) new Player(rs.getString("Username"), rs.getInt("Id_Utente"),
                                    rs.getInt("N_Partite"), rs.getInt("N_Vittorie"),
                                    rs.getDouble("Tempo_Medio_Risposta"));
                        } else {
                            if (rs.getString("Password").equals(password)) {
                                result = (T) new Player(rs.getString("Username"), rs.getInt("Id_Utente"),
                                        rs.getInt("N_Partite"), rs.getInt("N_Vittorie"),
                                        rs.getDouble("Tempo_Medio_Risposta"));
                            } else {
                                throw new SQLException("Password Errata");
                            }
                        }
                    } else {
                        if (password != null) {
                            throw new SQLException("Username Errato");
                        }
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Tipo utente non supportato dal metodo Cerca.");
        }

        return result;
    }

    /**
     * Registra un nuovo player nel DB.
     * 
     * @param user     Il player da registrare.
     * @param password La password del player.
     * @return Il player registrato.
     * @throws SQLException             In caso di errori SQL.
     * @throws IllegalArgumentException Se gli argomenti non sono validi.
     * @throws Exception                In caso di errori generici.
     */
    @Override
    public T registrazione(T user, String password) throws SQLException, Exception {

        if (user instanceof Player) {

            Player p = (Player) user;

            try (Connection c = DriverManager.getConnection(dbURL, dbUsername, dbPassword)) {

                String insertStatement = String.format("INSERT INTO Player (Username,Password) VALUES ('%s','%s')",
                        p.getUsername(), password);

                // Controllo sulla lunghezza dello Username
                if (p.getUsername().length() < 3)
                    throw new SQLException("Username troppo corto;\n    Deve avere almeno 3 caratteri");

                // Verifico che l'utente non sia già presente nel DB
                if (cerca(user, null) != null) {
                    throw new Exception("Username non disponibile");
                }

                try (Statement stmt = c.createStatement()) {
                    stmt.executeUpdate(insertStatement);
                }
            }

            return cerca(user, password); // Ritorno tutte le informazioni del nuovo utente loggato
        }

        return null;
    }

    /**
     * Rimuove un player dal DB.
     * 
     * @param p1 Il player da rimuovere.
     * @throws SQLException             In caso di errori SQL.
     * @throws IllegalArgumentException Se il player non è valido.
     */
    @Override
    public void rimuoviPlayer(T p1) throws SQLException, IllegalArgumentException {
        if (p1 instanceof Player) {

            Player p = (Player) p1;
            
            Properties props = new Properties();
            if (dbUsername != null) props.setProperty("user", dbUsername);
            if (dbPassword != null) props.setProperty("password", dbPassword);
            
            props.setProperty("foreign_keys", "true");  // Richiamo anche le foreign key altrimenti la rimozione non avviene correttamente

            try (Connection c = DriverManager.getConnection(dbURL, props)) {

                c.setAutoCommit(false);

                String removeStudent = String.format("DELETE FROM Player WHERE Id_Utente = '%s'", p.getId());

                try (Statement stmt = c.createStatement()) {

                    stmt.executeUpdate(removeStudent);

                    c.commit();

                } catch (SQLException e) {
                    c.rollback();
                    System.err.println("Errore durante l'eliminazione. Effettuo il rollback.  " + e);
                    throw e;
                }
            }
        } else
            throw new IllegalArgumentException();
    }

    /**
     * Elenca tutti i player registrati nel DB.
     * 
     * @return La lista di tutti i player.
     * @throws SQLException In caso di errori SQL.
     */
    @Override
    public List<T> elencaTuttiPlayer() throws SQLException {

        List<Player> elenco = new ArrayList<>();

        try (Connection c = DriverManager.getConnection(dbURL, dbUsername, dbPassword);
                PreparedStatement ps = c.prepareStatement(
                        " SELECT Username,Id_Utente,N_Vittorie,N_Partite,Tempo_Medio_Risposta FROM Player WHERE Id_Utente != 0 ORDER BY N_Vittorie DESC, Tempo_Medio_Risposta ASC");) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                elenco.add(new Player(rs.getString("Username"), rs.getInt("Id_Utente"), rs.getInt("N_Partite"),
                        rs.getInt("N_Vittorie"), rs.getDouble("Tempo_Medio_Risposta")));
            }
        }

        return (List<T>) elenco;

    }

    /**
     * Carica le sfide di un player dal DB.
     * 
     * @param idPlayer L'id del player.
     * @return La lista delle sfide.
     * @throws SQLException In caso di errori SQL.
     */
    @Override
    public List<T> caricaSfide(int idPlayer) throws SQLException {

        List<Sfida> sfide = new ArrayList<>();

        try (Connection c = DriverManager.getConnection(dbURL, dbUsername, dbPassword)) {

            // Stesso nella query cerco le sfide effettuate dall'utente IdPlayer, raggruppo
            // tutte le soluzioni in un'unica stringa e ricavo l'username dell'avversario
            // Inoltre Setto il P1, i suoi tempi e il risultato della partita in funzione
            // dell'utente loggato (Che diventa P1)
            try (PreparedStatement ps = c.prepareStatement("SELECT S.Id_Documento, S.Durata, S.Id_P1, S.Id_P2, " +
                    "CASE WHEN S.Id_P1 = Me.MyId THEN S.Tempo_RispostaP1 ELSE S.Tempo_RispostaP2 END AS MioTempo, " +
                    "CASE WHEN S.Id_P1 = Me.MyId THEN S.Tempo_RispostaP2 ELSE S.Tempo_RispostaP1 END AS SuoTempo, " +
                    "CASE WHEN S.Id_P1 = Me.MyId THEN S.Risultato " +
                    "     WHEN S.Risultato = 0 THEN 1 WHEN S.Risultato = 1 THEN 0 ELSE S.Risultato END AS MioRisultato, "
                    +
                    "GROUP_CONCAT(Sol.Parola, ', ') AS ElencoSoluzioni, " +
                    "Avversario.Username AS UsernameAvversario " +
                    "FROM (SELECT ? AS MyId) Me " +
                    "JOIN Sfida S ON S.Id_P1 = Me.MyId OR S.Id_P2 = Me.MyId " +
                    "JOIN Soluzione Sol ON S.Id_Sfida = Sol.Id_Sfida " +
                    "JOIN Player Avversario ON (Avversario.Id_Utente = S.Id_P1 OR Avversario.Id_Utente = S.Id_P2) AND Avversario.Id_Utente != Me.MyId "
                    +
                    "GROUP BY S.Id_Sfida");) {

                ps.setInt(1, idPlayer);

                try (ResultSet rs = ps.executeQuery()) {

                    TextEditor te = new TextEditor();
                    te.leggiReport();

                    HashMap<Integer, String> x = te.getTitleMap();

                    while (rs.next()) {
                        String soluzioni = rs.getString("ElencoSoluzioni");

                        if (soluzioni == null)
                            soluzioni = "";

                        Sfida s = new Sfida(rs.getInt("Id_Documento"), rs.getInt("Durata"), rs.getInt("MioTempo"),
                                rs.getInt("SuoTempo"),
                                rs.getInt("id_P1"), rs.getInt("id_P2"), rs.getString("UsernameAvversario"),
                                rs.getInt("MioRisultato") == 1 ? Esito.Vittoria : Esito.Sconfitta, soluzioni);

                        s.setTitoloTesto(x.get(rs.getInt("Id_Documento")));

                        sfide.add(s);
                    }

                } catch (Exception e) {
                    System.err.println("Errore durante l'esecuzione della query: " + e);
                }
            }
        }

        return (List<T>) sfide;
    }

    /**
     * Aggiunge una sfida al DB.
     * In particolare effettua insert su Sfida e Soluzione
     * 
     * @param sfida La sfida da aggiungere.
     * @throws SQLException In caso di errori SQL.
     */
    @Override
    public void aggiungiSfida(T sfida) throws SQLException {

        if (sfida instanceof Sfida) {

            Sfida s = (Sfida) sfida;

            try (Connection c = DriverManager.getConnection(dbURL, dbUsername, dbPassword)) {

                c.setAutoCommit(false);

                String insertSfida = "INSERT INTO Sfida (Id_Documento, Durata, Id_P1, Id_P2, Id_Amm, Tempo_RispostaP1, Tempo_RispostaP2, Risultato) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                int idSfidaGenerato = -1;

                try (PreparedStatement psSfida = c.prepareStatement(insertSfida,
                        java.sql.Statement.RETURN_GENERATED_KEYS)) {

                    psSfida.setInt(1, s.getIdDocumento());
                    psSfida.setDouble(2, s.getDurata());
                    psSfida.setInt(3, s.getId1());
                    psSfida.setInt(4, s.getId2());
                    psSfida.setInt(5, Sessione.getAdmin() != null ? Sessione.getAdmin().getId() : 0);
                    psSfida.setDouble(6, s.getTRisposta1());
                    psSfida.setDouble(7, s.getTRisposta2());
                    psSfida.setInt(8, s.getRisultato().equals(Esito.Vittoria) ? 1 : 0);

                    psSfida.executeUpdate(); // Esegue l'inserimento

                    // Recupero l'id appena generato dal db
                    try (Statement stmtId = c.createStatement();
                            ResultSet rsKeys = stmtId.executeQuery("SELECT last_insert_rowid()")) {

                        if (rsKeys.next()) {
                            idSfidaGenerato = rsKeys.getInt(1); // Prende il risultato della query
                        } else {
                            throw new SQLException("Creazione sfida fallita, nessun ID ottenuto.");
                        }
                    }
                }

                if (idSfidaGenerato != -1 && s.getSoluzione() != null && !s.getSoluzione().isEmpty()) {

                    String insertSoluzione = "INSERT INTO Soluzione (Parola, Id_Sfida) VALUES (?, ?)";

                    try (PreparedStatement psSoluzione = c.prepareStatement(insertSoluzione)) {

                        String[] soluzioni = s.getSoluzione().split(",\\s*");

                        for (String ss : soluzioni) {
                            psSoluzione.setString(1, ss);
                            psSoluzione.setInt(2, idSfidaGenerato);

                            psSoluzione.addBatch();
                        }

                        // Esegue tutte le query del gruppo
                        psSoluzione.executeBatch();
                    }
                }

                // Confermo le modifiche sul db
                c.commit();

            } catch (SQLException e) {
                System.err.println("Errore durante l'inserimento della sfida: " + e.getMessage());
                throw e; // Rilanciamo l'eccezione per farla gestire al chiamante
            }
        } else {
            throw new IllegalArgumentException("Tipo utente non supportato dal metodo aggiungiSfida.");
        }
    }

}
