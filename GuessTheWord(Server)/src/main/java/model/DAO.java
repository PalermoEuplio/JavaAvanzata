package model;

// Interfaccia per la comunicazione col db
public interface DAO<T> {
    
    public T cerca(Amministratore admin, String password) throws Exception;
    
}
