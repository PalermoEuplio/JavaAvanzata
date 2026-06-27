/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author euppa
 */
public class Sessione {
    
    private static Amministratore adminLoggato;
    
    // Metodo da chiamare quando si effettua il login
    public static void setAdmin(Amministratore admin) {
        adminLoggato = admin;
    }

    // Metodo per recuperare l'amministratore nelle altre schermate
    public static Amministratore getAdmin() {
        return adminLoggato;
    }

    // Metodo per fare il logout
    public static void logout() {
        adminLoggato = null;
    }
    
}
