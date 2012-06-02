package pool;

import com.sun.j3d.utils.geometry.Cylinder;
import java.awt.geom.Point2D;
import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3d;
import unbboolean.j3dbool.BooleanModeller;
import unbboolean.j3dbool.Solid;
import unbboolean.solids.DefaultCoordinates;

public class Pocket {
    Point2D.Double pos;
    float size;
    TransformGroup transformGroup = new TransformGroup();
    BranchGroup group = new BranchGroup();    
        
    public Pocket(double x, double y, double s, float h, float bs, Appearance ap) {
        Cylinder cylinder;
        pos = new Point2D.Double(x,y);
        Matrix3f matrix = new Matrix3f();
        matrix.rotX((float)Math.PI/2);
        Transform3D transform = new Transform3D();
        transform.setTranslation(new Vector3d(pos.x, pos.y, -h/2-bs));
        transform.setRotation(matrix);
	size = (float)s;
        cylinder = new Cylinder(size, h, ap);
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
    
    public Pocket(double x, double y, double s, float h, float bs, Color3f color) {
        Solid outer = new Solid(), inner = new Solid(), base = new Solid();
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
        inner.scale(s, h, s);
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
    
    public double detectCollisionWith(Ball ball) {
	return ball.detectCollisionWith(pos, (size - ball.size));
    }    
}