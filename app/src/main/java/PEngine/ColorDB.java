package PEngine;


import android.graphics.Paint;

import java.util.Vector;

public class ColorDB {

    private static Vector<Paint> paints;

    public static void init(short c_count)
    {
        paints = new Vector<>(c_count);
        for(int i = 0; i < paints.capacity(); ++i)
        {
            Paint p = new Paint();
            p.setARGB(255, 255, 255, 255);
            paints.add(p);
        }
    }

    public static void setColor(short id, int red, int green, int blue)
    {
        if(id < paints.size() && id >= 0)
            paints.elementAt(id).setARGB(255, red, green, blue);
    }
    public static Paint getColor(short id)
    {
        if(id < paints.size() && id >= 0)
            return paints.elementAt(id);
        else
            return new Paint();
    }
    public static void addColor(int red, int green, int blue)
    {
        Paint p = new Paint();
        p.setARGB(255, red, green, blue);
        paints.add(p);
    }
}
