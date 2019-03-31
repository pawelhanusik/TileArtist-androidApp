package PEngine.Graphics;


import android.graphics.Canvas;
import android.graphics.Paint;

import PEngine.Entities.Entity;
import PEngine.Math.Vec2;

public class Palette extends Entity {


    public Palette(Vec2 pos, Vec2 size) {
        super(pos, size);
    }

    @Override
    public void draw(Canvas canvas, Paint paint, float baseX, float baseY, float scale) {
        /*int thickness = 1;
        for(int y = 0; y < m_size.y; y++) {
            for (int x = 0; x < m_size.x; x++) {
                int color = (int)(y*m_size.x +x) * 100;
                paint.setARGB(255, (color>>16) & 0xff, (color>>8) & 0xff, color & 0xff);
                canvas.drawRect(x*thickness +m_pos.x, y*thickness +m_pos.y, (x+1)*thickness +m_pos.x, (y+1)*thickness +m_pos.y, paint);
            }
        }*/
        int r = 255;
        int g = 0;
        int b = 0;
        int factor = 2;
        for (int y = 0; y < m_size.y; y++) {
            paint.setARGB(255, r, g, b);
            canvas.drawRect(m_pos.x, y+m_pos.y, m_size.x + m_pos.x, (y+1) +m_pos.y, paint);
            if     ( g < 255 ) { g += factor; if(g > 255) g = 255; }
            else if( r > 0 )   { r -= factor; if(r < 0)   r = 0;   }
            else if( b < 255 ) { b += factor; if(b > 255) b = 255; }
            else if( g > 0 )   { g -= factor; if(g < 0)   g = 0;   }
            else if( r < 255 ) { r += factor; if(r > 255) r = 255; }
            else if( b > 0 )   { b -= factor; if(b < 0)   b = 0;   }
        }

        /*if( m_pos.x >= -m_size.x && m_pos.x <= canvas.getWidth() && m_pos.y >= -m_size.y && m_pos.y <= canvas.getHeight())
        {
            canvas.drawBitmap(m_img, m_pos.x, m_pos.y, paint);
        }*/
    }
}
