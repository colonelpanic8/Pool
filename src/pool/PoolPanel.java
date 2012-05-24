package pool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.Comparator;
import java.util.PriorityQueue;
import javax.swing.JPanel;
import javax.swing.Timer;

public class PoolPanel extends JPanel implements ActionListener, Comparator {
    Ball[] balls; // Contains all the balls on the table. Cueball always found at balls[0].
    boolean gball; // True when a ghostball needs to be drawn.
    int gcx;
    int gcy; // location of center of ghostball.
    double pocketSize;
    Ball wob; // the object ball that the ghost ball will be drawn next to
    int gballx; // location of ghostball (for drawing) (top right hand corner)
    int gbally;
    int ballSize;
    int numberofballs; // number of balls currently on the table
    Ball cueball;
    double tval;
    Point aPoint;
    int ticks;
    int pad;
    PriorityQueue<Collision> collisions;
    private double tillnframe;
    Aimer aimer;
    
    public PoolPanel(){
	setBackground(Color.gray);
	setPreferredSize(new Dimension(800,600));
	pad = 25;
	ballSize = 42;
	numberofballs = 1;
	pocketSize = 1.5*ballSize;
	aPoint = new Point((int)(1.5*ballSize + pad), pad);
	balls = new Ball[16];
	cueball = new Ball(Color.WHITE, 850, 200, 0, 0, ballSize);
	collisions = new PriorityQueue(16, this);
	balls[0] = cueball;
        ticks = 0;
	aimer = new Aimer(25, 100, cueball);
	this.addMouseListener(aimer);
	this.addMouseMotionListener(aimer);
	Timer timer = new Timer(15, this);
	timer.start();
	tval = 0;
    }
    
    public int compare(Object a, Object b) {
	double val =  ((Collision)a).time - ((Collision)b).time;
	if(val < 0) {
	    return -1;
	} else if (val > 0) {
	    return 1;
	} else {
	    return 0;
	}
    }

    @Override
    public boolean equals(Object obj) {
	return true;
    }
    
    @Override public void paintComponent(Graphics g){
	super.paintComponent(g);
	g.setColor(Color.BLACK);
	int height = getHeight();
	int width = getWidth();

	//WALLS

	g.drawLine(pad, 0, pad, height);
	g.drawLine(width - pad, 0, width-pad, height);
	g.drawLine(0, pad, width, pad);
	g.drawLine(0, height-pad, width, height-pad);
	

	//POCKETS
	g.fillOval(3*-cueball.size/2+pad, 3*-cueball.size/2+pad, 3*cueball.size, 3*cueball.size);
	g.fillOval(3*-cueball.size/2+pad, 3*-cueball.size/2+height-pad, 3*cueball.size, 3*cueball.size);
	g.fillOval(3*-cueball.size/2 + width - pad, 3*-cueball.size/2+pad, 3*cueball.size, 3*cueball.size);
	g.fillOval(3*-cueball.size/2 + width - pad, 3*-cueball.size/2+height - pad, 3*cueball.size, 3*cueball.size);
        
	int count = 0;
	while(count < numberofballs){
	    Ball temp = balls[count];
	    if (temp.remove) {
		balls[count] = balls[numberofballs - 1];
                numberofballs--;
		count--;
		temp = balls[count];
	    }
	    temp.draw(g);
	    count++;
	}
	g.setColor(Color.BLACK);
	if(Math.abs(cueball.vel.x) + Math.abs(cueball.vel.y) < 1){
	    g.drawLine((int)cueball.getcx(), (int)cueball.getcy(), 
                    (int)(cueball.getcx()+(-aimer.aim.x*600)), (int)(cueball.getcy()+(-aimer.aim.y*600)));
	    /*
	    g.drawLine( (int)(cx - -aimer.aim.y*cueball.size/2),
			(int)(cy + -aimer.aim.x*cueball.size/2), 
			(int)(cx + -aimer.aim.x*600 - -aimer.aim.y*cueball.size/2), 
			(int)(cy + -aimer.aim.y*600 + -aimer.aim.x*cueball.size/2));
	    g.drawLine( (int)(cx + -aimer.aim.y*cueball.size/2),
			(int)(cy - -aimer.aim.x*cueball.size/2), 
			(int)(cx + -aimer.aim.x*600 + -aimer.aim.y*cueball.size/2),
			(int)(cy + -aimer.aim.y*600 - -aimer.aim.x*cueball.size/2));
	    */
            if (gball){
		g.fillOval(gballx, gbally, cueball.size, cueball.size);
		int cenx = (int)(wob.pos.x+wob.size/2);
		int ceny = (int)(wob.pos.y+wob.size/2);
		int vgx = (int)(cenx - gcx);
		int vgy = (int)(ceny - gcy);
		g.drawLine(gcx, gcy, gcx + 15*vgx, gcy + 15*vgy); 
	    }

	    g.fillOval((int)(aimer.tracker.x - aimer.size/2),
		       (int)(aimer.tracker.y - aimer.size/2), aimer.size, aimer.size);
	}
	g.drawString(Double.toString(tval), 100, 100);

	//RAIL
	g.setColor(Color.darkGray);
	g.fillRect(0,0,width,pad);
	g.fillRect(0,0,pad,width);
	g.fillRect(0,height-pad,width,pad);
	g.fillRect(width-pad,0,pad,width);
    }
    
    
    
    public void detectBallCollisions() {
	tillnframe = 1;
	int count = 0;
	while (count < numberofballs){
	    Ball temp1 = balls[count];
	    int count2 = count + 1;
	    while(count2 < numberofballs){
		
		Ball temp2 = balls[count2];
		// a b c are the terms of a quadratic.  at^2 + bt + c  This code uses the quadratic equation to check for collisions.
		double a = ( (temp2.vel.x) * (temp2.vel.x) + (temp1.vel.x) * (temp1.vel.x) - 2*(temp2.vel.x)*(temp1.vel.x) +
			     (temp2.vel.y)*(temp2.vel.y) + (temp1.vel.y) * (temp1.vel.y) - 2*(temp2.vel.y)*(temp1.vel.y) );
		
		double b = 2 * ( (temp2.getcx() * temp2.vel.x) + (temp1.getcx() * temp1.vel.x) - (temp2.getcx() * temp1.vel.x) -
				 (temp1.getcx() * temp2.vel.x) + (temp2.getcy() * temp2.vel.y) + (temp1.getcy() * temp1.vel.y) - 
				 (temp2.getcy() * temp1.vel.y) - (temp1.getcy() * temp2.vel.y) );
		
		double c = temp2.getcx() * temp2.getcx() + temp1.getcx() * temp1.getcx() - 2 * (temp1.getcx() * temp2.getcx()) +
		    temp2.getcy() * temp2.getcy() + temp1.getcy() * temp1.getcy() - 2 * (temp1.getcy() * temp2.getcy())
		    - (temp1.size+temp2.size)*(temp1.size+temp2.size)/4;
		double dist = Math.sqrt( (temp1.getcx()-temp2.getcx())*(temp1.getcx()-temp2.getcx())   +  (temp1.getcy()-temp2.getcy())*(temp1.getcy()-temp2.getcy()) );
		double t;
		if (a !=0 ){
		    double t1 = ( -b - Math.sqrt(b*b-4*a*c) )/(2 * a);  // These are the two solutions to the quadratic equation.
		    double t2 = ( -b + Math.sqrt(b*b-4*a*c) )/(2 * a);  // The smaller solution is always selected (unless it is
		    t = t1 < t2 ? t1 : t2;
		} else {
                    t = -c/b;  
                }
                if(t < tillnframe && 0 <= t){
                    collisions.add(new Collision(t, temp1, temp2));
		} else {
                    if(Math.abs(t) < 3) {
                        temp1 = temp2;
                    }
		}
		count2++;
	    }
            count++;
        } 
    }
    
    public void detectWallCollisions() {
	int height = getHeight(); 
	int width = getWidth();
	int count = 0;
	while (count < numberofballs) {
	    Ball temp = balls[count];
	    double left, right, top, bottom, time;
	    left = (pad - temp.pos.x)/temp.vel.x;
	    right = (width - pad - temp.pos.x-temp.size)/temp.vel.x;
	    top = (pad - temp.pos.y)/temp.vel.y;
	    bottom = (height - pad - temp.pos.y-temp.size)/temp.vel.y;
	    
	    if(left >= 0 && left < 1) {
		if(left*temp.vel.y + temp.getcy() > 1.5*ballSize + pad &&
		   left*temp.vel.y + temp.getcy() < height - (1.5*ballSize + pad)) {
		    collisions.add(new Collision(left, temp, true));
		    return;
		} 
	    }
	    if(right >= 0 && right < 1) {
		if(right*temp.vel.y + temp.getcy() > 1.5*ballSize + pad &&
		   right*temp.vel.y + temp.getcy() < height - (1.5*ballSize + pad)) {
		    collisions.add(new Collision(right, temp, true));
		    return;
		}
	    }
	    if(top >= 0 && top < 1) {
		if(top*temp.vel.x + temp.getcx() > 1.5*ballSize + pad &&
		   top*temp.vel.x + temp.getcx() < width - (1.5*ballSize + pad)) {
		    collisions.add(new Collision(top, temp, false));
		    return;
		}
	    }
	    if(bottom >= 0 && bottom < 1) {
		if(bottom*temp.vel.x + temp.getcx() > 1.5*ballSize + pad &&
		   bottom*temp.vel.x + temp.getcx() < width - (1.5*ballSize + pad)) {
		    collisions.add(new Collision(bottom, temp, false));
		    return;
		}
	    }
	    //Top Left
            Point p = new Point((int)(1.5*ballSize + pad), pad);
	    time = checkPoint(temp, p);
	    if(time < 1 && time > 0) {
		collisions.add(new Collision(time, p, temp));
	    }
	    p = new Point(pad, (int)(1.5*ballSize + pad));
	    time = checkPoint(temp, p);
	    if(time < 1 && time > 0) {
		collisions.add(new Collision(time, p, temp));
	    }
	    
	    //Top Right
	    p = new Point((int)(width - (1.5*ballSize + pad)), pad);
	    time = checkPoint(temp, p);
	    if(time < 1 && time > 0) {
		collisions.add(new Collision(time, p, temp));
	    }
	    p = new Point((int)(width - pad), (int)(1.5*ballSize + pad));
	    time = checkPoint(temp, p);
	    if(time < 1 && time > 0) {
		collisions.add(new Collision(time, p, temp));
	    }

	    //Bottom Right
	    p = new Point((int)(width - (1.5*ballSize + pad)), height - pad);
	    time = checkPoint(temp, p);
	    if(time < 1 && time > 0) {
		collisions.add(new Collision(time, p, temp));
	    }
	    p = new Point((int)(width - pad), (int)(height - (1.5*ballSize + pad)));
	    time = checkPoint(temp, p);
	    if(time < 1 && time > 0) {
		collisions.add(new Collision(time, p, temp));
	    }

	    //Bottom Left
	    p = new Point((int)(1.5*ballSize + pad), height - pad);
	    time = checkPoint(temp, p);
	    if(time < 1 && time > 0) {
		collisions.add(new Collision(time, p, temp));
	    }
	    p = new Point(pad, (int)(height - (1.5*ballSize + pad)));
	    time = checkPoint(temp, p);
	    if(time < 1 && time > 0) {
		collisions.add(new Collision(time, p, temp));
	    }
	    count++;
	}
    }

    public double checkPoint(Ball ball, Point p) {
	double a,b,c,t;
	a = ball.vel.y*ball.vel.y + ball.vel.x*ball.vel.x;
	b = 2*(ball.vel.y*(ball.getcy()-p.y) + ball.vel.x*(ball.getcx() - p.x));
	c = p.x*p.x + p.y*p.y - 2*p.x*ball.getcx() - 2*p.y*ball.getcy()
	    + ball.getcy()*ball.getcy() + ball.getcx()*ball.getcx() - ball.size*ball.size/4;
	if (a !=0 ){
		    double t1 = ( -b - Math.sqrt(b*b-4*a*c) )/(2 * a);  // These are the two solutions to the quadratic equation.
		    double t2 = ( -b + Math.sqrt(b*b-4*a*c) )/(2 * a);  // The smaller solution is always selected (unless it is
		    t = t1 < t2 ? t1 : t2;
	} else {
	    t = -c/b;  
	}
	return t;
    }
    
    public void collisionEffects() {
	Collision coll = collisions.poll();
        while(coll != null) {
	    double time = coll.time;
	    if(coll.ball2 != null) {
		Ball tempa = coll.ball1;
		Ball tempb = coll.ball2;
		tempa.pos.x = tempa.pos.x + time * tempa.vel.x;
		tempa.pos.y = tempa.pos.y + time * tempa.vel.y;
		tempb.pos.x = tempb.pos.x + time * tempb.vel.x;
		tempb.pos.y = tempb.pos.y + time * tempb.vel.y;
		double xdif = tempb.getcx() - tempa.getcx();
		double ydif = tempb.getcy() - tempa.getcy();
		double dist = Math.sqrt(xdif*xdif + ydif*ydif);
		double xp = xdif/dist;
		double yp = ydif/dist;
		double xo = -yp;
		double yo = xp;
		double vp1 = xp * tempa.vel.x + yp * tempa.vel.y;
		double vp2 = xp * tempb.vel.x + yp * tempb.vel.y;
		double vo1 = xo * tempa.vel.x + yo * tempa.vel.y;
		double vo2 = xo * tempb.vel.x + yo * tempb.vel.y;
		tempa.vel.x = vp2 * xp - vo1 * yp;
		tempa.vel.y = vp2 * yp + vo1 * xp;
		tempb.vel.x = vp1 * xp - vo2 * yp;
		tempb.vel.y = vp1 * yp + vo2 * xp;
	    } else {
		coll.ball1.pos.x += time*coll.ball1.vel.x;
		coll.ball1.pos.y += time*coll.ball1.vel.y;
		if(coll.point != null) {
		    Point2D.Double unit, trans, temp, res;
		    double dist = coll.point.distance(coll.ball1.getcx(), coll.ball1.getcy());
		    unit = new Point2D.Double((coll.point.x - coll.ball1.getcx())/dist,
					      (coll.point.y - coll.ball1.getcy())/dist );
		    trans = new Point2D.Double(1/(unit.x + unit.y*unit.y/unit.x), 1/(unit.y + unit.x*unit.x/unit.y));
		    temp = new Point2D.Double(trans.x*coll.ball1.vel.x, trans.y*coll.ball1.vel.y);
		    temp.x += trans.y*coll.ball1.vel.y;
		    temp.y += -trans.x*coll.ball1.vel.y;
		    temp.x = -temp.x;
		    
		    res = new Point2D.Double(temp.x*unit.x, temp.x*unit.y);
		    res.x += temp.y*unit.y;
		    res.y += -temp.y*unit.x;
		    coll.ball1.vel = res;
		} else{ 
		    if(coll.inX) {
			coll.ball1.vel.x = -coll.ball1.vel.x;
		    } else{
			coll.ball1.vel.y = -coll.ball1.vel.y;
		    }
                }
	    }
	    tillnframe = tillnframe - time;
	    int count = 0;
	    while(count < numberofballs){
		Ball temp = balls[count];
		if (!(temp == coll.ball1) && !(temp == coll.ball2)){
		    temp.pos.x = temp.pos.x + temp.vel.x * time;
		    temp.pos.y = temp.pos.y + temp.vel.y * time;
		}
		count++;
	    }
            coll = collisions.poll();
	}
	int count = 0;
	while(count < numberofballs) {
	    Ball temp = balls[count];
	    temp.pos.x += temp.vel.x*tillnframe;
            temp.pos.y += temp.vel.y*tillnframe;
	    if (Math.abs(temp.vel.x) < .10 && Math.abs(temp.vel.y) < .10){
		temp.vel.x = 0;
		temp.vel.y = 0;
	    }
	    temp.vel.x = .99*temp.vel.x;
	    temp.vel.y = .99*temp.vel.y;
            count++;
	}
    }

    public void updateGhostBall() {
	int count;
        double tmin = 2000;
	gball = false;
	for(count = 1; count < numberofballs; count ++) {
	    Ball temp = balls[count];
	    if(!(temp==cueball)){
		// Quadratic with solutions (similar to collision detection)
		double a = aimer.aim.x*aimer.aim.x + aimer.aim.y*aimer.aim.y;
		double b = 2 * (cueball.getcx()*-aimer.aim.x - temp.getcx()*-aimer.aim.x + 
				cueball.getcy()*-aimer.aim.y - temp.getcy()*-aimer.aim.y);
		double c = cueball.getcx()*cueball.getcx() + temp.getcx()*temp.getcx() +
		    cueball.getcy()*cueball.getcy() + temp.getcy()*temp.getcy() - 2*cueball.getcx()*temp.getcx() - 
		    2*cueball.getcy()*temp.getcy() - (temp.size + cueball.size)*(temp.size + cueball.size)/4;
		double t1 = ( -b - Math.sqrt(b*b-4*a*c) )/(2 * a);
		double t2 = ( -b + Math.sqrt(b*b-4*a*c) )/(2 * a);
		double time = t1 < t2 ? t1 : t2;
		
		if( !(Double.isNaN(time)) && time < tmin && time > 0){
		    gball = true;
		    gballx = (int)(cueball.pos.x + time * -aimer.aim.x);
		    gbally = (int)(cueball.pos.y + time * -aimer.aim.y);
		    gcx = (int)(cueball.getcx() + time * -aimer.aim.x);
		    gcy = (int)(cueball.getcy() + time * -aimer.aim.y);
		    wob = temp; // sets temp to the object ball on which the ghostball is drawn
		    tmin = time;
		}
	    }
	}
    }

    public void checkPockets() {
	for(int count = 0; count < numberofballs; count++) {
	    Ball temp = balls[count];
	    Point p = new Point(pad,pad);
	    if(p.distance(temp.getcx(), temp.getcy()) < pocketSize-temp.size/2) {
		removeBall(temp);
	    }
	    p.setLocation(pad,getHeight()-pad);
	    if(p.distance(temp.getcx(), temp.getcy()) < pocketSize-temp.size/2) {
		removeBall(temp);
	    }
	    p.setLocation(getWidth()-pad,pad);
	    if(p.distance(temp.getcx(), temp.getcy()) < pocketSize-temp.size/2) {
		removeBall(temp);
	    }
	    p.setLocation(getWidth() -pad,getHeight()-pad);
	    if(p.distance(temp.getcx(), temp.getcy()) < pocketSize-temp.size/2) {
		removeBall(temp);
	    }
	}
    }

    public void removeBall(Ball b) {
	b.vel.x = 0;
	b.vel.y = 0;
	b.sunk = true;
    }
    
    public void actionPerformed(ActionEvent evt){
	detectBallCollisions();
	detectWallCollisions();
	collisionEffects();
	checkPockets();
	updateGhostBall();
	aimer.doShoot();
	tval = Math.sqrt(cueball.vel.x*cueball.vel.x + cueball.vel.y*cueball.vel.y);
	this.repaint();
    }
}

class Aimer implements MouseMotionListener, MouseListener {
    boolean dragging, shooting;
    Ball cb;
    int size;
    int length;
    Point.Double aim;
    Point.Double tracker;
    Point.Double click;
    int vel;
    int acc;
    double distance;
    Aimer(int s, int l, Ball ball) {
	size = s;
	length = l;
	cb = ball;
	aim = new Point2D.Double(1,0);
	tracker = new Point2D.Double(0,1);
	click = new Point2D.Double(0,1);
	dragging = false;
	shooting = false;
	vel = 0;
	acc = 2;
    }

    public void doShoot() {
	if(!dragging && ! shooting) {
	    tracker.setLocation(cb.getcx(), cb.getcy());
	}
	if(shooting) {
	    vel += acc;
	    if(distance > vel) {
		distance -= vel;
                tracker.x = cb.getcx() + aim.x*distance;
		tracker.y = cb.getcy() + aim.y*distance;   
	    } else {
		shooting = false;
		cb.vel.x = -aim.x*vel;
		cb.vel.y = -aim.y*vel;
		vel = 0;
            }
	}
    }
    
    public void mousePressed(MouseEvent evt) {
	click.setLocation(evt.getX(), evt.getY());
	PoolPanel a = (PoolPanel)evt.getSource();
	if(click.distance(tracker) < size && !shooting) {
	    dragging = true;
	}
    }

    public void mouseEntered(MouseEvent evt) { }
    public void mouseExited(MouseEvent evt) { }
    public void mouseClicked(MouseEvent evt) { }
    public void mouseReleased(MouseEvent evt) {
	if(dragging) {
	    dragging = false;
	    if(distance > 20) {
		shooting = true;
	    }
	}
    }
    public void mouseMoved(MouseEvent evt) {
	click.setLocation(evt.getX(), evt.getY());
	PoolPanel a = (PoolPanel)evt.getSource();
	a.tval = click.distance(tracker.x, tracker.y);
    }
    public void mouseDragged(MouseEvent evt){
	PoolPanel a = (PoolPanel)evt.getSource();
	if (dragging){
	    tracker.setLocation(evt.getX(), evt.getY());
	    distance = tracker.distance(cb.getcx(), cb.getcy());
	    aim.x = (tracker.x - cb.getcx())/distance;
	    aim.y = (tracker.y - cb.getcy())/distance;
	}
    }
}