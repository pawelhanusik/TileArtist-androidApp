package PEngine;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

import PEngine.Entities.Tile;
import PEngine.Graphics.Screen;

public class Game extends SurfaceView implements Runnable {

    public long imageID;

    public static Screen screen;
    public static Controls controls;

    // This is our thread
    Thread gameThread = null;
    // A boolean which we will set and unset
    // when the game is running- or not.
    volatile boolean playing;
    // This variable tracks the game frame rate
    static long fps;
    // This is used to help calculate the fps
    private long timeThisFrame;

    public boolean isSandbox;
    public boolean isMultiplayer;

    public Game(Context context, int imageID) {
        this(context, imageID, false);
    }
    public Game(Context context, long imageID, boolean isSandbox) {
        this(context, imageID, isSandbox, false);
    }
    public Game(Context context, long imageID, boolean isSandbox, boolean isMultiplayer) {
        super(context);
        this.imageID = imageID;
        this.isSandbox = isSandbox;
        this.isMultiplayer = isMultiplayer;

        screen = new Screen(this.getHolder(), isSandbox);
        controls = new Controls(context);

        playing = true;

        Tile.tiles_left = 0;
    }

    public static long getFps()
    {
        return fps;
    }

    @Override
    public void run() {
        while (playing) {

            // Capture the current time in milliseconds in startFrameTime
            long startFrameTime = System.currentTimeMillis();

            // Update the frame
            screen.update();

            /*synchronized (this) {
                try {
                    this.wait(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }*/

            // Draw the frame
            screen.draw();


            // Calculate the fps this frame
            // We can then use the result to
            // time animations and more.
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame > 0) {
                fps = 1000 / timeThisFrame;
            }

        }

    }


    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }
    }
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    // The SurfaceView class implements onTouchListener
    // So we can override this method and detect screen touches.
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        controls.onTouchEvent(motionEvent);
        return true;
    }
}