package cameracontrol;

import com.sun.j3d.utils.universe.SimpleUniverse;
import java.awt.Point;
import java.awt.event.*;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Transform3D;
import javax.swing.Timer;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import vector.ChangeBasis3f;
import vector.Rotater3f;

public class CameraController implements MouseMotionListener, MouseListener, KeyListener, ActionListener {
    
    public static final Vector3f zero = new Vector3f(0.0f, 0.0f, 0.0f);

    protected SimpleUniverse universe;
    protected Canvas3D canvas;
    protected Point click = new Point(0,0);
    protected Transform3D transform = new Transform3D();
    protected Vector3f planePosition = new Vector3f();
    protected Vector3f cameraPosition = new Vector3f(0f, 0f, 1f);
    protected Vector3f upVector = new Vector3f(0f, 1f, 0f);
    protected Point3f cameraTranslation = new Point3f();
    protected Vector3d upVec = new Vector3d(upVector);
    protected Point3d cameraPos = new Point3d(cameraPosition);
    protected Point3d cameraTrans = new Point3d();
    protected Rotater3f rotater = new Rotater3f();
    protected float cameraDistance = 42f;
    protected float camDistance = cameraDistance;
    protected ChangeBasis3f changeBasis;
    protected boolean updateCameraPos, rotateUpVector = true;
    
    //User configuration
    protected int leftClickMode = ROTATION, rightClickMode = TRANSLATION, keyPressMode = ZOOM_ROLL;
    protected float zoomSensitivity = 20f;
    protected float translationSensitivity = 6f;
    protected float keyYSensitivity = .05f;
    protected float keyXSensitivity = .05f;        
    
    //Camera motion.
    protected Vector3f cameraRotationAxis = new Vector3f();
    protected Vector3f upVecRotationAxis = new Vector3f();
    protected Vector3f aVector = new Vector3f();
    protected Vector3f temp = new Vector3f();
    
    protected Vector3f translationalVelocity = new Vector3f();
    protected float distanceVelocity = 0f;
    protected float upVecVelocity = 0f;
    protected float angleVelocity = 0f;
    protected int keyPressed = 0;
    protected boolean transitioning = false;
    
    //Speed Constants
    static float camTransThresh = .001f;
    static float camAngleThresh = .001f;
    static float camUpVecThresh = .001f;
    static float camDistThresh = .01f;
    static float transitionSpeed = .06f;
    static int threshFrames = 10;
    
    static float distanceAcc = .05f;
    static float camAngleAcc = .001f;
    static float upVecAcc = .003f;
    static float transAcc = .004f;
    
    //Mode Constants
    protected static final int TRANSLATION = 0;
    protected static final int ZOOM_ROLL = 1;
    protected static final int ROTATION = 2;

    public CameraController(SimpleUniverse u, Canvas3D c) {
        universe = u;
        canvas = c;
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        canvas.addKeyListener(this);
        Timer timer = new Timer(30, this);
        timer.start();
        updateCamera();
    }
    
    public void startTransitionTo(Vector3f center, Vector3f cameraVec, Vector3f up, float distance) {        
        transitioning = true;
        //Angular velocity.
        cameraRotationAxis.cross(cameraVec, cameraPosition);
        
        //Upvector angular velocity
        upVecRotationAxis.cross(up, upVector);
        upVector.set(up);
        
        cameraPos.set(cameraPosition);
        cameraPosition.set(cameraVec);
        cameraPosition.normalize();
        cameraTranslation.set(center);
        cameraDistance = distance;
    }    
    
    @Override
    public void actionPerformed(ActionEvent evt) {
        if(transitioning) {            
            doTransition();
        } else {
            doCameraVelocity();
            
            if(Math.abs(angleVelocity) > camAngleThresh) {
                rotater.setAndRotateInPlace(upVector, angleVelocity, cameraPos);
                cameraPosition.set(cameraPos);                
            } else {
                angleVelocity = 0f;
            }
            
            if(Math.abs(upVecVelocity) > camUpVecThresh) {
                rotater.setAndRotateInPlace(cameraPosition, upVecVelocity, upVec);
                upVector.set(upVec);
            } else {
                upVecVelocity = 0f;
            }
            
            if(Math.abs(distanceVelocity) > camDistThresh) {
                this.camDistance += distanceVelocity;
                this.cameraDistance = camDistance;
            } else {
                distanceVelocity = 0f;
            }
            
            if(translationalVelocity.length() > camTransThresh) {
                cameraTranslation.add(translationalVelocity);
                cameraTrans.set(cameraTranslation);
            } else {
                translationalVelocity.set(zero);
            }            
            updateCamera();
        }        
    }
    
    protected void doCameraVelocity() {
        switch(keyPressMode) {
            case ZOOM_ROLL:
                switch(keyPressed) {
                    case KeyEvent.VK_UP:
                        distanceVelocity -= distanceAcc;
                        break;
                    case KeyEvent.VK_DOWN:
                        distanceVelocity += distanceAcc;
                        break;
                    case KeyEvent.VK_LEFT:
                        upVecVelocity -= upVecAcc;
                        break;
                    case KeyEvent.VK_RIGHT:
                        upVecVelocity += upVecAcc;
                        break;
                    default:                        
                        break;
                }
                break;
            case ROTATION:
                switch(keyPressed) {
                    case KeyEvent.VK_UP:
                        upVecVelocity -= upVecAcc;
                        break;
                    case KeyEvent.VK_DOWN:
                        upVecVelocity += upVecAcc;
                        break;
                    case KeyEvent.VK_LEFT:
                        angleVelocity -= camAngleAcc;
                        break;
                    case KeyEvent.VK_RIGHT:
                        angleVelocity += camAngleAcc;
                        break;
                    default:                        
                        break;
                }
                break;
            case TRANSLATION:
                if(keyPressed != 0) {
                    temp.set(cameraPosition);
                    temp.scale(-1f);
                    aVector.cross(upVector, temp);
                    this.changeBasis.setFrom(aVector, upVector , temp);
                    changeBasis.transform(translationalVelocity);
                    switch(keyPressed) {
                        case KeyEvent.VK_UP:
                            translationalVelocity.y += transAcc;
                            break;
                        case KeyEvent.VK_DOWN:
                            translationalVelocity.y -= transAcc;
                            break;
                        case KeyEvent.VK_LEFT:
                            translationalVelocity.x += transAcc;
                            break;
                        case KeyEvent.VK_RIGHT:
                            translationalVelocity.x -= transAcc;
                            break;
                        default:                        
                            break;
                    }
                    changeBasis.invert();
                    changeBasis.transform(translationalVelocity);
                }
                break;
            default:
                break;
        }
        distanceVelocity *= .95f;
        upVecVelocity *=.95f;
        translationalVelocity.scale(.95f);
        angleVelocity *= .95f;
    }
    
    protected void doTransition() {
        //Camera distance
        if(Math.abs(cameraDistance-camDistance) > camDistThresh) {
            camDistance += (cameraDistance-camDistance)*transitionSpeed;
        } else {
            camDistance = cameraDistance;
        }
        
        //Camera angle
        aVector.set(cameraPos);
        float angle = (float) (aVector.angle(cameraPosition));
        if(Math.abs(angle) < camAngleThresh*threshFrames) {
            if(Math.abs(angle) < camAngleThresh) {
                cameraPos.set(cameraPosition);
            } else {
                rotater.setAndRotateInPlace(cameraRotationAxis, -angle*camAngleThresh/Math.abs(angle), cameraPos);
            }
        } else {
            rotater.setAndRotateInPlace(cameraRotationAxis, -angle*transitionSpeed, cameraPos);
        }
        
        //Up Vector
        aVector.set(upVec);
        angle = (float) (aVector.angle(upVector));
        if(Math.abs(angle) < camUpVecThresh*threshFrames) {
            if(Math.abs(angle) < camUpVecThresh) {
                upVec.set(upVector);
            } else {
                rotater.setAndRotateInPlace(upVecRotationAxis, camUpVecThresh*-angle/Math.abs(angle), upVec);
                upVec.normalize();
            }
        } else {
            rotater.setAndRotateInPlace(upVecRotationAxis, -angle*transitionSpeed, upVec);
        }
        
        //Camera Tranlation
        translationalVelocity.set(cameraTrans);
        translationalVelocity.scale(-1f);
        translationalVelocity.add(cameraTranslation);
        translationalVelocity.scale(transitionSpeed);
        if(translationalVelocity.length() < camTransThresh) {
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
        if(camDistance == cameraDistance                                   && 
                cameraPosition.epsilonEquals(aVector, 0f)                  &&
                cameraTranslation.epsilonEquals(translationalVelocity, 0f) &&
                upVector.epsilonEquals(temp, 0f)) {
            transitioning = false;
            translationalVelocity.set(zero);
        }
        
    }
    
    public Vector3f mouseToXYPlane(int mx, int my) {
        //Calculate the constants
        float x,y;
        float fieldOfView = (float) universe.getViewer().getView().getFieldOfView();
        x = ((float)(mx - canvas.getWidth()/2)/(canvas.getWidth()/2));
        y = ((float)(canvas.getHeight()/2 - my)/(canvas.getWidth()/2));                        
        x *= fieldOfView/2;
        y *= fieldOfView/2;
        x = (float) Math.sin(x);
        y = (float) Math.sin(y);
        float _z = 1 - x * x - y * y;
        float z = (float) (_z > 0 ? Math.sqrt(_z) : 0);
        Vector3f lookDirection = new Vector3f(cameraPosition);
        lookDirection.scale(-1f);
        Vector3f cross = new Vector3f();            
        cross.cross(lookDirection, upVector);
        ChangeBasis3f cb = new ChangeBasis3f(cross, upVector, lookDirection, true);
        Vector3f clickVector = new Vector3f(x,y,z);
        cb.transform(clickVector);
        lookDirection.set(clickVector);        
        
        //Get camera translation
        Vector3f planePos = new Vector3f();
        universe.getViewingPlatform().getViewPlatformTransform().getTransform(transform);
        transform.get(planePos);
        
        //Calculate the maginitude of the ray from the camera position
        //defined by the mouse to the x,y plane to get the x,y values.
        float magnitude = -planePos.z/lookDirection.z;
        lookDirection.scale(magnitude);
        planePos.add(lookDirection);
        return planePos;
    }
    
    protected Vector3f mouseToXYPlaneLocal(int mx, int my) {
        //Calculate the constants
        float x,y;
        float fieldOfView = (float) universe.getViewer().getView().getFieldOfView();
        x = ((float)(mx - canvas.getWidth()/2)/(canvas.getWidth()/2));
        y = ((float)(canvas.getHeight()/2 - my)/(canvas.getWidth()/2));                        
        x *= fieldOfView/2;
        y *= fieldOfView/2;
        x = (float) Math.sin(x);
        y = (float) Math.sin(y);
        float _z = 1 - x * x - y * y;
        float z = (float) (_z > 0 ? Math.sqrt(_z) : 0);
        Vector3f lookDirection = new Vector3f(cameraPosition);
        lookDirection.scale(-1f);
        Vector3f cross = new Vector3f();            
        cross.cross(lookDirection, upVector);
        ChangeBasis3f cb = new ChangeBasis3f(cross, upVector, lookDirection, true);
        Vector3f clickVector = new Vector3f(x,y,z);
        cb.transform(clickVector);
        lookDirection.set(clickVector);        
        
        //Get camera translation
        universe.getViewingPlatform().getViewPlatformTransform().getTransform(transform);
        transform.get(planePosition);
        
        //Calculate the maginitude of the ray from the camera position
        //defined by the mouse to the x,y plane to get the x,y values.
        float magnitude = -planePosition.z/lookDirection.z;
        lookDirection.scale(magnitude);
        planePosition.add(lookDirection);
        return planePosition;
    }
    
    final public void updateCamera() {
        Point3d transCameraPos = new Point3d(cameraPos);
        transCameraPos.scale(camDistance);
        transCameraPos.add(cameraTrans);
        transform.lookAt(transCameraPos, cameraTrans, upVec);
        transform.invert();
        universe.getViewingPlatform().getViewPlatformTransform().setTransform(transform);        
    }
    
    @Override
    public void mouseDragged(MouseEvent me) {
        float x,y;
        x = ((float)(me.getX() - click.x))/(canvas.getWidth()/2);
        y = ((float)(click.y - me.getY()))/(canvas.getHeight()/2);
        camDistance = cameraDistance;
        if(me.getButton() == MouseEvent.BUTTON1) {
            switch(leftClickMode) {
            case ROTATION:
                doRotation(x,y);
                break;
	    case TRANSLATION:            
		doTranslation(x,y);                 
		break;
	    case ZOOM_ROLL:
		doZoomRoll(x,y);                 
		break;
            }            
        } else {
            switch(rightClickMode) {
            case ROTATION:
                doRotation(x,y);
                break;
	    case TRANSLATION:            
		doTranslation(x,y);                 
		break;
	    case ZOOM_ROLL:
		doZoomRoll(x,y);                 
		break;
            }                                    
        }
        //Set the transform
        updateCamera();        
    }
    
    public void doTranslation(float x, float y) {
        Vector3f point = new Vector3f(-x*translationSensitivity,-y*translationSensitivity,0);
        changeBasis.transform(point);
        cameraTrans.set(cameraTranslation);
        cameraTrans.scaleAdd(1f, new Vector3d(point));
        cameraPos.set(cameraPosition);
    }
    
    public void doZoomRoll(float x, float y) {
        camDistance = Math.abs(cameraDistance - y*zoomSensitivity);
        Vector3f axis = new Vector3f();
        axis.set(cameraPosition);
        upVec.set(rotater.setAndRotate(axis, x, upVector));
        cameraPos.set(cameraPosition);
    }
        
    public void doRotation(float x, float y) { 
        //Determine the point z for the current rotation given the click x, y.            
        float _z = 1 - x * x - y * y;
        float z = (float) (_z > 0 ? Math.sqrt(_z) : 0);
        Vector3f point = new Vector3f(x,y,z);
        point.normalize();
        changeBasis.transform(point);
        
        //Get the axis and angle of rotation.
        Vector3f axis = new Vector3f();
        float angle;
        axis.cross(point, cameraPosition);
        angle = point.angle(cameraPosition);       
        axis.normalize();
        
        //Do the rotations
        cameraPos.set(rotater.setAndRotate(axis, angle, cameraPosition));
        if(rotateUpVector) {
            upVec.set(rotater.rotate(upVector));
        }
        updateCameraPos = true;
    }
    
    @Override
    public void mousePressed(MouseEvent me) {
        if(cameraPosition.length() > 1 ) {
            cameraPosition.normalize();
        }
        click.setLocation(me.getX(), me.getY());
        Vector3f sideVector = new Vector3f(), camVec = new Vector3f();
        camVec.normalize(cameraPosition);
        sideVector.cross(upVector, camVec);
        changeBasis = new ChangeBasis3f(sideVector, upVector, camVec,
                      new Vector3f(1.0f, 0.0f, 0.0f),
                      new Vector3f(0.0f, 1.0f, 0.0f), 
                      new Vector3f(0.0f, 0.0f, 1.0f));
       
    }  

    @Override
    public void mouseReleased(MouseEvent me) {
        if(updateCameraPos) {
            cameraPosition.set(cameraPos);
            cameraPosition.normalize();
            updateCameraPos = false;
        }
        upVector.set(upVec);
        cameraTranslation.set(cameraTrans);
        cameraDistance = camDistance;
    }
    
    @Override
    public void keyPressed(KeyEvent ke) {
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
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) { }
    
    @Override
    public void mouseClicked(MouseEvent me) { }
    
    @Override
    public void mouseMoved(MouseEvent me) { }

    @Override
    public void mouseEntered(MouseEvent me) { }

    @Override
    public void mouseExited(MouseEvent me) { }

    @Override
    public void keyTyped(KeyEvent ke) { }    

}