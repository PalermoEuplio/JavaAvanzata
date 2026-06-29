/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.utility;

import model.connection.ServerConnection;

/**
 *
 * @author euppa
 */
public class Sessione {
    
    private static Amministratore adminLoggato;
    
    private static ServerConnection server;
    
    // Metodo da chiamare quando si effettua il login
    public static void setAdmin(Amministratore admin) {
        adminLoggato = admin;
    }

    // Metodo per recuperare l'amministratore nelle altre schermate
    public static Amministratore getAdmin() {
        return adminLoggato;
    }
    
    public static void setServer(ServerConnection s) {
        server = s;
    }
    
    public static ServerConnection getServer() {
        return server;
    }    

    // Metodo per fare il logout
    public static void logout() {
        adminLoggato = null;
        server=null;
    }
    
}
