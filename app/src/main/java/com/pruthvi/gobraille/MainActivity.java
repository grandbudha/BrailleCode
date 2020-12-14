package com.pruthvi.gobraille;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.MemoryFile;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.pruthvi.gobraille.Entity.Braille;
import com.pruthvi.gobraille.Util.FileUtil;
import com.pruthvi.gobraille.Util.UploadImageUtil;
import com.pruthvi.gobraille.databinding.ActivityMainBinding;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final int TOTAL_COLUMNS_IN_GRID = 3;
    private final int UPLOAD_IMAGE_REQUEST_CODE = 0xbcd;
    private final int SCAN_IMAGE_REQUEST_CODE = 0xcde;

    private MainActivity mActivity;
    private ActivityMainBinding mBinding;
    private BottomSheetBehavior mBottomSheetBehaviour;
    private FileUtil mFileUtil;
    private File mFile;
    private GridViewAdapter mAdapter;
    private Braille mBraille;
    public static boolean isVisible;
    
    private void setBottomSheetSize(){
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mBinding.uploadScanSheet.bottomSheetParentView.getLayoutParams();
        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        mBinding.uploadScanSheet.bottomSheetParentView.setLayoutParams(layoutParams);
    }// end

    private void uploadImage(){

        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

            Intent intent =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, UPLOAD_IMAGE_REQUEST_CODE);
        }
        else {
            Constants.showToast(mActivity, ("Required permission to access media files."));
        }
    }

    private void scanImage(){

        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){

            try {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                mFile = mBraille.getNewBrailleFile();
                Uri uri = FileProvider.getUriForFile(mActivity, mFileUtil.getAuthority(), mFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(intent, SCAN_IMAGE_REQUEST_CODE);
            }
            catch (IllegalArgumentException e){
                Constants.showToast(mActivity, ("Camera device busy."));
                Log.wtf(Constants.TAG, "Exception while creating new file => " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
        else {
            Constants.showToast(mActivity, ("Required permission to capture image using camera."));
        }
    }

    private void showCustomDialog(){

        final String heading = "About Braille";
        final String message = "Braille is a tactile writing system used by people who are visually impaired.\n\n" +
                "It is traditionally written with embossed paper.\n\n" +
                "They can write braille with the original slate and stylus or type it on a braille writer, " +
                "such as a portable braille note-taker or computer that prints with a braille embosser.\n\n" +
                "Braille characters have rectangular blocks called cells that have tiny bumps called raised dots." +
                "The number and arrangement of these dots distinguish one character from another. Since the various braille alphabets originated as " +
                "transcription codes for printed writing, " +
                "the mappings vary from " +
                "language to language, and even within one; \n\n" +
                "In English Braille there are three levels of encoding: \n" +
                "Grade 1 – a letter-by-letter transcription used for basic literacy; \n" +
                "Grade 2 – an addition of abbreviations and contractions; \n" +
                "and Grade 3 – various non-standardized personal stenography.\n";

        LayoutInflater layoutInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View view = layoutInflater.inflate(R.layout.custom_dailog_layout, null);
        AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setView(view);
        builder.setCancelable(false);
        AlertDialog alertDialog =  builder.create();
        TextView headingTextview = view.findViewById(R.id.headingTextview);
        headingTextview.setText(heading);
        TextView messageTextview = view.findViewById(R.id.messageTextview);
        messageTextview.setText(message);

        Button okayButton = view.findViewById(R.id.okayButton);
        okayButton.setOnClickListener((v)->{
            alertDialog.dismiss();
        });
        alertDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;
        mBinding = DataBindingUtil.setContentView(mActivity, R.layout.activity_main);
        mFileUtil = new FileUtil(mActivity);
        mBraille = Braille.getInstance(mActivity.getApplicationContext());

        setSupportActionBar(mBinding.toolbar);
        mBinding.toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mBinding.toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        mBinding.toolbar.setTitle("Braille");

        mBottomSheetBehaviour = BottomSheetBehavior.from(mBinding.uploadScanSheet.bottomSheetParentView);
        setBottomSheetSize();

        mAdapter = new GridViewAdapter(mActivity, mBraille.getFileList());
        mBinding.recyclerList.setLayoutManager(new GridLayoutManager(mActivity, TOTAL_COLUMNS_IN_GRID, GridLayoutManager.VERTICAL, (false)));
        mBinding.recyclerList.setAdapter(mAdapter);


        mBinding.uploadScanSheet.uploadBottomSheet.setOnClickListener((v)->{
            mBottomSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            uploadImage();
        });

        mBinding.uploadScanSheet.scanBottomSheet.setOnClickListener((v)->{
            mBottomSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            scanImage();
        });

        mBinding.addButton.setOnClickListener((v)->{
            mBottomSheetBehaviour.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        });

        mBinding.uploadScanSheet.closeBottomSheet.setOnClickListener((v)->{
            mBottomSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });

        mBraille.refresh(()->{
            runOnUiThread(()->{
                mAdapter.notifyDataSetChanged();
            });
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        isVisible = true;

        if (requestCode == UPLOAD_IMAGE_REQUEST_CODE && resultCode == RESULT_OK){

            if (data != null && UploadImageUtil.save(mActivity, data.getData(), mBraille))
                Constants.showToast(mActivity, ("File saved successfully"));
            else Constants.showToast(mActivity, ("Error while storing selected image."));
        }
        else if (requestCode == SCAN_IMAGE_REQUEST_CODE){

            if (resultCode == RESULT_OK){
                mBraille.add(mFile);
                Constants.showToast(mActivity, ("File saved successfully"));
            }
            else {
                Log.d(Constants.TAG, "File deleted");
                mFile.delete();
            }
            mFile = null;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.toolbarInfo){
            showCustomDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private class GridViewAdapter extends RecyclerView.Adapter<GridViewAdapter.MyView>{

        private Context context;
        private int screenH;
        private int screenW;
        private ArrayList<File> list;

        public GridViewAdapter(Context context, ArrayList<File> list){
            this.context = context;
            this.list = list;
            getScreenMetrics();
        }

        private void getScreenMetrics(){
            DisplayMetrics displayMetrics = new DisplayMetrics();
            mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            screenH = displayMetrics.heightPixels;
            screenW = displayMetrics.widthPixels;
        }// end

        @NonNull
        @Override
        public MyView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(context).inflate(R.layout.image_grid_layout, parent, (false));
            return new MyView(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MyView holder, int position) {

            final File currentFile = list.get(position);
            Glide.with(context)
                    .load(currentFile)
                    .centerCrop()
                    .apply(new RequestOptions().override((screenW/TOTAL_COLUMNS_IN_GRID), screenH/5))
                    .into(holder.imageView);

            holder.imageView.setOnClickListener((v)->{
                startActivity(ImageActivity.newInstance(mActivity, currentFile));
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        private class MyView extends RecyclerView.ViewHolder {

            private ImageView imageView;
            private LinearLayout parentView;

            public MyView(@NonNull View itemView) {
                super(itemView);
                parentView = itemView.findViewById(R.id.parentView);
                imageView = itemView.findViewById(R.id.imageView);
            }
        }

    }


}