package com.project.moviebookingapp.model;

public class BookedConcession {
    //for saving in bookedConcessions collection
    private String concessionID;
    private String ticketID;
    private int quantity;

    //for saving in bookedConcessions collection
    public BookedConcession(String concessionID, String ticketID, int quantity){
        this.concessionID = concessionID;
        this.ticketID = ticketID;
        this.quantity = quantity;
    }

    public int getQuantity(){ return quantity; }
    public String getConcessionID() { return concessionID; }
    public String getTicketID() { return ticketID; }

    public void setConcessionID(String concessionID) { this.concessionID = concessionID; }
    public void setTicketID(String ticketID) { this.ticketID = ticketID; }
    public void setQuantity(int quantity){
        this.quantity = quantity;
    }

}
