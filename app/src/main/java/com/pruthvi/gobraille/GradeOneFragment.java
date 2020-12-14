package com.pruthvi.gobraille;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pruthvi.gobraille.Entity.Braille;
import com.pruthvi.gobraille.Util.BrailleUtil;
import com.pruthvi.gobraille.Util.DatabaseUtil;
import com.pruthvi.gobraille.Util.FileUtil;
import com.pruthvi.gobraille.databinding.FragmentGradeOneBinding;

import java.io.File;
import java.util.ArrayList;


public class GradeOneFragment extends Fragment {

    private static final String FILE_PATH_EXTRAS ="file_path_extras";
    private FragmentGradeOneBinding mBinding;
    private String mFilePath;
    private Activity mParentActivity;
    private Bitmap mBitmap;
    private DatabaseUtil mDatabaseUtil;
    private BrailleUtil mBrailleUtil;



    public GradeOneFragment() { /* Constructor */ }

    public static GradeOneFragment newInstance(String path) {
        GradeOneFragment fragment = new GradeOneFragment();
        Bundle args = new Bundle();
        args.putString(FILE_PATH_EXTRAS, path);
        fragment.setArguments(args);
        return fragment;
    }

    private class AsyncWork extends AsyncTask<Void, Void, Void>{

        private String result;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mBinding.progressBar.setVisibility(View.VISIBLE);
            Log.d(Constants.TAG, "AsyncWork in GradeOne started");
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {

                // Wait for conversion thread to finish
                while (!mBrailleUtil.isConversionCompleted())
                    Thread.sleep((3000));
                final ArrayList<String> bitsList = mBrailleUtil.getBrailleCharStringList();
                result = mDatabaseUtil.gradeOneText(bitsList);
              //  Log.d(Constants.TAG, "Its already done");
            }
            catch (InterruptedException e){

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mBinding.progressBar.setVisibility(View.GONE);
            mBinding.scrollView.setVisibility(View.VISIBLE);
            mBinding.convertedTextView.setText(result);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_grade_one, container, (false));
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mParentActivity = getActivity();
        ((ImageActivity)getActivity()).setSupportActionBar(mBinding.toolbar);
        ((ImageActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((ImageActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (getArguments() != null){
            mFilePath = getArguments().getString(FILE_PATH_EXTRAS);
            if (mFilePath != null && !mFilePath.isEmpty())
                mBitmap = new FileUtil(mParentActivity).toBitmap(new File(mFilePath));
            mBrailleUtil = BrailleUtil.getInstance(mBitmap);
            mBrailleUtil.startConversion();
            new AsyncWork().execute();

            Log.d(Constants.TAG, "DeleteFragment path => " + mFilePath);
        }

        mDatabaseUtil = DatabaseUtil.getInstance(mParentActivity);
        mBinding.toolbar.setNavigationOnClickListener((v)->{
            getActivity().onBackPressed();
        });
    }



}