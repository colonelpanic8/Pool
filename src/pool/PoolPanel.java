package pool;

import com.sun.j3d.utils.universe.SimpleUniverse;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import javax.media.j3d.*;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;



public final class PoolPanel extends JPanel implements ActionListener, Comparator, HierarchyBoundsListener {
    
    double pocketSize, railSize, ballSize, borderSize, railIndent, sidePocketSize, sideIndent;
    boolean selMode, sliderPower;
    Ball cueball, shootingBall, ghostBallObjectBall;
    ArrayList<Ball> balls;
    ArrayList<Pocket> pockets;
    ArrayList<PoolPolygon> polygons;
    PriorityQueue<Collision> collisions;
    Point2D.Double aim, ghostBallPosition;
    Color tableColor;
    double height, width;
    double spin, power;
    int collisionsExecuted;
    boolean frameSkip, err;
    
    //Java3D
    Canvas3D canvas;
    SimpleUniverse universe;
    BranchGroup group;
    TransformGroup transformGroup;
    LineArray aimLine;
    
    int ticks = 0;
    
    //INITIALIZATION
    public PoolPanel(int bs, int rs, double h, double w) {
        //Initialize size values
        height = h;
        width = w;
        ballSize = bs;
        railSize = rs;
        pocketSize = (2.2*ballSize);
        sidePocketSize = (1.8*ballSize);
        borderSize = pocketSize/2 + 10;
        railIndent = railSize;
        sideIndent = railIndent/4;
        
        //Initialize boolean values.
        selMode = false;
        sliderPower = false;        
        frameSkip = false;
        err = false;
        
        //Set component color.
        tableColor = new Color(48,130,100);
	setBackground(tableColor);
        
        //Initialize other primitives.
        power = .1;
        spin = 0;
        collisionsExecuted = 0;
        
        //Initialize table item arrays.
        balls = new ArrayList(16);
        pockets = new ArrayList(6);
        polygons = new ArrayList(10);
        
        init3D();
        
        //Initialize table items.
        initPockets();
        initPolygons();
        cueball = addBall(Color.WHITE, 0, 0, 0, 0, ballSize);
        shootingBall = cueball;
        
        //Initialize mouse listeners.
        
        //Add listeners.
	addHierarchyBoundsListener(this);
        
        //Miscellaneous
	ghostBallPosition = new Point2D.Double(0,0);
        aim = new Point2D.Double(0,0);
	collisions = new PriorityQueue(16, this);
       
        //Start timer
	Timer timer = new Timer(15, this);
	timer.start();
        
        
    }
    
    public void init3D() {
        //Initialize Java 3D components.
        canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        add("Center", canvas);
        universe = new SimpleUniverse(canvas);
        
        //Light
        BoundingBox bounds = new BoundingBox();
        bounds.setLower(-width/2, -height/2, -3);
        bounds.setUpper(width/2, height/2, 3);
        group = new BranchGroup();
        Color3f lightColor = new Color3f(1.0f, 0.0f, 0.2f);
        Vector3f lightDirection = new Vector3f(4.0f, -7.0f, -12.0f);        
        DirectionalLight light = new DirectionalLight(lightColor, lightDirection);
        light.setInfluencingBounds(bounds);
        group.addChild(light);
        
        Color3f ambientColor = new Color3f(1.0f, 1.0f, 1.0f);        
        AmbientLight ambientLight = new AmbientLight(ambientColor);
        ambientLight.setInfluencingBounds(bounds);
        group.addChild(ambientLight);
        
        universe.addBranchGraph(group);        
        universe.getViewingPlatform().setNominalViewingTransform();
        
        TransformGroup VpTG = universe.getViewingPlatform().getViewPlatformTransform();
        Transform3D Trfcamera = new Transform3D();
        Trfcamera.setTranslation(new Vector3f(3.0f, 3.0f, 50.0f));  
        VpTG.setTransform(Trfcamera);
        
        aimLine = new LineArray(2, GeometryArray.COORDINATES);

        
    }
    
    public void initPolygons() {
	Color color = tableColor.darker();
        int[] xpoints, ypoints;
	xpoints = new int[4];
	ypoints = new int[4];
        /*
	xpoints[0] = (borderSize + pocketSize/2);
	xpoints[1] = borderSize + pocketSize/2 + railIndent;
	xpoints[2] = width - pocketSize/2 + sideIndent;
	xpoints[3] = width - pocketSize/2;
	ypoints[0] = borderSize;
	ypoints[1] = borderSize + railSize;
	ypoints[2] = borderSize + railSize;
	ypoints[3] = borderSize;
        polygons.add(new PoolPolygon(xpoints, ypoints, 4, color));

	xpoints[0] = width/2 + pocketSize/2;
	xpoints[1] = width/2 + pocketSize/2 + sideIndent;
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
	xpoints[1] = width/2 + pocketSize/2 +sideIndent;
	xpoints[2] = width - pocketSize/2 - railIndent - borderSize;
        xpoints[3] = width - pocketSize/2 - borderSize;
	ypoints[0] = height - borderSize;
	ypoints[1] = height - (borderSize + railSize);
	ypoints[2] = height - (borderSize + railSize);
	ypoints[3] = height - borderSize;
        polygons.add(new PoolPolygon(xpoints, ypoints, 4, color));

	xpoints[0] = borderSize + pocketSize/2;
	xpoints[1] = borderSize + pocketSize/2 + railIndent;
	xpoints[2] = width/2 - pocketSize/2 + sideIndent;
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
        polygons.add(new PoolPolygon(xpoints, ypoints, 4, color));*/
    }

    public void initPockets() {
	pockets.add(new Pocket(borderSize , borderSize , pocketSize));
	pockets.add(new Pocket(width/2, borderSize , sidePocketSize));
	pockets.add(new Pocket(width - borderSize , borderSize , pocketSize));
	pockets.add(new Pocket(borderSize , height - borderSize , pocketSize));
	pockets.add(new Pocket(width/2, height - borderSize , sidePocketSize));
	pockets.add(new Pocket(width - borderSize, height - borderSize , pocketSize));
    }
    
    //SIMULATION
    public void actionPerformed(ActionEvent evt){
        
        
        aimLine.setCoordinate(0, shootingBall.pos);
        
	Iterator<Ball> iter;
	iter = balls.iterator();
        validate();
        if(err) {
            err = false;
            frameSkip = true;
            rewind();
            return;   
        }
        if(frameSkip) {
            frameSkip = true;
        }
	while(iter.hasNext()) {
	    Ball ball = iter.next();
            detectPolygonCollisions(ball, 0);
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
            ball.draw3D();
	}
        updateBallPositions();
	updateGhostBall();
    }
    
    public void updateBallPositions(){
	Iterator<Ball> ballIterator;
	Collision collision = collisions.poll();
	double timePassed = 0;
        while(collision != null) {
	    //Advance balls to the point where the collision occurs.
            ballIterator = balls.iterator();
	    while(ballIterator.hasNext()) {
		Ball ball = ballIterator.next();
		ball.pos.setLocation(ball.pos.x + (collision.time-timePassed) * ball.vel.x,
                        ball.pos.y + (collision.time-timePassed) * ball.vel.y,
                        0);
                
            }
            timePassed = collision.time;
	    
            collision.doCollision(this);
            collisionsExecuted++;
	    collision = collisions.poll();	    
	}
        ballIterator = balls.iterator();
	while(ballIterator.hasNext()) {
	    Ball ball = ballIterator.next();
	    ball.pos.setLocation(ball.pos.x + (1-timePassed)*ball.vel.x,
				 ball.pos.y + (1-timePassed)*ball.vel.y,
                                 0);
            ball.vel.x += ball.acc.x;
            ball.vel.y += ball.acc.y;
            if(ball.vel.distance(0,0) < .02) {
                ball.vel.x = 0;
                ball.vel.y = 0;
            } else {
                ball.vel.x = ball.vel.x*.99;
                ball.vel.y = ball.vel.y*.99;
            }
            if(ball.acc.distance(0,0) < .01) {
                ball.acc.x = 0;
                ball.acc.y = 0;
            } else {
                ball.acc.x = .98*ball.acc.x;
                ball.acc.y = .98*ball.acc.y;
            }
            
	}
        ballIterator = balls.iterator();
        while(ballIterator.hasNext()) {
            Ball ball = ballIterator.next();
            checkOverlaps(ball);
            ball.draw3D();
        }
    }
    
    public void updateGhostBall() {
	Iterator<Ball> iter;
        double min = Double.POSITIVE_INFINITY;
	iter = balls.iterator();
	ghostBallObjectBall = null;
	while(iter.hasNext()) {
	    Ball ball = iter.next();
	    if(!(ball==cueball)){
		double a = aim.x*aim.x + aim.y*aim.y;
		double b = 2 * (cueball.getcx()*-aim.x - ball.getcx()*-aim.x + 
				cueball.getcy()*-aim.y - ball.getcy()*-aim.y);
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
		    ghostBallPosition.setLocation((int)(cueball.pos.x + t * -aim.x),
						  (int)(cueball.pos.y + t * -aim.y));
		    ghostBallObjectBall = ball;
		    min = t;
		}
	    }
	}
    }
    
    public void detectPolygonCollisions(Ball ball, double t) {
        Iterator<PoolPolygon> iter = polygons.iterator();
        while(iter.hasNext()) {
            PoolPolygon p = iter.next();
            p.detectCollisions(ball, collisions, t);
        }
    }
    
    public void detectPolygonCollisions(Ball ball, double t, WallCollision collision) {
        Iterator<PoolPolygon> iter = polygons.iterator();
        while(iter.hasNext()) {
            PoolPolygon p = iter.next();
            if(p == collision.poly) {
                p.detectCollisionsWithoutWall(ball, collisions, t, collision.wall);
            } else {
                p.detectCollisions(ball, collisions, t);
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
    
    //DRAWING 
    @Override public void paintComponent(Graphics g){
	super.paintComponent(g);        
        /*
        //BORDER
	g.setColor(Color.DARK_GRAY);
	g.fillRect(0,0,width,borderSize);
	g.fillRect(0,0,borderSize,width);
	g.fillRect(0,height-borderSize,width,borderSize);
	g.fillRect(width-borderSize,0,borderSize,width);
        
	g.setColor(Color.BLACK);
        
        //POLYGONS
        Iterator<PoolPolygon> iterator = polygons.iterator();
        while(iterator.hasNext()){
	    PoolPolygon p = iterator.next();
	    p.draw(g);
	}

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
        
	//GHOST BALL AND AIMER
	g.setColor(Color.BLACK);
        drawGhostBall(g);
		
        /*
         * Draw information on screen.
         * g.drawString(Integer.toString(modeListener.click.x), 100, 140);
         * g.drawString(Integer.toString(modeListener.click.y), 160, 140);
         * g.drawString(Integer.toString(collisionsExecuted), 160, 160);
         * g.drawString(Double.toString(power), 200, 140);
         * g.drawString(Double.toString(spin), 200, 160);
        */        
    }
    
    void drawGhostBall(Graphics g) {
        
        if(Math.abs(cueball.vel.x) + Math.abs(cueball.vel.y) < 1) {
            /*g.drawLine((int)cueball.getcx(), 
		       (int)cueball.getcy(), 
		       (int)(cueball.getcx()+(-aim.x*2000)), 
		       (int)(cueball.getcy()+(-aim.y*2000)));*/
	    //Additional aiming lines that were removed.
	    
            /*
	    g.drawLine( (int)(cueball.getcx() - -aim.y*cueball.size/2),
			(int)(cueball.getcy() + -aim.x*cueball.size/2), 
			(int)(cueball.getcx() + -aim.x*2000 - -aim.y*cueball.size/2), 
			(int)(cueball.getcy() + -aim.y*2000 + -aim.x*cueball.size/2));
	    g.drawLine( (int)(cueball.getcx() + -aim.y*cueball.size/2),
			(int)(cueball.getcy() - -aim.x*cueball.size/2), 
			(int)(cueball.getcx() + -aim.x*2000 + -aim.y*cueball.size/2),
			(int)(cueball.getcy() + -aim.y*2000 - -aim.x*cueball.size/2));
                        * 
                        */
            /*
            Color color = Color.WHITE;
            g.setColor(color);
            if (ghostBallObjectBall != null){
                double gcx = ghostBallPosition.x + ballSize/2;
                double gcy = ghostBallPosition.y + ballSize/2;
                g.fillOval(ghostBallPosition.x, ghostBallPosition.y, ballSize, ballSize);
                Point2D.Double last = new Point2D.Double(gcx, gcy);                                              
                Point2D.Double unit = new Point2D.Double(ghostBallObjectBall.getcx() - gcx,
                        ghostBallObjectBall.getcy() - gcy);
                drawPoolPath(unit, last, g);
                double temp = unit.y;
                unit.y = unit.x;
                unit.x = -temp;                      
                g.setColor(Color.MAGENTA);
                g.drawString(Double.toString(unit.x), 300, 140);
                g.drawString(Double.toString(unit.y), 300, 180);
                last.setLocation(cueball.getcx()-ghostBallObjectBall.getcx(), cueball.getcy()-ghostBallObjectBall.getcy());
                Point2D.Double trans = new Point2D.Double(1/(last.x + last.y*last.y/last.x),
                        1/(last.y + last.x*last.x/last.y));
                temp = trans.y*gcx - trans.x*gcy;
                double unitY = trans.y*unit.x - trans.x*unit.y;
                double cueballY = trans.y*cueball.getcx() - trans.x*cueball.getcy();
                if((unitY < 0) != (temp-cueballY < 0)){
                    unit.x = -unit.x;
                    unit.y = -unit.y;
                }
                last.setLocation(gcx,gcy);
                drawPoolPath(unit, last, g);
            }
            g.setColor(Color.BLACK);
	    g.fillOval((int)(tracker.x - size/2),
		       (int)(tracker.y - size/2), size, size);*/
	}
    }
    
    public void drawPoolPath(Point2D.Double unit, Point2D.Double last, Graphics g) {
        Point2D.Double next = new Point2D.Double();
        double horizontal = 0, vertical = 0;
        for(int i = 0; i < 6; i++) {
            horizontal = Double.POSITIVE_INFINITY;
            vertical = Double.POSITIVE_INFINITY;
            
            if(unit.x != 0) {
                horizontal = (borderSize + railSize - last.x)/unit.x;
                if(horizontal <= .00001) {
                    horizontal = (width - (borderSize + railSize) - last.x)/unit.x;
                }
            }
            if(unit.y != 0) {
                vertical = (borderSize + railSize - last.y)/unit.y;
                if(vertical <= .00001) {
                    vertical = (height - (borderSize + railSize) - last.y)/unit.y;
                }
            }
            if(vertical < horizontal && vertical > 0) {
                next.setLocation((last.x + unit.x*vertical), (last.y+unit.y*vertical));
                g.drawLine((int)last.x, (int)last.y, (int)next.x, (int)(next.y));
                unit.y = -unit.y;
            } else {
                next.setLocation((last.x+unit.x*horizontal), (last.y+unit.y*horizontal));
                g.drawLine((int)last.x, (int)last.y, (int)(next.x), (int)(next.y));
                unit.x = -unit.x;
            }
            last.setLocation(next);
        }        
    }
    
    //ERROR HANDLING
    public boolean checkBounds(Ball b) {
        if(b.sunk) {
            return false;
        }
        if(b.pos.x < 0 || b.pos.y < 0 || b.pos.x > width - b.size || b.pos.y > height - b.size)
            return true;
        return false;
    }
    
    public void checkOverlaps(Ball ball) {
        Iterator<Ball> ballIterator = balls.iterator();
        while(ballIterator.hasNext()) {
            Ball ball2 = ballIterator.next();
            if(ball2 != ball && ball.checkOverlap(ball2)) {
               fixOverlap(ball, ball2);
            }
        }
        Iterator<PoolPolygon> polyIterator = polygons.iterator();
        while(polyIterator.hasNext()) {
            PoolPolygon poly = polyIterator.next();
            if(poly.checkOverlap(ball)) {
                poly.color = poly.color.darker();
                ball.color = ball.color.darker();
                err = true;
            }
        }
    }
    
    public void fixOverlap(Ball a, Ball b) {
        a.color = Color.MAGENTA;
        b.color = Color.MAGENTA;
        err = true;
        
    }
    
    //ACTIONS
    public void newRack() {
        /*
        Color def, other;
        int x = width*2/3;
        def = Color.ORANGE.darker();
        other = Color.CYAN.darker();
        for(int i = 0; i < 5; i++) {
            int y = height/2 - i*ballSize/2;
            for(int j = 0; j <= i; j++) {
                if(j == 1 && i == 2) {
                    balls.add(new Ball(Color.BLACK, x, y, 0, 0, ballSize));
                    def = Color.CYAN.darker();
                    other = Color.ORANGE.darker();
                } else if((i+j)%2 == 0) {
                    balls.add(new Ball(def, x, y, 0, 0, ballSize));
                } else {
                    balls.add(new Ball(other, x, y, 0, 0, ballSize));
                }
                y += ballSize+1; 
            }
            x += 2 + ballSize*Math.sqrt(3)/2;
        }
        cueball.pos.setLocation(width/4, height/2);
        cueball.vel.setLocation(0,0);*/
    }
    
    public void shoot() {
        shootingBall.vel.x = -aim.x * power;
        shootingBall.vel.y = -aim.y * power;
        shootingBall.acc.x = -aim.x * spin;
        shootingBall.acc.y = -aim.y * spin;
    }
    
    public Ball addBall(Color color, double x, double y, double a, double b, double s) {
        Ball ball = new Ball(color, x, y, a, b, s);
        universe.addBranchGraph(ball.group);        
        balls.add(ball);
        return ball;
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

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + (this.balls != null ? this.balls.hashCode() : 0);
        hash = 13 * hash + (this.pockets != null ? this.pockets.hashCode() : 0);
        hash = 13 * hash + (this.polygons != null ? this.polygons.hashCode() : 0);
        hash = 13 * hash + (this.tableColor != null ? this.tableColor.hashCode() : 0);
        hash = 13 * hash + (this.cueball != null ? this.cueball.hashCode() : 0);
        hash = 13 * hash + (this.shootingBall != null ? this.shootingBall.hashCode() : 0);
        hash = 13 * hash + (this.ghostBallObjectBall != null ? this.ghostBallObjectBall.hashCode() : 0);
        hash = 13 * hash + (this.ghostBallPosition != null ? this.ghostBallPosition.hashCode() : 0);
        return hash;
    }

    @Override public boolean equals(Object obj) {
        if(obj instanceof PoolPanel)
            return true;
        else
            return false;
    }
    
    //HIERARCHY BOUNDS INTERFACE
    public void ancestorResized(HierarchyEvent he) {        
        canvas.setSize(getWidth(), getHeight()-10);
        /*
        if(pockets != null) {
	    pockets.get(0).pos.setLocation(borderSize , borderSize );
	    pockets.get(1).pos.setLocation(width/2, borderSize );
	    pockets.get(2).pos.setLocation(width - borderSize , borderSize );
	    pockets.get(3).pos.setLocation(borderSize , height - borderSize );
	    pockets.get(4).pos.setLocation(width/2, height - borderSize );
	    pockets.get(5).pos.setLocation(width - borderSize, height - borderSize);
        }
        
        if(polygons != null) {
            polygons.get(0).xpoints[2] = width/2 - sidePocketSize/2 - sideIndent;
            polygons.get(0).xpoints[3] = width/2 - sidePocketSize/2;

	    polygons.get(1).xpoints[0] = width/2 + sidePocketSize/2;
	    polygons.get(1).xpoints[1] = width/2 + sidePocketSize/2 + sideIndent;
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

	    polygons.get(3).xpoints[0] = width/2 + sidePocketSize/2;
	    polygons.get(3).xpoints[1] = width/2 + sidePocketSize/2 + sideIndent;
	    polygons.get(3).xpoints[2] = width - pocketSize/2 - railIndent - borderSize;
	    polygons.get(3).xpoints[3] = width - pocketSize/2 - borderSize;
	    polygons.get(3).ypoints[0] = height - borderSize;
	    polygons.get(3).ypoints[1] = height - (borderSize + railSize);
	    polygons.get(3).ypoints[2] = height - (borderSize + railSize);
	    polygons.get(3).ypoints[3] = height - borderSize;
	    
	    
	    polygons.get(4).xpoints[0] = borderSize + pocketSize/2;
	    polygons.get(4).xpoints[1] = borderSize + pocketSize/2 + railIndent;
	    polygons.get(4).xpoints[2] = width/2 - sidePocketSize/2 - sideIndent;
	    polygons.get(4).xpoints[3] = width/2 - sidePocketSize/2;
	    polygons.get(4).ypoints[0] = height - borderSize;
	    polygons.get(4).ypoints[1] = height - (borderSize + railSize);
	    polygons.get(4).ypoints[2] = height - (borderSize + railSize);
	    polygons.get(4).ypoints[3] = height - borderSize;
           
            polygons.get(5).ypoints[2] = height - borderSize - railIndent - pocketSize/2;
            polygons.get(5).ypoints[3] = height - borderSize - pocketSize/2;
        }*/
    }
    
    public void ancestorMoved(HierarchyEvent he) { }
    
}
