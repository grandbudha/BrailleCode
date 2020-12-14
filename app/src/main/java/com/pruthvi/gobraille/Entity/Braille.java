package com.pruthvi.gobraille.Entity;

import android.content.Context;
import android.util.Log;

import com.pruthvi.gobraille.Constants;
import com.pruthvi.gobraille.Interface.RefreshView;
import com.pruthvi.gobraille.MainActivity;
import com.pruthvi.gobraille.Util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;

public class Braille {

    private Context context;
    private static Braille instance;
    private FileUtil fileUtil;
    private ArrayList<File> fileList;
    private String braillePath;
    private RefreshView refreshView;


    private Braille(Context context){
        this.context = context;
        this.fileUtil = new FileUtil(context);
        this.fileList = new ArrayList<>();
        this.braillePath = fileUtil.getBrailleImageDirPath();
        init();
    }

    public void refresh(RefreshView ref){
        refreshView = ref;
    }

    public File getNewBrailleFile(){
        return new File((this.braillePath + File.separator + Constants.getDateTime()+".jpg"));
    }

    public ArrayList<File> getFileList() {
        return fileList;
    }

    private void init(){
        File directory = new File(this.braillePath);
        File[] array = directory.listFiles();
        if (array != null)
            fileList.addAll(Arrays.asList(array));
        Log.d(Constants.TAG, "File length => " + array.length);
    }

    public void add(File file){
        this.fileList.add(file);
        //fileUtil.compressFile(file);
        if (MainActivity.isVisible) refreshView.refresh();
    }

    public void delete(String filePath){

        final int size = fileList.size();

        for (int i = 0; i < size; ++i){

            File current = fileList.get(i);
            if (current.getAbsolutePath().equals(filePath)){
                fileList.remove(i);
                current.delete();
                break;
            }
        }
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public void saveBraille(String path){

        new Thread(()->{

            File oldFile = new File(path);
            File newFile = getNewBrailleFile();

            FileChannel oldFileChannel = null;
            FileChannel newFileChannel = null;

            try {

               // newFile.createNewFile();
                oldFileChannel = new FileInputStream(oldFile).getChannel();
                newFileChannel = new FileOutputStream(newFile).getChannel();

                if (oldFileChannel != null && newFileChannel != null)
                    newFileChannel.transferFrom(oldFileChannel, 0, oldFileChannel.size());

                // Add up the file
                add(newFile);
            }
            catch (FileNotFoundException e){
                Log.wtf(Constants.TAG, "Exception while copying image => " + e.getLocalizedMessage());
                e.printStackTrace();
            }
            catch (IOException ioe){
                Log.wtf(Constants.TAG, "IOException while copying image => " + ioe.getLocalizedMessage());
                ioe.printStackTrace();
            }
            finally {

                try {
                    if (newFileChannel != null) newFileChannel.close();
                    if (oldFileChannel != null) oldFileChannel.close();
                }
                catch (IOException e){}
            }
        }).start();
    }

    public static Braille getInstance(Context context){
        if (instance == null) instance = new Braille(context);
        return instance;
    }
}// END
