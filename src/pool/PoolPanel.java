package pool;

import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.SimpleUniverse;
import java.awt.Color;
import java.awt.Graphics;
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
import javax.vecmath.*;

public final class PoolPanel extends JPanel implements ActionListener, Comparator, HierarchyBoundsListener {
    
    double pocketSize, railSize, ballSize, borderSize, railIndent, sidePocketSize, sideIndent;
    boolean selMode, sliderPower;
    Ball cueball, shootingBall, ghostBallObjectBall;
    ArrayList<Ball> balls;
    ArrayList<Pocket> pockets;
    ArrayList<PoolPolygon> polygons;
    PriorityQueue<Collision> collisions;
    Color tableColor;
    double height, width;
    double spin, power;
    int collisionsExecuted;
    boolean frameSkip, err;
    
    //Java3D
    Canvas3D canvas;
    SimpleUniverse universe;
    PoolCameraController cameraController;
    BranchGroup group;
    
    //Aim
    Shape3D aimLine;
    LineArray aimLineGeometry;
    Point3d aim;
    
    //Ghostball
    TransformGroup ghostBallTransformGroup = new TransformGroup();
    Sphere ghostBall;
    Vector3f ghostBallPosition;
    Appearance ghostBallAppearance;
    Shape3D ghostBallLine;
    LineArray ghostBallLineGeometry;
    RenderingAttributes ra = new RenderingAttributes();
    
    //Colors
    Color3f white;
    
    int numberOfAimLines = 3;
    
    //INITIALIZATION
    public PoolPanel(double bs, double rs, double w, double h) {
        //Initialize size values
        height = h;
        width = w;
        ballSize = bs;
        railSize = rs;
        pocketSize = (6.0*ballSize);
        sidePocketSize = (3.0*ballSize);
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
        cueball = addBall(0, 0, 0, 0, ballSize);
        shootingBall = cueball;
        
        //Initialize mouse listeners.
        
        //Add listeners.
	addHierarchyBoundsListener(this);
        
        //Miscellaneous
	ghostBallPosition = new Vector3f(0,0,0);
        aim = new Point3d(1.0, 0.0, 0.0);
	collisions = new PriorityQueue(16, this);
       
        //Start timer
	Timer timer = new Timer(30, this);
	timer.start();
               
    }
    
    void init3D() {
        //Initialize Java 3D components.
        canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        add("Center", canvas);
        universe = new SimpleUniverse(canvas);
        group = new BranchGroup();
        
        //Create the bounding box for the game.
        BoundingBox bounds = new BoundingBox();
        bounds.setLower(-width/2-borderSize, -height/2-borderSize, -3);
        bounds.setUpper(width/2+borderSize, height/2+borderSize, 3);
        
        //Create light sources.
        Color3f lightColor = new Color3f(1.0f, 1.0f, 1.0f);
        Vector3f lightDirection = new Vector3f(4.0f, -7.0f, -12.0f);        
        DirectionalLight light = new DirectionalLight(lightColor, lightDirection);
        light.setInfluencingBounds(bounds);
        group.addChild(light);
        
        Color3f ambientColor = new Color3f(1.0f, 1.0f, 1.0f);        
        AmbientLight ambientLight = new AmbientLight(ambientColor);
        ambientLight.setInfluencingBounds(bounds);
        group.addChild(ambientLight);        
        
        //Add aiming line.
        white = new Color3f(1.0f, 1.0f, 1.0f);
        Appearance app = new Appearance();
        ColoringAttributes ca = new ColoringAttributes(white, ColoringAttributes.SHADE_FLAT);
        LineAttributes dashLa = new LineAttributes();
        dashLa.setLineWidth(1.0f);
        app.setColoringAttributes(ca);
        app.setLineAttributes(dashLa);
        //dashLa.setLinePattern(LineAttributes.PATTERN_DASH);
        aimLineGeometry = new LineArray(this.numberOfAimLines*2, LineArray.COORDINATES);
        aimLineGeometry.setCapability(LineArray.ALLOW_COORDINATE_WRITE);
        aimLine = new Shape3D(aimLineGeometry, app);
        aimLine.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
        group.addChild(aimLine);
        
        //Add ghost ball.        
        ghostBallAppearance = new Appearance();
        RenderingAttributes invisible = new RenderingAttributes();
        ra.setVisible(false);
        ra.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        ghostBallAppearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_WRITE);
        ghostBallAppearance.setRenderingAttributes(ra);
        ghostBall = new Sphere((float)ballSize, Sphere.ENABLE_APPEARANCE_MODIFY, ghostBallAppearance);
        ghostBallTransformGroup.addChild(ghostBall);
        ghostBallTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        group.addChild(ghostBallTransformGroup);
        
        //Add ghost ball aiming line
        Appearance appearance = new Appearance();
        appearance.setColoringAttributes(ca);
        appearance.setLineAttributes(dashLa);
        ghostBallLineGeometry = new LineArray(this.numberOfAimLines*2, LineArray.COORDINATES);        
        ghostBallLineGeometry.setCapability(LineArray.ALLOW_COORDINATE_WRITE);
        ghostBallLine = new Shape3D(aimLineGeometry, app);
        ghostBallLine.setAppearance(appearance);
        //ghostBallLine.setAppearance(ghostBallAppearance);
        ghostBallLine.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        ghostBallLine.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
        group.addChild(ghostBallLine);
        
        //Finalize 3D setup, initialize camera control.
        universe.addBranchGraph(group);        
        cameraController = new PoolCameraController(this);                      
    }        
    
    void initPolygons() {
	Color color = tableColor.darker();
        double[] xpoints, ypoints;
	xpoints = new double[4];
	ypoints = new double[4];
        
	xpoints[0] = -width/2 + (pocketSize/2);
	xpoints[1] = -width/2 + (pocketSize/2 + railIndent);
	xpoints[2] = -(sidePocketSize/2 + sideIndent);
	xpoints[3] = -sidePocketSize/2;
        
	ypoints[0] = height/2;
	ypoints[1] = height/2 - railSize;
	ypoints[2] = height/2 - railSize;
	ypoints[3] = height/2;
        this.addPolygon(xpoints, ypoints, 4, color, ballSize);
        
        xpoints[0] = (sidePocketSize/2);
	xpoints[1] = (sidePocketSize/2 + sideIndent);
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
	xpoints[1] = -(sidePocketSize/2 + sideIndent);
	xpoints[0] = -sidePocketSize/2;
        
	ypoints[3] = -height/2;
	ypoints[2] = -height/2 + railSize;
	ypoints[1] = -height/2 + railSize;
	ypoints[0] = -height/2;
        this.addPolygon(xpoints, ypoints, 4, color, ballSize);
        
        xpoints[3] = (sidePocketSize/2);
	xpoints[2] = (sidePocketSize/2 + sideIndent);
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

    void initPockets() {
	pockets.add(new Pocket(borderSize , borderSize , pocketSize));
	pockets.add(new Pocket(width/2, borderSize , sidePocketSize));
	pockets.add(new Pocket(width - borderSize , borderSize , pocketSize));
	pockets.add(new Pocket(borderSize , height - borderSize , pocketSize));
	pockets.add(new Pocket(width/2, height - borderSize , sidePocketSize));
	pockets.add(new Pocket(width - borderSize, height - borderSize , pocketSize));
    }
    
    //SIMULATION
    public void actionPerformed(ActionEvent evt){
        //this.repaint();
        doAim();
	Iterator<Ball> iter;
	iter = balls.iterator();
        validate();
        if(err) {
            rewind();
            frameSkip = true;
        }
        if(frameSkip) {
            err = false;
        }
	while(iter.hasNext()) {
	    Ball ball = iter.next();
            
            //For error handling           
            ball.lpos.set(ball.pos);
            ball.lvel.setLocation(ball.vel);
            
            detectPolygonCollisions(ball, 0);
	    checkPockets(ball, 0);
	    for(int i = balls.lastIndexOf(ball)+1; i < balls.size(); i++) {
		double t = ball.detectCollisionWith(balls.get(i));
		if(t < 1 && 0 <= t){
		    collisions.add(new BallCollision(t, ball, balls.get(i)));
		}
	    }            
	}
        updateBallPositions();
        updateGhostBall();
    }
    
    void updateBallPositions(){
	Iterator<Ball> ballIterator;
	Collision collision = collisions.poll();
	double timePassed = 0;
        while(collision != null) {
	    //Advance balls to the point where the collision occurs.
            ballIterator = balls.iterator();
	    while(ballIterator.hasNext()) {
		Ball ball = ballIterator.next();
		ball.move(collision.time-timePassed);
                
            }
            timePassed = collision.time;
	    
            collision.doCollision(this);
            collisionsExecuted++;
	    collision = collisions.poll();	    
	}
        ballIterator = balls.iterator();
	while(ballIterator.hasNext()) {
	    Ball ball = ballIterator.next();
	    ball.move(1-timePassed);
            ball.vel.x += ball.acc.x;
            ball.vel.y += ball.acc.y;
            if(ball.vel.distance(0,0) < .001) {
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
        }
    }
    
    void updateGhostBall() {
	Iterator<Ball> iter;
        double min = Double.POSITIVE_INFINITY;
	iter = balls.iterator();
	ghostBallObjectBall = null;
	while(iter.hasNext()) {
	    Ball ball = iter.next();
	    if(ball != shootingBall){
                double t;
		double a = aim.x*aim.x + aim.y*aim.y;
                
		double b = 2*-aim.x*shootingBall.pos.x + 2*-aim.y*shootingBall.pos.y -
                           2*-aim.x*ball.pos.x         - 2*-aim.y*ball.pos.y         ;
                
		double c = ball.pos.y*ball.pos.y                 + ball.pos.x*ball.pos.x                 +
                           shootingBall.pos.y*shootingBall.pos.y + shootingBall.pos.x*shootingBall.pos.x -
                           2*(shootingBall.pos.y*ball.pos.y      + shootingBall.pos.x*ball.pos.x)        -
                           (ball.size + shootingBall.size)*(ball.size + shootingBall.size);
                
		if (a !=0 ){
		    double t1 = ( -b - Math.sqrt(b*b-4*a*c) )/(2 * a);
		    double t2 = ( -b + Math.sqrt(b*b-4*a*c) )/(2 * a);
		    t = t1 < t2 ? t1 : t2;
		} else {
		    t = -c/b;  
		}

		if( !(Double.isNaN(t)) && t < min && t > 0){
		    ghostBallPosition.set((float)(shootingBall.pos.x + t * -aim.x),
                                          (float)(shootingBall.pos.y + t * -aim.y),
                                          0.0f);
		    ghostBallObjectBall = ball;
		    min = t;
		}
	    }
	}
    }
    
    //COLLISION DETECTION
    void detectPolygonCollisions(Ball ball, double t) {
        Iterator<PoolPolygon> iter = polygons.iterator();
        while(iter.hasNext()) {
            PoolPolygon p = iter.next();
            p.detectCollisions(ball, collisions, t);
        }
    }
    
    void detectPolygonCollisions(Ball ball, double t, WallCollision collision) {
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
    
    void checkPockets(Ball ball, double timePassed) {
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
    
    //GRAPHICAL FUNCTIONS
    @Override public void paintComponent(Graphics g){
        
        /*super.paintComponent(g);
        g.drawString(Float.toString(cameraController.cameraTranslation.x), 600, getHeight()-20);
        g.drawString(Float.toString(cameraController.cameraTranslation.y), 700, getHeight()-20);
        g.drawString(Float.toString(cameraController.cameraTranslation.z), 800, getHeight()-20);
        * */
        /*
        super.paintComponent(g);
        g.drawString(Float.toString(cameraController.cameraPosition.x), 0, getHeight()-20);
         g.drawString(Float.toString(cameraController.cameraPosition.y), 100, getHeight()-20);
         g.drawString(Float.toString(cameraController.cameraPosition.z), 200, getHeight()-20);
         */
                 /*
        g.setColor(Color.BLACK);
        g.drawString(Float.toString(cueball.rotation.x), 0, getHeight()-20);
         g.drawString(Float.toString(cueball.rotation.y), 100, getHeight()-20);
         g.drawString(Float.toString(cueball.rotation.z), 200, getHeight()-20);*/
        /*
         * This was removed for performance reasons.
         * super.paintComponent(g);
         g.setColor(Color.BLACK);
          g.drawString(Float.toString(cameraController.cameraPosition.x), 0, getHeight()-20);
         g.drawString(Float.toString(cameraController.cameraPosition.y), 100, getHeight()-20);
         g.drawString(Float.toString(cameraController.cameraPosition.z), 200, getHeight()-20);
         * 
         * g.drawString(Double.toString(cameraController.cameraPos.x), 0, getHeight());
         * g.drawString(Double.toString(cameraController.cameraPos.y), 100, getHeight());
         * g.drawString(Double.toString(cameraController.cameraPos.z), 200, getHeight());
         * 
         * g.drawString(Float.toString(cameraController.upVector.x), 300, getHeight()-20);
         * g.drawString(Float.toString(cameraController.upVector.y), 400, getHeight()-20);
         * g.drawString(Float.toString(cameraController.upVector.z), 500, getHeight()-20);
         * 
         * g.drawString(Double.toString(cameraController.upVec.x), 300, getHeight());
         * g.drawString(Double.toString(cameraController.upVec.y), 400, getHeight());
         * g.drawString(Double.toString(cameraController.upVec.z), 500, getHeight());
         * 
         * g.drawString(Float.toString(cameraController.cameraTranslation.x), 600, getHeight()-20);
         * g.drawString(Float.toString(cameraController.cameraTranslation.y), 700, getHeight()-20);
         * g.drawString(Float.toString(cameraController.cameraTranslation.z), 800, getHeight()-20);
         * 
         * g.drawString(Double.toString(cameraController.cameraTrans.x), 600, getHeight());
         * g.drawString(Double.toString(cameraController.cameraTrans.y), 700, getHeight());
         * g.drawString(Double.toString(cameraController.cameraTrans.z), 800, getHeight());
         */                        
    }
    
    void doAim() {        
        if(shootingBall != null && shootingBall.vel.distance(0.0, 0.0) < .01) {                                
            if(ghostBallObjectBall == null) {
                Vector3f unit = new Vector3f();
                unit.set(aim);
                unit.scale(-1f);
                unit.normalize();
                Point3f start = new Point3f(shootingBall.pos);
                drawPoolPath(unit, start, numberOfAimLines, aimLineGeometry,0);
                ra.setVisible(false);
            } else {
                //Set the ghost ball to be visible.
                ra.setVisible(true);
                
                //Set the first line to be a line from the shooting ball to the location of the ghost ball.
                aimLineGeometry.setCoordinate(0, shootingBall.pos);
                aimLineGeometry.setCoordinate(1, new Point3f(ghostBallPosition));
                
                //Determine the unit vectors that describe the trajectory of the object ball.
                Vector3f unit = new Vector3f();
                unit.set((float)(ghostBallObjectBall.pos.x - ghostBallPosition.x),
                         (float)(ghostBallObjectBall.pos.y - ghostBallPosition.y),
                         0.0f);               
                unit.normalize();
                //Store unit in shootingBallUnit as it will get overwritten by drawPoolPath.

                Vector3f shootingBallUnit = new Vector3f(unit);
                
                //Draw the trajectory of the object ball.
                Point3f start = new Point3f(ghostBallObjectBall.pos);
                drawPoolPath(unit, start, numberOfAimLines, ghostBallLineGeometry, 0);
                
                
                
                //Determine which of the two perpendicular directions the shooting ball will travel in.
                unit.set(shootingBallUnit);
                shootingBallUnit.set((float)(ghostBallPosition.x - shootingBall.pos.x),
                                     (float)(ghostBallPosition.y - shootingBall.pos.y),
                                     0.0f);
                shootingBallUnit.normalize();
                Vector3f temp = new Vector3f(shootingBallUnit.y, -shootingBallUnit.x, 0.0f);
                ChangeBasis changeBasis = new ChangeBasis(temp, shootingBallUnit, new Vector3f(0.0f,0.0f,1.0f));
                
                temp.set(ghostBallPosition);
                changeBasis.transform(temp);
                start.set(shootingBallUnit);
                changeBasis.transform(start);
                if((temp.y < 0) != (temp.y-start.y < 0)){
                    shootingBallUnit.scale(-1f);
                }
                
                //Draw the path of the shooting ball.
                start.set(ghostBallPosition);
                shootingBallUnit.set(unit.y, -unit.x, unit.z);
                drawPoolPath(shootingBallUnit, start, numberOfAimLines, aimLineGeometry, 1);
                Transform3D transform = new Transform3D();
                transform.setTranslation(ghostBallPosition);
                ghostBallTransformGroup.setTransform(transform);
            }
        }
        
        if(cueball.vel.distance(0.0, 0.0) == 0) {

            if (ghostBallObjectBall != null){
                
                /*
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
                */
            }                       
	}
    }
    
    void drawPoolPath(Vector3f unit, Point3f last, int numLines, LineArray array, int offset) {
        Point3d next = new Point3d();
        double horizontal, vertical;
        for(int i = offset; i < numLines; i++) {
            horizontal = Double.POSITIVE_INFINITY;
            vertical = Double.POSITIVE_INFINITY;
            
            if(unit.x != 0) {
                horizontal = (width/2 - (railSize + ballSize) - last.x)/unit.x;
                if(horizontal <= .00001) {
                    horizontal = ((railSize + ballSize) - width/2 - last.x)/unit.x;
                }
            }
            if(unit.y != 0) {
                vertical = (height/2 - (railSize + ballSize) - last.y)/unit.y;
                if(vertical <= .00001) {
                    vertical = ((railSize + ballSize) - height/2 - last.y)/unit.y;
                }
            }
            if(vertical < horizontal && vertical > 0) {
                next.set((last.x + unit.x*vertical), (last.y+unit.y*vertical), 0);                
                unit.y = -unit.y;
            } else {
                next.set((last.x+unit.x*horizontal), (last.y+unit.y*horizontal), 0);
                unit.x = -unit.x;
            }
            array.setCoordinate(2*i, last);
            array.setCoordinate(2*i + 1, next);
            last.set(next);
        }        
    }
    
    //ACTIONS
    public void newRack() {        
        cueball.pos.set(-width/4, 0, 0);
        cueball.vel.setLocation(0,0);
        cueball.move(0.0);
        aim.x = -1.0;
        aim.y = 0.0;
        doAim();
        this.cameraController.overheadView();
        double x = width/8;
        for(int i = 0; i < 5; i++) {
            double y = -i*ballSize + .01;
            for(int j = 0; j <= i; j++) {
                if(j == 1 && i == 2) {

                } else if((i+j)%2 == 0) {
                    this.addBall(x, y, 0, 0, ballSize);
                } else {
                    this.addBall(x, y, 0, 0, ballSize);
                }
                y += 2*ballSize; 
            }
            x += 2*ballSize*Math.sqrt(3)/2+.01;
        }                       
    }
     
    public void shoot() {
        shootingBall.vel.x = -aim.x * power;
        shootingBall.vel.y = -aim.y * power;
        shootingBall.acc.x = -aim.x * spin;
        shootingBall.acc.y = -aim.y * spin;
        ra.setVisible(false);
    }
    
    public Ball addBall(double x, double y, double a, double b, double s) {
        Texture texImage = new TextureLoader("1.jpg",this).getTexture();        
        Appearance appearance = new Appearance();
        appearance.setTexture(texImage);
        Ball ball = new Ball(appearance, x, y, a, b, s);
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
    
    public void rewind() {
       Iterator<Ball> iter = balls.iterator();
        while(iter.hasNext()) {
	    Ball ball = iter.next();
            ball.pos.set(ball.lpos);
            ball.vel.setLocation(ball.lvel);
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

                err = true;
            }
        }
    }
    
    public void fixOverlap(Ball a, Ball b) {
        a.color = Color.MAGENTA;
        b.color = Color.MAGENTA;
        err = true;
        
    }
    
}

class PoolCameraController extends CameraController {
    PoolPanel pp;
    
    public PoolCameraController(PoolPanel p) {
        super(p.universe, p.canvas);
        pp = p;
    }
    
    public void snapToShootingBall() {
        cameraTrans.set(pp.shootingBall.pos);
        cameraTranslation.set(cameraTrans);
        cameraPosition.set(pp.aim);
        double angle = .1;
        Vector3f aimPerp = new Vector3f();
        aimPerp.x = (float) pp.aim.y;
        aimPerp.y = (float) -pp.aim.x;
        aimPerp.z = (float) pp.aim.z;
        
        aimPerp.scale((float)Math.sin(angle/2));
        
        rotation.set(aimPerp.x,
                     aimPerp.y,
                     aimPerp.z, 
                     (float)Math.cos(angle/2));
        inverse.inverse(rotation);
        //Rotate the upVector.
        vector.set(cameraPosition.x,
                cameraPosition.y,
                cameraPosition.z,
                0f);
        vector.mul(rotation,vector);
        vector.mul(inverse);
        
        cameraPosition.x = vector.x;
        cameraPosition.y = vector.y;
        cameraPosition.z = vector.z;        
                
        cameraPosition.normalize();
        cameraPos.set(cameraPosition);
        upVector.set(0.0f, 0.0f, 1.0f);
        upVec.set(upVector);    
        updateCamera();
    }
    
    public void overheadView() {
        cameraPos.set(0.0, 0.0, 1.0);
        upVec.set(0.0, 1.0, 0.0);
        cameraTrans.set(0.0, 0.0, 0.0f);
        camDistance = 34f;
        updateCamera();
        mouseReleased(null);
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