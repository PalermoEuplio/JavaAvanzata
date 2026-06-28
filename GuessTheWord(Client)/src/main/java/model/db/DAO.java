package model.db;

// Interfaccia per la comunicazione col db

import java.util.List;

public interface DAO<T> {
    
    public T cerca(T user, String password) throws Exception;
    
    public T registrazione(T user, String password) throws Exception;
    
    public List<T> elencaTuttiPlayer()throws Exception;
    
    public List<T> caricaSfide(int idPlayer) throws Exception;
    
    
    
}
