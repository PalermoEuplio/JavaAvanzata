package model.db;
import java.util.List;
import model.utility.Player;
import model.utility.Sfida;

/**
 * Interfaccia per la comunicazione col db.
 */
public interface DAO<T> {
    
    public T cerca(T user, String password) throws Exception;
    
    public T registrazione(T user, String password) throws Exception;
    
    public void rimuoviPlayer(T p1) throws Exception;
    
    public List<Player> elencaTuttiPlayer()throws Exception;
    
    public List<Sfida> caricaSfide(int idPlayer) throws Exception;
    
    public void aggiungiSfida(T sf) throws Exception;
    
}
