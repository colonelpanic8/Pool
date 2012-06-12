/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.J3DGraphics2D;
import org.j3d.geom.overlay.ImageOverlay;

/**
 *
 * @author ivanmalison
 */
public class PoolCanvas extends Canvas3D {    
    PoolPanel poolPanel;
    ImageOverlay io;
    SpinControl spinControl;
    
    public PoolCanvas(GraphicsConfiguration g, PoolPanel pp) {
        super(g);
        poolPanel = pp;
        spinControl = new SpinControl(64, 64, 64);
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
    
    @Override
    public void postRender()  {
        /*
        J3DGraphics2D g = this.getGraphics2D();
        spinControl.draw(g);
        g.flush(false);*/
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

class SpinControl {
    int radius;
    int xpos, ypos;
    
    public SpinControl(int s, int x, int y) {
        radius = s;
        xpos = x;
        ypos = y;
    }
    
    
    public void draw(J3DGraphics2D g) {
        g.setColor(Color.white);
        //g.drawImage(img1, new AffineTransform(1f,0f,0f,1f,x,y), null);
        g.fillOval(xpos - radius, ypos-radius, 2*radius, 2*radius);
    }
}
