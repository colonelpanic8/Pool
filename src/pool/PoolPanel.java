package pool;

import cameracontrol.CameraController;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.pickfast.PickCanvas;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.universe.SimpleUniverse;
import java.awt.Color;
import java.awt.event.*;
import java.util.*;
import javax.media.j3d.*;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.vecmath.*;
import unbboolean.j3dbool.BooleanModeller;
import unbboolean.j3dbool.Solid;
import unbboolean.solids.DefaultCoordinates;



public final class PoolPanel extends JPanel implements ActionListener, Comparator, HierarchyBoundsListener {
    
    static final float gravity = .01f;
    protected static final float friction = .0075f, rollingResistance = .001f, frictionThreshold = .015f;
    static double spinS = 4.0, powerS = 1.3f;
    static double height, width;    
    static double pocketSize, railSize, ballSize, borderSize, railIndent, sidePocketSize, sideIndent, pocketDepth;
    boolean selectionMode = false, sliderPower = false;
    PoolBall cueball, shootingBall, ghostBallObjectBall;
    ArrayList<PoolBall>     balls = new ArrayList(16);
    ArrayList<PoolBall>  activeBalls = new ArrayList(16);
    ArrayList<PoolPocket> pockets = new ArrayList(6);
    ArrayList<PoolPolygon> polygons = new ArrayList(10);
    PriorityQueue<PoolCollision> collisions;
    double spin, power;
    int collisionsExecuted = 0;
    int numberOfAimLines = 3;
    boolean inMotion = false;
    
    //Java3D
    Canvas3D canvas;
    PickCanvas ballPicker;
    SimpleUniverse universe;
    PoolCameraController cameraController;
    BranchGroup group;
    
    //Overlay Buttons
    //ImageButtonOverlay snapButton;
    
    //Aim
    Shape3D aimLine;    
    LineArray aimLineGeometry;
    Point3d aim;
    RenderingAttributes aimLineRA;
    
    //Ghostball
    TransformGroup ghostBallTransformGroup = new TransformGroup();
    RenderingAttributes ghostBallRA = new RenderingAttributes();
    Sphere ghostBall;
    Vector3f ghostBallPosition;
    Appearance ghostBallAppearance;
    Shape3D ghostBallLine;
    LineArray ghostBallLineGeometry;
    
    //Colors
    Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
    Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
    Color3f gray = new Color3f(.5f, .5f, .5f);
    Color3f turqoise = new Color3f(0.0f, .5f, .5f);
    Color3f darkGreen = new Color3f(0.0f, 0.5f, 0.0f);
    Color3f darkBlue = new Color3f(0.0f, 0.0f, 0.5f);
    Color3f darkRed = new Color3f(.5f, 0.0f, 0.0f);
    Color3f tableColor = gray;
    Color3f darkerTableColor = new Color3f(tableColor.x*.80f, tableColor.y*.80f, tableColor.z*.80f);
    Color3f darkestTableColor = new Color3f(darkerTableColor.x*.80f, darkerTableColor.y*.80f, darkerTableColor.z*.80f);
    Color3f railColor = darkerTableColor;
    Color3f borderColor = darkestTableColor;
    Color3f pocketColor = turqoise;
    
    //Shared Scene Graph Objects
    TextureAttributes ta = new TextureAttributes();
    Material ballMaterial = new Material(new Color3f(.2f,.2f,.2f), white, white, white, 128f);
    
    //Debug
    boolean frameSkip = false, err = false;
    
    //--------------------INITIALIZATION--------------------//
    
    public PoolPanel(double bs, double rs, double w, double h) {
        //Initialize size values
        //this.setLayout(new OverlayLayout(this));
        height = h;
        width = w;
        ballSize = bs;
        railSize = rs;
        pocketSize = (2.2*ballSize);
        sidePocketSize = (1.8*ballSize);
        borderSize = pocketSize + .4;
        railIndent = railSize;
        sideIndent = railIndent/4;
        pocketDepth = ballSize*8;       
        
        //Initialize 3D components
        ta.setTextureMode(TextureAttributes.MODULATE);
        init3D();        
        initPockets();
        initPolygons();
        initTable();
        initBalls();
        
        /*
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("images/SnapButton.jpg"));
        } catch (IOException e) {
        }*/
        /*
        snapButton = new ImageButtonOverlay(canvas, new Dimension(40,40), new BufferedImage[] {img, img, img, img});
        snapButton.initialize();
        if(snapButton.isVisible()) {
            snapButton.setLocation(100, 100);
            snapButton.setSize(100,100);
            snapButton.repaint();
        }*/
        
        /*
        ImageOverlay io = new ImageOverlay(canvas, new Dimension(40,40), img);
        io.initialize();*/
                
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
        canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration()){
            
            @Override
            public void postRender()
            {
                this.getGraphics2D().setColor(Color.white);
                this.getGraphics2D().drawString("Heads Up Display (HUD) Works!",100,100);
                this.getGraphics2D().flush(false);
            }
        };
        add(canvas);
        universe = new SimpleUniverse(canvas);
        universe.getViewer().getView().setBackClipDistance(width*3);
        group = new BranchGroup();
        group.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        group.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        ballPicker = new PickCanvas(canvas, group);
        ballPicker.setMode(PickCanvas.TYPE_PRIMITIVE);
        
        //Create the bounding box for the game.
        BoundingBox bounds = new BoundingBox();
        bounds.setLower(-width/2-borderSize, -height/2-borderSize, -3);
        bounds.setUpper(width/2+borderSize, height/2+borderSize, 3);
        
        //Create light sources.
        Color3f lightColor = white;
        Vector3f lightDirection = new Vector3f(0.0f, -1.0f, -1.0f);        
        DirectionalLight light = new DirectionalLight(lightColor, lightDirection);
        light.setInfluencingBounds(bounds);
        group.addChild(light);
        
        Color3f ambientColor = white;        
        AmbientLight ambientLight = new AmbientLight(ambientColor);
        ambientLight.setInfluencingBounds(bounds);
        group.addChild(ambientLight);
        
        SpotLight sl = new SpotLight(true, white, new Point3f(0.0f,0.0f, 5.0f),
        new Point3f(1.0f,1.0f, 3.0f), new Vector3f(0.0f,0.0f, -1.0f), (float)Math.PI, 100.0f);
        sl.setInfluencingBounds(bounds);
        //group.addChild(sl);
        //group.addChild(this.newSpotLight(bounds, new Point3f(0.0f,0.0f, 5.0f), (float)Math.PI, 100.0f));
        
        //Add aiming line.
        Appearance appearance = new Appearance();
        ColoringAttributes ca = new ColoringAttributes(white, ColoringAttributes.SHADE_FLAT);
        LineAttributes dashLa = new LineAttributes();
        aimLineRA = new RenderingAttributes();
        dashLa.setLineWidth(1.0f);
        appearance.setColoringAttributes(ca);
        appearance.setLineAttributes(dashLa);
        appearance.setRenderingAttributes(aimLineRA);
        aimLineRA.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        appearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_WRITE);
        //dashLa.setLinePattern(LineAttributes.PATTERN_DASH);
        aimLineGeometry = new LineArray(this.numberOfAimLines*2, LineArray.COORDINATES);
        aimLineGeometry.setCapability(LineArray.ALLOW_COORDINATE_WRITE);
        aimLine = new Shape3D(aimLineGeometry, appearance);
        aimLine.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
        group.addChild(aimLine);
        
        //Add ghost ball.        
        ghostBallAppearance = new Appearance();
        ghostBallRA.setVisible(false);
        ghostBallRA.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        ghostBallAppearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_WRITE);
        ghostBallAppearance.setRenderingAttributes(ghostBallRA);
        ghostBall = new Sphere((float)ballSize, Sphere.ENABLE_APPEARANCE_MODIFY, ghostBallAppearance);
        ghostBallTransformGroup.addChild(ghostBall);
        ghostBallTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        group.addChild(ghostBallTransformGroup);
        
        //Add ghost ball aiming line
        appearance = new Appearance();
        LineAttributes la = new LineAttributes();
        la.setLineWidth(1.0f);
        appearance.setColoringAttributes(ca);
        appearance.setLineAttributes(la);
        appearance.setRenderingAttributes(ghostBallRA);
        ghostBallLineGeometry = new LineArray(this.numberOfAimLines*2, LineArray.COORDINATES);
        ghostBallLineGeometry.setCoordinate(0, new Point3f(1.0f, 0.0f, 0.0f));
        ghostBallLineGeometry.setCoordinate(1, new Point3f(0.0f, 1.0f, 0.0f));
        ghostBallLine = new Shape3D(ghostBallLineGeometry, appearance);
        ghostBallLineGeometry.setCapability(LineArray.ALLOW_COORDINATE_WRITE);
        ghostBallLine.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        ghostBallLine.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
        group.addChild(ghostBallLine);
        
        //Finalize 3D setup, initialize camera control.
        universe.addBranchGraph(group);        
        cameraController = new PoolCameraController(this);                      
    }
    
    void initPolygons() {
        //Appearance appearance = createMatAppear(turqoise, white, 5.0f);
        double[] xpoints, ypoints;
	xpoints = new double[4];
	ypoints = new double[4];
        
	xpoints[0] = -width/2 + (pocketSize);
	xpoints[1] = -width/2 + (pocketSize + railIndent);
	xpoints[2] = -(sidePocketSize + sideIndent);
	xpoints[3] = -sidePocketSize;
        
	ypoints[0] = height/2;
	ypoints[1] = height/2 - railSize;
	ypoints[2] = height/2 - railSize;
	ypoints[3] = height/2;
        this.addPolygon(xpoints, ypoints, 4, railColor, ballSize);
        
        xpoints[0] = (sidePocketSize);
	xpoints[1] = (sidePocketSize + sideIndent);
	xpoints[2] = width/2-(pocketSize + railIndent);
	xpoints[3] = width/2-pocketSize;
        
	ypoints[0] = height/2;
	ypoints[1] = height/2 - railSize;
	ypoints[2] = height/2 - railSize;
	ypoints[3] = height/2;
        this.addPolygon(xpoints, ypoints, 4, railColor, ballSize);
        
        xpoints[0] = width/2 ;
	xpoints[1] = width/2 - railSize;
	xpoints[2] = width/2 - railSize;
	xpoints[3] = width/2;
        
	ypoints[0] = height/2 - pocketSize;
	ypoints[1] = height/2 - (pocketSize + railIndent);
	ypoints[2] = (pocketSize + railIndent) - height/2;
	ypoints[3] = pocketSize-height/2;
        this.addPolygon(xpoints, ypoints, 4, railColor, ballSize);                
        
        xpoints[3] = -width/2 + (pocketSize);
	xpoints[2] = -width/2 + (pocketSize + railIndent);
	xpoints[1] = -(sidePocketSize + sideIndent);
	xpoints[0] = -sidePocketSize;
        
	ypoints[3] = -height/2;
	ypoints[2] = -height/2 + railSize;
	ypoints[1] = -height/2 + railSize;
	ypoints[0] = -height/2;
        this.addPolygon(xpoints, ypoints, 4, railColor, ballSize);
        
        xpoints[3] = (sidePocketSize);
	xpoints[2] = (sidePocketSize + sideIndent);
	xpoints[1] = width/2-(pocketSize + railIndent);
	xpoints[0] = width/2-pocketSize;
        
	ypoints[3] = -height/2;
	ypoints[2] = -height/2 + railSize;
	ypoints[1] = -height/2 + railSize;
	ypoints[0] = -height/2;
        this.addPolygon(xpoints, ypoints, 4, railColor, ballSize);
        
        xpoints[3] = -width/2 ;
	xpoints[2] = -width/2 + railSize;
	xpoints[1] = -width/2 + railSize;
	xpoints[0] = -width/2;
        
	ypoints[3] = height/2 - pocketSize;
	ypoints[2] = height/2 - (pocketSize + railIndent);
	ypoints[1] = (pocketSize + railIndent) - height/2;
	ypoints[0] = pocketSize-height/2;
        this.addPolygon(xpoints, ypoints, 4, railColor, ballSize);
    }

    void initPockets() {
        Iterator<PoolPocket> iter;
        pockets.add(new PoolPocket(width/2,  height/2,  pocketSize,     (float)pocketDepth, (float)ballSize, pocketColor));
        pockets.add(new PoolPocket(-width/2, height/2,  pocketSize,     (float)pocketDepth, (float)ballSize, pocketColor));
        pockets.add(new PoolPocket(0.0,      height/2,  sidePocketSize, (float)pocketDepth, (float)ballSize, pocketColor));        
        pockets.add(new PoolPocket(width/2,  -height/2, pocketSize,     (float)pocketDepth, (float)ballSize, pocketColor));
	pockets.add(new PoolPocket(-width/2, -height/2, pocketSize,     (float)pocketDepth, (float)ballSize, pocketColor));
        pockets.add(new PoolPocket(0.0,      -height/2, sidePocketSize, (float)pocketDepth, (float)ballSize, pocketColor));        
        iter = pockets.iterator();
        while(iter.hasNext()) {
            PoolPocket pocket = iter.next();
            universe.addBranchGraph(pocket.group);
        }
    }
    
    void initBalls() {
        //Initialize cueball.
        Texture cueballImage = new TextureLoader("textures/cueball.jpg",this).getTexture();        
        Appearance appearance = new Appearance();
        appearance.setMaterial(ballMaterial);
        appearance.setTexture(cueballImage);
        appearance.setTextureAttributes(ta);
        cueball = addBall(-width/4, 0, ballSize, appearance);
        shootingBall = cueball;
        
        for(int i = 1; i < 16; i++) {
            Texture texture = new TextureLoader(String.format("textures/%d.jpg",i),this).getTexture();
            Appearance app = new Appearance();
            app.setTexture(texture);
            app.setTextureAttributes(ta);
            app.setMaterial(ballMaterial);
            PoolBall ball = new PoolBall(app, ballSize, i);            
            group.addChild(ball.group);      
            balls.add(ball);
        }                
    }
    
    private void initTable() {
        PoolPocket pocket;
        BooleanModeller bm;        
        BranchGroup borderGroup = new BranchGroup();
        Solid solid1 = new Solid();
        Solid solid2 = new Solid();        
        
        //Top border
        solid1.setData(DefaultCoordinates.DEFAULT_BOX_VERTICES, DefaultCoordinates.DEFAULT_BOX_COORDINATES, borderColor);
        solid1.scale(width + 2*borderSize, borderSize, ballSize*2);
        
        //Bottom
        solid2.setData(DefaultCoordinates.DEFAULT_BOX_VERTICES, DefaultCoordinates.DEFAULT_BOX_COORDINATES, borderColor);
        solid2.scale(width + 2*borderSize, borderSize, ballSize*2);

        
        solid1.translate(0, height/2 + borderSize/2);
        solid2.translate(0, -(height/2 + borderSize/2));
        bm = new BooleanModeller(solid1, solid2);
        solid1 = bm.getUnion();
        
        //Left
        solid2.setData(DefaultCoordinates.DEFAULT_BOX_VERTICES, DefaultCoordinates.DEFAULT_BOX_COORDINATES, borderColor);
        solid2.scale(borderSize, height, ballSize*2);
        solid2.translate(-(width/2 + borderSize/2) ,0);
        
        bm = new BooleanModeller(solid1, solid2);
        solid1 = bm.getUnion();
        
        solid2.setData(DefaultCoordinates.DEFAULT_BOX_VERTICES, DefaultCoordinates.DEFAULT_BOX_COORDINATES, borderColor);
        solid2.scale(borderSize, height, ballSize*2);
        solid2.translate(width/2 + borderSize/2,0);
        
        bm = new BooleanModeller(solid1, solid2);
        solid1 = bm.getUnion();
        
        //Table
        solid2.setData(DefaultCoordinates.DEFAULT_BOX_VERTICES, DefaultCoordinates.DEFAULT_BOX_COORDINATES, tableColor);
        solid2.scale(width, height, ballSize/2);
        
        for(int i = 0; i < 6; i++) {            
            pocket = pockets.get(i);
            pocket.inner.translate(pocket.pos.x, pocket.pos.y);
            pocket.inner.rotate(Math.PI/2, 0);
            bm = new BooleanModeller(solid1, pocket.inner);
            solid1 = bm.getDifference();
        }
        
        
        Solid solid3 = new Solid();
        /*
        solid3.setData(DefaultCoordinates.DEFAULT_BOX_VERTICES, DefaultCoordinates.DEFAULT_BOX_COORDINATES, darkBlue);
                
        solid2.setData(DefaultCoordinates.DEFAULT_SPHERE_VERTICES, DefaultCoordinates.DEFAULT_SPHERE_COORDINATES, darkBlue);
        //solid3.scale(.5f,.5f,1f);
        //solid3.translate(.25,.25);
        //bm = new BooleanModeller(solid3, solid2);
        //solid3 = bm.getDifference();
        //solid3.translate(0,2);
        borderGroup.addChild(solid2);
        borderGroup.addChild(solid3);
        //solid2.scale(ballSize, ballSize, 2*ballSize);
        
        //solid2.translate(width/2+borderSize-ballSize, height/2+borderSize-ballSize);
        //bm = new BooleanModeller(solid1, solid2);        */
        
        borderGroup.addChild(solid1);
        
        //Table
        TransformGroup tg = new TransformGroup();
        Vector3f shift = new Vector3f(0.0f, 0.0f, (float)-ballSize*3/2);
        Transform3D transform = new Transform3D();
        transform.setTranslation(shift);
        tg.setTransform(transform);
        solid2.setData(DefaultCoordinates.DEFAULT_BOX_VERTICES, DefaultCoordinates.DEFAULT_BOX_COORDINATES, tableColor);
        solid2.scale(width, height, ballSize);        
        for(int i = 0; i < 6; i++) {            
            pocket = pockets.get(i);
            bm = new BooleanModeller(solid2, pocket.inner);
            solid2 = bm.getDifference();
        }
        tg.addChild(solid2);
        borderGroup.addChild(tg);
        universe.addBranchGraph(borderGroup);
        
    }
    
    //--------------------SIMULATION--------------------//
    
    @Override
    public void actionPerformed(ActionEvent evt){
        //doOverlays();
        doAim();
	Iterator<PoolBall> iter;
	iter = activeBalls.iterator();
        //validate();
        if(err) {
            rewind();
            frameSkip = true;
        }
        if(frameSkip) {
            err = false;
        }
	while(iter.hasNext()) {
	    PoolBall ball = iter.next();            
            //For error handling           
            //ball.lpos.set(ball.pos);
            //ball.lvel.set(ball.vel);            
            detectPolygonCollisions(ball, 0);
	    detectPocketCollisions(ball, 0);
            for(int i = balls.lastIndexOf(ball) + 1; i < balls.size(); i++) {
                double t = ball.detectCollisionWith(balls.get(i));
                if(t < 1 && 0 <= t){
                    collisions.add(new BallCollision(t, ball, balls.get(i)));
                }
            }
	}
        updateBallPositions();
        updateGhostBall();
        
        iter = activeBalls.iterator();        
        while(iter.hasNext()) {
            Iterator<PoolPocket> pocketIterator = pockets.iterator();
            PoolBall ball = iter.next();
            while(pocketIterator.hasNext()) {
                PoolPocket pocket = pocketIterator.next();
                if(pocket.ballIsOver(ball) && !ball.sunk) {
                    ball.doGravity(pocket);
                }                
                if(ball.sunk) {
                    ball.decreaseTransparency();
                }
            }
        }
    }
    
    void updateBallPositions(){
	Iterator<PoolBall> ballIterator;
	PoolCollision collision = collisions.poll();
	double timePassed = 0;
        while(collision != null) {
	    //Advance balls to the point where the collision occurs.
            ballIterator = activeBalls.iterator();
	    while(ballIterator.hasNext()) {
		PoolBall ball = ballIterator.next();
		ball.move(collision.time-timePassed);
                
            }
            timePassed = collision.time;
	    
            collision.doCollision(this);
            collisionsExecuted++;
	    collision = collisions.poll();	    
	}
        boolean ballsMoving = false;
        ballIterator = activeBalls.iterator();
	while(ballIterator.hasNext()) {
	    PoolBall ball = ballIterator.next();
            if(ball.move(1-timePassed)) {
               ballsMoving = true; 
            }
            ball.updateVelocity();            
	}
        if(!ballsMoving && inMotion) {
            inMotion = false;
            ballsStopped();
        }
    }
    
    void updateGhostBall() {
	Iterator<PoolBall> iter;
        double min = Double.POSITIVE_INFINITY;
	iter = activeBalls.iterator();
	ghostBallObjectBall = null;
	while(iter.hasNext()) {
	    PoolBall ball = iter.next();
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
    
    //--------------------COLLISION DETECTION--------------------//
    
    void detectBallCollisions(PoolBall ball, int i, double timePassed) {
        
    }
    
    void detectPolygonCollisions(PoolBall ball, double t) {
        Iterator<PoolPolygon> iter = polygons.iterator();
        while(iter.hasNext()) {
            PoolPolygon p = iter.next();
            p.detectCollisions(ball, collisions, t);
        }
    }
    
    //This version ignores the wall of the previous collision.
    void detectPolygonCollisions(PoolBall ball, double t, WallCollision collision) {
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
    
    void detectPocketCollisions(PoolBall ball, double timePassed) {
	double time;
	Iterator<PoolPocket> pocketItr;
	pocketItr = pockets.iterator();
        while(pocketItr.hasNext()) {
            PoolPocket pocket = pocketItr.next();            
            time = pocket.detectCollisionWith(ball);
            time += timePassed;
            if(time >= timePassed && time < 1) {
                collisions.add(new PocketCollision(ball, time, pocket));
                return;
            }            
	}
    }
    
    //--------------------GRAPHICAL FUNCTIONS--------------------//
        
    Appearance createMatAppear(Color3f dColor, Color3f sColor, float shine) {
        
        Appearance appear = new Appearance();
        Material material = new Material();
        material.setDiffuseColor(dColor);
        material.setSpecularColor(sColor);
        material.setShininess(shine);
        appear.setMaterial(material);        
        return appear;
  }
    
    SpotLight newSpotLight(Bounds bounds, Point3f pos, float spread,
            float concentration) {
        SpotLight sl = new SpotLight();
        sl.setInfluencingBounds(bounds);
        sl.setPosition(pos);
        sl.setSpreadAngle(spread);
        sl.setConcentration(concentration);
        return sl;
    }
    
    void doAim() {        
        if(shootingBall != null && shootingBall.vel.length() == 0) {
            aimLineRA.setVisible(true);
            if(ghostBallObjectBall == null) {
                Vector3f unit = new Vector3f();
                unit.set(aim);
                unit.scale(-1f);
                unit.normalize();
                Point3f start = new Point3f(shootingBall.pos);
                drawPoolPath(unit, start, numberOfAimLines, aimLineGeometry,0);
                ghostBallRA.setVisible(false);
            } else {
                //Set the ghost ball to be visible.
                ghostBallRA.setVisible(true);
                
                //Set the first line to be a line from the shooting ball to the location of the ghost ball.
                aimLineGeometry.setCoordinate(0, new Point3d(shootingBall.pos));
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
                shootingBallUnit.set(unit.y, -unit.x, unit.z);
                Vector3f temp = new Vector3f((float)(shootingBall.pos.x - ghostBallPosition.x),
                                             (float)(shootingBall.pos.y - ghostBallPosition.y),
                                             0.0f);
                float angle = Math.abs(shootingBallUnit.angle(temp));
                if(angle < Math.PI/2) {
                    shootingBallUnit.scale(-1f);
                }               
                
                //Draw the path of the shooting ball.
                start.set(ghostBallPosition);               
                drawPoolPath(shootingBallUnit, start, numberOfAimLines, aimLineGeometry, 1);
                Transform3D transform = new Transform3D();
                transform.setTranslation(ghostBallPosition);
                ghostBallTransformGroup.setTransform(transform);
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
    
    //--------------------ACTIONS--------------------//
    
    public void new9BallRack() {
        cueball.pos.set(-width/4, 0, 0);
        cueball.vel.set(0.0,0.0,0.0);
        cueball.move(0.0);
        aim.x = -1.0;
        aim.y = 0.0;
        doAim();
        cameraController.overheadView();
        ArrayList<PoolBall> solids = new ArrayList();
        Random random = new Random();
        for(int i = 2; i < 9; i++) {
            solids.add(balls.get(i));
        }
        PoolBall item;
        double x = width/5;
        double y = 0;
        double space = .005;
        
        makeActive(balls.get(1), x, y);
        
        
        //SecondRow
        y = -(ballSize + space/2);
        x += ballSize*Math.sqrt(3) + space; 
        item = solids.get(Math.abs(random.nextInt()) % solids.size());
        solids.remove(item);
        makeActive(item,x,y);
        y += 2*ballSize + space;
        item = solids.get(Math.abs(random.nextInt()) % solids.size());
        solids.remove(item);
        makeActive(item,x,y);        
        
        
        //Third row
        x += ballSize*Math.sqrt(3) + space;
        y = -2*(ballSize + space/2);
        item = solids.get(Math.abs(random.nextInt()) % solids.size());
        solids.remove(item);
        makeActive(item,x,y);
        y += 2*ballSize + space;
        item = balls.get(9);
        makeActive(item,x,y);
        y += 2*ballSize + space;
        item = solids.get(Math.abs(random.nextInt()) % solids.size());
        solids.remove(item);
        makeActive(item,x,y);
        
        //Fourth row
        y = -(ballSize + space/2);
        x += ballSize*Math.sqrt(3) + space; 
        item = solids.get(Math.abs(random.nextInt()) % solids.size());
        solids.remove(item);
        makeActive(item,x,y);
        y += 2*ballSize + space;
        item = solids.get(Math.abs(random.nextInt()) % solids.size());
        solids.remove(item);
        makeActive(item,x,y);
        
        //Fifth row
        x += ballSize*Math.sqrt(3) + space;
        makeActive(solids.get(0),x,0);
        
        
    }
    
    public void new8BallRack() {
        cueball.pos.set(-width/4, 0, 0);
        cueball.vel.set(0.0,0.0,0.0);
        cueball.move(0.0);
        aim.x = -1.0;
        aim.y = 0.0;
        doAim();
        cameraController.overheadView();
        
        ArrayList<PoolBall> solids = new ArrayList(), stripes = new ArrayList();
        Random random = new Random();
        for(int i = 1; i < 8; i++) {
            solids.add(balls.get(i));
        }
        for(int i = 9; i < 16; i++) {
            stripes.add(balls.get(i));
        }
        
        if (Math.abs(random.nextInt()) % 2 == 0) {
            ArrayList<PoolBall> temp = solids;
            solids = stripes;
            stripes = temp;
        }
        
        
        double x = width/5;
        double y = 0;
        double space = .005;
        //First Row               
        PoolBall item = solids.get(Math.abs(random.nextInt()) % solids.size());
        solids.remove(item);        
        makeActive(item, x, y);
        
        //SecondRow
        y = -(ballSize + space/2);
        x += ballSize*Math.sqrt(3) + space;                        
        if (Math.abs(random.nextInt()) % 2 == 0) {
            ArrayList<PoolBall> temp = solids;
            solids = stripes;
            stripes = temp;
        }
        item = solids.get(Math.abs(random.nextInt()) % solids.size());
        solids.remove(item);
        makeActive(item,x,y);
        y += 2*ballSize + space;
        item = stripes.get(Math.abs(random.nextInt()) % stripes.size());
        stripes.remove(item);
        makeActive(item, x, y);
        
        //Third Row
        x += ballSize*Math.sqrt(3) + space;
        y = -2*(ballSize + space/2);
        item = stripes.get(Math.abs(random.nextInt()) % stripes.size());
        stripes.remove(item);
        makeActive(item,x,y);
        y += 2*ballSize + space;
        item = balls.get(8);
        makeActive(item,x,y);
        y += 2*ballSize + space;
        item = solids.get(Math.abs(random.nextInt()) % solids.size());
        solids.remove(item);
        makeActive(item,x,y);
        
        
        //Fourth Row
        x += ballSize*Math.sqrt(3) + space;
        y = -3*(ballSize + space/2);
        item = solids.get(Math.abs(random.nextInt()) % solids.size());
        solids.remove(item);
        makeActive(item,x,y);
        y += 2*ballSize + space;
        item = stripes.get(Math.abs(random.nextInt()) % stripes.size());
        stripes.remove(item);
        makeActive(item,x,y);
        y += 2*ballSize + space;
        item = solids.get(Math.abs(random.nextInt()) % solids.size());
        solids.remove(item);
        makeActive(item,x,y);
        y += 2*ballSize + space;
        item = stripes.get(Math.abs(random.nextInt()) % stripes.size());
        stripes.remove(item);
        makeActive(item,x,y);
        
        //FifthRow
        x += ballSize*Math.sqrt(3) + space;
        y = -4*(ballSize + space/2);
        PoolBall last = solids.get(Math.abs(random.nextInt()) % solids.size());
        solids.remove(last);
        
        item = stripes.get(Math.abs(random.nextInt()) % stripes.size());
        stripes.remove(item);
        makeActive(item,x,y);
        y += 2*ballSize + space;        
        item = solids.get(Math.abs(random.nextInt()) % solids.size());
        solids.remove(item);
        makeActive(item,x,y);
        y += 2*ballSize + space;
        
        solids.addAll(stripes);
        item = solids.get(Math.abs(random.nextInt()) % solids.size());
        solids.remove(item);
        makeActive(item,x,y);
        y += 2*ballSize + space;
        item = solids.get(Math.abs(random.nextInt()) % solids.size());
        solids.remove(item);
        makeActive(item,x,y);
        y += 2*ballSize + space;
        
        makeActive(last,x,y);                                
    }                
     
    public void shoot() {
        inMotion = true;
        shootingBall.vel.x = -aim.x * power * powerS;
        shootingBall.vel.y = -aim.y * power * powerS;
        shootingBall.spin.x = -aim.x * spin * spinS;
        shootingBall.spin.y = -aim.y * spin * spinS;
        ghostBallRA.setVisible(false);
        aimLineRA.setVisible(false);
    }
    
    public void setAim(double x, double y) {
        aim.x = x;
        aim.y = y;
    }
    
    public void setAim(Tuple3f v) {
        aim.x = v.x;
        aim.y = v.y;
        //PoolFrame pf = (PoolFrame)this.getParent().getParent().getParent().getParent();
        //pf.angleSlider.setValue((int)(Math.atan(aim.x/aim.y)*pf.aimRange/Math.PI*2));
    }
    
    public void setSpin(double v) {
        spin = v;
    }
    
    public void setPower(double p) {
        power = p;
    }
    
    public void flipSelectionMode() {
        selectionMode = !selectionMode;
    }
    
    public void setSelectionMode(boolean v) {
        selectionMode = v;
    }            
    
    public PoolBall addBall(double x, double y, double s, Appearance appearance) {        
        PoolBall ball = new PoolBall(appearance, ballSize, 0);
        ball.pos.x = x;
        ball.pos.y = y;
        ball.vel.x = 0.0;
        ball.vel.y = 0.0;
        ball.pos.z = 0.0;
        group.addChild(ball.group);
        balls.add(ball);
        activeBalls.add(ball);
        return ball;
    }
    
    public void makeActive(PoolBall ball, double x, double y) {
        ball.sunk = false;
        activeBalls.remove(ball);
        activeBalls.add(ball);
        ball.active = true;
        ball.vel.set(0.0,0.0,0.0);
        ball.spin.set(ball.vel);
        ball.pos.set(x,y,0);
    }
    
    public PoolPolygon addPolygon(double[] xpoints, double[] ypoints, int npoints,
            Color3f c, double ballsize) {
        PoolPolygon poly = new PoolPolygon(xpoints, ypoints, npoints, c, ballsize);
        universe.addBranchGraph(poly.group);
        polygons.add(poly);
        return poly;        
    }
    
    public PoolPolygon addPolygon(double[] xpoints, double[] ypoints, int npoints,
            Appearance app, double ballsize) {
        PoolPolygon poly = new PoolPolygon(xpoints, ypoints, npoints, app, ballsize);
        universe.addBranchGraph(poly.group);
        polygons.add(poly);
        return poly;
    }
    
    public void rewind() {
       Iterator<PoolBall> iter = activeBalls.iterator();
        while(iter.hasNext()) {
	    PoolBall ball = iter.next();
            ball.pos.set(ball.lpos);
            ball.vel.set(ball.lvel);
        }                
    }
    
    //--------------------EVENTS--------------------//    
    
    void ballsStopped() {
        if(cameraController.autoCamera) {
            cameraController.snapToShootingBall();
        }
    }

    //--------------------COMPARATOR INTERFACE--------------------//
    
    @Override
    public int compare(Object a, Object b) {
	double val =  ((PoolCollision)a).time - ((PoolCollision)b).time;
	if(val < 0) {
	    return -1;
	} else if (val > 0) {
	    return 1;
	} else {
	    return 0;
	}
    }

    @Override public int hashCode() {
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
    
    //--------------------HIERARCHY BOUNDS INTERFACE--------------------//
    
    @Override
    public void ancestorResized(HierarchyEvent he) {        
        canvas.setSize(getWidth(), getHeight());
    }
    
    @Override
    public void ancestorMoved(HierarchyEvent he) { }
    
    //--------------------ERROR HANDLING--------------------//
    
    public boolean checkBounds(PoolBall b) {
        return false;
    }
    
    public void checkOverlaps(PoolBall ball) {
        Iterator<PoolBall> ballIterator = activeBalls.iterator();
        while(ballIterator.hasNext()) {
            PoolBall ball2 = ballIterator.next();
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
    
    public void fixOverlap(PoolBall a, PoolBall b) {
        err = true;
        
    }
    
}

class PoolCameraController extends CameraController implements ActionListener {
    PoolPanel pp;
    boolean mouseAim = true, autoCamera = false, transitioning = false;
    float angleVelocity = 0f;
    int keyPressed = 0;
    float distanceVelocity = 0f;
    Vector3f cameraRotationAxis = new Vector3f();
    Vector3f upVecRotationAxis = new Vector3f();
    Vector3f translationalVelocity = new Vector3f();
    Vector3f aVector = new Vector3f();
    Vector3f temp = new Vector3f();
    
    public PoolCameraController(PoolPanel p) {
        super(p.universe, p.canvas);
        pp = p;
        
        //Start timer
	Timer timer = new Timer(30, this);
	timer.start();
    }
    
    public void startTransitionTo(Vector3f center, Vector3f cameraVec, Vector3f up, float distance) {
        
        transitioning = true;
        //Angular velocity.
        cameraRotationAxis.cross(cameraVec, cameraPosition);
        
        //Upvector angular velocity
        upVecRotationAxis.cross(up, upVector);
        upVector.set(up);
        
        //Tranlational Velocity
        /*translationalVelocity.set(cameraTranslation);
        translationalVelocity.scale(-1f);
        translationalVelocity.add(center);
        translationalVelocity.normalize();*/
        
        cameraPos.set(cameraPosition);
        cameraPosition.set(cameraVec);
        cameraPosition.normalize();
        cameraTranslation.set(center);
        cameraDistance = distance;
    }    
    
    @Override
    public void actionPerformed(ActionEvent evt) {
        if(transitioning) {            
            //Camera distance
            if(Math.abs(cameraDistance-camDistance) > PoolSettings.camDistThresh) {
                camDistance += (cameraDistance-camDistance)*.06;
            } else {
                camDistance = cameraDistance;
            }
        
            //Camera angle
            aVector.set(cameraPos);
            float angle = (float) (aVector.angle(cameraPosition)*.06);
            if(Math.abs(angle) < PoolSettings.camAngleThresh) {
                cameraPos.set(cameraPosition);
            } else {
                rotater.setAndRotateInPlace(cameraRotationAxis, -angle, cameraPos);
            }
                        
            //Up Vector
            aVector.set(upVec);
            angle = (float) (aVector.angle(upVector)*.06);
            if(Math.abs(angle) < PoolSettings.camAngleThresh) {
                upVec.set(upVector);
            } else {
                rotater.setAndRotateInPlace(upVecRotationAxis, -angle, upVec);
            }
        
            //Camera Tranlation
            translationalVelocity.set(cameraTrans);
            translationalVelocity.scale(-1f);
            translationalVelocity.add(cameraTranslation);
            translationalVelocity.scale(.06f);
            if(translationalVelocity.length() < PoolSettings.camTransThresh) {
                cameraTrans.set(cameraTranslation);
            } else {
                cameraTrans.add(new Vector3d(translationalVelocity));
            }            
            updateCamera();
            
            //Setup temporary translations into Vector3fs
            aVector.set(cameraPos);
            translationalVelocity.set(cameraTrans);
            
            temp = new Vector3f(upVec);

            //Check to see if the transition is done.
            if(camDistance == cameraDistance                             && 
               cameraPosition.epsilonEquals(aVector, 0f)                 &&
               cameraTranslation.epsilonEquals(translationalVelocity, 0f) &&
               upVector.epsilonEquals(temp, 0f))
                transitioning = false;
        } else {
            switch(keyPressed) {
            case KeyEvent.VK_UP:
                break;
            case KeyEvent.VK_DOWN:
                break;
            case KeyEvent.VK_LEFT:
                angleVelocity -= .001;
                break;
            case KeyEvent.VK_RIGHT:
                angleVelocity += .001;
                break;
            default:
                angleVelocity *=.95;
                break;
            }
            if(Math.abs(angleVelocity) > 0.0001) {
                rotater.setAndRotateInPlace(this.cameraRotationAxis, angleVelocity, cameraPos);
                cameraPosition.set(cameraPos);
                pp.setAim(cameraPosition);
                updateCamera();
            }
        }
        
    }        
    
    @Override public void mouseClicked(MouseEvent me) {
        pp.ballPicker.setShapeLocation(me);
        PickInfo res = pp.ballPicker.pickClosest();
        if(res == null) {
            if(me.getButton() == MouseEvent.BUTTON1) {
                pp.shoot();            
            } else {
                mouseAim = !mouseAim;
            }
        } else {
            Primitive p = (Primitive)res.getNode();
        }
    }
    
    @Override public void mouseDragged(MouseEvent me) {
        if(!autoCamera && !transitioning) {
            super.mouseDragged(me);
        }
    }
    
    @Override public void mouseReleased(MouseEvent me) {
        if(!autoCamera && !transitioning) {
            super.mouseReleased(me);
        }
    }
    
    @Override public void mouseMoved(MouseEvent me) {
        if(mouseAim) {
            Vector3f pos = mouseToXYPlane(me.getX(), me.getY());
            pos.scale(-1f);
            pos.add(new Vector3f(pp.shootingBall.pos));
            pos.normalize();
            pp.setAim(pos);
        }
    }
    
    @Override public void keyPressed(KeyEvent ke) {
        if(!autoCamera) {
            super.keyPressed(ke);
            return;
        }
        if(keyPressed == 0) {
            switch(ke.getKeyCode()) {
                case KeyEvent.VK_UP:
                    keyPressed = KeyEvent.VK_UP;
                    break;
                case KeyEvent.VK_DOWN:
                    keyPressed = KeyEvent.VK_DOWN;
                    break;
                case KeyEvent.VK_LEFT:
                    keyPressed = KeyEvent.VK_LEFT;
                    this.cameraRotationAxis.set(upVector);
                    break;
                case KeyEvent.VK_RIGHT:
                    keyPressed = KeyEvent.VK_RIGHT;
                    this.cameraRotationAxis.set(upVector);
                    break;
                default:
                    return;
            }
        }
            
    }    
    
    @Override public void keyReleased(KeyEvent ke) {
        if(ke.getKeyCode() == keyPressed) {
            keyPressed = 0;
        }
    }

    public void snapToShootingBall() {   
        Vector3f newCamPos = new Vector3f(pp.aim);
        double angle = .3;
        Vector3f aimPerp = new Vector3f();
        aimPerp.x = (float) pp.aim.y;
        aimPerp.y = (float) -pp.aim.x;
        aimPerp.z = (float) pp.aim.z;        
        rotater.setAndRotateInPlace(aimPerp, angle, newCamPos);
        this.startTransitionTo(new Vector3f(pp.shootingBall.pos), newCamPos, new Vector3f(0.0f, 0.0f, 1.0f), 20.0f);
    }
    
    public void overheadView() {
        this.startTransitionTo(new Vector3f(0.0f, 0.0f, 0.0f), 
                               new Vector3f(0.0f, 0.0f, 1.0f),
                               new Vector3f(0.0f, 1.0f, 0.0f),
                               PoolSettings.OverheadDistance);
    }    
}