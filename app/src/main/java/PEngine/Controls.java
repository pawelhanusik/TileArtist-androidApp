package PEngine;


import android.content.Context;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import PEngine.Math.Vec2;
import pl.com.gemstones.tileartist.MainActivity;
import pl.com.gemstones.tileartist.Settings;

public class Controls {
    private int mode = 1;
    private boolean holdAndSwipeSetting = true;

    ///Moving
    private boolean m_isDown = false;
    private Vec2 sPoint;
    private Vec2 dPoint = new Vec2();
    private Vec2 loc = new Vec2();

    /*private MotionEvent.PointerCoords s_sa = new MotionEvent.PointerCoords();
    private MotionEvent.PointerCoords s_sb = new MotionEvent.PointerCoords();
    private float startDistance = -1;
    private float tmpDistance;
    private float scale = 1.0f;*/

    ///Scaling
    private MotionEvent.PointerCoords mScaleMoveA = new MotionEvent.PointerCoords();
    private MotionEvent.PointerCoords mScaleMoveB = new MotionEvent.PointerCoords();
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
    private float mScaleFactorDelta = 0.f;

    private boolean isClicked = false;
    private Vec2 clickedPos = new Vec2();

    ///Hold & swipe
    private boolean holdedSwipe = false;
    private boolean movingLock = false;

    private Vec2 middle = new Vec2();

    private int dp_15;
    public Controls(Context context)
    {
        holdAndSwipeSetting = Settings.getSetting(Settings.Name.HOLD_AND_SWIPE);

        dp_15 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, context.getResources().getDisplayMetrics());
        mScaleDetector = new ScaleGestureDetector(context,  new ScaleListener());
    }
    public void scaleImageRightInTheMiddle()
    {
        //Scale and move to (0, 0)
        mScaleFactor = MainActivity.widthInPixels / Game.screen.imageSizeInPixels.x;
        loc.x = -dPoint.x;
        loc.y = -dPoint.y;

        loc.y -= (Game.screen.dimensions.y - Game.screen.imageSizeInPixels.y * mScaleFactor) / 2 + dp_15*5;
        //mScaleFactor = 0.2f;
    }

    public void onTouchEvent(MotionEvent motionEvent)
    {
        mScaleDetector.onTouchEvent(motionEvent);

        if(motionEvent.getPointerCount() == 2 && mode != 2) {
            mode = 2;
        }

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                //Log.d("Controls", "DOWN");
                sPoint = new Vec2(motionEvent.getX(), motionEvent.getY());
                m_isDown = true;
                break;
            case MotionEvent.ACTION_MOVE:
                //Log.d("Controls", "MOVE");
                if(mode == 2) {
                    if(motionEvent.getPointerCount() == 2 && mScaleFactorDelta != 0 && mScaleDetector.isInProgress()) {
                        /*motionEvent.getPointerCoords(0, mScaleMoveA);
                        motionEvent.getPointerCoords(1, mScaleMoveB);
                        middle.set( (mScaleMoveA.x + mScaleMoveB.x) / 2,
                                (mScaleMoveA.y + mScaleMoveB.y) / 2);*/
                        middle.x = mScaleDetector.getFocusX();
                        middle.y = mScaleDetector.getFocusY();

                        dPoint.subtract( new Vec2(middle.x * (mScaleFactorDelta - 1),
                                middle.y * (mScaleFactorDelta- 1)) );
                    }
                }else{
                    if(!holdedSwipe) {
                        Vec2 cPoint = new Vec2(motionEvent.getX(), motionEvent.getY());
                        dPoint.set(cPoint.x - sPoint.x, cPoint.y - sPoint.y);
                        dPoint.multiply(1 / mScaleFactor);
                        if (holdAndSwipeSetting && !movingLock && SystemClock.uptimeMillis() - motionEvent.getDownTime() > 500){
                            //&& SystemClock.uptimeMillis() - motionEvent.getDownTime() < 1500
                            if (Math.abs(dPoint.x) < dp_15 && Math.abs(dPoint.y) < dp_15) {
                                holdedSwipe = true;
                            }else{
                                movingLock = true;
                            }
                        }
                    }else {
                        isClicked = true;
                        clickedPos.set(motionEvent.getX(), motionEvent.getY());
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                //Log.d("Controls", "UP");
                m_isDown = false;
                loc.add(dPoint);
                dPoint.set(0.0f, 0.0f);

                if(mode != 2 && SystemClock.uptimeMillis() - motionEvent.getDownTime() < 120)
                {
                    isClicked = true;
                    clickedPos.set(motionEvent.getX(), motionEvent.getY());
                }
                holdedSwipe = false;
                movingLock = false;
                mode = 1;
                break;
        }

    }


    public boolean isDown()
    {
        return m_isDown;
    }
    public Vec2 getLoc()
    {
        return Vec2.add(loc,  dPoint);
    }
    public float getScale()
    {
        return mScaleFactor;
    }

    public boolean isClicked()
    {
        return isClicked;
    }
    public Vec2 getLastClickedPos()
    {
        isClicked = false;
        return clickedPos;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactorDelta = detector.getScaleFactor();
            mScaleFactor *= mScaleFactorDelta ;

            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 2.0f));

            return true;
        }
    }
}
