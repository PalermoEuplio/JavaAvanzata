package model;

import java.util.List;

// Interfaccia per la comunicazione col db
public interface DAO<T> {
    
    
    public void aggiungi(T el) throws Exception;
    
    public void rimuovi(T el) throws Exception;
    
    public void aggiorna(T el) throws Exception;
    
    public T cerca(String key) throws Exception;
    
    public List<T> elencaTutti()throws Exception;
    
}
