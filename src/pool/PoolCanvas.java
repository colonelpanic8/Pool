/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pool;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.J3DGraphics2D;

/**
 *
 * @author ivanmalison
 */
public class PoolCanvas extends Canvas3D {    
    PoolPanel poolPanel;  
    SpinController sc;
    
    public PoolCanvas(GraphicsConfiguration g, PoolPanel pp) {
        super(g);
        poolPanel = pp;
        sc = new SpinController(64, 64, 64, pp);
        /*
        URL filename = this.getClass().getResource("/images/SnapIcon.jpg");
        BufferedImage i = null;
        try {
            i = ImageIO.read(filename);
        } catch (IOException ex) {
            Logger.getLogger(PoolCanvas.class.getName()).log(Level.SEVERE, null, ex);
        }
        io = new ImageOverlay(this, new Dimension(64,64), i);
        io.initialize();
        io.setSize(64,64);
        io.setLocation(100, 100);
        io.setVisible(true);*/
    }
    
    public boolean respondToClick(Point p) {
        return sc.respondToClick(p);
    }
    
    @Override
    public void postRender()  {
        
        J3DGraphics2D g = this.getGraphics2D();
        sc.draw(g);
        g.flush(false);
        /*
        g.setColor(Color.white);
        if(poolPanel.cueball != null && false) {
            g.drawString(poolPanel.cueball.spin.toString(), 0, 20);
            g.drawString(poolPanel.cueball.vel.toString(), 0, 40);            
            g.drawString(String.format("%.3f", poolPanel.cueball.spin.z ), 0, 60);
        }
        g.flush(false);
        * 
        */
    }
}

abstract class C3DUIElem {
    public abstract void draw(Graphics g);
    public abstract boolean respondToClick(Point click);
}

class SpinController extends C3DUIElem {
    int radius;
    int xpos, ypos;
    Point click = new Point(0,0);
    PoolPanel poolPanel;
    
    
    public SpinController(int s, int x, int y, PoolPanel pp) {
        radius = s;
        click.setLocation(x, y);
        xpos = x; ypos = y;
        poolPanel = pp;
    }
    
    public void set(int x, int y) {
        click.setLocation(x,y);
        poolPanel.setSpin((double)(click.x-xpos)/radius, (double)-(click.y-ypos)/radius);
    }
    
    public void set(Point p) {
        click.setLocation(p);
        poolPanel.setSpin((double)(click.x-xpos)/radius, (double)-(click.y-ypos)/radius);
    }
    
    @Override
    public void draw(Graphics g) {
        g.setColor(Color.white);
        g.fillOval(xpos-radius, ypos-radius, 2*radius, 2*radius);
        g.setColor(Color.BLACK);
        g.fillOval(xpos - 1, ypos - 1, 2, 2);
        g.setColor(Color.RED);
        g.fillOval(click.x-2, click.y-2, 4, 4);
    }

    @Override
    public boolean respondToClick(Point p) {
        if(p.distance(xpos, ypos) <= radius) {
            set(p);
            return true;
        }
        return false;
    }
}
