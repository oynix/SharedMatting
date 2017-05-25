package com.jb.sharedmatting;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.jb.sharedmatting.R.id.imageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

//    private static final String SOURCE_IMAGE_URL = "/storage/emulated/0/20170425.jpg";
//    private static final String SOURCE_IMAGE_URL = "/storage/emulated/0/GT04.png";
    private static final String SOURCE_IMAGE_URL = "/storage/emulated/0/1.jpg";
    private static final String TRI_MAP_URL = "/storage/emulated/0/tri.png";
    private static final String TRI_MAP_URL_2 = "/storage/emulated/0/dest_contours.png";
    private static final String MATTE_URL = "/storage/emulated/0/matte.png";
    private static final String DESTINATION_IMAGE_URL = "/storage/emulated/0/desi.png";

    private static final int FOREGROUND_COLOR = 0xffffffff;
    private static final int MEDIUM_COLOR = 0xff7f7f7f;
    private static final int BACKGROUND_COLOR = 0xff000000;

    private ImageView srcImg;
    private List<PathBean> mList = new ArrayList<>();
    private int currentType = PathBean.TYPE_FORE;
    private float mRatio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        Button btnFore = (Button) findViewById(R.id.btn_fore);
        btnFore.setOnClickListener(this);
        Button btnMedium = (Button) findViewById(R.id.btn_medium);
        btnMedium.setOnClickListener(this);
        Button btnBack = (Button) findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);
        Button btnFinish = (Button) findViewById(R.id.btn_finish);
        btnFinish.setOnClickListener(this);

        img = (ImageView) findViewById(imageView);
        srcImg = (ImageView) findViewById(R.id.src_img);

        Bitmap bitmap = BitmapFactory.decodeFile(SOURCE_IMAGE_URL);
        srcImg.setImageBitmap(bitmap);
        // 创建一张空白图片
        mBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        mRatio = getResources().getDisplayMetrics().density * 300 / bitmap.getWidth();
        showImage();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public native void handleImage(String fileName, String trimapName, String matteName);

    public native void cvFindContours(String filePath);

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    private ImageView img;
    private Bitmap mBitmap;
    private Canvas canvas;
    private Paint paint;
    private Path mPath;

    private void showImage() {
        // 创建一张画布
        canvas = new Canvas(mBitmap);
        // 画布背景为白色
        //  canvas.drawColor(Color.WHITE);
        // 创建画笔
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        // 画笔颜色为蓝色
        paint.setColor(Color.BLUE);
        // 宽度5个像素
        paint.setStrokeWidth(10 * getResources().getDisplayMetrics().density);
        // 先将白色背景画上
        canvas.drawBitmap(mBitmap, new Matrix(), paint);
        img.setImageBitmap(mBitmap);

        img.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX() / mRatio;
                float y = event.getY() / mRatio;
                if (x < 0) x = 0;
                if (y < 0) y = 0;
                if (x > srcImg.getWidth()) x = srcImg.getWidth();
                if (y > srcImg.getHeight()) y = srcImg.getHeight();
//                Log.e("Main", "Touch Event x = " + x + "..y = " + y);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 获取手按下时的坐标
                        if (mPath == null) {
                            mPath = new Path();
                        }
                        mPath.moveTo(x, y);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 获取手移动后的坐标
                        mPath.lineTo(x, y);
                        canvas.drawPath(mPath, paint);
                        img.setImageBitmap(mBitmap);
                        break;
                    case MotionEvent.ACTION_UP:
                        mList.add(new PathBean(mPath, currentType));
                        mPath = null;
                        break;
                }
                return true;
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_fore:
                currentType = PathBean.TYPE_FORE;
                paint.setColor(FOREGROUND_COLOR);
                break;
            case R.id.btn_medium:
                currentType = PathBean.TYPE_MEDIUM;
                paint.setColor(MEDIUM_COLOR);
                break;
            case R.id.btn_back:
                currentType = PathBean.TYPE_BACK;
                paint.setColor(BACKGROUND_COLOR);
                break;
            case R.id.btn_finish:
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                // 画黑色
                canvas.drawColor(0xff000000);
                // 画不确定区域
                Path path = new Path();
                for (PathBean pathBean : mList) {
                    path.addPath(pathBean.mPath);
                }
                paint.setColor(MEDIUM_COLOR);
                canvas.drawPath(path, paint);

                Path edge = new Path();
                edge.moveTo(0, 0);
                edge.lineTo(0, canvas.getHeight());
                edge.lineTo(canvas.getWidth(), canvas.getHeight());
                edge.lineTo(canvas.getWidth(), 0);
                edge.lineTo(10, 0);
//                edge.close();
//                edge.op(path, Path.Op.XOR);
//                Log.e("path", "isRectf : " + edge.isRect(new RectF()));
//                Log.e("path", "isEmpty : " + edge.isEmpty());
                paint.setStrokeWidth(5);
                canvas.drawPath(edge, paint);
                paint.setStrokeWidth(10 * getResources().getDisplayMetrics().density);

//                RectF bounds = new RectF();
//                path.computeBounds(bounds, false);
//                path.set();
//                paint.setStrokeWidth(1);
//                canvas.drawRect(bounds, paint);
//                Log.e("Main", "src width = " + img.getWidth() / mRatio + "..src height = " + img.getHeight() / mRatio);
//                Log.e("Main", "left = " + bounds.left + "..top = " + bounds.top + "..right = " + bounds.right + "..bottom = " + bounds.bottom);
                // Bitmap序列化
                File file = new File(TRI_MAP_URL);
                OutputStream stream;
                try {
                    stream = new FileOutputStream(file);
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                handleImage(SOURCE_IMAGE_URL, TRI_MAP_URL, MATTE_URL);
                cvFindContours(TRI_MAP_URL);
                handleImage(SOURCE_IMAGE_URL, TRI_MAP_URL_2, MATTE_URL);

                Toast.makeText(getApplicationContext(), "accomplishment", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void threeLayerTriMapOperation() {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawColor(0xff7f7f7f);
        for (PathBean pathBean : mList) {
            int type = pathBean.type;
            if (type == 0) {
                paint.setColor(0xffffffff);
            } else if (type == 1) {
                paint.setColor(0xff7f7f7f);
            } else {
                paint.setColor(0xff000000);
            }
            canvas.drawPath(pathBean.mPath, paint);
        }
        File file = new File(TRI_MAP_URL);
        OutputStream stream;
        try {
            stream = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        handleImage(SOURCE_IMAGE_URL, TRI_MAP_URL, MATTE_URL);
        Toast.makeText(getApplicationContext(), "accomplishment", Toast.LENGTH_SHORT).show();
    }
}
