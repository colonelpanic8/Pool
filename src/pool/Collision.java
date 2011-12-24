package pool;

//
//  Collision.java
//  
//
//  Created by Ivan Malison on 12/8/09.
//  Copyright 2009 Reed College. All rights reserved.
//

public class Collision {
	double time;
	int type;
	Ball ball;
	int wpb;  //wall pocket or ball
	public Collision(double t, Ball b, int a, int c){
		time = t;
		ball = b;
		type = a;
		wpb = c;
	}
}
