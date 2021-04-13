package com.project.moviebookingapp.listener;

//interface to pass the movie seat data to the activity class, which then determines what dataset to pass
//to the movieseatadapter
//used for MovieSeatSelectionActivity.java
public interface OnSelectSeatListener {
    void onSeatCallBack(String s);
}
