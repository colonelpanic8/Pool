package pool;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
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

    public double detectCollisionWith(Ball ball) {
	// a b c are the terms of a quadratic.  at^2 + bt + c  This code uses the quadratic equation to check for collisions.
	double a = ( (ball.vel.x) * (ball.vel.x) + (vel.x) * (vel.x) - 2*(ball.vel.x)*(vel.x) +
		     (ball.vel.y)*(ball.vel.y) + (vel.y) * (vel.y) - 2*(ball.vel.y)*(vel.y) );
	
	double b = 2 * ( (ball.getcx() * ball.vel.x) + (getcx() * vel.x) - (ball.getcx() * vel.x) -
			 (getcx() * ball.vel.x) + (ball.getcy() * ball.vel.y) + (getcy() * vel.y) - 
			 (ball.getcy() * vel.y) - (getcy() * ball.vel.y) );
	
	double c = ball.getcx() * ball.getcx() + getcx() * getcx() - 2 * (getcx() * ball.getcx()) +
	    ball.getcy() * ball.getcy() + getcy() * getcy() - 2 * (getcy() * ball.getcy())
	    - (size+ball.size)*(size+ball.size)/4;
	double t;
	if (a !=0 ){
	    double t1 = ( -b - Math.sqrt(b*b-4*a*c) )/(2 * a);  // These are the two solutions to the quadratic equation.
	    double t2 = ( -b + Math.sqrt(b*b-4*a*c) )/(2 * a);  // The smaller solution is always selected (unless it is
	    t = t1 < t2 ? t1 : t2;
	} else {
	    t = -c/b;  
	}
	return t; 
    }

    public double detectCollisionWith(Point p) {
	double a,b,c,t;
	a = vel.y*vel.y + vel.x*vel.x;
	b = 2*(vel.y*(getcy()-p.y) + vel.x*(getcx() - p.x));
	c = p.x*p.x + p.y*p.y - 2*p.x*getcx() - 2*p.y*getcy() +
	    getcy()*getcy() + getcx()*getcx() - size*size/4;
	if (a !=0 ){
	    double t1 = ( -b - Math.sqrt(b*b-4*a*c) )/(2 * a);
	    double t2 = ( -b + Math.sqrt(b*b-4*a*c) )/(2 * a);
	    t = t1 < t2 ? t1 : t2;
	} else {
	    t = -c/b;  
	}
	return t;
    }

    public double detectCollisionWith(Point p, int distance) {
	double a,b,c,t;
	a = vel.y*vel.y + vel.x*vel.x;
	b = 2*(vel.y*(getcy()-p.y) + vel.x*(getcx() - p.x));
	c = p.x*p.x + p.y*p.y - 2*p.x*getcx() - 2*p.y*getcy()
	    + getcy()*getcy() + getcx()*getcx() - distance*distance;
	if (a !=0 ){
	    double t1 = ( -b - Math.sqrt(b*b-4*a*c) )/(2 * a);
	    double t2 = ( -b + Math.sqrt(b*b-4*a*c) )/(2 * a);
	    t = t1 < t2 ? t1 : t2;
	} else {
	    t = -c/b;  
	}
	return t;
    }
    
}