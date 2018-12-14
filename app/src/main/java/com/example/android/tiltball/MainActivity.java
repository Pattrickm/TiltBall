package com.example.android.tiltball;

import java.util.Timer;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class MainActivity extends Activity implements SensorEventListener{

    // sensor-related
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    // animated view
    private ShapeView mShapeView;

    // screen size
    private int mWidthScreen;
    private int mHeightScreen;

    // motion parameters
    private final float FACTOR_FRICTION = 0.5f; // imaginary friction on the screen
    private final float GRAVITY = 9.8f; // acceleration of gravity
    private float mAx; // acceleration along x axis
    private float mAy; // acceleration along y axis
    private final float mDeltaT = 0.5f; // imaginary time interval between each acceleration updates

    // timer
    private Timer mTimer;
    private Handler mHandler;
    private boolean isTimerStarted = false;
    private long mStart;
    public Maze maze;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // 0 == floor, 1 == wall, 2 == different looking wall
        int[][] mazeArray = {
                {0, 0, 0, 0, 0, 0, 1, 0, 1, 0},
                {0, 1, 0, 1, 0, 0, 1, 0, 1, 0},
                {0, 0, 0, 1, 0, 0, 1, 0, 1, 0},
                {1, 1, 0, 1, 0, 0, 1, 0, 1, 0},
                {0, 2, 0, 0, 0, 0, 1, 0, 1, 0},
                {0, 0, 0, 0, 0, 0, 1, 1, 1, 1},
                {0, 1, 0, 1, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 1, 0, 0, 1, 0, 0, 0},
                {1, 1, 0, 1, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 1, 0, 0, 2}
        };

        Bitmap[] bitmaps = {
                BitmapFactory.decodeResource(getResources(), R.drawable.floor),
                BitmapFactory.decodeResource(getResources(), R.drawable.wall),
                BitmapFactory.decodeResource(getResources(), R.drawable.hole)
                //BitmapFactory.decodeResource(getResources(), R.drawable.secondwall)
        };







        // set the screen always portait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // initializing sensors
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // obtain screen width and height
        Display display = ((WindowManager)this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mWidthScreen = display.getWidth();
        mHeightScreen = display.getHeight();

        // Chance the 480 and 320 to match the screen size of your device
        maze = new Maze(bitmaps, mazeArray, 10, 10, mWidthScreen, mHeightScreen);

        // initializing the view that renders the ball
        mShapeView = new ShapeView(this);
        mShapeView.setOvalCenter((int)(mWidthScreen * 0.6), (int)(mHeightScreen * 0.6));


        setContentView(mShapeView);


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // obtain the three accelerations from sensors
        mAx = event.values[0];
        mAy = event.values[1];

        float mAz = event.values[2];

        // taking into account the frictions
        mAx = Math.signum(mAx) * Math.abs(mAx) * (1 - FACTOR_FRICTION * Math.abs(mAz) / GRAVITY);
        mAy = Math.signum(mAy) * Math.abs(mAy) * (1 - FACTOR_FRICTION * Math.abs(mAz) / GRAVITY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // start sensor sensing
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // stop senser sensing
        mSensorManager.unregisterListener(this);
    }

    // the view that renders the ball
    private class ShapeView extends SurfaceView implements SurfaceHolder.Callback{

        private final int RADIUS = 50;
        private final float FACTOR_BOUNCEBACK = 0.5f;

        private int mXCenter;
        private int mYCenter;
        private RectF mRectF;
        private final Paint mPaint;
        private ShapeThread mThread;

        private float mVx;
        private float mVy;

        public ShapeView(Context context) {
            super(context);

            getHolder().addCallback(this);
            mThread = new ShapeThread(getHolder(), this);
            setFocusable(true);

            mPaint = new Paint();
            mPaint.setColor(0xFF0000FF);
            mPaint.setAlpha(192);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setAntiAlias(true);

            mRectF = new RectF();
        }

        // set the position of the ball
        public boolean setOvalCenter(int x, int y)
        {
            mXCenter = x;
            mYCenter = y;
            return true;
        }

        // calculate and update the ball's position
        public boolean updateOvalCenter()
        {
            mVx -= mAx * mDeltaT;
            mVy += mAy * mDeltaT;

            mXCenter += (int)(mDeltaT * (mVx + 0.5 * mAx * mDeltaT));
            mYCenter += (int)(mDeltaT * (mVy + 0.5 * mAy * mDeltaT));

            if(mXCenter < RADIUS)
            {
                mXCenter = RADIUS;
                mVx = -mVx * FACTOR_BOUNCEBACK;
            }

            if(mYCenter < RADIUS)
            {
                mYCenter = RADIUS;  mVy = -mVy * FACTOR_BOUNCEBACK;
            }

            if(mXCenter > mWidthScreen - RADIUS)
            {
                mXCenter = mWidthScreen - RADIUS;
                mVx = -mVx * FACTOR_BOUNCEBACK;
            }

            if(mYCenter > mHeightScreen - 2 * RADIUS)
            {
                mYCenter = mHeightScreen - 2 * RADIUS;
                mVy = -mVy * FACTOR_BOUNCEBACK;
            }

            return true;
        }

        // update the canvas
        protected void onDraw(Canvas canvas)
        {

            if(mRectF != null)
            {
                maze.drawMaze(canvas, 0, 0);

                mRectF.set(mXCenter - RADIUS, mYCenter - RADIUS, mXCenter + RADIUS, mYCenter + RADIUS);
                canvas.drawColor(0XFF000000);
                canvas.drawOval(mRectF, mPaint);
                canvas.drawRect(mRectF, mPaint); //TODO: Remove this square

//                canvas.drawColor(Color.red(0));
//                Paint paint = new Paint();
//                paint.setColor(0xFFFF3432);
//                paint.setStyle(Paint.Style.STROKE);
//                paint.setStrokeWidth(4);
//
//                RectF drawRect = new RectF();
//                drawRect.set(0, 0, mWidthScreen / 5, mHeightScreen / 5);
//                //canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.wall), 0,0, paint);
//                //canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.wall), 20,0, paint);
//                canvas.drawLine(0,0, 500, 500, paint);
//                canvas.drawCircle(50,50, 40, paint);
            }




        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mThread.setRunning(true);
            mThread.start();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            boolean retry = true;
            mThread.setRunning(false);
            while(retry)
            {
                try{
                    mThread.join();
                    retry = false;
                } catch (InterruptedException e){

                }
            }
        }
    }

    class ShapeThread extends Thread {
        private SurfaceHolder mSurfaceHolder;
        private ShapeView mShapeView;
        private boolean mRun = false;

        public ShapeThread(SurfaceHolder surfaceHolder, ShapeView shapeView) {
            mSurfaceHolder = surfaceHolder;
            mShapeView = shapeView;
        }

        public void setRunning(boolean run) {
            mRun = run;
        }

        public SurfaceHolder getSurfaceHolder() {
            return mSurfaceHolder;
        }

        @Override
        public void run() {
            Canvas c;

            while (mRun) {
                mShapeView.updateOvalCenter();
                c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                        mShapeView.onDraw(c);
                    }
                } finally {
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
    }
}