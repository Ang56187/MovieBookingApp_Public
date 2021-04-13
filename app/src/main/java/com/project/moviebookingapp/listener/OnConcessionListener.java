package com.project.moviebookingapp.listener;

import com.project.moviebookingapp.model.Concession;

import java.util.ArrayList;

public interface OnConcessionListener {
    void concessionCallback(ArrayList<Concession> arr);
}
