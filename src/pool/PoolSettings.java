package pool;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JFrame;

public class PoolSettings extends JFrame implements ActionListener {
    //Values
    static float camTransThresh = .0001f;
    static float camAngleThresh = .0001f;
    static float camDistThresh = .001f;
    static float OverheadDistance = 40f;
    
    JCheckBox enableBallSelection;
    JCheckBox idealRails;
    
    private static PoolSettings ref;    
    
    private PoolSettings() {
        super("Settings and Preferences");
        
    }

    public static PoolSettings getSettings()
    {
        if (ref == null)
            ref = new PoolSettings();		
        return ref;
    }
    
    @Override
    public Object clone()
            throws CloneNotSupportedException {
        throw new CloneNotSupportedException(); 
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        
    }
    
}
