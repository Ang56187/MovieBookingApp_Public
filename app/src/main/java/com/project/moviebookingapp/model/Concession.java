package com.project.moviebookingapp.model;

import java.io.Serializable;

public class Concession implements Serializable {
    private String concessionName;
    private String concessionImageURL;
    private Double concessionPrice;
    private int quantity;
    private String concessionID;

    public Concession(){}

    public Concession(String concessionName , String concessionImageURL, Double concessionPrice){
        this.concessionName = concessionName;
        this.concessionImageURL = concessionImageURL;
        this.concessionPrice = concessionPrice;
    }

    public String getConcessionName() { return concessionName; }
    public String getConcessionImageURL(){ return concessionImageURL;}
    public Double getConcessionPrice() { return concessionPrice; }
    public int getQuantity(){ return quantity; }
    public String getConcessionID() { return concessionID; }

    public void setConcessionName(String concessionName) {
        this.concessionName = concessionName;
    }
    public void setConcessionImageURL(String concessionImageURL) {
        this.concessionImageURL = concessionImageURL;
    }
    public void setConcessionPrice(Double concessionPrice) {
        this.concessionPrice = concessionPrice;
    }
    public void setQuantity(int quantity){
        this.quantity = quantity;
    }
    public void setConcessionID(String concessionID) { this.concessionID = concessionID; }

    //for debugging
    public String toString(){
        return String.format("Name "+concessionName+ "price"+concessionPrice+ " quantity "+quantity );
    }
}
