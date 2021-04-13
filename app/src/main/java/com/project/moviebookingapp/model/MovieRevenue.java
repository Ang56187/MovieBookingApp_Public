package com.project.moviebookingapp.model;

import java.util.List;

public class MovieRevenue {
    private String movieName;
    private String movieID;
    private Long startDateSeconds;
    private Long endDateSeconds;
    private int ticketCount;
    private double totalMovieRevenue;

    public MovieRevenue(String movieID,String movieName, Long startDateSeconds, Long endDateSeconds){
        this.movieID = movieID;
        this.movieName = movieName;
        this.startDateSeconds = startDateSeconds;
        this.endDateSeconds = endDateSeconds;
    }

    public MovieRevenue(String movieID,String movieName, Long startDateSeconds, Long endDateSeconds,int ticketCount,
                        double totalMovieRevenue){
        this.movieID = movieID;
        this.movieName = movieName;
        this.startDateSeconds = startDateSeconds;
        this.endDateSeconds = endDateSeconds;
        this.ticketCount = ticketCount;
        this.totalMovieRevenue = totalMovieRevenue;
    }

    public String getMovieID() { return movieID; }
    public double getTotalMovieRevenue() { return totalMovieRevenue; }
    public int getTicketCount() { return ticketCount; }
    public Long getEndDateSeconds() { return endDateSeconds; }
    public Long getStartDateSeconds() { return startDateSeconds; }
    public String getMovieName() { return movieName; }

    public void setEndDateSeconds(Long endDateSeconds) { this.endDateSeconds = endDateSeconds; }
    public void setMovieName(String movieName) { this.movieName = movieName; }
    public void setStartDateSeconds(Long startDateSeconds) {
        this.startDateSeconds = startDateSeconds;
    }
    public void setTicketCount(int ticketCount) { this.ticketCount = ticketCount; }
    public void setTotalMovieRevenue(double totalMovieRevenue) {
        this.totalMovieRevenue = totalMovieRevenue;
    }
}
