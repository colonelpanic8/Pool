package pool;

import com.sun.j3d.utils.geometry.Cylinder;
import java.awt.geom.Point2D;
import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import unbboolean.j3dbool.BooleanModeller;
import unbboolean.j3dbool.Solid;
import unbboolean.solids.DefaultCoordinates;

public class PoolPocket {
    Point2D.Double pos;
    float size, depth, ballSize;
    TransformGroup transformGroup = new TransformGroup();
    BranchGroup group = new BranchGroup();
    Solid inner = new Solid();
        
    public PoolPocket(double x, double y, double s, float h, float bs, Appearance ap) {
        Cylinder cylinder;
        pos = new Point2D.Double(x,y);
        Matrix3f matrix = new Matrix3f();
        matrix.rotX((float)Math.PI/2);
        Transform3D transform = new Transform3D();
        transform.setTranslation(new Vector3d(pos.x, pos.y, -h/2-bs));
        transform.setRotation(matrix);
	size = (float)s;
        depth = h;
        ballSize = bs;
        cylinder = new Cylinder(size, depth, ap);
        Shape3D topFace = cylinder.getShape(Cylinder.TOP);        
        RenderingAttributes ra = new RenderingAttributes();
        Appearance appearance = new Appearance();
        ra.setVisible(false);
        appearance.setRenderingAttributes(ra);
        topFace.setAppearance(appearance);
        transformGroup.setTransform(transform);
        transformGroup.addChild(cylinder);
        group.addChild(transformGroup);
    }
    
    public PoolPocket(double x, double y, double s, float h, float bs, Color3f color) {
        Solid outer = new Solid(), base = new Solid();
        pos = new Point2D.Double(x,y);
        Matrix3f matrix = new Matrix3f();
        matrix.rotX((float)Math.PI/2);
        Transform3D transform = new Transform3D();
        transform.setTranslation(new Vector3d(pos.x, pos.y, -h/2-bs));
        transform.setRotation(matrix);
	size = (float)s;
        outer.setData(DefaultCoordinates.DEFAULT_CYLINDER_VERTICES,
                      DefaultCoordinates.DEFAULT_CYLINDER_COORDINATES, color);
        Color3f darker = new Color3f(color);
        darker.scale((float).80);
        inner.setData(DefaultCoordinates.DEFAULT_CYLINDER_VERTICES,
                      DefaultCoordinates.DEFAULT_CYLINDER_COORDINATES, darker);
        Color3f darkest = new Color3f(darker);
        darkest.scale((float).65);
        base.setData(DefaultCoordinates.DEFAULT_CYLINDER_VERTICES,
                      DefaultCoordinates.DEFAULT_CYLINDER_COORDINATES, darkest);
        inner.scale(s, 2*h, s);
        outer.scale(s+.1,h,s+.1);
        base.scale(s+.1,.2,s+.1);
        base.translate(0, -h/2+.1);        
        BooleanModeller bm = new BooleanModeller(outer, inner);
        Solid ring = bm.getDifference();      
        bm = new BooleanModeller(ring,base);      
        transformGroup.setTransform(transform);
        transformGroup.addChild(bm.getUnion());
        group.addChild(transformGroup);
    }
    
    public double detectCollisionWith(PoolBall ball) {
        Point2D.Double times = ball.detectCollisionWith(pos, (size - ball.size));
        if(times.x >= 0) {
            Vector3f vector = 
                    new Vector3f((float)-(pos.x - (ball.pos.x + times.x*ball.vel.x)),
                                 (float)-(pos.y - (ball.pos.y + times.x*ball.vel.y)),
                                 0.0f);
            Vector3f velVector = new Vector3f((float)ball.vel.x, (float)ball.vel.y, 0.0f);
            
            if(Math.abs(vector.angle(velVector)) <= Math.PI/2)
                return times.x;
            else if (times.x < 1) {
                times.x = 0;
            }
            
        }
        if(times.y >= 0) {
            Vector3f vector = 
                    new Vector3f((float)-(pos.x - (ball.pos.x + times.y*ball.vel.x)),
                                 (float)-(pos.y - (ball.pos.y + times.y*ball.vel.y)),
                                 0.0f);
            Vector3f velVector = new Vector3f((float)ball.vel.x, (float)ball.vel.y, 0.0f);
            
            if(Math.abs(vector.angle(velVector)) <= Math.PI/2)
                return times.y;
            else if (times.x < 1) {
                times.y = 0;
            }
        }
        return Double.NaN;
    }
    
    public double detectSinkCollisionWith(PoolBall ball) {
        return (-(ballSize + depth + ball.pos.z)/ball.vel.z);
        
    }
    
    public boolean ballIsOver(PoolBall ball) {
        if(pos.distance(ball.pos.x, ball.pos.y) < size) {
            return true;
        }
        return false;
    }
        
}