package com.project.moviebookingapp.model;

public class Favourite {
    private String accountID;
    private String movieID;

    public Favourite(String accountID,String movieID){
        this.accountID = accountID;
        this.movieID = movieID;
    }

    public String getAccountID() { return accountID; }
    public String getMovieID() { return movieID; }

    public void setAccountID(String accountID) { this.accountID = accountID; }
    public void setMovieID(String movieID) { this.movieID = movieID; }

}
