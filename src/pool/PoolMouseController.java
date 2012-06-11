package pool;

import cameracontrol.CameraController;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.picking.PickResult;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.vecmath.Vector3f;

class PoolMouseController extends CameraController{
    PoolPanel pp;
    boolean mouseAim = true, autoCamera = false,
            selectionMode = false;    
    PoolBall ballInHand = null;
    
    public PoolMouseController(PoolPanel p) {
        super(p.universe, p.canvas);
        pp = p;
        //Start timer
    }
    
    public void putBallInHand(PoolBall ball) {
        ballInHand = ball;
    }
    
    @Override
    protected void doCameraVelocity() {
        if(autoCamera) {
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
            pp.setAim(cameraPosition);
        } else {
            super.doCameraVelocity();
        }
    }

    
    @Override public void mouseClicked(MouseEvent me) {        
        if(ballInHand == null) {
            if(selectionMode) {
                pp.pickCanvas.setShapeLocation(me);
                PickResult[] res = pp.pickCanvas.pickAllSorted();
                int i;
                for(i = 0; i < res.length; i++) {
                    if(res[i].getNode(PickResult.PRIMITIVE) instanceof PoolBall) {
                        break;
                    }
                }
                if(i == res.length) {
                    return;
                }
                
                if(res[i] != null) {
                    Primitive obj = (Primitive) res[i].getNode(PickResult.PRIMITIVE);
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