package com.project.moviebookingapp.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.project.moviebookingapp.ui.account.tabs.FavouritesTab;
import com.project.moviebookingapp.ui.account.tabs.ReviewedTab;
import com.project.moviebookingapp.ui.account.tabs.WatchHistoryTab;

public class ProfilePagerAdapter extends FragmentStatePagerAdapter {

    private int mNoOfTabs;

    public ProfilePagerAdapter(FragmentManager fm, int NumberOfTabs)
    {
        super(fm);
        this.mNoOfTabs = NumberOfTabs;
    }


    @Override
    public Fragment getItem(int position) {
        switch(position)
        {

            case 0:
                WatchHistoryTab tab1 = new WatchHistoryTab();
                return tab1;
            case 1:
                FavouritesTab tab2 = new FavouritesTab();
                return tab2;
            case 2:
                ReviewedTab tab3 = new ReviewedTab();
                return tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNoOfTabs;
    }
}