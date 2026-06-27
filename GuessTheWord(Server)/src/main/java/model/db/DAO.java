package model.db;

// Interfaccia per la comunicazione col db

import java.util.List;

public interface DAO<T> {
    
    public T cerca(T admin, String password) throws Exception;
    
    public void rimuoviPlayer(T p1) throws Exception;
    
    public List<T> elencaTuttiPlayer()throws Exception;
    
    public void aggiungiSfida(T sf) throws Exception;
    
}
