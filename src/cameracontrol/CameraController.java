package cameracontrol;

import com.sun.j3d.utils.universe.SimpleUniverse;
import java.awt.Canvas;
import java.awt.Point;
import java.awt.event.*;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

public class CameraController implements MouseMotionListener, MouseListener, KeyListener {

    protected SimpleUniverse universe;
    protected Canvas canvas;
    protected Point click = new Point(0,0);
    protected Transform3D transform = new Transform3D();
    protected Vector3f cameraPosition = new Vector3f(0f, 0f, 1f);
    protected Vector3f upVector = new Vector3f(0f, 1f, 0f);
    protected Point3f cameraTranslation = new Point3f();
    protected Vector3d upVec = new Vector3d(upVector);
    protected Point3d cameraPos = new Point3d(cameraPosition);
    protected Point3d cameraTrans = new Point3d();
    protected Rotater3f rotater = new Rotater3f();
    protected float cameraDistance = 40f;
    protected float camDistance = cameraDistance;
    protected ChangeBasis changeBasis;
    protected boolean updateCameraPos;
    
    //User configuration
    protected int leftClickMode = ROTATION, rightClickMode = TRANSLATION, keyPressMode = ZOOM_ROLL;
    protected float zoomSensitivity = 20f;
    protected float translationSensitivity = 4f;
    protected float keyYSensitivity = .05f;
    protected float keyXSensitivity = .05f;
    
    //Constants
    static final int TRANSLATION = 0;
    static final int ZOOM_ROLL = 1;
    static final int ROTATION = 2;    

    public CameraController(SimpleUniverse u, Canvas c) {
        universe = u;
        canvas = c;
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        canvas.addKeyListener(this); 
        updateCamera();
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
        ChangeBasis cb = new ChangeBasis(cross, upVector, lookDirection);
        cb.invert();
        Vector3f clickVector = new Vector3f(x,y,z);
        cb.transform(clickVector);
        lookDirection.set(clickVector);        
        
        //Get camera translation
        Vector3f planePosition = new Vector3f();
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
        upVec.set(rotater.rotate(upVector));        
        updateCameraPos = true;
    }
    
    public void mousePressed(MouseEvent me) {
        if(cameraPosition.length() > 1 ) {
            cameraPosition.normalize();
        }
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
        if(updateCameraPos) {
            cameraPosition.set(cameraPos);
            cameraPosition.normalize();
            updateCameraPos = false;
        }
        upVector.set(upVec);
        cameraTranslation.set(cameraTrans);
        cameraDistance = camDistance;
    }
    
    public void keyPressed(KeyEvent ke) {
        float x = keyXSensitivity, y = keyYSensitivity;
        switch(ke.getKeyCode()) {
            case KeyEvent.VK_UP:
                x *= 0;
                y *= 1;
                break;
            case KeyEvent.VK_DOWN:
                x *= 0;
                y *= -1;
                break;
            case KeyEvent.VK_LEFT:
                x *= 1;
                y *= 0;
                break;
            case KeyEvent.VK_RIGHT:
                x *= -1;
                y *= 0;
                break;
            default:
                return;
        }
        switch(keyPressMode) {
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
        updateCamera();
        mouseReleased(null);
    }

    public void keyReleased(KeyEvent ke) { }
    
    public void mouseClicked(MouseEvent me) { }
    
    public void mouseMoved(MouseEvent me) { }

    public void mouseEntered(MouseEvent me) { }

    public void mouseExited(MouseEvent me) { }

    public void keyTyped(KeyEvent ke) { }

}