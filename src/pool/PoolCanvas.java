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
        pp = poolPanel;
    }        
    
    @Override
    public void postRender()  {
        J3DGraphics2D g = this.getGraphics2D();
        g.setColor(Color.white);
        g.flush(false);
    }
}
