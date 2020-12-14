package com.pruthvi.gobraille;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class GradeTwoFragment extends Fragment {

    public GradeTwoFragment() {
        // Required empty public constructor
    }


    public static GradeTwoFragment newInstance() {
        GradeTwoFragment fragment = new GradeTwoFragment();
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grade_two, container, false);
    }
}