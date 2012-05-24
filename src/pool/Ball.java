package pool;

import java.awt.Color;
import java.awt.geom.Point2D;

public class Ball{
    Point2D.Double pos;
    Point2D.Double vel;
    int size;
    Color color;
    boolean sel;
    public Ball(Color col, double x, double y, double a, double b, int s){
	pos = new Point2D.Double(x,y);
	vel = new Point2D.Double(a,b);
	color = col;
	size = s;
    }
    public double getcx(){
	return pos.x + size/2;
    }
    public double getcy(){
	return pos.y + size/2;
    }
}