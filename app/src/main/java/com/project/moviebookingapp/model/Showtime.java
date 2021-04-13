package com.project.moviebookingapp.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class Showtime{
    private String hallID;
    private String movieID;
    private Timestamp startTime;
    private Double price;

    public Showtime(){ }

    public Showtime(String hallID, String movieID,Timestamp startTime, Double price){
        this.hallID = hallID;
        this.movieID = movieID;
        this.startTime = startTime;
        this.price = price;
    }

    public String getHallID(){return hallID;}
    public String getMovieID(){return movieID;}
    public Timestamp getStartTime(){return startTime;}
    public Double getPrice(){return price;}

    public void setHallID(String hallID){ this.hallID = hallID; }
    public void setMovieID(String movieID){this.movieID = movieID;}
    public void setStartTime(Timestamp startTime){this.startTime = startTime;}
    public void setPrice(Double price){this.price = price;}

}
