package PEngine.Math;

public class Vec2 {
    public float x, y;
    public Vec2()
    {
        this.x = 0.0f;
        this.y = 0.0f;
    }
    public Vec2(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    public void set(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    public void add(Vec2 a)
    {
        this.x += a.x;
        this.y += a.y;
    }
    public void subtract(Vec2 a)
    {
        this.x -= a.x;
        this.y -= a.y;
    }
    public void multiply(Vec2 a)
    {
        this.x *= a.x;
        this.y *= a.y;
    }
    public void divide(Vec2 a)
    {
        this.x /= a.x;
        this.y /= a.y;
    }
    public void multiply(float f)
    {
        this.x *= f;
        this.y *= f;
    }

    public static Vec2 add(Vec2 a, Vec2 b)
    {
        return new Vec2(a.x + b.x, a.y + b.y);
    }
    public static Vec2 subtract(Vec2 a, Vec2 b)
    {
        return new Vec2(a.x - b.x, a.y - b.y);
    }
    public static Vec2 multiply(Vec2 a, Vec2 b)
    {
        return new Vec2(a.x * b.x, a.y * b.y);
    }
    public static Vec2 divide(Vec2 a, Vec2 b)
    {
        return new Vec2(a.x / b.x, a.y / b.y);
    }

    public static Vec2 multiply(Vec2 a, float f)
    {
        return new Vec2(a.x * f, a.y * f);
    }



    public static boolean lessThanOrEqual(Vec2 a, Vec2 b)
    {
        return (a.x <= b.x && a.y <= b.y);
    }
    public static boolean greaterThanOrEqual(Vec2 a, Vec2 b)
    {
        return (a.x >= b.x && a.y >= b.y);
    }
}
