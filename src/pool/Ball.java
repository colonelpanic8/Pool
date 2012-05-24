package pool;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;

public class Ball{
    Point2D.Double pos;
    Point2D.Double vel;
    int size;
    int alpha;
    Color color;
    boolean sel, sunk, remove;
    public Ball(Color col, double x, double y, double a, double b, int s){
	pos = new Point2D.Double(x,y);
	vel = new Point2D.Double(a,b);
	color = col;
	size = s;
	sunk = false;
	remove = false;
	alpha = 255;
    }
    public double getcx(){
	return pos.x + size/2;
    }
    public double getcy(){
	return pos.y + size/2;
    }
    public void draw(Graphics g) {
	if(sunk) {
	    alpha -= 5;
	    if(alpha > 0) {
		g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
		g.fillOval((int)pos.x, (int)pos.y, size, size);
	    } else {
		remove = true;
	    }
	} else {
	    g.setColor(color);
	    g.fillOval((int)pos.x, (int)pos.y, size, size);
	}
    }
}