package com.james.gobraille;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.james.gobraille.Entity.Braille;
import com.james.gobraille.Util.DatabaseUtil;
import com.james.gobraille.Util.FileUtil;
import com.james.gobraille.Util.SharedPreferenceUtil;

public class SplashActivity extends AppCompatActivity {

    private final int REQUEST_PERMISSION_CODE = 0xabc;
    private SplashActivity mActivity;
    private SharedPreferenceUtil mSharedPrefernceUtil;
    private FileUtil mFileUtil;
    private final String[] REQUIRED_USER_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private String[] mUserPermission;
    private int mUserPermissionIndex;

    private int checkPermissions(){

        for (int i = 0; i < REQUIRED_USER_PERMISSIONS.length; ++i){
            if (ActivityCompat.checkSelfPermission(mActivity, REQUIRED_USER_PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED){
                mUserPermission[mUserPermissionIndex++] = REQUIRED_USER_PERMISSIONS[i];
            }
        }
        return mUserPermissionIndex;
    }// end
    private LinearLayout mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mActivity = this;
        mSharedPrefernceUtil = new SharedPreferenceUtil(mActivity);
        mFileUtil = new FileUtil(mActivity);
        mUserPermission = new String[REQUIRED_USER_PERMISSIONS.length];
        mUserPermissionIndex = 0;
        mProgressBar = findViewById(R.id.progressBar);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (checkPermissions() > 0){
            ActivityCompat.requestPermissions(mActivity, mUserPermission,REQUEST_PERMISSION_CODE);
        }
        else {
            new Async().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
      //  super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            new Async().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else {
            Constants.showToast(mActivity, ("You won't like if I crash, allow permissions."));
        }
    }

    private class Async extends AsyncTask<Void, Void, Void>{

        private void init(){

            if (mSharedPrefernceUtil.get(SharedPreferenceUtil.INIT).equals(Constants.NULL)){
                mFileUtil.init();
                DatabaseUtil.getInstance(mActivity).initDatabase();
                mSharedPrefernceUtil.add(SharedPreferenceUtil.INIT, Constants.NOT_NULL);
            }
            Braille.getInstance(mActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                init();
                Thread.sleep(2000);
            }
            catch (Exception e){}
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressBar.setVisibility(View.GONE);
            startActivity(new Intent((SplashActivity.this), MainActivity.class));
        }
    }

}