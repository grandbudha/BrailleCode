package com.james.gobraille;

import android.app.Activity;
import android.graphics.Bitmap;
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

import com.james.gobraille.Util.BrailleUtil;
import com.james.gobraille.Util.DatabaseUtil;
import com.james.gobraille.Util.FileUtil;
import com.james.gobraille.databinding.FragmentGradeConversionBinding;

import java.io.File;


public class GradeConversionFragment extends Fragment {

    private static final String FILE_PATH_EXTRAS ="file_path_extras";
    private FragmentGradeConversionBinding mBinding;
    private String mFilePath;
    private Activity mParentActivity;
    private Bitmap mBitmap;
    private DatabaseUtil mDatabaseUtil;
    private BrailleUtil mBrailleUtil;

    public GradeConversionFragment() { /* Constructor */ }

    public static GradeConversionFragment newInstance(String path) {
        GradeConversionFragment fragment = new GradeConversionFragment();
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

                while (!mBrailleUtil.isConversionCompleted())
                    Thread.sleep((3000));
                final String bitsList = mBrailleUtil.getImageBits();
                result = mDatabaseUtil.gradeText(bitsList);
            }
            catch (InterruptedException e){
                //Log.d(Constants.TAG, "Exception while converting => " + e.getMessage());
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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_grade_conversion, container, (false));
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
            mBrailleUtil.setFile(new File(mFilePath).getName());
            mBrailleUtil.startConversion();
            new AsyncWork().execute();
        }

        mDatabaseUtil = DatabaseUtil.getInstance(mParentActivity);
        mBinding.toolbar.setNavigationOnClickListener((v)->{
            getActivity().onBackPressed();
        });
    }



}