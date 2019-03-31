package PEngine.Entities;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import PEngine.Game;
import PEngine.Graphics.Graphable;
import PEngine.Math.Vec2;

public class Entity implements Graphable {
    protected Vec2 m_pos;
    protected Vec2 m_size;
    Bitmap m_img;

    //boolean m_isMoving = false;
    float walkSpeedPerSecond = 150;

    public Entity(Vec2 pos, Bitmap img)
    {
        //BitmapFactory.decodeResource(this.getResources(), R.drawable.bob)
        this.m_pos = pos;
        this.m_img = img;
        this.m_size = new Vec2(img.getWidth(), img.getHeight());
    }
    public Entity(Vec2 pos, Vec2 size)
    {
        this.m_pos = pos;
        this.m_img = null;
        this.m_size = size;
    }

    public Vec2 getRealPos()
    {
        return Vec2.multiply(Vec2.add(m_pos, Game.controls.getLoc()), Game.controls.getScale());
    }
    public Vec2 getRealSize()
    {
        return Vec2.multiply(m_size, Game.controls.getScale());
    }

    public void move(Vec2 v)
    {
        m_pos.add(v);
    }
    public void teleport(Vec2 v)
    {
        m_pos = v;
    }

    public void update(Vec2 dimensions, float baseX, float baseY, float scale) {
        /*if(Game.controls.isDown()){
            m_pos.x += walkSpeedPerSecond / Game.getFps();
        }*/
    }
    public void draw(Canvas canvas, Paint paint, float baseX, float baseY, float scale) {
        if( m_pos.x >= -m_size.x && m_pos.x <= canvas.getWidth() && m_pos.y >= -m_size.y && m_pos.y <= canvas.getHeight())
        {
            canvas.drawBitmap(m_img, m_pos.x, m_pos.y, paint);
        }
    }

}
