package com.project.moviebookingapp.custom;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.View;
import android.view.animation.AlphaAnimation;

public class CustomAnimations {

    //for back buttons
    public static AlphaAnimation transparentClickAnim = new AlphaAnimation(1F, 0.5F);

    //for ripple animation
    public static RippleDrawable getRippleBackgroundDrawable(int pressedColor, Drawable backgroundDrawable)
    {
        RippleDrawable rd =  new RippleDrawable(getPressedState(pressedColor),backgroundDrawable,null);
        rd.setRadius(30);
        return rd;
    }

    public static ColorStateList getPressedState(int pressedColor)
    {
        return new ColorStateList(new int[][]{ new int[]{}},new int[]{pressedColor});
    }

    //constructor
    public CustomAnimations(){
        transparentClickAnim.setDuration(300);
    }

}
