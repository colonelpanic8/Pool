package pool;

//

import java.awt.Point;

//  Collision.java
//  
//
//  Created by Ivan Malison on 12/8/09.
//  Copyright 2009 Reed College. All rights reserved.
//

public class Collision {
    double time;
    Ball ball1;
    Ball ball2;
    boolean inX;
    Point point;
    public Collision(double t, Ball b, Ball c){
	time = t;
	ball1 = b;
        ball2 = c;
    }
    public Collision(double t, Ball b, boolean dir) {
	time = t;
	ball1 = b;
	ball2 = null;
	inX = dir;
    }
    public Collision(double t, Point p, Ball b) {
	time = t;
	ball1 = b;
	ball2 = null;
	point = p;
    }    
}
