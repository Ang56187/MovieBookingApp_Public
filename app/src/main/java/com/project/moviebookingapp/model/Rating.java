package com.project.moviebookingapp.model;

public class Rating {
    private String accountID;
    private String movieID;
    private Double ratingScore;
    private String textReview;
    private String userName;
    private String profileImgURL;

    public Rating(){}

    public Rating(String accountID, String movieID,Double ratingScore,String textReview ,
                  String userName, String profileImgURL){
        this.accountID = accountID;
        this.movieID = movieID;
        this.ratingScore = ratingScore;
        this.textReview = textReview;
        this.userName = userName;
        this.profileImgURL = profileImgURL;
    }

    public String getAccountID() { return accountID; }
    public String getMovieID() { return movieID; }
    public Double getRatingScore() { return ratingScore; }
    public String getTextReview(){ return textReview; }
    public String getProfileImgURL() { return profileImgURL; }
    public String getUserName() { return userName; }

    public void setAccountID(String accountID) { this.accountID = accountID; }
    public void setMovieID(String movieID) { this.movieID = movieID; }
    public void setRatingScore(Double ratingScore) { this.ratingScore = ratingScore; }
    public void setTextReview(String textReview) { this.textReview = textReview; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setProfileImgURL(String profileImgURL) { this.profileImgURL = profileImgURL; }

}
