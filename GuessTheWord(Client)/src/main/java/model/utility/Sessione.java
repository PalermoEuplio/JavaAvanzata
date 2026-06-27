/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.utility;

/**
 *
 * @author euppa
 */
public class Sessione {
    
    private static Player playerLoggato;
    
    // Metodo da chiamare quando si effettua il login
    public static void setPlayer(Player pl) {
        playerLoggato = pl;
    }

    // Metodo per recuperare l'amministratore nelle altre schermate
    public static Player getPlayer() {
        return playerLoggato;
    }

    // Metodo per fare il logout
    public static void logout() {
        playerLoggato = null;
    }
    
}
