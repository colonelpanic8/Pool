package pool;

import com.sun.j3d.utils.universe.SimpleUniverse;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import javax.media.j3d.*;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.vecmath.*;



public final class PoolPanel extends JPanel implements ActionListener, Comparator, HierarchyBoundsListener {
    
    double pocketSize, railSize, ballSize, borderSize, railIndent, sidePocketSize, sideIndent;
    boolean selMode, sliderPower;
    Ball cueball, shootingBall, ghostBallObjectBall;
    ArrayList<Ball> balls;
    ArrayList<Pocket> pockets;
    ArrayList<PoolPolygon> polygons;
    PriorityQueue<Collision> collisions;
    myPoint3d aim, ghostBallPosition;
    Color tableColor;
    double height, width;
    double spin, power;
    int collisionsExecuted;
    boolean frameSkip, err;
    
    //Java3D
    Canvas3D canvas;
    SimpleUniverse universe;
    Point3d cameraPosition = new Point3d(0f,0f,10f);
    BranchGroup group;
    TransformGroup transformGroup;
    Shape3D aimLine;
    LineArray aimLineGeometry;
    
    //Colors
    Color3f white;
    CameraController cr = new CameraController(this);
    
    int ticks = 0;
    
    //INITIALIZATION
    public PoolPanel(double bs, double rs, double w, double h) {
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
	ghostBallPosition = new myPoint3d(0,0,0);
        aim = new myPoint3d(0,0,0);
	collisions = new PriorityQueue(16, this);
       
        //Start timer
	Timer timer = new Timer(60, this);
	timer.start();
               
    }
    
    
    public void init3D() {
        //Initialize Java 3D components.
        canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        add("Center", canvas);
        universe = new SimpleUniverse(canvas);
        
        //Light
        BoundingBox bounds = new BoundingBox();
        bounds.setLower(-width/2-borderSize, -height/2-borderSize, -3);
        bounds.setUpper(width/2+borderSize, height/2+borderSize, 3);
        //BoundingSphere bounds = new BoundingSphere(new Point3d(), 100.0);
        group = new BranchGroup();
        Color3f lightColor = new Color3f(1.0f, 1.0f, 1.0f);
        Vector3f lightDirection = new Vector3f(4.0f, -7.0f, -12.0f);        
        DirectionalLight light = new DirectionalLight(lightColor, lightDirection);
        light.setInfluencingBounds(bounds);
        group.addChild(light);
        
        Color3f ambientColor = new Color3f(1.0f, 1.0f, 1.0f);        
        AmbientLight ambientLight = new AmbientLight(ambientColor);
        ambientLight.setInfluencingBounds(bounds);
        group.addChild(ambientLight);
        
        Background bg = new Background(new Color3f(0.0f, 1.0f, 1.0f));
        bg.setApplicationBounds(bounds);
        group.addChild(bg);
        
        //Add aiming line.
        transformGroup = new TransformGroup();
        Transform3D transform = new Transform3D();
        transform.setTranslation(new Vector3d(0.0, 0.0, 0.0));
        transformGroup.setTransform(transform);
        Point3f[] plaPts = new Point3f[2];
       
        
        white = new Color3f(1.0f, 1.0f, 1.0f);        
        Appearance app = new Appearance();
        ColoringAttributes ca = new ColoringAttributes(new Color3f(0.0f, 0.0f, 0.0f), ColoringAttributes.SHADE_FLAT);
        LineAttributes dashLa = new LineAttributes();
        dashLa.setLineWidth(5.0f);
        app.setColoringAttributes(ca);
        app.setLineAttributes(dashLa);
        dashLa.setLinePattern(LineAttributes.PATTERN_DASH);
        aimLineGeometry = new LineArray(2, LineArray.COORDINATES);
        aimLineGeometry.setCapability(LineArray.ALLOW_COORDINATE_WRITE);
        aimLine = new Shape3D(aimLineGeometry, app);
        aimLine.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
        transformGroup.addChild(aimLine);
        transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        
        
        //
        
        transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);        
        group.addChild(transformGroup);        
        universe.addBranchGraph(group);        
        universe.getViewingPlatform().setNominalViewingTransform();
        
        TransformGroup VpTG = universe.getViewingPlatform().getViewPlatformTransform();
        Transform3D Trfcamera = new Transform3D();
        Trfcamera.lookAt(cameraPosition, new Point3d(0.0,0.0,0.0), new Vector3d(0,1,0));
        Trfcamera.invert();
        VpTG.setTransform(Trfcamera);
        
        canvas.addMouseListener(cr);
        canvas.addMouseMotionListener(cr);
        
        
    }        
    
    public void initPolygons() {
	Color color = tableColor.darker();
        double[] xpoints, ypoints;
	xpoints = new double[4];
	ypoints = new double[4];
        
	xpoints[0] = -width/2 + (pocketSize/2);
	xpoints[1] = -width/2 + (pocketSize/2 + railIndent);
	xpoints[2] = -(pocketSize/2 + sideIndent);
	xpoints[3] = -pocketSize/2;
        
	ypoints[0] = height/2;
	ypoints[1] = height/2 - railSize;
	ypoints[2] = height/2 - railSize;
	ypoints[3] = height/2;
        this.addPolygon(xpoints, ypoints, 4, color, ballSize);
        
        xpoints[0] = (pocketSize/2);
	xpoints[1] = (pocketSize/2 + sideIndent);
	xpoints[2] = width/2-(pocketSize/2 + railIndent);
	xpoints[3] = width/2-pocketSize/2;
        
	ypoints[0] = height/2;
	ypoints[1] = height/2 - railSize;
	ypoints[2] = height/2 - railSize;
	ypoints[3] = height/2;
        this.addPolygon(xpoints, ypoints, 4, color, ballSize);
        
        xpoints[0] = width/2 ;
	xpoints[1] = width/2 - railSize;
	xpoints[2] = width/2 - railSize;
	xpoints[3] = width/2;
        
	ypoints[0] = height/2 - pocketSize/2;
	ypoints[1] = height/2 - (pocketSize/2 + railIndent);
	ypoints[2] = (pocketSize/2 + railIndent) - height/2;
	ypoints[3] = pocketSize/2-height/2;
        this.addPolygon(xpoints, ypoints, 4, color, ballSize);                
        
        xpoints[3] = -width/2 + (pocketSize/2);
	xpoints[2] = -width/2 + (pocketSize/2 + railIndent);
	xpoints[1] = -(pocketSize/2 + sideIndent);
	xpoints[0] = -pocketSize/2;
        
	ypoints[3] = -height/2;
	ypoints[2] = -height/2 + railSize;
	ypoints[1] = -height/2 + railSize;
	ypoints[0] = -height/2;
        this.addPolygon(xpoints, ypoints, 4, color, ballSize);
        
        xpoints[3] = (pocketSize/2);
	xpoints[2] = (pocketSize/2 + sideIndent);
	xpoints[1] = width/2-(pocketSize/2 + railIndent);
	xpoints[0] = width/2-pocketSize/2;
        
	ypoints[3] = -height/2;
	ypoints[2] = -height/2 + railSize;
	ypoints[1] = -height/2 + railSize;
	ypoints[0] = -height/2;
        this.addPolygon(xpoints, ypoints, 4, color, ballSize);
        
        xpoints[3] = -width/2 ;
	xpoints[2] = -width/2 + railSize;
	xpoints[1] = -width/2 + railSize;
	xpoints[0] = -width/2;
        
	ypoints[3] = height/2 - pocketSize/2;
	ypoints[2] = height/2 - (pocketSize/2 + railIndent);
	ypoints[1] = (pocketSize/2 + railIndent) - height/2;
	ypoints[0] = pocketSize/2-height/2;
        this.addPolygon(xpoints, ypoints, 4, color, ballSize);
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
        this.repaint();                
        if(shootingBall != null) {
            Transform3D transform;
            transform = new Transform3D();
            transform.setTranslation(new Vector3d(shootingBall.pos.x, shootingBall.pos.y, shootingBall.pos.z));
            transformGroup.setTransform(transform);
        }
        Point3d line = (Point3d) aim.clone();
        line.scale(-10);
        aimLineGeometry.setCoordinate(1, line);
        
	Iterator<Ball> iter;
	iter = balls.iterator();
        validate();
        if(err) {
            err = false;
            frameSkip = true;
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
		double b = 2 * (cueball.pos.y*-aim.x - ball.pos.y*-aim.x + 
				cueball.pos.y*-aim.y - ball.pos.y*-aim.y);
		double c = cueball.pos.y*cueball.pos.y + ball.pos.y*ball.pos.y +
		    cueball.pos.y*cueball.pos.y + ball.pos.y*ball.pos.y - 2*cueball.pos.y*ball.pos.y - 
		    2*cueball.pos.y*ball.pos.y - (ball.size + cueball.size)*(ball.size + cueball.size)/4;
		double t;
		if (a !=0 ){
		    double t1 = ( -b - Math.sqrt(b*b-4*a*c) )/(2 * a);
		    double t2 = ( -b + Math.sqrt(b*b-4*a*c) )/(2 * a);
		    t = t1 < t2 ? t1 : t2;
		} else {
		    t = -c/b;  
		}

		if( !(Double.isNaN(t)) && t < min && t > 0){
		    ghostBallPosition.setLocation((cueball.pos.x + t * -aim.x),
						  (cueball.pos.y + t * -aim.y),
                                                  0);
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
        g.setColor(Color.BLACK);
        g.drawString(Float.toString(cr.cameraPosition.x), 0, getHeight());
        g.drawString(Float.toString(cr.cameraPosition.y), 100, getHeight());
        g.drawString(Float.toString(cr.cameraPosition.z), 200, getHeight());
        
        g.drawString(Float.toString(cr.upVector.x), 0, getHeight()-20);
        g.drawString(Float.toString(cr.upVector.y), 100, getHeight()-20);
        g.drawString(Float.toString(cr.upVector.z), 200, getHeight()-20);
    }
    
    void drawGhostBall(Graphics g) {
        
        if(Math.abs(cueball.vel.x) + Math.abs(cueball.vel.y) < 1) {
            /*g.drawLine((int)cueball.pos.y, 
		       (int)cueball.pos.y, 
		       (int)(cueball.pos.y+(-aim.x*2000)), 
		       (int)(cueball.pos.y+(-aim.y*2000)));*/
	    //Additional aiming lines that were removed.
	    
            /*
	    g.drawLine( (int)(cueball.pos.y - -aim.y*cueball.size/2),
			(int)(cueball.pos.y + -aim.x*cueball.size/2), 
			(int)(cueball.pos.y + -aim.x*2000 - -aim.y*cueball.size/2), 
			(int)(cueball.pos.y + -aim.y*2000 + -aim.x*cueball.size/2));
	    g.drawLine( (int)(cueball.pos.y + -aim.y*cueball.size/2),
			(int)(cueball.pos.y - -aim.x*cueball.size/2), 
			(int)(cueball.pos.y + -aim.x*2000 + -aim.y*cueball.size/2),
			(int)(cueball.pos.y + -aim.y*2000 - -aim.x*cueball.size/2));
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
                Point2D.Double unit = new Point2D.Double(ghostBallObjectBall.pos.y - gcx,
                        ghostBallObjectBall.pos.y - gcy);
                drawPoolPath(unit, last, g);
                double temp = unit.y;
                unit.y = unit.x;
                unit.x = -temp;                      
                g.setColor(Color.MAGENTA);
                g.drawString(Double.toString(unit.x), 300, 140);
                g.drawString(Double.toString(unit.y), 300, 180);
                last.setLocation(cueball.pos.y-ghostBallObjectBall.pos.y, cueball.pos.y-ghostBallObjectBall.pos.y);
                Point2D.Double trans = new Point2D.Double(1/(last.x + last.y*last.y/last.x),
                        1/(last.y + last.x*last.x/last.y));
                temp = trans.y*gcx - trans.x*gcy;
                double unitY = trans.y*unit.x - trans.x*unit.y;
                double cueballY = trans.y*cueball.pos.y - trans.x*cueball.pos.y;
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
    
    public PoolPolygon addPolygon(double[] xpoints, double[] ypoints, int npoints, Color c, double ballsize) {
        PoolPolygon poly = new PoolPolygon(xpoints, ypoints, npoints, c, ballsize);
        universe.addBranchGraph(poly.group);
        polygons.add(poly);
        return poly;
        
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
        canvas.setSize(getWidth(), getHeight()-40);
    }
    
    public void ancestorMoved(HierarchyEvent he) { }
    
    
    
}




class CameraController implements MouseMotionListener, MouseListener, Action {

    PoolPanel pp;
    Point click = new Point(0,0);
    Transform3D transform = new Transform3D();
    Vector3f cameraPosition = new Vector3f(0f, 0f, 1f);
    Vector3f upVector = new Vector3f(0f, 1f, 0f);
    Point3f cameraTranslation = new Point3f();
    Vector3d upVec = new Vector3d(upVector);
    Point3d cameraPos = new Point3d(cameraPosition);
    Point3d cameraTrans = new Point3d();
    Quat4f rotation = new Quat4f(), inverse = new Quat4f(), vector = new Quat4f();
    float cameraDistance = 10.0f;
    float camDistance = cameraDistance;
    ChangeBasis changeBasis;
    
    //User configuration
    int mode = TRANSLATION_MODE;
    float zoomSensitivity = 20f;
    
    //Constants
    static final int TRANSLATION_MODE = 0;
    static final int ZOOM_MODE = 1;

    public CameraController(PoolPanel p) {
        pp = p;
    }
    
    public void mouseDragged(MouseEvent me) {
        float x,y;
        x = ((float)(me.getX() - click.x))/(pp.canvas.getWidth()/2);
        y = ((float)(click.y - me.getY()))/(pp.canvas.getHeight()/2);
        if(me.getButton() == MouseEvent.BUTTON1) {
            //Determine the point z for the current rotation given the click x, y.            
            float _z = 1 - x * x - y * y;
            float z = (float) (_z > 0 ? Math.sqrt(_z) : 0);
            Vector3f point = new Vector3f(x,y,z);
            point.normalize();
            changeBasis.transform(point);
            doRotate(point);
        } else {
            camDistance = cameraDistance;
            switch(mode) {
	    case TRANSLATION_MODE:            
		Vector3f point = new Vector3f(-x*4,-y*4,0);
		changeBasis.transform(point);
		cameraTrans.set(cameraTranslation);
		cameraTrans.scaleAdd(1f, point);                    
		break;
	    case ZOOM_MODE:
		camDistance = cameraDistance-y*zoomSensitivity;
		Vector3f axis = new Vector3f();
		axis.set(cameraPosition);
		float angle = -x;
		axis.scale((float)Math.sin(angle)/2);
		rotation.set(axis.x,
			     axis.y,
			     axis.z, 
			     (float)Math.cos(angle)/2);
		inverse.inverse(rotation);
		//Rotate the upVector.
		vector.set(upVector.x,
			   upVector.y,
			   upVector.z,
			   0f);
		vector.mul(rotation,vector);
		vector.mul(inverse);
		
		upVec.x = vector.x;
		upVec.y = vector.y;
		upVec.z = vector.z;                   
		break;
            }            
            cameraPos.set(cameraPosition);
            cameraPos.scale(camDistance);
        }
        //Set the transform
        cameraPos.scaleAdd(1f, cameraTrans);
        transform.lookAt(cameraPos, cameraTrans, upVec);
        transform.invert();
        pp.universe.getViewingPlatform().getViewPlatformTransform().setTransform(transform);
        
    }
        
    public void doRotate(Vector3f point) {        
        //Get the axis and angle of rotation.
        Vector3f axis = new Vector3f();
        float angle;
        axis.cross(point, cameraPosition);
        angle = point.angle(cameraPosition);       
        axis.normalize();
        
        //Calculate the quarternion that represents this rotation and its inverse.
        axis.scale((float)Math.sin(angle)/2);
        rotation.set(axis.x,
		     axis.y,
		     axis.z, 
		     (float)Math.cos(angle)/2);
        inverse.inverse(rotation);
        
        //Rotate the camera, store the result in point.
        vector.set(cameraPosition.x,
                   cameraPosition.y,
                   cameraPosition.z,
		   0f);
        vector.mul(rotation,vector);
        vector.mul(inverse);        
        cameraPos.x = vector.x;
        cameraPos.y = vector.y;
        cameraPos.z = vector.z;
        
        //Rotate the upVector.
        vector.set(upVector.x,
		   upVector.y,
		   upVector.z,
		   0f);
        vector.mul(rotation,vector);
        vector.mul(inverse);
        
        upVec.x = vector.x;
        upVec.y = vector.y;
        upVec.z = vector.z;
        
        //Scale point by the camera distance.        
        cameraPos.scale(cameraDistance);                
    }
    
    public void mousePressed(MouseEvent me) {
        click.setLocation(me.getX(), me.getY());
        Vector3f sideVector = new Vector3f(), camVec = new Vector3f();
        camVec.normalize(cameraPosition);
        sideVector.cross(upVector, camVec);
        changeBasis = new ChangeBasis(sideVector, upVector, camVec,
                      new Vector3f(1.0f, 0.0f, 0.0f),
                      new Vector3f(0.0f, 1.0f, 0.0f), 
                      new Vector3f(0.0f, 0.0f, 1.0f));
       
    }  

    public void mouseReleased(MouseEvent me) {
        cameraPosition.set(cameraPos);
        cameraPosition.normalize();
        upVector.set(upVec);
        cameraTranslation.set(cameraTrans);
        cameraDistance = camDistance;
    }
    
    public void mouseClicked(MouseEvent me) { }
    
    public void mouseMoved(MouseEvent me) { }

    public void mouseEntered(MouseEvent me) { }

    public void mouseExited(MouseEvent me) { }

    public void actionPerformed(ActionEvent ae) {
        
    }

    public Object getValue(String string) {
        return null;
    }

    public void putValue(String string, Object o) {
        
    }

    public void setEnabled(boolean bln) {
        
    }

    public boolean isEnabled() {
        return true;
    }

    public void addPropertyChangeListener(PropertyChangeListener pl) {
        
    }

    public void removePropertyChangeListener(PropertyChangeListener pl) {
        
    }
    
}

class ChangeBasis extends Matrix3f {
    public ChangeBasis(Vector3f a, Vector3f b, Vector3f c,
		       Vector3f x, Vector3f y, Vector3f z) {
        m00 = -(a.x*(z.z*y.y - y.z*z.y) + a.y*(y.z*z.x - z.z*y.x) + a.z*(z.y*y.x - y.y*z.x))/
	    (y.z*(z.y*x.x - x.y*z.x) + z.z*(x.y*y.x - y.y*x.x) + x.z*(y.y*z.x - z.y*y.x));
        m10 = -(a.x*(x.z*z.y - z.z*x.y) + a.y*(z.z*x.x - x.z*z.x) + a.z*(x.y*z.x - z.y*x.x))/
	    (z.z*(x.y*y.x - y.y*x.x) + y.z*(z.y*x.x - x.y*z.x) + x.z*(y.y*z.x - z.y*y.x));
        m20 = -(a.x*(y.z*x.y - x.z*y.y) + a.y*(x.z*y.x - y.z*x.x) + a.z*(y.y*x.x  - x.y*y.x))/
	    (y.z*(z.y*x.x - x.y*z.x) + z.z*(x.y*y.x - y.y*x.x) + x.z*(y.y*z.x - z.y*y.x));       
        m01 = -(b.x*(z.z*y.y - y.z*z.y) + b.y*(y.z*z.x - z.z*y.x) + b.z*(z.y*y.x - y.y*z.x))/
	    (y.z*(z.y*x.x - x.y*z.x) + z.z*(x.y*y.x - y.y*x.x) + x.z*(y.y*z.x - z.y*y.x));
        m11 = -(b.x*(x.z*z.y - z.z*x.y) + b.y*(z.z*x.x - x.z*z.x) + b.z*(x.y*z.x - z.y*x.x))/
	    (z.z*(x.y*y.x - y.y*x.x) + y.z*(z.y*x.x - x.y*z.x) + x.z*(y.y*z.x - z.y*y.x));
        m21 = -(b.x*(y.z*x.y - x.z*y.y) + b.y*(x.z*y.x - y.z*x.x) + b.z*(y.y*x.x  - x.y*y.x))/
	    (y.z*(z.y*x.x - x.y*z.x) + z.z*(x.y*y.x - y.y*x.x) + x.z*(y.y*z.x - z.y*y.x));        
        m02 = -(c.x*(z.z*y.y - y.z*z.y) + c.y*(y.z*z.x - z.z*y.x) + c.z*(z.y*y.x - y.y*z.x))/
	    (y.z*(z.y*x.x - x.y*z.x) + z.z*(x.y*y.x - y.y*x.x) + x.z*(y.y*z.x - z.y*y.x));
        m12 = -(c.x*(x.z*z.y - z.z*x.y) + c.y*(z.z*x.x - x.z*z.x) + c.z*(x.y*z.x - z.y*x.x))/
	    (z.z*(x.y*y.x - y.y*x.x) + y.z*(z.y*x.x - x.y*z.x) + x.z*(y.y*z.x - z.y*y.x));
        m22 = -(c.x*(y.z*x.y - x.z*y.y) + c.y*(x.z*y.x - y.z*x.x) + c.z*(y.y*x.x  - x.y*y.x))/
	    (y.z*(z.y*x.x - x.y*z.x) + z.z*(x.y*y.x - y.y*x.x) + x.z*(y.y*z.x - z.y*y.x));         
    }
    
    public ChangeBasis(Vector3f x, Vector3f y, Vector3f z) {
        m00 = -((z.z*y.y - y.z*z.y))/
	    (y.z*(z.y*x.x - x.y*z.x) + z.z*(x.y*y.x - y.y*x.x) + x.z*(y.y*z.x - z.y*y.x));
        m10 = -((x.z*z.y - z.z*x.y))/
	    (z.z*(x.y*y.x - y.y*x.x) + y.z*(z.y*x.x - x.y*z.x) + x.z*(y.y*z.x - z.y*y.x));
        m20 = -((y.z*x.y - x.z*y.y))/
	    (y.z*(z.y*x.x - x.y*z.x) + z.z*(x.y*y.x - y.y*x.x) + x.z*(y.y*z.x - z.y*y.x));       
        m01 = -((y.z*z.x - z.z*y.x))/
	    (y.z*(z.y*x.x - x.y*z.x) + z.z*(x.y*y.x - y.y*x.x) + x.z*(y.y*z.x - z.y*y.x));
        m11 = -((z.z*x.x - x.z*z.x))/
	    (z.z*(x.y*y.x - y.y*x.x) + y.z*(z.y*x.x - x.y*z.x) + x.z*(y.y*z.x - z.y*y.x));
        m21 = -((x.z*y.x - y.z*x.x))/
	    (y.z*(z.y*x.x - x.y*z.x) + z.z*(x.y*y.x - y.y*x.x) + x.z*(y.y*z.x - z.y*y.x));        
        m02 = -((z.y*y.x - y.y*z.x))/
	    (y.z*(z.y*x.x - x.y*z.x) + z.z*(x.y*y.x - y.y*x.x) + x.z*(y.y*z.x - z.y*y.x));
        m12 = -((x.y*z.x - z.y*x.x))/
	    (z.z*(x.y*y.x - y.y*x.x) + y.z*(z.y*x.x - x.y*z.x) + x.z*(y.y*z.x - z.y*y.x));
        m22 = -((y.y*x.x  - x.y*y.x))/
	    (y.z*(z.y*x.x - x.y*z.x) + z.z*(x.y*y.x - y.y*x.x) + x.z*(y.y*z.x - z.y*y.x));
    }
    
}
    