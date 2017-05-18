package com.jb.sharedmatting;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
//        Bitmap gt04_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gt04);
//        Bitmap gt04_trimap = BitmapFactory.decodeResource(getResources(), R.drawable.gt04_trimap);

        String fileName = Environment.getExternalStorageDirectory() + File.separator + "GT04.png";
        String trimapName = Environment.getExternalStorageDirectory() + File.separator + "GT04_trimap.png";
        String matteName = Environment.getExternalStorageDirectory() + File.separator + "GT04_matte.png";
        try{
//            OutputStream os = new FileOutputStream(fileName);
//            gt04_bitmap.compress(CompressFormat.PNG, 100, os);
//            os.close();
//            OutputStream os2 = new FileOutputStream(trimapName);
//            gt04_trimap.compress(CompressFormat.PNG, 100, os2);
//            os2.close();
        }catch(Exception e){
            Log.e("TAG", "", e);
        }
        handleImage(fileName, trimapName, matteName);
//        for (int i = 0; i < 4; ++i) {
//            for (int j = 0; j < 4; ++j) {
//                Log.e("test", "i = " + i + ";j = " + j);
//            }
//        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public native void handleImage(String fileName, String trimapName, String matteName);

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }
}
