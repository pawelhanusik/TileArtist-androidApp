package PEngine.Graphics;

import android.util.Log;

import PEngine.Entities.Tile;
import PEngine.Math.Vec2;

public class ScreenSandboxExtension {

    Screen screen;
    Vec2 m_pos1, m_pos2;
    public static boolean isWorking = false;
    public static final int sandboxSize = 10;

    ScreenSandboxExtension(Screen screen)
    {
        this.screen = screen;

        m_pos1 = new Vec2();
        m_pos2 = new Vec2(sandboxSize * Tile.m_size.x, sandboxSize * Tile.m_size.y);
    }

    public void checkBounds(Vec2 dimensions, Vec2 basePos, float scale)
    {
        if(!isWorking)
            return;

        Vec2 tmp1 = Vec2.multiply( Vec2.add( m_pos1, basePos), scale);
        Vec2 tmp2 = Vec2.multiply( Vec2.add( m_pos2, basePos), scale);
        //Log.d("ScreenSandboxExtension", m_pos1.x + ", " + m_pos1.y + " | " + tmp1.x + ", " + tmp1.y);
        if(tmp1.x > 0) {
            //add to left
            for(int i = 0; i < (m_pos2.y - m_pos1.y)/ Tile.m_size.y ; ++i ) {
                screen.addGraphableToProcess(new Tile(new Vec2(m_pos1.x - Tile.m_size.x, m_pos1.y + Tile.m_size.y * i), (short)-1));
            }
            m_pos1.x -= Tile.m_size.x;
        }
        if(tmp1.y > 0) {
            //add to up
            for(int i = 0; i < (m_pos2.x - m_pos1.x)/ Tile.m_size.x ; ++i ) {
                screen.addGraphableToProcess(new Tile(new Vec2(m_pos1.x + Tile.m_size.x * i, m_pos1.y - Tile.m_size.y ), (short)-1));
            }
            m_pos1.y -= Tile.m_size.y;
        }

        if(tmp2.x < dimensions.x){
            //add to right
            for(int i = 0; i < (m_pos2.y - m_pos1.y)/ Tile.m_size.y ; ++i ) {
                screen.addGraphableToProcess(new Tile(new Vec2(m_pos2.x, m_pos1.y + Tile.m_size.y * i), (short)-1));
            }
            m_pos2.x += Tile.m_size.x;
        }
        if(tmp2.y < dimensions.y) {
            //add to down
            for(int i = 0; i < (m_pos2.x - m_pos1.x)/ Tile.m_size.x ; ++i ) {
                screen.addGraphableToProcess(new Tile(new Vec2(m_pos1.x + Tile.m_size.x * i, m_pos2.y ), (short)-1));
            }
            m_pos2.y += Tile.m_size.y;
        }

    }

}
