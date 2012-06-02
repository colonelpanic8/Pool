package pool;

import com.sun.j3d.utils.universe.SimpleUniverse;
import java.awt.Canvas;
import java.awt.Point;
import java.awt.event.*;
import javax.media.j3d.Transform3D;
import javax.vecmath.*;

class CameraController implements MouseMotionListener, MouseListener, KeyListener {

    SimpleUniverse universe;
    Canvas canvas;
    Point click = new Point(0,0);
    Transform3D transform = new Transform3D();
    Vector3f cameraPosition = new Vector3f(0f, 0f, 1f);
    Vector3f upVector = new Vector3f(0f, 1f, 0f);
    Point3f cameraTranslation = new Point3f();
    Vector3d upVec = new Vector3d(upVector);
    Point3d cameraPos = new Point3d(cameraPosition);
    Point3d cameraTrans = new Point3d();
    Quat4f rotation = new Quat4f(), inverse = new Quat4f(), vector = new Quat4f();
    float cameraDistance = 34f;
    float camDistance = cameraDistance;
    ChangeBasis changeBasis;
    boolean updateCameraPos;
    
    //User configuration
    int leftClickMode = ROTATION, rightClickMode = TRANSLATION, keyPressMode = ZOOM_ROLL;
    float zoomSensitivity = 20f;
    float translationSensitivity = 4f;
    float keyYSensitivity = .05f;
    float keyXSensitivity = .05f;
    
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
    
    final public void updateCamera() {
        cameraPos.scale(camDistance);
        Point3d transCameraPos = new Point3d(cameraPos);
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
        float angle = x;
        axis.scale((float)Math.sin(angle/2));
        rotation.set(axis.x,
                axis.y,
                axis.z, 
                (float)Math.cos(angle/2));
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
        
        //Calculate the quarternion that represents this rotation and its inverse.
        axis.scale((float)Math.sin(angle/2));
        rotation.set(axis.x,
		     axis.y,
		     axis.z, 
		     (float)Math.cos(angle/2));
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