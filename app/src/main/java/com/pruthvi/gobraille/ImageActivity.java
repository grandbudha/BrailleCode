package com.pruthvi.gobraille;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.pruthvi.gobraille.Util.BrailleUtil;
import com.pruthvi.gobraille.Util.FileUtil;
import com.pruthvi.gobraille.databinding.ActivityImageBinding;

import org.opencv.android.OpenCVLoader;

import java.io.File;

public class ImageActivity extends AppCompatActivity {

    private static final String INTENT_FILE_PATH_EXTRAS = "file_path_extra";
    private ActivityImageBinding mBinding;
    private ImageActivity mActivity;
    private String mFilePath;
    private Bitmap mBitmap;

    // Need to add back button
    // add previous old code
    // send the work


    static {
        if(OpenCVLoader.initDebug()){
            Log.d(Constants.TAG, "OpenCV initialized successfully");
        }
    }

    private void openSelectedFragment(Fragment fragment){

        if (mFilePath != null && !mFilePath.isEmpty()) {

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(mBinding.frameLayoutContainer.getId(), fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        else {
            Constants.showToast(mActivity, "No file found for operation.");
        }
    }

    public static Intent newInstance(Context context, File file) {
        Intent intent = new Intent(context, ImageActivity.class);
        intent.putExtra(INTENT_FILE_PATH_EXTRAS, file.getAbsolutePath());
        return intent;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        mBinding = DataBindingUtil.setContentView(mActivity, R.layout.activity_image);


        if (getIntent().getExtras() != null){
            mFilePath = getIntent().getExtras().getString(INTENT_FILE_PATH_EXTRAS);
            mBitmap = new FileUtil(mActivity).toBitmap(new File(mFilePath));
        }

        mBinding.bottomNavBar.setOnNavigationItemSelectedListener((item)->{

            switch (item.getItemId()){

                case R.id.delete_bottom_nav:{
                    openSelectedFragment(DeleteFragment.newInstance(mFilePath));
                    return true;
                }

                case R.id.grade_one_bottom_nav:{
                    openSelectedFragment(GradeOneFragment.newInstance(mFilePath));
                    return true;
                }

                case R.id.grade_two_bottom_nav:{
                    openSelectedFragment(GradeTwoFragment.newInstance());
                    return true;
                }
                default: return false;
            }
        });

        // Open delete ones user clicks on braille image
        mBinding.bottomNavBar.setSelectedItemId(R.id.delete_bottom_nav);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}