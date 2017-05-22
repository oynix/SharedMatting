package com.jb.sharedmatting;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        tv.setText(stringFromJNI());

//        InputStream inputStream = getResources().openRawResource(R.raw.gt04);
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inMutable = true;
//        Bitmap gt04_bitmap = BitmapFactory.decodeStream(inputStream, null, options);
//        Log.e("config", "config = " + gt04_bitmap.getConfig().name());
//        for (int i = 0; i < 40; i++) {
//            for (int i1 = 0; i1 < 40; i1++) {
//                int pixel = gt04_bitmap.getPixel(i, i1);
//                int alpha = Color.alpha(pixel);
//                Log.e("Pixel", "value = " + pixel);
//                int argb = Color.argb(0, Color.red(pixel), Color.green(pixel), Color.blue(pixel));
//                gt04_bitmap.setPixel(i, i1, argb);
//            }
//        }
//        imageView.setImageBitmap(gt04_bitmap);
        imageView.setImageResource(R.drawable.gt04);
//        int width = gt04_bitmap.getWidth();
//        int height = gt04_bitmap.getHeight();
//        Log.e("test", "width = " + width + "..height = " + height);

//        Bitmap gt04_trimap = BitmapFactory.decodeResource(getResources(), R.drawable.gt04_trimap);

        String fileName = Environment.getExternalStorageDirectory() + File.separator + "GT04.png";
        String trimapName = Environment.getExternalStorageDirectory() + File.separator + "GT04_trimap.png";
        String matteName = Environment.getExternalStorageDirectory() + File.separator + "GT04_matte.png";
//        try {
//            OutputStream os = new FileOutputStream(fileName);
//            gt04_bitmap.compress(CompressFormat.PNG, 100, os);
//            os.close();
//            OutputStream os2 = new FileOutputStream(trimapName);
//            gt04_trimap.compress(CompressFormat.PNG, 100, os2);
//            os2.close();
//        } catch (Exception e) {
//            Log.e("TAG", "", e);
//        }
        //handleImage(fileName, trimapName, matteName);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public native void handleImage(String fileName, String trimapName, String matteName);

    public native Bitmap handleBitmap(Bitmap bitmap);

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    private Bitmap decodeResource(Resources resources, int id) {
        TypedValue value = new TypedValue();
        resources.openRawResource(id, value);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inTargetDensity = value.density;
        return BitmapFactory.decodeResource(resources, id, opts);
    }
}
