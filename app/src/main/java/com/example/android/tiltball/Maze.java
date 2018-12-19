package com.example.android.tiltball;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

public class Maze {
    private RectF drawRect = new RectF();

    private Bitmap[] bitmaps;
    private int[][] tileType;

    private float screenWidth, screenHeight;

    public Maze(Bitmap[] bitmaps, int[][] tileType, float xCellCountOnScreen, float yCellCountOnScreen, float screenWidth, float screenHeight){
        this.bitmaps = bitmaps;
        this.tileType = tileType;

        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        drawRect.set(0, 0, screenWidth / xCellCountOnScreen, screenHeight / yCellCountOnScreen);
    }

    public int getType(int x, int y){
        if(y < tileType.length && x < tileType[y].length){
//            if(tileType[y][x] == 0){
//                Log.d("square on ", "FLOOR");
//            }else if (tileType[y][x] == 1){
//                Log.d("square on ", "WALL");
//            }else if (tileType[y][x] == 2){
//                Log.d("square on ", "HOLE");
//            }else{
//                Log.d("square on ", "ERRRRROR");
//            }

            return tileType[y][x];
        }
        return -1;
    }
    //TODO: remove passing screen sizes for all four below, already have them in private variables
    public int getTop(int mHeightScreen, int y){
        return y * (mHeightScreen / 16);
    }
    public int getBottom(int mHeightScreen, int y){
        return y * (mHeightScreen / 16) + (mHeightScreen / 16);
    }
    public int getRight(int mWidthScreen, int x){
        return x * (mWidthScreen / 10) + (mWidthScreen / 10);
    }
    public int getLeft(int mWidthScreen, int x){
        return x * (mWidthScreen / 10);
    }

    public float getCellWidth(){ return drawRect.width(); }
    public float getCellHeight(){ return drawRect.height(); }
    
    public void drawMaze(Canvas canvas, float viewX, float viewY){
        int tileX = 0;
        int tileY = 0;
        float xCoord = -viewX;
        float yCoord = -viewY;



        while(tileY < tileType.length && yCoord <= screenHeight){
            // Begin drawing a new column
            tileX = 0;
            xCoord = -viewX;

            while(tileX < tileType[tileY].length && xCoord <= screenWidth){
                // Check if the tile is not null
                if(bitmaps[tileType[tileY][tileX]] != null){
                    //canvas.drawBitmap(bitmaps[tileType[tileY][tileX]], null, drawRect, null);
                    // This tile is not null, so check if it has to be drawn
                    if(xCoord + drawRect.width() >= 0 && yCoord + drawRect.height() >= 0){

                        //Log.d("tileX_Y", Integer.toString(tileX) + " " + Integer.toString(tileX));
                        //Log.d("coordsX_Y", Float.toString(xCoord) + " " + Float.toString(yCoord));
                        // The tile actually visible to the user, so draw it
                        drawRect.offsetTo(xCoord, yCoord); // Move the rectangle to the coordinates

                        //canvas.drawBitmap(bitmaps[tileType[tileY][tileX]],new Rect(0,0,20,20), drawRect, null);
                        canvas.drawBitmap(bitmaps[tileType[tileY][tileX]], null, drawRect, null);
                        //canvas.drawBitmap(bitmaps[tileType[tileY][tileX]], new Rect(0,0,20,20), drawRect, null);
                        ///canvas.drawBitmap(bitmaps[tileType[tileY][tileX]],xCoord , yCoord, null);
                        //canvas.drawBitmap(bitmaps[tileType[tileY][tileX]], 20+xCoord*20,20+yCoord*20, null);
                    }
                }

                // Move to the next tile on the X axis
                tileX++;

//               Log.d("XUp", Integer.toString(tileX));
//                if(tileX < tileType[tileY].length)
//                    Log.d("firstCheckT", "First check true");
//                else
//                    Log.d("firstCheckF", "First check FALSE");
//
//                if(xCoord <= screenWidth)
//                    Log.d("secondCheckT", "Second check true");
//                else
//                    Log.d("secondCheckF", "Second check FALSE");

                xCoord += drawRect.width();
            }

            // Move to the next tile on the Y axis
            tileY++;

            yCoord += drawRect.height();

//            Log.d("screenWidth",Float.toString(screenWidth));
//            Log.d("coordsX_Y", Float.toString(xCoord) + " " + Float.toString(yCoord));
        }
    }
}