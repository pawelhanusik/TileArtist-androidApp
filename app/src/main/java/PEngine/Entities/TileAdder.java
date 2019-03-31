package PEngine.Entities;

import android.graphics.Canvas;
import android.graphics.Paint;

import PEngine.Math.Vec2;

public class TileAdder extends Tile {

    private Paint paint_plusSign;
    private Vec2 d_plusPos1, d_plusPos2;
    private Vec2 d_plusSize;

    public TileAdder(Vec2 pos) {
        super(pos, (short) -5);


        d_plusPos1 = new Vec2();
        d_plusPos2 = new Vec2();
        d_plusSize = new Vec2();

        paint_plusSign = new Paint();
        paint_plusSign.setARGB(255, 255, 255, 255);
    }

    @Override
    public void update(Vec2 dimensions, float baseX, float baseY, float scale) {
        super.update(dimensions, baseX, baseY, scale);

        if(d_fitOnScreen) {
            d_plusSize.x = 10 * scale;
            d_plusSize.y = 5 * scale;


            d_plusPos1.x = (d_pos2.x - d_pos1.x - d_plusSize.x) / 2;
            d_plusPos1.y = (d_pos2.y - d_pos1.y - d_plusSize.y) / 2;

            d_plusPos2.x = (d_pos2.x - d_pos1.x - d_plusSize.y) / 2;
            d_plusPos2.y = (d_pos2.y - d_pos1.y - d_plusSize.x) / 2;
        }
    }

    @Override
    public void draw(Canvas canvas, Paint paint, float baseX, float baseY, float scale) {
        super.draw(canvas, paint, baseX, baseY, scale);

        if(d_fitOnScreen) {
            canvas.drawRect(d_plusPos1.x, d_plusPos1.y, d_plusPos1.x + d_plusSize.x, d_plusPos1.y + d_plusSize.y, paint_plusSign);
            canvas.drawRect(d_plusPos2.x, d_plusPos2.y, d_plusPos2.x + d_plusSize.y, d_plusPos2.y + d_plusSize.x, paint_plusSign);
        }
    }
}
