package pool;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;

public class Pocket {
    Point2D.Double pos;
    double size;
    
    public Pocket(double x, double y, double s) {
	pos = new Point2D.Double(x,y);
	size = s;
    }
    
    public double detectCollisionWith(Ball ball) {
	//return ball.detectCollisionWith(pos, (size - ball.size)/2);
        return 2;
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLACK);
        //g.fillOval(pos.x - size/2, pos.y - size/2, size, size);
    }
}
