package pool;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.border.BevelBorder;

public class PoolHelp extends JFrame {
        
    public PoolHelp(PoolFrame pf) {
        super("Help");
        this.setSize(new Dimension(300,300));
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        validate();
        
        JLabel aimInfo = new JLabel("<html>The user can aim in three ways: "
                + "by moving the mouse, "
                + "by adjusting the slider at the bottom of the screen, "
                + "and by using the arrow keys when in auto camera mode.</html>", 0);
        aimInfo.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        
        JLabel snapInfo = new JLabel("<html>The snap button rotates and"
                + " translates the camera so that it is focused on the "
                + "current shooting ball at a 30 degree angle above the "
                + "table in the current aiming direction.</html>",
                                     pf.snapIcon , 0);
        snapInfo.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        
        JLabel overheadInfo = new JLabel("<html>The overhead view button rotates"
                + "and translates the camera so that the user has an overhead"
                + "view of the pool table</html>", 0);
        overheadInfo.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        
        
        content.add(aimInfo);
        content.add(Box.createRigidArea(new Dimension(10, 0)));
        content.add(snapInfo);
        content.add(Box.createRigidArea(new Dimension(10, 0)));
        content.add(overheadInfo);
        this.add(content);
    }    
}
