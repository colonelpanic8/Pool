package pool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Comparator;
import java.util.PriorityQueue;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Point;
import java.awt.event.*;
import java.awt.geom.*;

public class PoolPanel extends JPanel implements ActionListener, Comparator {
    Ball[] balls; // Contains all the balls on the table. Cueball always found at balls[0].
    boolean gball; // True when a ghostball needs to be drawn.
    int gcx;
    int gcy; // location of center of ghostball.
    double pocketsize;
    Ball wob; // the object ball that the ghost ball will be drawn next to
    int gballx; // location of ghostball (for drawing) (top right hand corner)
    int gbally;
    int numberofballs; // number of balls currently on the table
    Ball cueball;
    double tval;
    int ticks;
    PriorityQueue<Collision> collisions;
    private double tillnframe;
    Aimer aimer;
    
    public PoolPanel(){
	setBackground(Color.GREEN);
	setPreferredSize(new Dimension(800,600));
	numberofballs = 1;
	pocketsize = 1.5;
	balls = new Ball[16];
	cueball = new Ball(Color.WHITE, 500, 400, 0, 0, 42);
	collisions = new PriorityQueue(16, this);
	balls[0] = cueball;
        ticks = 0;
	Timer timer = new Timer(15, this);
	timer.start();
	tval = 0;
	aimer = new Aimer(25, 100, cueball);
	this.addMouseListener(aimer);
	this.addMouseMotionListener(aimer);
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
	if (gball){
	    g.fillOval(gballx, gbally, cueball.size, cueball.size);
	    int cenx = (int)(wob.pos.x+wob.size/2);
	    int ceny = (int)(wob.pos.y+wob.size/2);
	    int vgx = (int)(cenx - gcx);
	    int vgy = (int)(ceny - gcy);
	    g.drawLine(gcx, gcy, gcx + 15*vgx, gcy + 15*vgy); 
	}
	g.drawLine(0, (int)(pocketsize*cueball.size), (int)(pocketsize*cueball.size), 0);
	g.drawLine(0, (int)height - (int)(pocketsize*cueball.size), (int)(pocketsize*cueball.size), height);
	g.drawLine((int)(width - pocketsize*cueball.size), height, width, (int)(height-pocketsize*cueball.size));
	g.drawLine((int)(width - pocketsize*cueball.size), 0, width, (int)(pocketsize*cueball.size));
	int cx = (int)(cueball.pos.x + cueball.size/2);
	int cy = (int)(cueball.pos.y + cueball.size/2);
	int count = 0;
	while(count < numberofballs){
	    Ball temp = balls[count];
	    g.setColor(temp.color);
	    g.fillOval((int)temp.pos.x, (int)temp.pos.y, temp.size, temp.size);
	    count++;
	}
	int c2 = 0;
	g.setColor(Color.BLACK);
	if(cueball.vel.x < 1 && cueball.vel.y < 1){
	    g.drawLine( (int)(cx - -aimer.aim.y*cueball.size/2) , (int)(cy + -aimer.aim.x*cueball.size/2) , (int)(cx + -aimer.aim.x*600 - -aimer.aim.y*cueball.size/2) , (int)(cy + -aimer.aim.y*600 + -aimer.aim.x*cueball.size/2) );
	    g.drawLine(cx, cy, (int)(cx+(-aimer.aim.x*600)), (int)(cy+(-aimer.aim.y*600)));
	    g.drawLine( (int)(cx + -aimer.aim.y*cueball.size/2) , (int)(cy - -aimer.aim.x*cueball.size/2) , (int)(cx + -aimer.aim.x*600 + -aimer.aim.y*cueball.size/2) , (int)(cy + -aimer.aim.y*600 - -aimer.aim.x*cueball.size/2) );
	    g.fillOval((int)(cueball.getcx() + aimer.length*aimer.aim.x - aimer.size/2),
		       (int)(cueball.getcy() + aimer.length*aimer.aim.y - aimer.size/2), aimer.size, aimer.size);
	    g.drawLine((int)(cueball.getcx() + aimer.length*aimer.aim.x),
		       (int)(cueball.getcy() + aimer.length*aimer.aim.y), (int)cueball.getcx(), (int)cueball.getcy());
	}
	g.fillOval(3*-cueball.size/2, 3*-cueball.size/2, 3*cueball.size, 3*cueball.size);
	g.drawString(Double.toString(tval), 100, 100);
    }
    
    
    
    public void detectCollisions() {
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
    
    public void collisionEffects() {
	Collision coll = collisions.poll();
        while(coll != null) {
	    double time = coll.time;
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
	    tillnframe = tillnframe - time;
	    int count = 0;
	    while(count < numberofballs){
		Ball temp = balls[count];
		if (!(temp == tempa) && !(temp == tempb)){
		    temp.pos.x = temp.pos.x + temp.vel.x * time;
		    temp.pos.y = temp.pos.y + temp.vel.y * time;
		}
		count++;
	    }
            coll = collisions.poll();
	}

    }

    public void updateGhostBall() {
	int count;
        double tmin = 2000;
	gball = false;
	for(count = 1; count < numberofballs; count ++) {
	    Ball temp = balls[count];
	    if(cueball.vel.x == 0 && cueball.vel.y == 0 && ! (temp==cueball)){
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
    
    
    public void actionPerformed(ActionEvent evt){
	int height = getHeight(); 
	int width = getWidth();
        ticks += 1;
	detectCollisions();
	collisionEffects();
	updateGhostBall();

	int count = 0;
	while (count < numberofballs) {
	    Ball temp = balls[count];
	    if( !(temp==cueball) && (
				     temp.pos.x+cueball.size + temp.pos.y+cueball.size <= pocketsize*cueball.size ||
				     temp.pos.x < -cueball.size || temp.pos.x > width || temp.pos.y> height || temp.pos.y < -cueball.size
				     )  ){
		int c = count;
		while(c<numberofballs-1){
		    balls[c] = balls[c+1];
		    c++;
		}
		temp = balls[count];
		numberofballs = numberofballs-1;
	    }
	    if( (width <= (temp.pos.x+temp.size) && temp.vel.x > 0) && !( temp.getcy() <= pocketsize*cueball.size ) && !(height - temp.getcy() <= pocketsize*cueball.size) ){
		temp.vel.x = -temp.vel.x;
	    }
	    if(temp.pos.x <= 0 && temp.vel.x < 0 && !( temp.getcy() <= pocketsize*cueball.size ) && !(height - temp.getcy() <= pocketsize*cueball.size) ){
		temp.vel.x = -temp.vel.x;
	    }
	    if(temp.pos.y <= 0 && temp.vel.y < 0 &&  !( temp.getcx() <= pocketsize*cueball.size ) && !(width - temp.getcx() <= pocketsize*cueball.size)){
		temp.vel.y = -temp.vel.y;
	    }
	    if(height <= (temp.pos.y + temp.size) && temp.vel.y > 0 && !( temp.getcx() <= pocketsize*cueball.size ) && !(width - temp.getcx() <= pocketsize*cueball.size) ){
		temp.vel.y = -temp.vel.y;
	    }
	  
	
	    temp.pos.x += temp.vel.x*tillnframe; 
            temp.pos.y += temp.vel.y*tillnframe; 


	    if (Math.abs(temp.vel.x) < .10 && Math.abs(temp.vel.y) < .10){
		temp.vel.x = 0;
		temp.vel.y = 0;
	    }
	    temp.vel.x = .99*temp.vel.x;  // Simulates friction by decreasing velocity slightly each frame.
	    temp.vel.y = .99*temp.vel.y;
	    count++;
	}
	this.repaint();
    }
}

class Aimer implements MouseMotionListener, MouseListener {
    boolean dragging;
    Ball cb; //cueball
    int offset;
    int size;
    int length;
    Point.Double aim;
    Point.Double click;
    Aimer(int s, int l, Ball ball) {
	size = s;
	length = l;
	cb = ball;
	aim = new Point2D.Double(1,0);
	click = new Point2D.Double(0,1);
	dragging = false;
    }
    
    public void mousePressed(MouseEvent evt) {
	click.setLocation(evt.getX(), evt.getY());
	if(click.distance(cb.getcx()+length*aim.x, cb.getcy() + length*aim.y) < size) {
	    dragging = true;
	}
	PoolPanel a = (PoolPanel)evt.getSource();
	a.tval = 2;
    }

    public void mouseEntered(MouseEvent evt) { }
    public void mouseExited(MouseEvent evt) { }
    public void mouseClicked(MouseEvent evt) { }
    public void mouseReleased(MouseEvent evt) {
	dragging = false;
    }
    public void mouseMoved(MouseEvent evt) {

	click.setLocation(evt.getX(), evt.getY());

	  //  1+1;
	//}
    }
    public void mouseDragged(MouseEvent evt){
	PoolPanel a = (PoolPanel)evt.getSource();
	if (dragging){
	    click.setLocation(evt.getX(), evt.getY());
	    double distance = click.distance(cb.getcx(), cb.getcy());
	    aim.x = (click.x - cb.getcx())/distance;
	    aim.y = (click.y - cb.getcy())/distance;
	    a.tval =  distance;
	}
    }
}