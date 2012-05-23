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
    Ball ball1;
    Ball ball2;
    public Collision(double t, Ball b, Ball c){
	time = t;
	ball1 = b;
        ball2 = c;
    }
}
