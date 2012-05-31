package pool;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.PriorityQueue;
import javax.media.j3d.*;
import javax.vecmath.Point3d;

public class PoolPolygon extends Polygon2D {
    Color color;
    QuadArray vertices;
    BranchGroup group;
    
    
    public PoolPolygon(double[] xpoints, double[] ypoints, int npoints, Color c, double ballsize) {
        super(xpoints, ypoints, npoints);
        color = c;
        
        vertices = new QuadArray ((npoints+1)*4, QuadArray.COORDINATES);
        int j = 0;
        for(int i = 0; i < npoints - 1; i++) {
            vertices.setCoordinate (j++, new Point3d(xpoints[i], ypoints[i], ballsize));
            vertices.setCoordinate (j++, new Point3d (xpoints[i], ypoints[i], -ballsize));
            vertices.setCoordinate (j++, new Point3d(xpoints[i+1], ypoints[i+1], -ballsize));
            vertices.setCoordinate (j++, new Point3d (xpoints[i+1], ypoints[i+1], ballsize));
        }
        
        int i = 0;
        vertices.setCoordinate (j++, new Point3d(xpoints[i], ypoints[i++], ballsize));
        vertices.setCoordinate (j++, new Point3d(xpoints[i], ypoints[i++], ballsize));
        vertices.setCoordinate (j++, new Point3d(xpoints[i], ypoints[i++], ballsize));
        vertices.setCoordinate (j++, new Point3d(xpoints[i], ypoints[i++], ballsize));

    	group = new BranchGroup();
        group.addChild(new Shape3D(vertices, new Appearance()));
    }
    
    
    
    public boolean checkOverlap(Ball ball) {
        Point2D.Double a = new Point.Double(xpoints[npoints-1], ypoints[npoints-1]);        
        Point2D.Double b = new Point.Double(xpoints[0], ypoints[0]);        
        Point2D.Double center = new Point2D.Double(ball.pos.x, ball.pos.y);
        double min = Double.POSITIVE_INFINITY;
        for(int i = 0; i < npoints; i++) {
            b.setLocation(xpoints[i], ypoints[i]);
            double temp = PoolPolygon.distanceToSegment(a,b,center);
            if(temp < min)
                min = temp;
            a.setLocation(b);
        }
        if(min < ball.size/2){
            return true;
        }
        return false;
    }
    
    public void detectCollisions(Ball ball, PriorityQueue<Collision> collisions, double timePassed) {
        double min, t;
        int minWall = -1;
        boolean minIsPoint = false;
        Point2D.Double minVel = new Point2D.Double(0,0);
        Point2D.Double newVel = new Point2D.Double(0,0);
        Point2D.Double a = new Point2D.Double(xpoints[npoints-1], ypoints[npoints-1]);
        Point2D.Double b = new Point2D.Double(xpoints[0],ypoints[0]);
        min = Double.POSITIVE_INFINITY;
        for(int i = 0; i < npoints; i++) {
            b.setLocation(xpoints[i], ypoints[i]);
            t = detectWallCollision(a,b, ball, newVel);
            if(t >= 0 && t < min) {
                min = t;
                minWall = i;
                minVel.setLocation(newVel);
                minIsPoint = false;
            }
            t = ball.detectCollisionWith(b);
            if(t >= 0 && t < min) {
                min = t;
                minVel.setLocation(b);
                minIsPoint = true;
            }
            a.setLocation(b);
        }
        if(min + timePassed < 1) {
            if(minIsPoint) {
                Point p = new Point((int)minVel.x, (int)minVel.y);
                collisions.add(new PointCollision(min + timePassed, p, ball));              
            } else {
                collisions.add(new WallCollision(min + timePassed, ball, minVel, this, minWall));               
            }
        }    
    }
    
    public void detectCollisionsWithoutWall(Ball ball, PriorityQueue<Collision> collisions, double timePassed, int wall) {
        double min, t;
        boolean minIsPoint = false;
        int minWall = -1;
        Point2D.Double minVel = new Point2D.Double(0,0);
        Point2D.Double newVel = new Point2D.Double(0,0);
        Point2D.Double a = new Point2D.Double(xpoints[npoints-1], ypoints[npoints-1]);
        Point2D.Double b = new Point2D.Double(xpoints[0],ypoints[0]);
        min = Double.POSITIVE_INFINITY;
        for(int i = 0; i < npoints; i++) {
            b.setLocation(xpoints[i], ypoints[i]);
            if(i != wall) {
                t = detectWallCollision(a,b, ball, newVel);
                if(t > 0 && t < min) {
                    min = t;
                    minWall = i;
                    minVel.setLocation(newVel);
                    minIsPoint = false;
                }
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
                collisions.add(new PointCollision(min + timePassed, p, ball));              
            } else {
                collisions.add(new WallCollision(min + timePassed, ball, minVel, this, minWall));               
            }
        }   
    }
    
    
    
    public static double detectWallCollision(Point2D.Double a, Point2D.Double b, Ball ball, Point2D.Double res) {
	Point2D.Double unit, trans, aInNewBasis, bInNewBasis, velInNewBasis, posInNewBasis;
	double dist = a.distance(b);
	unit          = new Point2D.Double((a.x-b.x)/dist,
					   (a.y-b.y)/dist);
	trans         = new Point2D.Double(1/(unit.x + unit.y*unit.y/unit.x),
					   1/(unit.y + unit.x*unit.x/unit.y));
	velInNewBasis = new Point2D.Double(trans.x*ball.vel.x + trans.y*ball.vel.y,
					   trans.y*ball.vel.x - trans.x*ball.vel.y);
	posInNewBasis = new Point2D.Double(trans.x*ball.pos.x + trans.y*ball.pos.y,
					   trans.y*ball.pos.x - trans.x*ball.pos.y);
	aInNewBasis   = new Point2D.Double(trans.x*a.x + trans.y*a.y,
					   trans.y*a.x - trans.x*a.y);
	bInNewBasis   = new Point2D.Double(trans.x*b.x + trans.y*b.y,
					   trans.y*b.x - trans.x*b.y);
	double t;
	if (velInNewBasis.y != 0) {
            if(aInNewBasis.y > posInNewBasis.y){
                t = (aInNewBasis.y-(ball.size)-posInNewBasis.y)/velInNewBasis.y;
            } else {
                t = (aInNewBasis.y+(ball.size)-posInNewBasis.y)/velInNewBasis.y;
            }
	} else {
	    return Double.POSITIVE_INFINITY;
	}
        
        //We don't want to generate a collision if the ball is resting or sliding along the wall
        if(t == 0 ) {
            if(velInNewBasis.y == 0) {
                return Double.POSITIVE_INFINITY;
            } if(velInNewBasis.y < 0 != aInNewBasis.y - posInNewBasis.y < 0) {
                return Double.POSITIVE_INFINITY;
            }
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
    
     public static double distanceToSegment(double x3, double y3, double x1, double y1, double x2, double y2) {
	final Point2D p3 = new Point2D.Double(x3, y3);
	final Point2D p1 = new Point2D.Double(x1, y1);
	final Point2D p2 = new Point2D.Double(x2, y2);
	return distanceToSegment(p1, p2, p3);
    }


    public static double distanceToSegment(Point2D p1, Point2D p2, Point2D p3) {

	final double xDelta = p2.getX() - p1.getX();
        final double yDelta = p2.getY() - p1.getY();
        
        if ((xDelta == 0) && (yDelta == 0)) {
            throw new IllegalArgumentException("p1 and p2 cannot be the same point");
        }
        
	final double u = ((p3.getX() - p1.getX()) * xDelta + (p3.getY() - p1.getY()) * yDelta) / (xDelta * xDelta + yDelta * yDelta);
        
        final Point2D closestPoint;
        if (u < 0) {
            closestPoint = p1;
	} else if (u > 1) {
            closestPoint = p2;
        } else {
	    closestPoint = new Point2D.Double(p1.getX() + u * xDelta, p1.getY() + u * yDelta);
        }

	return closestPoint.distance(p3);
    }
}