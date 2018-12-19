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
import android.util.Log;
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


        // 0 == floor, 1 == wall, 2 == hole
        int[][] mazeArray = {
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 0, 0, 0, 0, 0, 0, 1, 1},
                {1, 1, 0, 0, 0, 0, 0, 0, 1, 1},
                {1, 1, 0, 0, 0, 0, 0, 0, 1, 1},
                {1, 1, 2, 0, 0, 0, 0, 0, 1, 1},
                {1, 1, 0, 1, 1, 0, 1, 0, 1, 1},
                {1, 1, 1, 1, 0, 0, 1, 0, 1, 1},
                {1, 1, 0, 0, 0, 0, 1, 0, 1, 1},
                {1, 1, 0, 0, 0, 1, 1, 0, 1, 1},
                {1, 1, 0, 0, 0, 1, 0, 0, 1, 1},
                {1, 1, 0, 0, 0, 0, 0, 0, 1, 1},
                {1, 1, 2, 0, 0, 0, 0, 0, 1, 1},
                {1, 1, 2, 2, 0, 0, 0, 0, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };

        Bitmap[] bitmaps = {
                BitmapFactory.decodeResource(getResources(), R.drawable.floors),
                BitmapFactory.decodeResource(getResources(), R.drawable.walls),
                BitmapFactory.decodeResource(getResources(), R.drawable.holes)
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
        maze = new Maze(bitmaps, mazeArray, 10, 16, mWidthScreen, mHeightScreen);

        // initializing the view that renders the ball
        mShapeView = new ShapeView(this);
        mShapeView.setOvalCenter((int)(mWidthScreen * 0.75), (int)(mHeightScreen * 0.75));


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
        mAx = (Math.signum(mAx) * Math.abs(mAx) * (1 - FACTOR_FRICTION * Math.abs(mAz) / GRAVITY))/3.0f;// TODO: remove the last divisor(trying to slow movement)
        mAy = (Math.signum(mAy) * Math.abs(mAy) * (1 - FACTOR_FRICTION * Math.abs(mAz) / GRAVITY))/3.0f;// TODO: same as above
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

//            if(mXCenter < RADIUS)
//            {
//                mXCenter = RADIUS;
//                mVx = -mVx * FACTOR_BOUNCEBACK;
//            }
//
//            if(mYCenter < RADIUS)
//            {
//                mYCenter = RADIUS;  mVy = -mVy * FACTOR_BOUNCEBACK;
//            }
//
//            if(mXCenter > mWidthScreen - RADIUS)
//            {
//                mXCenter = mWidthScreen - RADIUS;
//                mVx = -mVx * FACTOR_BOUNCEBACK;
//            }
//
//            if(mYCenter > mHeightScreen - 2 * RADIUS)
//            {
//                mYCenter = mHeightScreen - 2 * RADIUS;
//                mVy = -mVy * FACTOR_BOUNCEBACK;
//            }

            int sq = maze.getType(mXCenter/100, mYCenter/100);
            int hitOnBot = maze.getType(mXCenter/100, (int)(((mYCenter+131)/100)* 0.8));
            int hitOnTop = maze.getType(mXCenter/100, (int)(((mYCenter-131)/100)* 0.8));
            if(mVy > 0){
                if(hitOnTop == 1){
                    //mVx = -mVx * 0.7f;
                    //mVy = -mVy * 0.7f;

                    Log.d("HEIGHT", Integer.toString(mHeightScreen));
                    int top = maze.getTop(mHeightScreen, (int)(((mYCenter+131)/100)* 0.8));
//                    if(mYCenter > top)//"top of square at postion x, y")//mYCenter+100)//mHeightScreen - 2 * RADIUS)
//                    {
//                        Log.d("HIT", "BOTTOM");
//                        mYCenter = top;
//                        mVy = -mVy * FACTOR_BOUNCEBACK;
//                    }
                }
            } else {
                if(hitOnBot == 1){
                    //mVx = -mVx * 0.7f;
                    //mVy = -mVy * 0.7f;


                    int bot = maze.getTop(mHeightScreen, (int)(((mYCenter+131)/100)* 0.8));
                    Log.d("HIT", Integer.toString(mYCenter) + " > " + Integer.toString(bot));
                    if(mYCenter > bot)//"top of square at postion x, y")//mYCenter+100)//mHeightScreen - 2 * RADIUS)
                    {
                        Log.d("HIT", "TOP");
                        mYCenter = bot;
                        mVy = -mVy * FACTOR_BOUNCEBACK;
                    }
                }
            }

            int hitOnRight = maze.getType((mXCenter+14)/100, (int)((mYCenter/100)* 0.8));
            int hitOnLeft = maze.getType((mXCenter-108)/100, (int)((mYCenter/100)* 0.8));
            if (mVx > 0) {
                if(hitOnRight ==1){


                    int left = maze.getLeft(mWidthScreen, (mXCenter+14)/100);
                    //Log.d("HIT", Integer.toString(mXCenter) + " > " + Integer.toString(left));
                    if(mXCenter > left){
                        //Log.d("HIT", "RIGHT");
                        mXCenter = left;
                        mVx = -mVx * FACTOR_BOUNCEBACK;
                    }
                }
            }else {
                if(hitOnLeft ==1){
                    int right = maze.getRight(mWidthScreen, (mXCenter-108)/100);
                    if(mXCenter < right){
                        //Log.d("HIT", "LEFT");
                        mXCenter = right;
                        mVx = -mVx * FACTOR_BOUNCEBACK;
                    }
                }
            }



//            else if (sq == 2) {
//                //mVx = 0;
//                //mVy = 0;
//                Log.d("HIT", "HOLE");
//            }


            return true;
        }

        // update the canvas
        protected void onDraw(Canvas canvas)
        {

            if(mRectF != null)
            {
                maze.drawMaze(canvas, 0, 0);

                mRectF.set(mXCenter - RADIUS, mYCenter - RADIUS, mXCenter + RADIUS, mYCenter + RADIUS);
                //canvas.drawColor(0XFF000000);


                Log.d("Square", Integer.toString(mXCenter/100) + " _ " + Integer.toString((int)((mYCenter/100)*0.8))); //TODO: fix horrible math trying to limit x and y to 10 and 16

                canvas.drawOval(mRectF, mPaint);
                canvas.drawRect(mRectF, mPaint); //TODO: Remove this square


                //canvas.drawColor(Color.red(0));
                Paint paint = new Paint();
                paint.setColor(0xFFFF3432);
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(36f);

                canvas.drawText("Hello", 50, 50, paint);
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