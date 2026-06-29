/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.game;

import java.io.Serializable;

/**
 *
 * @author euppa
 */
public class Sfida implements Serializable{
    
    private String titoloTesto;
    private int idDocumento;
    private double durata;
    private double tRisposta1;
    private double tRisposta2;
    private int id1;
    private int id2;
    private String oppUsername;
    private String risultato;
    private String soluzione;

    public Sfida(int idDocumento, double durata, double tRisposta1, double tRisposta2, int id1, int id2, String oppUsername, String risultato, String soluzione) {
        this.idDocumento = idDocumento;
        this.durata = durata;
        this.tRisposta1 = tRisposta1;
        this.tRisposta2 = tRisposta2;
        this.id1 = id1;
        this.id2 = id2;
        this.oppUsername=oppUsername;
        this.risultato = risultato;
        this.soluzione = soluzione;
    }

    public String getTitoloTesto() {
        return titoloTesto;
    }

    public int getIdDocumento() {
        return idDocumento;
    }

    public double getDurata() {
        return durata;
    }

    public double getTRisposta1() {
        return tRisposta1;
    }

    public double getTRisposta2() {
        return tRisposta2;
    }

    public int getId1() {
        return id1;
    }

    public int getId2() {
        return id2;
    }

    public String getOppUsername() {
        return oppUsername;
    }

    public String getRisultato() {
        return risultato;
    }

    public String getSoluzione() {
        return soluzione;
    }

    public void setTitoloTesto(String titoloTesto) {
        this.titoloTesto = titoloTesto;
    }

    public void setIdDocumento(int idDocumento) {
        this.idDocumento = idDocumento;
    }

    public void setDurata(double durata) {
        this.durata = durata;
    }

    public void settRisposta1(double tRisposta1) {
        this.tRisposta1 = tRisposta1;
    }

    public void settRisposta2(double tRisposta2) {
        this.tRisposta2 = tRisposta2;
    }

    public void setId1(int id1) {
        this.id1 = id1;
    }

    public void setId2(int id2) {
        this.id2 = id2;
    }

    public void setOppUsername(String oppUsername) {
        this.oppUsername = oppUsername;
    }

    public void setRisultato(String risultato) {
        this.risultato = risultato;
    }

    public void setSoluzione(String soluzione) {
        this.soluzione = soluzione;
    }

    @Override
    public String toString() {
        return titoloTesto + ", " + idDocumento + ", " + durata + ", " + tRisposta1 + ", " + 
                tRisposta2 + ", " + id1 + ", " + id2 + ", " + oppUsername + ", " + risultato + ", " + soluzione;
    }
}
