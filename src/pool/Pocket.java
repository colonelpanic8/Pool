package pool;

import com.sun.j3d.utils.geometry.Cylinder;
import java.awt.geom.Point2D;
import javax.media.j3d.*;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3d;

public class Pocket {
    Point2D.Double pos;
    float size;
    TransformGroup transformGroup = new TransformGroup();
    BranchGroup group = new BranchGroup();    
    Cylinder cylinder;
    
    public Pocket(double x, double y, double s, float h, float bs, Appearance ap) {
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
    
    public double detectCollisionWith(Ball ball) {
	return ball.detectCollisionWith(pos, (size - ball.size));
    }    
}