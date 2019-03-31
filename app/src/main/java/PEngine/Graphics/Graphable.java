package PEngine.Graphics;


import android.graphics.Canvas;
import android.graphics.Paint;

import PEngine.Math.Vec2;

public interface Graphable {
    Vec2 m_pos = new Vec2();
    Vec2 m_size = new Vec2();
    void update(Vec2 dimensions, float baseX, float baseY, float scale);
    void draw(Canvas canvas, Paint paint, float baseX, float baseY, float scale);
}
