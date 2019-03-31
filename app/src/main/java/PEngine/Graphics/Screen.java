package PEngine.Graphics;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;

import java.util.Vector;

import PEngine.Entities.Tile;
import PEngine.Game;
import PEngine.Math.Vec2;
import pl.com.gemstones.tileartist.Settings;


public class Screen {

    SurfaceHolder surfaceHolder;
    Canvas canvas;
    Paint paint;
    public Vec2 dimensions;

    public Vec2 imageStartPos;
    public Vec2 imageSizeInPixels;

    Vector<Graphable> toProcess;
    boolean toProcessToAddBlock = false;
    Vector<Graphable> toProcessToAdd;
    ScreenSandboxExtension screenSandboxExtension = null;

    public Screen(SurfaceHolder surfaceHolder){
        this(surfaceHolder, false);
    }
    public Screen(SurfaceHolder surfaceHolder, boolean isSandbox)
    {
        toProcess = new Vector<>();
        toProcessToAdd = new Vector<>();
        // Initialize ourHolder and paint objects
        this.surfaceHolder = surfaceHolder;
        paint = new Paint();
        this.dimensions = new Vec2();

        if(isSandbox)
            screenSandboxExtension = new ScreenSandboxExtension(this);
    }

    public void addGraphableToProcess(Graphable g){
        synchronized (this){
            while(toProcessToAddBlock) {
                try {
                    this.wait(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            toProcessToAdd.add(g);//TODO: get rid of toProcessToAdd buffer :D
        }
    }
    public void setImageDimensions(Vec2 imageSizeInPixels) {
        setImageDimensions(new Vec2(), imageSizeInPixels);
    }
    public void setImageDimensions(Vec2 imageStartPos, Vec2 imageSizeInPixels) {
        this.imageStartPos = imageStartPos;
        this.imageSizeInPixels = imageSizeInPixels;

        if(Settings.getSetting(Settings.Name.ZOOM_OUT_ON_IMAGE_OPEN)){
            Game.controls.scaleImageRightInTheMiddle();
        }
    }
    public Vector<Graphable> getAllGraphableToProcess(){
        return toProcess;
    }

    // Everything that needs to be updated goes in here
    // In later projects we will have dozens (arrays) of objects.
    // We will also do other things like collision detection.
    public void update() {
        if(toProcessToAdd.size() > 0){
            toProcessToAddBlock = true;
            for(Graphable g : toProcessToAdd){
                toProcess.add(g);
            }
            toProcessToAddBlock = false;
            toProcessToAdd.removeAllElements();
        }

        Vec2 basePos = Game.controls.getLoc();
        float scale = Game.controls.getScale();
        if(Game.controls.isClicked())
        {
            Vec2 clickedPos = Game.controls.getLastClickedPos();
            for(Graphable g : toProcess) {
                g.update(dimensions, basePos.x, basePos.y, scale);
                if(g instanceof Tile){
                    if( Vec2.greaterThanOrEqual( Vec2.subtract(clickedPos, ((Tile)g).getRealPos()), new Vec2() )
                    && Vec2.lessThanOrEqual( Vec2.subtract(clickedPos, ((Tile)g).getRealPos()), ((Tile)g).getRealSize() ) ){
                        ((Tile)g).colorize();
                    }
                }
            }
        }
        else
        {
            for(Graphable g : toProcess) {
                g.update(dimensions, basePos.x, basePos.y, scale);
            }
            if(screenSandboxExtension != null) {
                screenSandboxExtension.checkBounds(dimensions, basePos, scale);
            }
        }
    }

    // Draw the newly updated scene
    public void draw() {
        // Make sure our drawing surface is valid or we crash
        if (surfaceHolder.getSurface().isValid()) {
            // Lock the canvas ready to draw
            canvas = surfaceHolder.lockCanvas();
            if(dimensions.x == 0){
                dimensions.x = canvas.getWidth();
                dimensions.y = canvas.getHeight();
            }
            // Draw the background color
            canvas.drawColor(Color.argb(255, 26, 128, 182));

            // Draw all the graphable elements
            Vec2 basePos = Game.controls.getLoc();
            float scale = Game.controls.getScale();
            for(Graphable g : toProcess){
                g.draw(canvas, paint, basePos.x, basePos.y, scale);
            }

            // Choose the brush color for drawing
            paint.setColor(Color.argb(255,  249, 129, 0));
            // Make the text a bit bigger
            paint.setTextSize(45);
            // Display the current fps on the screen
            canvas.drawText("FPS:" + Game.getFps(), 20, 40, paint);
            canvas.drawText( Tile.tiles_left+"", 20, 80, paint);

            // Draw everything to the screen
            surfaceHolder.unlockCanvasAndPost(canvas);

        }

    }
}
