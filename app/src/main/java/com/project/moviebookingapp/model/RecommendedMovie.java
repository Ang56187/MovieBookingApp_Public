package com.project.moviebookingapp.model;

import java.util.Collections;
import java.util.List;

public class RecommendedMovie {
    private String accountID;
    private List<String> recommendedMovieIDList;

    public RecommendedMovie(String accountID){
        this.accountID = accountID;
        recommendedMovieIDList = Collections.emptyList();
    }

    public void setAccountID(String accountID) { this.accountID = accountID; }
    public void setRecommendedMovieIDList(List<String> recommendedMovieIDList) {
        this.recommendedMovieIDList = recommendedMovieIDList;
    }

    public String getAccountID() { return accountID; }
    public List<String> getRecommendedMovieIDList() {
        return recommendedMovieIDList;
    }
}
