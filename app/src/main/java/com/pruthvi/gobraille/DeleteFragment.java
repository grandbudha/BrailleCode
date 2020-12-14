package com.pruthvi.gobraille;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pruthvi.gobraille.Entity.Braille;
import com.pruthvi.gobraille.Util.BrailleUtil;
import com.pruthvi.gobraille.Util.FileUtil;
import com.pruthvi.gobraille.databinding.FragmentDeleteBinding;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.util.NoSuchElementException;

public class DeleteFragment extends Fragment {

    private final static String FILE_PATH_EXTRAS = "file_path_extras";
    private FragmentDeleteBinding mBinding;
    private Activity mParentActivity;
    private String mFilePath;
    private File mFile;
    private Bitmap mBitmap;
    private Braille mBraille;
    private FileUtil mFileUtil;

    public DeleteFragment() { /* Constructor */ }

    public static DeleteFragment newInstance(String path) {
        DeleteFragment fragment = new DeleteFragment();
        Bundle args = new Bundle();
        args.putString(FILE_PATH_EXTRAS, path);
        fragment.setArguments(args);
        return fragment;
    }

    private void showCustomDialog(){

        final String heading = "Confirmation";
        final String message = "Delete this file forever ?";

        LayoutInflater layoutInflater = (LayoutInflater)mParentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View view = layoutInflater.inflate(R.layout.custom_dailog_layout, null);
        AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(mParentActivity);
        builder.setView(view);
        builder.setCancelable(false);
        AlertDialog alertDialog =  builder.create();
        TextView headingTextview = view.findViewById(R.id.headingTextview);
        headingTextview.setText(heading);
        TextView messageTextview = view.findViewById(R.id.messageTextview);
        messageTextview.setText(message);

        Button okayButton = view.findViewById(R.id.okayButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setVisibility(View.VISIBLE);
        okayButton.setOnClickListener((v)->{

            mBraille.delete(mFilePath);
            alertDialog.dismiss();
            startActivity(new Intent(mParentActivity, MainActivity.class));
        });

        cancelButton.setOnClickListener((v)->{
            alertDialog.dismiss();
        });
        alertDialog.show();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_delete, container, (false));
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mParentActivity = getActivity();
        ((ImageActivity)getActivity()).setSupportActionBar(mBinding.toolbar);
        ((ImageActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((ImageActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        mFileUtil = new FileUtil(mParentActivity);
        mBraille = Braille.getInstance(mParentActivity);

        if (getArguments() != null){
            mFilePath = getArguments().getString(FILE_PATH_EXTRAS);
            Log.d(Constants.TAG, "DeleteFragment path => " + mFilePath);
        }

        if (mFilePath != null && !mFilePath.isEmpty()){

            mFile = new File(mFilePath);
            mBitmap = mFileUtil.toBitmap(mFile);

            Mat mat = BrailleUtil.getInstance(mBitmap).drawLines();
            Utils.matToBitmap(mat, mBitmap);
            mBinding.imageView.setImageBitmap(mBitmap);

            mBinding.deleteButton.setVisibility(View.VISIBLE);
            mBinding.deleteButton.setOnClickListener((v)->{
               showCustomDialog();
            });
        }

        mBinding.toolbar.setNavigationOnClickListener((v)->{
            getActivity().onBackPressed();
        });
    }







}