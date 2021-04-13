package com.project.moviebookingapp.model;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class Ticket {
    private String accountID;
    private String movieID;
    private String showtimeID;
    private String movieName;
    private String moviePosterURL;
    private boolean isUsed;
    private boolean isConcessionBooked;
    private Timestamp showtime;
    private String hallID;
    private String ticketID;
    private List<String> seat= new ArrayList<>();
    private double seatPrice;

    public Ticket(){}

    //used for queryMoviesByStatus
    public Ticket(String movieID,boolean isUsed){
        this.movieID = movieID;
        this.isUsed = isUsed;
    }

    //for retrieving tickets
    public Ticket(String accountID, String showtimeID, String movieID,String movieName,
                  boolean isUsed, Timestamp showtime, List<String> seat,
                  String moviePosterURL, String hallID){
        this.accountID = accountID;
        this.showtimeID = showtimeID;
        this.movieID = movieID;
        this.isUsed = isUsed;
        this.seat = seat;
        this.movieName = movieName;
        this.showtime = showtime;
        this.moviePosterURL = moviePosterURL;
        this.hallID = hallID;
    }

    //for saving tickrt
    public Ticket(String accountID, String showtimeID, String movieID,String movieName,
                  boolean isUsed, Timestamp showtime, List<String> seat,
                  String moviePosterURL, String hallID,double seatPrice){
        this.accountID = accountID;
        this.showtimeID = showtimeID;
        this.movieID = movieID;
        this.isUsed = isUsed;
        this.seat = seat;
        this.movieName = movieName;
        this.showtime = showtime;
        this.moviePosterURL = moviePosterURL;
        this.hallID = hallID;
        this.seatPrice = seatPrice;
    }

    public String getAccountID(){return accountID;}
    public String getShowtimeID(){ return showtimeID; }
    public String getMovieID() { return movieID; }
    public String getMovieName() {return movieName; }
    public List<String> getSeat() { return seat; }
    public Timestamp getShowtime() { return showtime; }
    public boolean getIsUsed() { return isUsed; }
    public String getMoviePosterURL(){ return moviePosterURL; }
    public String getHallID(){ return hallID; }
    public boolean getIsConcessionBooked() { return isConcessionBooked; }
    //not getTicketID() to avoid ticketID getting saved in firebase
    public String ticketID() { return ticketID; }
    public double getSeatPrice() { return seatPrice; }

    public void setAccountID(String accountID) { this.accountID = accountID; }
    public void setShowtimeID(String showtimeID) { this.showtimeID = showtimeID; }
    public void setMovieID(String movieID) { this.movieID = movieID; }
    public void setMovieName(String movieName) { this.movieName = movieName; }
    public boolean setIsUsed() { return isUsed; }
    public void setShowtime(Timestamp showtime) { this.showtime = showtime; }
    public void setSeat(List<String> seat) { this.seat = seat; }
    public void setMoviePosterURL(String moviePosterURL){ this.moviePosterURL = moviePosterURL; }
    public void setHallID(String hallID) { this.hallID = hallID; }
    public void setConcessionBooked(boolean concessionBooked) {
        isConcessionBooked = concessionBooked; }
    public void setTicketID(String ticketID) { this.ticketID = ticketID; }
    public void setSeatPrice(double seatPrice) { this.seatPrice = seatPrice; }
}
