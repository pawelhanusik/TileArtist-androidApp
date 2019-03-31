package PEngine.Entities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import java.util.Vector;

import PEngine.ColorDB;
import PEngine.Game;
import PEngine.Graphics.Graphable;
import PEngine.Math.Vec2;
import pl.com.gemstones.tileartist.Settings;

public class Tile implements Graphable {
    static public short selectedColorIndex = 0;
    static public boolean isInEraserMode = false;

    static private Paint paint_black_text = null;
    static private Paint paint_grey = null;
    private Paint paint_white = null;

    static public int tiles_left = 0;

    private Vec2 m_pos;
    public static Vec2 m_size = new Vec2(128, 128);

    private boolean m_isWellColored = false;
    private short m_howIsColored = -1;
    private short m_nr;
    private boolean m_isSandbox;

    ///UPDATE UPDATE xD
    protected boolean d_fitOnScreen;
    private Paint d_fillPaint;
    protected Vec2 d_pos1;
    protected Vec2 d_pos2;
    private int d_thickness;
    private static boolean d_showNumberSetting = false;
    private boolean d_drawNr;
    private Vec2 d_nrPos;
    ///END

    public Tile(Vec2 pos, short nr)
    {
        this(pos, nr, 0xffffffff);
    }
    public Tile(Vec2 pos, short nr, int whiteColor) {
        this(pos, nr, whiteColor, false, false);
    }
    public Tile(Vec2 pos, short nr, int whiteColor, boolean isWellColored) {
        this(pos, nr, whiteColor, isWellColored, false);
    }
    public Tile(Vec2 pos, short nr, boolean isWellColored) {
        this(pos, nr, 0xffffffff, isWellColored, false);
    }
    public Tile(Vec2 pos, short nr, boolean isWellColored, boolean isSandbox) {
        this(pos, nr, 0xffffffff, isWellColored, isSandbox);
    }
    public Tile(Vec2 pos, short nr, int whiteColor, boolean isWellColored, boolean isSandbox) {
        d_showNumberSetting = Settings.getSetting(Settings.Name.SHOW_NUMBERS_ON_TILES);

        this.m_nr = nr;
        this.m_pos = pos;
        this.m_isWellColored = isWellColored;
        this.m_isSandbox = isSandbox;
        if(this.m_isWellColored || this.m_isSandbox){
            m_howIsColored = m_nr;
        }

        if(paint_black_text == null || paint_grey == null) {
            paint_black_text = new Paint();
            paint_black_text.setARGB(255, 0, 0, 0);
            paint_black_text.setTextSize(30);
            paint_grey = new Paint();
            paint_grey.setARGB(255, 120, 120, 120);
        }

        paint_white = new Paint();
        paint_white.setColor(whiteColor);

        if(m_nr != -2 && !m_isWellColored)
            tiles_left++;
    }

    public void colorize()
    {
        if(!isInEraserMode) {
            if (!m_isWellColored) {
                m_howIsColored = this.selectedColorIndex;
                if (m_howIsColored == m_nr) {
                    m_isWellColored = true;
                    tiles_left--;
                }
            }
        }else{
            m_isWellColored = false;
            m_howIsColored = -1;
        }
    }
    public Vec2 getRealPos()
    {
        return Vec2.multiply(Vec2.add(m_pos, Game.controls.getLoc()), Game.controls.getScale());
    }
    public Vec2 getRealSize()
    {
        return Vec2.multiply(m_size, Game.controls.getScale());
    }


    public void update(Vec2 dimensions, float baseX, float baseY, float scale)
    {
        Vec2 pos = this.getRealPos();
        Vec2 size = this.getRealSize();
        if( pos.x >= -size.x && pos.x <= dimensions.x && pos.y >= -size.y && pos.y <= dimensions.y )
        {
            d_fitOnScreen = true;
            d_pos1 = pos;
            d_pos2 = Vec2.add(pos, size);

            if(scale > 0.2f && tiles_left > 0) {
                d_thickness = 1;
            }else{
                d_thickness = 0;
            }

            if(selectedColorIndex == m_nr && !m_isWellColored && !m_isSandbox) {
                d_fillPaint = paint_grey;
            }else if(m_howIsColored < 0) {
                d_fillPaint = paint_white;
            }else {
                d_fillPaint = ColorDB.getColor(m_howIsColored);
            }

            if(d_showNumberSetting && m_nr >= 0 && scale > 0.2 && !m_isWellColored) {
                d_drawNr = true;
                d_nrPos = Vec2.add(pos, Vec2.multiply(size, 0.5f));
            }else{
                d_drawNr = false;
            }
        }
        else
        {
            d_fitOnScreen = false;
        }
    }
    public void draw(Canvas canvas, Paint paint, float baseX, float baseY, float scale) {
        ///UPDATE UPDATE
        if( d_fitOnScreen )
        {
            if(d_thickness > 0) {
                Rect r = new Rect();
                r.set((int)d_pos1.x, (int)d_pos1.y, (int)d_pos2.x, (int)d_pos2.y);
                canvas.drawRect(r, paint_black_text);
            }

            canvas.drawRect(d_pos1.x + d_thickness, d_pos1.y + d_thickness, d_pos2.x - d_thickness, d_pos2.y - d_thickness, d_fillPaint);

            if(d_drawNr) {
                canvas.drawText(String.valueOf(m_nr), d_nrPos.x, d_nrPos.y, paint_black_text);
            }
        }
        ///END

        ///BEFORE UPDATE UPDATE
        /*
        //Vec2 pos = new Vec2( (m_pos.x+baseX)*scale, (m_pos.y+baseY)*scale );
        //Vec2 size = Vec2.multiply(m_size, scale);
        Vec2 pos = this.getRealPos();
        Vec2 size = this.getRealSize();
        if( pos.x >= -size.x && pos.x <= canvas.getWidth() && pos.y >= -size.y && pos.y <= canvas.getHeight() )
        {
            int thickness = 3;
            //canvas.drawBitmap(m_img, pos.x, pos.y, paint);

            if(scale > 0.2f && tiles_left > 0) {
                //canvas.drawRect(pos.x, pos.y, pos.x + size.x, pos.y + thickness, paint_black_text);
                //canvas.drawRect(pos.x, pos.y + size.y, pos.x + size.x, pos.y + size.y - thickness, paint_black_text);
                //canvas.drawRect(pos.x, pos.y, pos.x + thickness, pos.y + size.y, paint_black_text);
                //canvas.drawRect(pos.x + size.x, pos.y, pos.x + size.x - thickness, pos.y + size.y, paint_black_text);
                Rect r = new Rect();
                r.set((int)pos.x, (int)pos.y, (int)(pos.x+size.x), (int)(pos.y+size.y));
                canvas.drawRect(r, paint_black_text);

            }else{
                thickness = 0;
            }

            if(selectedColorIndex == m_nr && !m_isWellColored) {
                canvas.drawRect(pos.x + thickness, pos.y + thickness, pos.x + size.x - thickness, pos.y + size.y - thickness, paint_grey);
            }else if(m_howIsColored < 0) {
                canvas.drawRect(pos.x + thickness, pos.y + thickness, pos.x + size.x - thickness, pos.y + size.y - thickness, paint_white);
            }else {
                canvas.drawRect(pos.x + thickness, pos.y + thickness, pos.x + size.x - thickness, pos.y + size.y - thickness, ColorDB.getColor(m_howIsColored));
            }

            if(m_nr >= 0 && scale > 0.2 && !m_isWellColored) {
                canvas.drawText(String.valueOf(m_nr), pos.x + size.x / 2, pos.y + size.y / 2, paint_black_text);
            }
        }
        */
    }

    public static Bitmap getBitmapFromAllTiles(){
        return getBitmapFromAllTiles(false);
    }
    public static Bitmap getBitmapFromAllTiles(boolean isSandbox)
    {
        ///Get width and height info
        Vector<Graphable> all = Game.screen.getAllGraphableToProcess();
        int width = 0;
        int height = 0;
        Vec2 startPos = new Vec2();
        for(Graphable g : all)
        {
            if(g instanceof Tile)
            {
                /*if( ((Tile)g).m_isSandbox && ((Tile)g).m_howIsColored == -1 ){
                    //don't consider transparent tiles
                    continue;
                }*/
                width = Math.max(width, (int)((Tile)g).m_pos.x);
                height = Math.max(height, (int)((Tile)g).m_pos.y);
                startPos.x = Math.min(startPos.x, ((Tile)g).m_pos.x);
                startPos.y = Math.min(startPos.y, ((Tile)g).m_pos.y);
            }
        }
        Log.d("Tile", width + " x " + height);
        width -= startPos.x;
        height -= startPos.y;
        width /= Tile.m_size.x;
        height /= Tile.m_size.y;
        width++;
        height++;
        Log.d("Tile", width + " x " + height);

        ///Get pixel data
        Bitmap ret = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for(Graphable g : all)
        {
            if(g instanceof Tile)
            {
                int color;
                if(!isSandbox) {
                    //IMAGE
                    color = ColorDB.getColor(((Tile) g).m_nr).getColor();
                    if (!((Tile) g).m_isWellColored && ((Tile) g).m_nr != -1) {
                        //reduce alpha to make uncoloured pixels invisible
                        color = Color.argb(5, Color.red(color), Color.green(color), Color.blue(color));
                    }
                    ret.setPixel((int) ((((Tile) g).m_pos.x - startPos.x) / Tile.m_size.x), (int) ((((Tile) g).m_pos.y - startPos.y) / Tile.m_size.y), color);
                } else {
                    //SANDBOX
                    if(((Tile) g).m_howIsColored == -1){
                        color = Color.argb(0, 255, 255, 255);
                    }else{
                        color = ColorDB.getColor(((Tile) g).m_howIsColored).getColor();
                    }
                    try {
                        ret.setPixel((int) ((((Tile) g).m_pos.x - startPos.x) / Tile.m_size.x), (int) ((((Tile) g).m_pos.y - startPos.y) / Tile.m_size.y), color);
                    }catch (IllegalArgumentException e){
                        //e.printStackTrace();
                        if(color != Color.argb(0, 255, 255, 255)) {
                            Log.e("Tile", "getBitmapFromAllTiles(): Cutting out colored Tile.");
                        }
                    }
                }
            }
        }

        return ret;
    }
}
