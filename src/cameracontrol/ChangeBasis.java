package cameracontrol;

import javax.vecmath.Matrix3f;
import javax.vecmath.Tuple3f;

public class ChangeBasis extends Matrix3f {
    
    public ChangeBasis(Tuple3f a, Tuple3f b, Tuple3f c,
		       Tuple3f x, Tuple3f y, Tuple3f z) {
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
    
    public ChangeBasis(Tuple3f x, Tuple3f y, Tuple3f z) {
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
