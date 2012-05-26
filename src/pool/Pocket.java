package pool;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

public class Pocket {
    Point pos;
    int size;
    
    public Pocket(int x, int y, int s) {
	pos = new Point(x,y);
	size = s;
    }
    
    public double detectCollisionWith(Ball ball) {
	return ball.detectCollisionWith(pos, (size - ball.size)/2);	
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLACK);
	g.fillOval(pos.x - size/2, pos.y - size/2, size, size);
    }
}
