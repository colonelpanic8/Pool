package pool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import javax.swing.JPanel;
import javax.swing.Timer;



public final class PoolPanel extends JPanel implements ActionListener, Comparator, HierarchyBoundsListener {
    ArrayList<Ball> balls;
    ArrayList<Pocket> pockets;
    ArrayList<PoolPolygon> polygons;
    Ball cueball;
    Ball ghostBallObjectBall;
    Point ghostBallPosition;
    int pocketSize, railSize, ballSize, borderSize, railIndent;
    PriorityQueue<Collision> collisions;
    Aimer aimer;
    boolean selMode;
    SelectionModeListener modeListener;
    int height, width;


    //Should be removed or renamed
    double tval;
    Point aPoint;
    
    public PoolPanel(){        
	setBackground(new Color(48,130,100));
	setPreferredSize(new Dimension(800,600));
	addHierarchyBoundsListener(this);
        height = getHeight();
        width = getWidth();
        selMode = false;
	ballSize = 40;
        pockets = new ArrayList(6);
        pocketSize = (int)(2*ballSize);
        railSize = pocketSize/2-10;
        railIndent = railSize;
        borderSize = pocketSize/2 + 10;
        polygons = new ArrayList(10);
	initPockets();
        initPolygons();
	ghostBallPosition = new Point(0,0);
	aPoint = new Point((int)(1.5*ballSize + railSize), railSize);
	balls = new ArrayList(16);	
	cueball = new Ball(Color.WHITE, 850, 200, 2, 3, ballSize);
	collisions = new PriorityQueue(16, this);
	balls.add(cueball);
	aimer = new Aimer(25, 100, cueball);
	modeListener = new SelectionModeListener();
	this.addMouseListener(aimer);
	this.addMouseMotionListener(aimer);
	this.addMouseListener(modeListener);
	this.addMouseMotionListener(modeListener);
	Timer timer = new Timer(15, this);
	timer.start();
	tval = 0;
    }
    
    public void initPolygons() {
	Color color = new Color(48,130,100).darker();
        int[] xpoints, ypoints;
	xpoints = new int[4];
	ypoints = new int[4];

	xpoints[0] = borderSize + pocketSize/2;
	xpoints[1] = borderSize + pocketSize/2 + railIndent;
	xpoints[2] = width - pocketSize/2;
	xpoints[3] = width - pocketSize/2;
	ypoints[0] = borderSize;
	ypoints[1] = borderSize + railSize;
	ypoints[2] = borderSize + railSize;
	ypoints[3] = borderSize;
        polygons.add(new PoolPolygon(xpoints, ypoints, 4, color));

	xpoints[0] = width/2 + pocketSize/2;
	xpoints[1] = width/2 + pocketSize/2;
	xpoints[2] = width - pocketSize/2 - railIndent - borderSize;
        xpoints[3] = width - pocketSize/2 - borderSize;
        ypoints[0] = borderSize;
	ypoints[1] = borderSize + railSize;
	ypoints[2] = borderSize + railSize;
	ypoints[3] = borderSize;
        polygons.add(new PoolPolygon(xpoints, ypoints, 4, color));
        
	xpoints[0] = width - borderSize - pocketSize/2;
        xpoints[1] = width - borderSize - railSize;
        xpoints[2] = width - borderSize- railSize;
        xpoints[3] = width - borderSize;
	ypoints[0] = borderSize + pocketSize/2;
	ypoints[1] = borderSize + pocketSize/2 + railIndent;
	ypoints[2] = height - borderSize - railIndent;
	ypoints[3] = height - borderSize - pocketSize/2;
        polygons.add(new PoolPolygon(xpoints, ypoints, 4, color));

	xpoints[0] = width/2 + pocketSize/2;
	xpoints[1] = width/2 + pocketSize/2;
	xpoints[2] = width - pocketSize/2 - railIndent - borderSize;
        xpoints[3] = width - pocketSize/2 - borderSize;
	ypoints[0] = height - borderSize;
	ypoints[1] = height - (borderSize + railSize);
	ypoints[2] = height - (borderSize + railSize);
	ypoints[3] = height - borderSize;
        polygons.add(new PoolPolygon(xpoints, ypoints, 4, color));

	xpoints[0] = borderSize + pocketSize/2;
	xpoints[1] = borderSize + pocketSize/2 + railIndent;
	xpoints[2] = width/2 - pocketSize/2;
	xpoints[3] = width/2 - pocketSize/2;
	ypoints[0] = height - borderSize;
	ypoints[1] = height - (borderSize + railSize);
	ypoints[2] = height - (borderSize + railSize);
	ypoints[3] = height - borderSize;
        polygons.add(new PoolPolygon(xpoints, ypoints, 4, color));
        
	xpoints[0] = borderSize;
        xpoints[1] = borderSize+railSize;
        xpoints[2] = borderSize+railSize;
        xpoints[3] = borderSize;
        ypoints[0] = borderSize + pocketSize/2;
	ypoints[1] = borderSize + pocketSize/2 + railIndent;
	ypoints[2] = height - borderSize - railIndent;
	ypoints[3] = height - borderSize;
        polygons.add(new PoolPolygon(xpoints, ypoints, 4, color));
    }

    public void initPockets() {
	pockets.add(new Pocket(borderSize , borderSize , pocketSize));
	pockets.add(new Pocket(width/2, borderSize , pocketSize));
	pockets.add(new Pocket(width - borderSize , borderSize , pocketSize));
	pockets.add(new Pocket(borderSize , height - borderSize , pocketSize));
	pockets.add(new Pocket(width/2, height - borderSize , pocketSize));
	pockets.add(new Pocket(width - borderSize, height - borderSize , pocketSize));
    }
    
    public void ancestorMoved(HierarchyEvent he) {
         
    }

    public void ancestorResized(HierarchyEvent he) {
        height = getHeight();
        width = getWidth();
        if(pockets != null) {
	    pockets.get(0).pos.setLocation(borderSize , borderSize );
	    pockets.get(1).pos.setLocation(width/2, borderSize );
	    pockets.get(2).pos.setLocation(width - borderSize , borderSize );
	    pockets.get(3).pos.setLocation(borderSize , height - borderSize );
	    pockets.get(4).pos.setLocation(width/2, height - borderSize );
	    pockets.get(5).pos.setLocation(width - borderSize, height - borderSize);
        }
        if(polygons != null) {
            polygons.get(0).xpoints[2] = width/2 - pocketSize/2;
            polygons.get(0).xpoints[3] = width/2 - pocketSize/2;

	    polygons.get(1).xpoints[0] = width/2 + pocketSize/2;
	    polygons.get(1).xpoints[1] = width/2 + pocketSize/2;
	    polygons.get(1).xpoints[2] = width - pocketSize/2 - railIndent - borderSize;
	    polygons.get(1).xpoints[3] = width - pocketSize/2 - borderSize;

	    polygons.get(2).xpoints[0] = width - borderSize;
	    polygons.get(2).xpoints[1] = width - borderSize-railSize;
	    polygons.get(2).xpoints[2] = width - borderSize-railSize;
	    polygons.get(2).xpoints[3] = width - borderSize;
	    polygons.get(2).ypoints[0] = borderSize + pocketSize/2;
	    polygons.get(2).ypoints[1] = borderSize + pocketSize/2 + railIndent;
	    polygons.get(2).ypoints[2] = height - borderSize - railIndent - pocketSize/2;
	    polygons.get(2).ypoints[3] = height - borderSize - pocketSize/2;

	    polygons.get(3).xpoints[0] = width/2 + pocketSize/2;
	    polygons.get(3).xpoints[1] = width/2 + pocketSize/2;
	    polygons.get(3).xpoints[2] = width - pocketSize/2 - railIndent - borderSize;
	    polygons.get(3).xpoints[3] = width - pocketSize/2 - borderSize;
	    polygons.get(3).ypoints[0] = height - borderSize;
	    polygons.get(3).ypoints[1] = height - (borderSize + railSize);
	    polygons.get(3).ypoints[2] = height - (borderSize + railSize);
	    polygons.get(3).ypoints[3] = height - borderSize;
	    
	    
	    polygons.get(4).xpoints[0] = borderSize + pocketSize/2;
	    polygons.get(4).xpoints[1] = borderSize + pocketSize/2 + railIndent;
	    polygons.get(4).xpoints[2] = width/2 - pocketSize/2;
	    polygons.get(4).xpoints[3] = width/2 - pocketSize/2;
	    polygons.get(4).ypoints[0] = height - borderSize;
	    polygons.get(4).ypoints[1] = height - (borderSize + railSize);
	    polygons.get(4).ypoints[2] = height - (borderSize + railSize);
	    polygons.get(4).ypoints[3] = height - borderSize;
           
            polygons.get(5).ypoints[2] = height - borderSize - railIndent - pocketSize/2;
            polygons.get(5).ypoints[3] = height - borderSize - pocketSize/2;
        }
    }
    
    @Override public void paintComponent(Graphics g){
	super.paintComponent(g);
        //BORDER
	g.setColor(Color.DARK_GRAY);
	g.fillRect(0,0,width,borderSize);
	g.fillRect(0,0,borderSize,width);
	g.fillRect(0,height-borderSize,width,borderSize);
	g.fillRect(width-borderSize,0,borderSize,width);
        
	g.setColor(Color.BLACK);

	//POCKETS
	Iterator<Pocket> pocketItr;
	pocketItr = pockets.iterator();
	while(pocketItr.hasNext()) {
	    Pocket pocket = pocketItr.next();
	    pocket.draw(g);
	}
        
	
	//BALLS
	Iterator<Ball> iter = balls.iterator();
	while(iter.hasNext()){
	    Ball temp = iter.next();
	    temp.draw(g);
	}
        
        //POLYGONS
        Iterator<PoolPolygon> iterator = polygons.iterator();
        while(iterator.hasNext()){
	    PoolPolygon p = iterator.next();
	    p.draw(g);
	}

	//GHOST BALL AND AIMER
	g.setColor(Color.BLACK);
	if(Math.abs(cueball.vel.x) + Math.abs(cueball.vel.y) < 1) {
	    g.drawLine((int)cueball.getcx(), 
		       (int)cueball.getcy(), 
		       (int)(cueball.getcx()+(-aimer.aim.x*2000)), 
		       (int)(cueball.getcy()+(-aimer.aim.y*2000)));
	    //Additional aiming lines that were removed.
	    
	    g.drawLine( (int)(cueball.getcx() - -aimer.aim.y*cueball.size/2),
			(int)(cueball.getcy() + -aimer.aim.x*cueball.size/2), 
			(int)(cueball.getcx() + -aimer.aim.x*2000 - -aimer.aim.y*cueball.size/2), 
			(int)(cueball.getcy() + -aimer.aim.y*2000 + -aimer.aim.x*cueball.size/2));
	    g.drawLine( (int)(cueball.getcx() + -aimer.aim.y*cueball.size/2),
			(int)(cueball.getcy() - -aimer.aim.x*cueball.size/2), 
			(int)(cueball.getcx() + -aimer.aim.x*2000 + -aimer.aim.y*cueball.size/2),
			(int)(cueball.getcy() + -aimer.aim.y*2000 - -aimer.aim.x*cueball.size/2));
	    
            if (ghostBallObjectBall != null){
                int gcx = ghostBallPosition.x + ballSize/2;
                int gcy = ghostBallPosition.y + ballSize/2;
                g.fillOval(ghostBallPosition.x, ghostBallPosition.y, ballSize, ballSize);
		g.drawLine(gcx, gcy,
			   gcx + 15*(int)(ghostBallObjectBall.getcx() - gcx), 
			   gcy + 15*(int)(ghostBallObjectBall.getcy() - gcy)); 
	    }

	    g.fillOval((int)(aimer.tracker.x - aimer.size/2),
		       (int)(aimer.tracker.y - aimer.size/2), aimer.size, aimer.size);
	}
	
	//SPEED INDICATOR
	g.drawString(Double.toString(tval), 100, 100);
        
        g.drawString(Integer.toString(modeListener.click.x), 100, 140);
        g.drawString(Integer.toString(modeListener.click.y), 160, 140);
        
    }
    
    public void actionPerformed(ActionEvent evt){
	Iterator<Ball> iter;
	iter = balls.iterator();
	while(iter.hasNext()) {
	    Ball ball = iter.next();
            detectPolygonCollisions(ball, 0);
	    detectWallCollisions(ball, 0);
	    checkPockets(ball, 0);
	    for(int i = balls.lastIndexOf(ball)+1; i < balls.size(); i++) {
		double t = ball.detectCollisionWith(balls.get(i));
		if(t < 1 && 0 <= t){
		    collisions.add(new BallCollision(t, ball, balls.get(i)));
		}
	    }
	    if (ball.remove) {
		if(ball == cueball) {
		    cueball.alpha = 255;
		    cueball.remove = false;
		    cueball.pos.x = width/2;
		    cueball.pos.y = height/2;
		    cueball.sunk = false;
		} else {
		    iter.remove();
		}
	    }
	}
	updateBallPositions();
	updateGhostBall();
	aimer.doShoot();
	tval = Math.sqrt(cueball.vel.x*cueball.vel.x + cueball.vel.y*cueball.vel.y);
        Point2D.Double temp = new Point2D.Double(0,0);
        
	this.repaint();
    }
    
    public void detectWallCollisions(Ball aBall, double timePassed) {
	double left, right, top, bottom, time;
        Point2D.Double newVel = new Point2D.Double(aBall.vel.x, aBall.vel.y);
	left = (railSize - aBall.pos.x)/aBall.vel.x;
	right = (width - railSize - aBall.pos.x-aBall.size)/aBall.vel.x;
	top = (railSize - aBall.pos.y)/aBall.vel.y;
	bottom = (height - railSize - aBall.pos.y-aBall.size)/aBall.vel.y;
	left += timePassed;
	right += timePassed;
	top += timePassed;
	bottom += timePassed;
	
	if(left > timePassed  && left < 1) {
	    if(left*aBall.vel.y + aBall.getcy() > 1.5*ballSize + railSize &&
	       left*aBall.vel.y + aBall.getcy() < height - (1.5*ballSize + railSize)) {
                newVel.x = -newVel.x;
		collisions.add(new WallCollision(left, aBall, newVel));
		return;
	    } 
	}
	if(right > timePassed && right < 1) {
	    if(right*aBall.vel.y + aBall.getcy() > 1.5*ballSize + railSize &&
	       right*aBall.vel.y + aBall.getcy() < height - (1.5*ballSize + railSize)) {
		newVel.x = -newVel.x;
		collisions.add(new WallCollision(right, aBall, newVel));
		return;
	    }
	}
	if(top > timePassed && top < 1) {
	    if(top*aBall.vel.x + aBall.getcx() > 1.5*ballSize + railSize &&
	       top*aBall.vel.x + aBall.getcx() < width - (1.5*ballSize + railSize)) {
		newVel.y = -newVel.y;
		collisions.add(new WallCollision(top, aBall, newVel));
		return;
	    }
	}
	if(bottom > timePassed && bottom < 1) {
	    if(bottom*aBall.vel.x + aBall.getcx() > 1.5*ballSize + railSize &&
	       bottom*aBall.vel.x + aBall.getcx() < width - (1.5*ballSize + railSize)) {
		newVel.y = -newVel.y;
		collisions.add(new WallCollision(bottom, aBall, newVel));
		return;
	    }
	}
	//Top Left
	Point p = new Point((int)(1.5*ballSize + railSize), railSize);
	time = aBall.detectCollisionWith(p);
	if(time < 1 && time > 0) {
	    collisions.add(new PointCollision(time, p, aBall));
	}
	p = new Point(railSize, (int)(1.5*ballSize + railSize));
	time = aBall.detectCollisionWith(p);
	if(time < 1 && time > 0) {
	    collisions.add(new PointCollision(time, p, aBall));
	}
	
	//Top Right
	p = new Point((int)(width - (1.5*ballSize + railSize)), railSize);
	time = aBall.detectCollisionWith(p);
	if(time < 1 && time > 0) {
	    collisions.add(new PointCollision(time, p, aBall));
	}
	p = new Point((int)(width - railSize), (int)(1.5*ballSize + railSize));
	time = aBall.detectCollisionWith(p);
	if(time < 1 && time > 0) {
	    collisions.add(new PointCollision(time, p, aBall));
	}
	
	//Bottom Right
	p = new Point((int)(width - (1.5*ballSize + railSize)), height - railSize);
	time = aBall.detectCollisionWith(p);
	if(time < 1 && time > 0) {
	    collisions.add(new PointCollision(time, p, aBall));
	}
	p = new Point((int)(width - railSize), (int)(height - (1.5*ballSize + railSize)));
	time = aBall.detectCollisionWith(p);
	if(time < 1 && time > 0) {
	    collisions.add(new PointCollision(time, p, aBall));
	}
	
	//Bottom Left
	p = new Point((int)(1.5*ballSize + railSize), height - railSize);
	time = aBall.detectCollisionWith(p);
	if(time < 1 && time > 0) {
	    collisions.add(new PointCollision(time, p, aBall));
	}
	p = new Point(railSize, (int)(height - (1.5*ballSize + railSize)));
	time = aBall.detectCollisionWith(p);
	if(time < 1 && time > 0) {
	    collisions.add(new PointCollision(time, p, aBall));
	}
    }
    
    public void updateBallPositions() {
	Iterator<Ball> ballIterator;
	Collision collision = collisions.poll();
	double timePassed = 0;
        while(collision != null) {
	    //Advance balls to the point where the collision occurs
            ballIterator = balls.iterator();
	    while(ballIterator.hasNext()) {
		Ball ball = ballIterator.next();
		ball.pos.setLocation(ball.pos.x + (collision.time-timePassed) * ball.vel.x,
				     ball.pos.y + (collision.time-timePassed) * ball.vel.y);
		timePassed = collision.time;
	    }
	    
	    collision.doCollision(this);
	    collision = collisions.poll();	    
	}
        ballIterator = balls.iterator();
	while(ballIterator.hasNext()) {
	    Ball ball = ballIterator.next();
	    ball.pos.setLocation(ball.pos.x + (1-timePassed)*ball.vel.x,
				 ball.pos.y + (1-timePassed)*ball.vel.y);
	    if (Math.abs(ball.vel.x) < .10 && Math.abs(ball.vel.y) < .10){
		ball.vel.x = 0;
		ball.vel.y = 0;
	    }
	    ball.vel.x = .99*ball.vel.x;
	    ball.vel.y = .99*ball.vel.y;
	}
    }

    public void updateGhostBall() {
	Iterator<Ball> iter;
        double min = 5000;
	iter = balls.iterator();
	ghostBallObjectBall = null;
	while(iter.hasNext()) {
	    Ball ball = iter.next();
	    if(!(ball==cueball)){
		double a = aimer.aim.x*aimer.aim.x + aimer.aim.y*aimer.aim.y;
		double b = 2 * (cueball.getcx()*-aimer.aim.x - ball.getcx()*-aimer.aim.x + 
				cueball.getcy()*-aimer.aim.y - ball.getcy()*-aimer.aim.y);
		double c = cueball.getcx()*cueball.getcx() + ball.getcx()*ball.getcx() +
		    cueball.getcy()*cueball.getcy() + ball.getcy()*ball.getcy() - 2*cueball.getcx()*ball.getcx() - 
		    2*cueball.getcy()*ball.getcy() - (ball.size + cueball.size)*(ball.size + cueball.size)/4;
		double t;
		if (a !=0 ){
		    double t1 = ( -b - Math.sqrt(b*b-4*a*c) )/(2 * a);
		    double t2 = ( -b + Math.sqrt(b*b-4*a*c) )/(2 * a);
		    t = t1 < t2 ? t1 : t2;
		} else {
		    t = -c/b;  
		}

		if( !(Double.isNaN(t)) && t < min && t > 0){
		    ghostBallPosition.setLocation((int)(cueball.pos.x + t * -aimer.aim.x),
						  (int)(cueball.pos.y + t * -aimer.aim.y));
		    ghostBallObjectBall = ball;
		    min = t;
		}
	    }
	}
    }
    
    public void checkPockets(Ball ball, double timePassed) {
	double time;
	Iterator<Pocket> pocketItr;
	pocketItr = pockets.iterator();
        while(pocketItr.hasNext()) {
            Pocket pocket = pocketItr.next();
            time = pocket.detectCollisionWith(ball);
            time += timePassed;
	    if(time > timePassed && time < 1) {
		collisions.add(new PocketCollision(ball, time));
		return;
	    }
	}
    }

    //COMPARATOR INTERFACE
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

    @Override public boolean equals(Object obj) {
	return true;
    }

    public void detectPolygonCollisions(Ball ball, double t) {
        Iterator<PoolPolygon> iter = polygons.iterator();
        while(iter.hasNext()) {
            PoolPolygon p = iter.next();
            p.detectCollisions(ball, collisions, t);
        }
        
        
    }
}
class SelectionModeListener implements MouseMotionListener, MouseListener {
    Ball ball;
    Point click;

    public SelectionModeListener() {
        ball = null;
	click = new Point(0,0);
    }

    public void mousePressed(MouseEvent evt) {
	PoolPanel pp = (PoolPanel)evt.getSource();
	click.setLocation(evt.getX(), evt.getY());
	if(!pp.selMode) {
	    return;
	}
        Iterator<Ball> iter;
        iter = pp.balls.iterator();
	while(iter.hasNext()) {
            Ball aBall = iter.next();
	    if(click.distance(aBall.getcx(), aBall.getcy()) < aBall.size) {
		ball = aBall;
		aBall.vel.x = 0;
                aBall.vel.y = 0;
	    }
	}	
    }

    public void mouseReleased(MouseEvent evt) {
	ball = null;
    }

    public void mouseDragged(MouseEvent evt){
	if(ball != null) {
	    ball.pos.x = evt.getX() - ball.size/2;
	    ball.pos.y = evt.getY() - ball.size/2;
	}
    }

    public void mouseEntered(MouseEvent evt) { }
    public void mouseExited(MouseEvent evt) { }
    public void mouseClicked(MouseEvent evt) { }
    public void mouseMoved(MouseEvent evt) { 
        click.setLocation(evt.getX(), evt.getY());
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
    public void mouseEntered(MouseEvent evt) { }
    public void mouseExited(MouseEvent evt) { }
    public void mouseClicked(MouseEvent evt) { }
}