package com.project.moviebookingapp.custom;

import android.icu.text.NumberFormat;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

public class TextViewFormat {
    //for rating textview
    public static DecimalFormat df = new DecimalFormat("#.0");
    public static String ratingCountFormat(ArrayList<Double> list){
        return "("+list.size()+")";
    }

    //for date toggle text
    public static SimpleDateFormat getLocalDateToggleFormat(){
        SimpleDateFormat dateToggleFormat = new SimpleDateFormat("E\n dd MMM");
        TimeZone malaysianTimeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur");
        dateToggleFormat.setTimeZone(malaysianTimeZone);
        return dateToggleFormat;
    }

    //for date text
    public static SimpleDateFormat getLocalDateFormat(){
        SimpleDateFormat dateToggleFormat = new SimpleDateFormat("yyyy-MM-dd");
        TimeZone malaysianTimeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur");
        dateToggleFormat.setTimeZone(malaysianTimeZone);
        return dateToggleFormat;
    }
    //for time comparison
    public static SimpleDateFormat getCompareDateTime(){
        SimpleDateFormat dateToggleFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        TimeZone malaysianTimeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur");
        dateToggleFormat.setTimeZone(malaysianTimeZone);
        return dateToggleFormat;
    }

    //to show at checkout pages
    public static SimpleDateFormat getLocalDateTime(){
        SimpleDateFormat dateToggleFormat = new SimpleDateFormat("E yyyy-MM-dd,hh:mm a");
        TimeZone malaysianTimeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur");
        dateToggleFormat.setTimeZone(malaysianTimeZone);
        return dateToggleFormat;
    }

    //for time
    public static SimpleDateFormat getLocalTimeFormat(){
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        TimeZone malaysianTimeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur");
        timeFormat.setTimeZone(malaysianTimeZone);
        return timeFormat;
    }

    //for price format
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String retrieveLocalCurrencyFormat(Double price){
        String stringPrice = NumberFormat.getCurrencyInstance(new Locale("ms", "MY"))
                .format(price);
        return stringPrice;
    }

}
