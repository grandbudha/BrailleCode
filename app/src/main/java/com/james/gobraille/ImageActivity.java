package com.james.gobraille;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.james.gobraille.Util.FileUtil;
import com.james.gobraille.databinding.ActivityImageBinding;

import org.opencv.android.OpenCVLoader;

import java.io.File;

public class ImageActivity extends AppCompatActivity {

    private static final String INTENT_FILE_PATH_EXTRAS = "file_path_extra";
    private ActivityImageBinding mBinding;
    private ImageActivity mActivity;
    private String mFilePath;
    private Bitmap mBitmap;

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

            if (item.getItemId() == R.id.delete_bottom_nav){
                openSelectedFragment(DeleteFragment.newInstance(mFilePath));
                return true;
            }
            else {
                openSelectedFragment(GradeConversionFragment.newInstance(mFilePath));
                return true;
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