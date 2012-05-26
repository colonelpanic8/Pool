package pool;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.PriorityQueue;

public class PoolPolygon extends Polygon {
    Color color;
    
    public PoolPolygon(int[] xpoints, int[] ypoints, int npoints, Color c) {
        super(xpoints, ypoints, npoints);
        color = c;
    }
    
    public void draw(Graphics g) {
        g.drawPolygon(this);
    }
    
    public void detectCollisions(Ball ball, PriorityQueue<Collision> collisions, double timePassed) {
        double min, t;
        boolean minIsPoint = false;
        Point2D.Double minVel = new Point2D.Double(0,0);
        Point2D.Double newVel = new Point2D.Double(0,0);
        Point a = new Point(xpoints[npoints-1], ypoints[npoints-1]);
        Point b = new Point(xpoints[0],ypoints[0]);
        min = Double.POSITIVE_INFINITY;
        for(int i = 0; i < npoints; i++) {
            b.setLocation(xpoints[i], ypoints[i]);
            t = detectWallCollision(a,b, ball, newVel);
            if(t > 0 && t < min) {
                min = t;
                minVel.setLocation(newVel);
                minIsPoint = false;
            }
            t = ball.detectCollisionWith(b);
            if(t > 0 && t < min) {
                min = t;
                minVel.setLocation(b);
                minIsPoint = true;
            }
            a.setLocation(b);
        }
        if(min + timePassed < 1) {
            if(minIsPoint) {
                Point p = new Point((int)minVel.x, (int)minVel.y);
                collisions.add(new PointCollision(min, p, ball));              
            } else {
                collisions.add(new WallCollision(min, ball, minVel));               
            }
        }
        
    }
    
    public static double detectWallCollision(Point a, Point b, Ball ball, Point2D.Double res) {
	Point2D.Double unit, trans, aInNewBasis, bInNewBasis, velInNewBasis, posInNewBasis;
	double dist = a.distance(b);
	unit          = new Point2D.Double((a.x-b.x)/dist,
					   (a.y-b.y)/dist);
	trans         = new Point2D.Double(1/(unit.x + unit.y*unit.y/unit.x),
					   1/(unit.y + unit.x*unit.x/unit.y));
	velInNewBasis = new Point2D.Double(trans.x*ball.vel.x + trans.y*ball.vel.y,
					   trans.y*ball.vel.x - trans.x*ball.vel.y);
	posInNewBasis = new Point2D.Double(trans.x*ball.getcx() + trans.y*ball.getcy(),
					   trans.y*ball.getcx() - trans.x*ball.getcy());
	aInNewBasis   = new Point2D.Double(trans.x*a.x + trans.y*a.y,
					   trans.y*a.x - trans.x*a.y);
	bInNewBasis   = new Point2D.Double(trans.x*b.x + trans.y*b.y,
					   trans.y*b.x - trans.x*b.y);
	double t;
	if (velInNewBasis.y != 0) {
            if(aInNewBasis.y > posInNewBasis.y){
                t = (aInNewBasis.y-(ball.size/2)-posInNewBasis.y)/velInNewBasis.y;
            } else {
                t = (aInNewBasis.y+(ball.size/2)-posInNewBasis.y)/velInNewBasis.y;
            }
	} else {
	    return Double.POSITIVE_INFINITY;
	}
	posInNewBasis.x += velInNewBasis.x*t;
	
	double larger, smaller;
	if(aInNewBasis.x > bInNewBasis.x) {
	    larger = aInNewBasis.x;
	    smaller = bInNewBasis.x;
	} else {
	    larger = bInNewBasis.x;
	    smaller = aInNewBasis.x;
	}
	if(posInNewBasis.x > larger || posInNewBasis.x < smaller) {
	    return Double.POSITIVE_INFINITY;
	}
	
	velInNewBasis.y = -velInNewBasis.y;
	res.setLocation(velInNewBasis.x*unit.x + velInNewBasis.y*unit.y,
			velInNewBasis.x*unit.y - velInNewBasis.y*unit.x);
	return t;
    }
}
