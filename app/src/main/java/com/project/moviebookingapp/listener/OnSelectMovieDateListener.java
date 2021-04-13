package com.project.moviebookingapp.listener;

import com.google.firebase.Timestamp;
import com.project.moviebookingapp.model.Showtime;

//interface to pass the movie date data to the activity class, which then determines what dataset to pass
//to the movietimeadapter
//used for MovieDetailActivity.java and MovieSeatSelectionActivity.
public interface OnSelectMovieDateListener {
    //i use position as index to select the date
    //i would not pass the timestamp and try to match it, as it can complicate matters
    void onDateCallBack(Timestamp time,int i);

    void onTimeCallBack(Timestamp time,int i);

    void onTimeCallBack(int i,
                        String showtimeID, Showtime showtime);

}
