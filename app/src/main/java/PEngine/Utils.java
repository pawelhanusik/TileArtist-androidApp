package PEngine;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import android.util.TypedValue;

import java.util.Vector;

//Utils.toGrayscale(Utils.whitening(bitmap, 3.f));
public class Utils {
    public static String milisToTimeStirng(int milis)
    {
        String ret = "";

        int secs =  milis / 1000;
        int mins = secs / 60;
        int hours = mins / 60;

        milis %= 1000;
        secs %= 60;
        mins %= 60;
        hours %= 60;

        if(hours > 0) {
            ret = hours + ":"
                    + ((mins < 10) ? ("0" + mins) : mins) + ":"
                    + ((secs < 10) ? ("0" + secs) : secs) + "."
                    +  milis/100;
        }else if(mins > 0) {
            ret = mins + ":"
                    + ((secs < 10) ? ("0" + secs) : secs) + "."
                    +  milis/100;
        }else if(secs > 0) {
            ret = secs + "."
                    +  milis/100;
        }
        else if(milis > 0) {
            ret = milis + "";
        }

        return ret;
    }

    public static int dp2px(Context context, int i) {
        return  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, i, context.getResources().getDisplayMetrics());
    }


    ///============IMAGES===========================================================================

    /// Method by: lenooh found at: stackoverflow.com/a/48390103
    public static Bitmap toGrayscale(Bitmap srcImage) {

        Bitmap bmpGrayscale = Bitmap.createBitmap(srcImage.getWidth(), srcImage.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bmpGrayscale);
        Paint paint = new Paint();

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(srcImage, 0, 0, paint);

        return bmpGrayscale;
    }

    public static Bitmap whitening(Bitmap srcImage, float factor)
    {
        int width = srcImage.getWidth();
        int height = srcImage.getHeight();
        Bitmap ret = Bitmap.createBitmap(width, height, srcImage.getConfig());

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                int pixel = srcImage.getPixel(x, y);
                int A = Color.alpha(pixel);
                int R = Color.red(pixel);
                int G = Color.green(pixel);
                int B = Color.blue(pixel);

                R = 255 - (int)( (255-R)/factor );
                G = 255 - (int)( (255-G)/factor );
                B = 255 - (int)( (255-B)/factor );

                ret.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        return ret;
    }

    public static Bitmap join(Bitmap a, Bitmap b) {
        Bitmap ret = Bitmap.createBitmap(a.getWidth(), a.getHeight(), a.getConfig());
        Canvas canvas = new Canvas(ret);
        canvas.drawBitmap(a, new Matrix(), null);
        canvas.drawBitmap(b, 0, 0, null);
        return ret;
    }
    public static int setFullAlpha(int c)
    {
        return Color.argb(255, Color.red(c), Color.green(c), Color.blue(c));
    }
    public static Bitmap setAlpha(Bitmap bitmap, int alpha)
    {
        Bitmap ret = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Paint paint = new Paint();
        paint.setAlpha(alpha);
        Canvas canvas = new Canvas(ret);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return ret;
    }

    public static int makeDarker(int color, int amount)
    {
        int R = Color.red(color);
        int G = Color.green(color);
        int B = Color.blue(color);
        R -= amount;
        G -= amount;
        B -= amount;

        return Color.rgb(R, G, B);
    }

    public static Bitmap cropToRect(Bitmap src)
    {
        int size = Math.min(src.getWidth(), src.getHeight());
        Bitmap ret = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        /*for(int y = (src.getHeight()-size) / 2; y < src.getHeight() - (src.getHeight()-size)/2; ++y)
            for(int x = (src.getWidth()-size) / 2; x < src.getWidth() - (src.getWidth()-size)/2; ++x)
                ;*/
        for(int y = 0; y < size; ++y)
            for(int x = 0; x < size; ++x)
                ret.setPixel(x, y,
                        src.getPixel(x + (src.getWidth()-size) / 2, y + (src.getHeight()-size) / 2));

        return ret;
    }
    public static Bitmap tilelize(Bitmap src, int tilesAmount)
    {
        int size = Math.min(src.getWidth(), src.getHeight());
        //Log.d("Utils", "Size=" + size);
        //Log.d("Utils", "TileAmount=" + tilesAmount);
        Bitmap ret = Bitmap.createBitmap(tilesAmount, tilesAmount, Bitmap.Config.ARGB_8888);

        for(int y = 0; y < tilesAmount; ++y) {
            for (int x = 0; x < tilesAmount; ++x) {
                //get avreage color
                int R=0, G=0, B=0;
                /*for(int i = x; i < x + size/tilesAmount; ++i) {
                    for (int j = y; j < y + size / tilesAmount; ++j) {
                        R += (src.getPixel(i, j));
                        G += (src.getPixel(i, j));
                        B += (src.getPixel(i, j));
                    }
                }

                R /= size/tilesAmount;
                G /= size/tilesAmount;
                B /= size/tilesAmount;*/

                //set ret pixel
                ret.setPixel(x, y, src.getPixel(x * (size/tilesAmount), y * (size/tilesAmount) ));
            }
        }


        return ret;
    }

    public static int maxSimilarity(int colorA, int colorB)
    {
        int ret = Math.abs(Color.red(colorA) - Color.red(colorB));
        if( ret < Math.abs(Color.green(colorA) - Color.green(colorB)) )
            ret = Math.abs(Color.green(colorA) - Color.green(colorB));
        if( ret < Math.abs(Color.blue(colorA) - Color.blue(colorB)) )
            ret = Math.abs(Color.blue(colorA) - Color.blue(colorB));
        return ret;
    }
    public static int minSimilarity(int colorA, int colorB)
    {
        int ret = Math.abs(Color.red(colorA) - Color.red(colorB));
        if( ret > Math.abs(Color.green(colorA) - Color.green(colorB)) )
            ret = Math.abs(Color.green(colorA) - Color.green(colorB));
        if( ret > Math.abs(Color.blue(colorA) - Color.blue(colorB)) )
            ret = Math.abs(Color.blue(colorA) - Color.blue(colorB));
        return ret;
    }
    public static Bitmap deentropize(Bitmap src, int maxColors)
    {
        Bitmap ret = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);

        Vector<Integer> colors = new Vector<>(maxColors);
        for(int y = 0; y < src.getHeight(); ++y){
            for(int x = 0; x < src.getWidth(); ++x) {
                int srcColor = src.getPixel(x, y);
                if (colors.size() < maxColors){
                    colors.add(srcColor);
                    ret.setPixel(x, y, Color.rgb(0, 0, colors.size()-1));
                } else {
                    int minID = 0;
                    int minVal = maxSimilarity(srcColor, colors.elementAt(0));
                    for (int c = 1; c < maxColors; ++c) {
                        if( maxSimilarity(srcColor, colors.elementAt(c)) < minVal ){
                            minVal = maxSimilarity(srcColor, colors.elementAt(c));
                            minID = c;
                        }
                    }
                    //for minInterVal
                    int minInterID = 0;
                    int minInterVal = maxSimilarity(colors.elementAt(minID), colors.elementAt(0));
                    for (int c = 1; c < maxColors; ++c) {
                        if( maxSimilarity(colors.elementAt(minID), colors.elementAt(c)) < minInterVal ){
                            minInterVal  = maxSimilarity(colors.elementAt(minID), colors.elementAt(c));
                            minInterID = c;
                        }
                    }
                    if( minInterVal < minVal ){
                        colors.set(minInterID, (colors.elementAt(minInterID) + colors.elementAt(minID)) / 2 );
                        colors.set(minID, srcColor);
                    }else{
                        colors.set(minID, (colors.elementAt(minID) + srcColor)/2 );
                    }


                    ret.setPixel(x, y, Color.rgb(0, 0, minID));

                    //colors.set(minID, (colors.elementAt(minID)+srcColor) / 2 );
                    //Log.d("Utils", "Changing " + minID + " CHECK MF: " + ret.getPixel(x, y) );
                }


            }
        }

        for(int y = 0; y < ret.getHeight(); ++y) {
            for (int x = 0; x < ret.getWidth(); ++x) {
                int colorID =ret.getPixel(x, y);
                colorID -= -16777216;
                //Log.d("UTILS", x + ", " + y + " => " + colorID);
                ret.setPixel(x, y, colors.elementAt(colorID));
            }
        }

        return ret;
    }

    public static int colorAverage(int colorA, int colorB)
    {
        return Color.rgb(
                (Color.red(colorA) + Color.red(colorB)) / 2,
                (Color.green(colorA) + Color.green(colorB)) / 2,
                (Color.blue(colorA) + Color.blue(colorB)) / 2

        );
    }
    public static int colorAverage(Vector<Integer> colors) {
        if(colors.size() <= 0)
            return 0;

        long r = 0, g = 0, b = 0;
        for(int c : colors) {
            r += Color.red(c);
            g += Color.green(c);
            b += Color.blue(c);
        }
        r /= colors.size();
        g /= colors.size();
        b /= colors.size();

        return Color.rgb((int)r, (int)g, (int)b);
    }
    public static Bitmap removeFamiliarColors(Bitmap src, int similarity)
    {
        Bitmap ret = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);

        short[][] retArr = new short[src.getWidth()][src.getHeight()];
        Vector<Integer> colors = new Vector<>();
        for(int y = 0; y < src.getHeight(); ++y){
            for(int x = 0; x < src.getWidth(); ++x) {
                int srcColor = src.getPixel(x, y);

                short destID = -1;
                for (short c = 0; c < colors.size(); ++c) {
                    //Log.wtf("Utils", c + " / " + colors.size());
                    if( maxSimilarity(srcColor, colors.elementAt(c)) < similarity ){
                        colors.set(c,
                                colorAverage(colors.elementAt(c), srcColor)
                        );
                        destID = c;

                        break;
                    }
                }
                if(destID == -1){
                    colors.add(srcColor);
                    destID = (short)(colors.size()-1);
                }

                //ret.setPixel(x, y, Color.rgb(0, 0, destID));
                retArr[x][y] = destID;
            }
        }

        for(int y = 0; y < ret.getHeight(); ++y) {
            for (int x = 0; x < ret.getWidth(); ++x) {
                /*int colorID =ret.getPixel(x, y);
                colorID -= -16777216;
                //Log.d("UTILS", x + ", " + y + " => " + colorID);
                ret.setPixel(x, y, colors.elementAt(colorID));*/
                ret.setPixel(x, y, colors.elementAt(retArr[x][y]));
            }
        }

        return ret;
    }
    public static void removeFamiliarColors(Vector<Integer> groups, Vector<Vector<Integer>> members, Vector<Integer> all, int similarity)
    {
        for(int col : all){
            short destID = -1;
            for (short c = 0; c < groups.size(); ++c) {
                if( maxSimilarity(col, groups.elementAt(c)) < similarity ){
                    groups.set(c,
                            colorAverage(groups.elementAt(c), col)
                    );
                    members.elementAt(c).add(col);
                    destID = c;
                    break;
                }
            }
            if(destID == -1){
                groups.add(col);
                members.add(new Vector<Integer>());
                members.elementAt(members.size()-1).add(col);
            }
        }
        all.clear();
    }

    //TODO: try to change the way ImageView scales images
    public static Bitmap upscale(Bitmap b, int wh)
    {
        return Bitmap.createScaledBitmap(b, wh, wh, false);
    }

    public static Bitmap rotate(Bitmap bitmap, float degrees)
    {
        Log.d("Utils", "Rotation by" + degrees + " degrees.");
        /*Bitmap ret = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Paint paint = new Paint();
        Canvas canvas = new Canvas(ret);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        canvas.rotate(-degrees, bitmap.getWidth()/2, bitmap.getHeight()/2);*/

        Bitmap ret = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(ret);
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees, bitmap.getWidth()/2, bitmap.getHeight()/2);
        canvas.drawBitmap(bitmap, matrix, null);

        return ret;
    }

    public static void debugBitmap(Bitmap b)
    {
        for(int x = 0; x < b.getWidth(); ++x){
            for(int y = 0; y < b.getHeight(); ++y){
                int col = b.getPixel(x, y);
                Log.d("Utils", x + ", " + y + " -> " + Color.alpha(col) + "  " + Color.red(col) + "  " + Color.green(col) + "  " + Color.blue(col) );
            }
        }
    }
}
