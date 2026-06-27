/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

/**
 *
 * @author euppa
 */
public abstract class NetworkConnection {
    
    private String ip;
    private int port;
    private ConnectionThread connection;    // Salvo il riferimento alla connessione
    private Consumer<Serializable> onRecive;
    
    
    // Il consumer serve per specificare che operazione fare con il messaggio
    public NetworkConnection(String ip, int port, Consumer<Serializable> onRecive){
        this.ip = ip;
        this.port = port;
        this.connection = new ConnectionThread();   // Inizializzo la classe innestata per iniziare il thread di connessione
        this.onRecive = onRecive;
    }
    
    public void connection(){
        connection.start(); // Starto il thread della connessione
    }
    
    public void disconnect() throws IOException{
        connection.s.close();
    }
    
    
    // Metodo chemanda un tipo serializzato
    public void send(Serializable data) throws IOException{
        connection.oos.writeObject(data);
    }
    
    public void handleMessage(Serializable msg){
        System.out.println(msg);
    }
    
    
    
    public abstract boolean isServer();
    
    //  Classe necessaria al thread di connessione
    class ConnectionThread extends Thread {
        
        Socket s;
        ObjectOutputStream oos;
        
        
        // Inizializzo il thread di connessione
        @Override
        public void run(){
            try (
                    Socket s = isServer() ? new ServerSocket(port).accept() : new Socket(ip,port);
                    // Si istanzia sempre prima l'outputt che l'input per evitare il deadlock,
                    // per via del client che ricerca un server chenon esiste
                    ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                    ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                ){
            
                this.s = s; // Salvola socket
                this.oos = oos;
                
                
                
                // Ciclo true per l'attesa del messaggio
                while(true){
                    
                    Serializable msg = (Serializable) ois.readObject();
                    
                    onRecive.accept(msg);
                }
                
                
                
                
                
            }catch (IOException e){System.err.println("Errore"+e);}
            catch (Exception e){System.err.println("Error2"+e);}
        }
        
        
        
        
        
        
        
    }
    
    
}
