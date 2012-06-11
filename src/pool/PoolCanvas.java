/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pool;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.J3DGraphics2D;

/**
 *
 * @author ivanmalison
 */
public class PoolCanvas extends Canvas3D {    
    PoolPanel poolPanel;
    
    public PoolCanvas(GraphicsConfiguration g, PoolPanel pp) {
        super(g);
        poolPanel = pp;
    }        
    
    @Override
    public void postRender()  {
        J3DGraphics2D g = this.getGraphics2D();
        g.setColor(Color.white);
        if(poolPanel.cueball != null) {
            g.drawString(poolPanel.cueball.spin.toString(), 0, 20);
            g.drawString(poolPanel.cueball.vel.toString(), 0, 40);
            g.drawString(String.format("%.3f", poolPanel.cueball.spin.z ), 0, 60);
        }
        g.flush(false);
    }
}
