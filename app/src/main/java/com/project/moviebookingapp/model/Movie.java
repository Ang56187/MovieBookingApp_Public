package com.project.moviebookingapp.model;

import com.google.firebase.Timestamp;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Movie {
    private List<String> actor;
    private String movieID;
    private String movieName_lowerCase;//set lower case for searching purposes, as it is case sensitive
    private List<String> genre;
    private String director;
    private Timestamp startDate;
    private Timestamp endDate;
    private String movieName;
    private String moviePosterURL;
    private int runtime;
    private String synopsis;
    private String trailerURL;

    private Boolean isUsed;

    private ArrayList<Double> ratingScore = new ArrayList<>();

    public Movie(){}

    //actually used
    public Movie(List<String> actor, List<String> genre,String director, Timestamp startDate,
                 Timestamp endDate, String movieName, String moviePosterURL, int runtime,
                 String synopsis, String trailerURL){

        this.actor = actor;
        this.genre = genre;
        this.director = director;
        this.startDate = startDate;
        this.endDate = endDate;
        this.movieName = movieName;
        this.moviePosterURL = moviePosterURL;
        this.runtime = runtime;
        this.synopsis = synopsis;
        this.trailerURL = trailerURL;
    }
    //overlapped,used when adding new movies
    public Movie(List<String> actor, List<String> genre,String director, Timestamp startDate,
                 Timestamp endDate, String movieName_lowerCase,String movieName,
                 String moviePosterURL, int runtime, String synopsis, String trailerURL){
        this.movieName_lowerCase = movieName_lowerCase;
        this.actor = actor;
        this.genre = genre;
        this.director = director;
        this.startDate = startDate;
        this.endDate = endDate;
        this.movieName = movieName;
        this.moviePosterURL = moviePosterURL;
        this.runtime = runtime;
        this.synopsis = synopsis;
        this.trailerURL = trailerURL;
    }

    public List<String> getActor() {return actor;}
    public String getMovieID(){ return movieID;}
    public String getDirector() {return director;}
    public Timestamp getStartDate(){return startDate;}
    public Timestamp getEndDate(){return endDate;}
    public List<String> getGenre() {return genre;}
    public String getMovieName(){return movieName;}
    public String getMovieName_lowerCase() { return movieName_lowerCase; }
    public int getRuntime(){return runtime;}
    public String getSynopsis(){return synopsis;}
    public String getMoviePosterURL(){return moviePosterURL;}
    public String getTrailerURL() {return trailerURL;}

    //not getIsUsed() to avoid isUsed getting saved in firebase
    public Boolean isUsed() { return isUsed; }
    public ArrayList<Double> getRatingScore() {return ratingScore;}

    public void setActor(List<String> actor){ this.actor = actor; }
    public void setMovieID(String movieID){this.movieID = movieID;}
    public void setDirector(String director){this.director = director;}
    public void setStartDate(Timestamp startDate){this.startDate = startDate;}
    public void setEndDate(Timestamp endDate){this.endDate = startDate;}
    public void setGenre(List<String> genre){ this.genre = genre; }
    public void setMovieName(String movieName){this.movieName = movieName;}
    public void setMovieName_lowerCase(String movieName_lowerCase){
        this.movieName_lowerCase = movieName_lowerCase;
    }
    public void setRuntime(int runtime){this.runtime = runtime;}
    public void setMoviePosterURL(String moviePosterURL){this.moviePosterURL = moviePosterURL;}
    public void setTrailerURL(String trailerURL){this.trailerURL = trailerURL;}

    public void setUsed(Boolean used) { isUsed = used; }
    public void setRatingScore(ArrayList<Double> ratingScore){this.ratingScore = ratingScore;}

//https://firebase.googleblog.com/2014/04/best-practices-arrays-in-firebase.html
//https://stackoverflow.com/questions/50233281/how-to-get-an-array-from-firestore
//https://dev.to/nabettu/array-contains-any-was-created-in-firebase-firestore-so-i-will-explain-it-with-a-sample-site-4g9
}