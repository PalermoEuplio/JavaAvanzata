/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.utility;

import model.connection.ClientConnection;

/**
 *
 * @author euppa
 */
public class Sessione {
    
    private static Player playerLoggato;
    
    private static ClientConnection client;
    
    // Metodo da chiamare quando si effettua il login
    public static void setPlayer(Player pl) {
        playerLoggato = pl;
    }

    // Metodo per recuperare l'amministratore nelle altre schermate
    public static Player getPlayer() {
        return playerLoggato;
    }
    
    public static void setClient(ClientConnection c) {
        client = c;
    }
    
    public static ClientConnection getClient() {
        return client;
    }

    // Metodo per fare il logout
    public static void logout() {
        playerLoggato = null;
    }
    
}
