package pool;

import cameracontrol.CameraController;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.picking.PickResult;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.Timer;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

class PoolMouseController extends CameraController implements ActionListener {
    PoolPanel pp;
    boolean mouseAim = true, autoCamera = false, transitioning = false, selectionMode = false;
    
    float angleVelocity = 0f;
    int keyPressed = 0;
    
    float distanceVelocity = 0f;  
    Vector3f cameraRotationAxis = new Vector3f();
    Vector3f upVecRotationAxis = new Vector3f();
    Vector3f translationalVelocity = new Vector3f();
    Vector3f aVector = new Vector3f();
    Vector3f temp = new Vector3f();
    
    PoolBall ballInHand = null;
    
    public PoolMouseController(PoolPanel p) {
        super(p.universe, p.canvas);
        pp = p;
        
        //Start timer
	Timer timer = new Timer(30, this);
	timer.start();
    }
    
    public void putBallInHand(PoolBall ball) {
        ballInHand = ball;
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
            angle = (float) (aVector.angle(upVector)*.08);
            if(Math.abs(angle) < PoolSettings.camUpVecThresh) {
                upVec.set(upVector);
            } else {
                rotater.setAndRotateInPlace(upVecRotationAxis, -angle, upVec);
                upVec.normalize();
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
            if(camDistance == cameraDistance                              && 
               cameraPosition.epsilonEquals(aVector, 0f)                  &&
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
        if(ballInHand == null) {
            if(selectionMode) {
                pp.pickCanvas.setShapeLocation(me);
                PickResult res = pp.pickCanvas.pickClosest();
                if(res != null) {
                    Primitive obj = (Primitive) res.getNode(PickResult.PRIMITIVE);
                    if(obj == null) {
                        System.out.println("Nothing Picked");
                    } else {
                        if(obj instanceof PoolBall) {
                            putBallInHand((PoolBall)obj);
                        }
                    }
                }
            } else {
                if(me.getButton() == MouseEvent.BUTTON1) {
                    pp.shoot();
                } else {
                    mouseAim = !mouseAim;
                }
            }
        } else {
            if(!pp.checkOverlaps(ballInHand)) {
                pp.makeActive(ballInHand);
                ballInHand = null;
            }        
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
        if(ballInHand != null) {
            ballInHand.pos.set(mouseToXYPlaneLocal(me.getX(), me.getY()));
            return;
        }
        if(mouseAim) {
            Vector3f pos = mouseToXYPlaneLocal(me.getX(), me.getY());
            pos.scale(-1f);
            aVector.set(pp.shootingBall.pos);
            pos.add(aVector);
            pos.normalize();
            pp.setAim(pos);
        }
    }
    
    @Override public void keyPressed(KeyEvent ke) {
        if(ke.getKeyCode() == KeyEvent.VK_SPACE) {
            pp.shoot();
            return;
        }
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