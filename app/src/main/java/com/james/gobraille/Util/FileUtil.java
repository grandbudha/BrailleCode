package com.james.gobraille.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.james.gobraille.Constants;

import java.io.File;
import me.shaohui.advancedluban.Luban;
import me.shaohui.advancedluban.OnCompressListener;


public class FileUtil {

    private final String DIR_BRAILLE = "braille";
    private final String DIR_GRADE_1 = "grade_1";
    private final String DIR_GRADE_2 = "grade_2";

    private Context context;
    private String dataDir;
    private String brailleImageDirPath;
    private String gradeOneDirPath;
    private String gradeTwoDirPath;
    private String authority;

    public FileUtil(Context context){

        this.context = context;
        this.dataDir = getFilesDir();
        this.brailleImageDirPath = dataDir + File.separator + DIR_BRAILLE;
        this.gradeOneDirPath = dataDir + File.separator + DIR_GRADE_1;
        this.gradeTwoDirPath = dataDir + File.separator + DIR_GRADE_2;
        this.authority = context.getApplicationContext().getPackageName() + ".provider";
    }


    public String getBrailleImageDirPath() {
        return brailleImageDirPath;
    }

    public String getAuthority(){
        return this.authority;
    }

    private String getFilesDir(){
        return context.getFilesDir().getAbsolutePath();
    }

    private void makeNewDir(String path){
        File file = new File(path);
        if (!file.exists()) file.mkdir();
    }

    public void init(){
        makeNewDir(brailleImageDirPath);
        makeNewDir(gradeOneDirPath);
        makeNewDir(gradeTwoDirPath);
    }

    public void compressFile(File file){

        new Thread(()->{

            Luban.compress(context, file)
                    .setMaxHeight(1920)
                    .setMaxWidth(1080)
                    .setMaxSize(1000)
                    .putGear(Luban.CUSTOM_GEAR)
                    .launch(new OnCompressListener() {

                @Override
                public void onStart() {
                    Log.d(Constants.TAG, "File compressor started");
                }
                @Override
                public void onSuccess(File file) {
                }
                @Override
                public void onError(Throwable e) {
                    Constants.showToast((AppCompatActivity) context, ("Error while storing selected image."));
                }
            });
        }).start();
    }

    public Bitmap toBitmap(File file){
         Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), new BitmapFactory.Options());
         return Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
    }

}
