package pool;

import java.awt.geom.Point2D;

public class Pocket {
    Point2D.Double pos;
    double size;
    
    public Pocket(double x, double y, double s) {
	pos = new Point2D.Double(x,y);
	size = s;
    }
    
    public double detectCollisionWith(Ball ball) {
	return ball.detectCollisionWith(pos, (size - ball.size));
    }    
}