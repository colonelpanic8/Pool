package pool;

//

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Iterator;

//  Collision.java
//  
//
//  Created by Ivan Malison on 12/8/09.
//  Copyright 2009 Reed College. All rights reserved.
//

public abstract class Collision {
    double time;
    Ball ball1;

    public abstract void doEffects(PoolPanel pp);

    public void doCollision(PoolPanel pp) {
	Iterator<Collision> collIterator = pp.collisions.iterator();
	Iterator<Ball> ballIterator;
	while(collIterator.hasNext()) {
	    Collision item = collIterator.next();
	    if(item.involves(ball1)) {
		collIterator.remove();
	    }
	}
	doEffects(pp);
	ballIterator = pp.balls.iterator();
	while(ballIterator.hasNext()) {
	    Ball ball = ballIterator.next();
	    if( ball != ball1) {
		double t = ball1.detectCollisionWith(ball);
		t += time;
		if(t < 1 && t > time){
		    pp.collisions.add(new BallCollision(t, ball1, ball));
		}
	    }
	}
	pp.detectWallCollisions(ball1, time);
	pp.checkPockets(ball1, time);
    }
        
    public boolean involves(Ball b) {
        return ball1 == b;
    }
}

class BallCollision extends Collision {
    Ball ball2;

    public BallCollision(double t, Ball b, Ball c){
	time = t;
	ball1 = b;
        ball2 = c;
    }

    @Override public void doCollision(PoolPanel pp) {
	Iterator<Ball> ballIterator;
	super.doCollision(pp);
	ballIterator = pp.balls.iterator();
	while(ballIterator.hasNext()) {
	    Ball ball = ballIterator.next();
	    if(ball != ball1 && ball != ball2){
		double t = ball2.detectCollisionWith(ball);
		t += time;
		if(t < 1 && t > time){
		    pp.collisions.add(new BallCollision(t, ball2, ball));
		}
	    }
	}
	pp.detectWallCollisions(ball2, time);
	pp.checkPockets(ball2, time);
    }
    
    @Override public void doEffects(PoolPanel pp) {
	//Remove collisions involving ball2
	Iterator<Collision> collIterator = pp.collisions.iterator();
	while(collIterator.hasNext()) {
	    Collision item = collIterator.next();
	    if(item.involves(ball2)) {
		collIterator.remove();
	    }
	}
	//Collision effects
	double xdif = ball2.getcx() - ball1.getcx();
	double ydif = ball2.getcy() - ball1.getcy();
	double dist = Math.sqrt(xdif*xdif + ydif*ydif);
	double xp = xdif/dist;
	double yp = ydif/dist;
	double xo = -yp;
	double yo = xp;
	double vp1 = xp * ball1.vel.x + yp * ball1.vel.y;
	double vp2 = xp * ball2.vel.x + yp * ball2.vel.y;
	double vo1 = xo * ball1.vel.x + yo * ball1.vel.y;
	double vo2 = xo * ball2.vel.x + yo * ball2.vel.y;
	ball1.vel.x = vp2 * xp - vo1 * yp;
	ball1.vel.y = vp2 * yp + vo1 * xp;
	ball2.vel.x = vp1 * xp - vo2 * yp;
	ball2.vel.y = vp1 * yp + vo2 * xp;
    }
}

class WallCollision extends Collision {
    boolean inX;
    public WallCollision(double t, Ball b, boolean dir) {
	time = t;
	ball1 = b;
	inX = dir;
    }

    @Override public void doEffects(PoolPanel pp) {
	if(inX) {
	    ball1.vel.x = -ball1.vel.x;
	} else{
	    ball1.vel.y = -ball1.vel.y;
	}
    }
}

class PointCollision extends Collision {
    Point point;
    public PointCollision(double t, Point p, Ball b) {
	time = t;
	ball1 = b;
	point = p;
    }

    @Override public void doEffects(PoolPanel pp) {
	Point2D.Double unit, trans, temp, res;
	double dist = point.distance(ball1.getcx(), ball1.getcy());
	unit = new Point2D.Double((point.x - ball1.getcx())/dist,
				  (point.y - ball1.getcy())/dist );
	trans = new Point2D.Double(1/(unit.x + unit.y*unit.y/unit.x), 1/(unit.y + unit.x*unit.x/unit.y));
	temp = new Point2D.Double(trans.x*ball1.vel.x, trans.y*ball1.vel.y);
	temp.x += trans.y*ball1.vel.y;
	temp.y += -trans.x*ball1.vel.y;
	temp.x = -temp.x;
	
	res = new Point2D.Double(temp.x*unit.x, temp.x*unit.y);
	res.x += temp.y*unit.y;
	res.y += -temp.y*unit.x;
	ball1.vel = res;
    }
}

class PocketCollision extends Collision {
    public PocketCollision(Ball b, double t) {
	ball1 = b;
	time = t;
    }
    @Override public void doEffects(PoolPanel pp) {
	ball1.vel.x = 0;
	ball1.vel.y = 0;
	ball1.sunk = true;
    }
}
