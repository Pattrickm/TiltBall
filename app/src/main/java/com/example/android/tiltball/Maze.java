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

        // Creates a rect by splitting the screen size by the numer of tiles in X and Y
        drawRect.set(0, 0, screenWidth / xCellCountOnScreen, screenHeight / yCellCountOnScreen);
    }

    public int getType(int x, int y){
        if(y < tileType.length && x < tileType[y].length){
            // Returns what kind of tile is at cords X,Y
            return tileType[y][x];
        }
        return -1;
    }
    //TODO: remove passing screen sizes for all four below, already have them in private variables
    // Gets the top pixel cord of the tile
    public int getTop(int mHeightScreen, int y){
        return y * (mHeightScreen / 16);
    }
    // Gets the bottom pixel cord of the tile
    public int getBottom(int mHeightScreen, int y){
        return y * (mHeightScreen / 16) + (mHeightScreen / 16);
    }
    // Gets the right pixel cord of the tile
    public int getRight(int mWidthScreen, int x){
        return x * (mWidthScreen / 10) + (mWidthScreen / 10);
    }
    // Gets the left pixel cord of the tile
    public int getLeft(int mWidthScreen, int x){
        return x * (mWidthScreen / 10);
    }

    // Draws the maze
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

                    // This tile is not null, so check if it has to be drawn
                    if(xCoord + drawRect.width() >= 0 && yCoord + drawRect.height() >= 0){

                        // The tile actually visible to the user, so draw it
                        drawRect.offsetTo(xCoord, yCoord); // Move the rectangle to the coordinates

                        // Draws the tile from the location X,Y in the space of drawRect
                        canvas.drawBitmap(bitmaps[tileType[tileY][tileX]], null, drawRect, null);
                    }
                }

                // Move to the next tile on the X axis
                tileX++;
                xCoord += drawRect.width();
            }

            // Move to the next tile on the Y axis
            tileY++;

            yCoord += drawRect.height();
        }
    }
}