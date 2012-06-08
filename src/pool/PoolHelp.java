package pool;

import java.awt.Dimension;
import javax.swing.*;
import javax.swing.border.BevelBorder;

public class PoolHelp extends JFrame {
        
    public PoolHelp(PoolFrame pf) {
        super("Help");
        this.setSize(new Dimension(700,500));
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        validate();
        
        JLabel aimInfo = new JLabel("<html>The user can aim in three ways: "
                + "by moving the mouse, "
                + "by adjusting the slider at the bottom of the screen, "
                + "and by using the arrow keys when in auto camera mode.</html>", 0);
        aimInfo.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        
        JLabel cameraInfo = new JLabel("<html>In the default camera mode, the"
                + " user can rotate the camera by dragging with a left click,"
                + " translate the camera by dragging with a right click,"
                + " rotate the notion up (control the roll) of the camera with the"
                + " left and right arrow keys, and control the camera zoom"
                + " with the up and down arrow keys.</html>", 0);
        cameraInfo.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        
        JLabel sliderInfo = new JLabel("<html>The slider on the right side of the window is "
                + "used to control the amount of power that will be imparted on"
                + " the cueball when it is shot, while the slider on the top of"
                + " the screen is used to control the amount of spin imparted"
                + " on the ball.</html>", 0);
        sliderInfo.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        
        JLabel selectionInfo = new JLabel("<html>The selection mode button allows"
                + " the user to pick up and place balls anywhere on the table."
                + " This include the inactive balls that hover above the table.</html>", 0);
        selectionInfo.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        
        JLabel makeRackInfo = new JLabel("<html>The make rack button makes a new rack"
                + "of balls at the foot of the table and places the shooting ball in"
                + "the kitchen. The kind of rack that is created can be controlled"
                + "by changing the game type setting in the settings window.</html>", 0);
        makeRackInfo.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        
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
        
        int space = 10;
        
        content.add(aimInfo);
        content.add(Box.createRigidArea(new Dimension(0, space)));
        content.add(cameraInfo);
        content.add(Box.createRigidArea(new Dimension(0, space)));
        content.add(sliderInfo);
        content.add(Box.createRigidArea(new Dimension(0, space)));
        content.add(selectionInfo);
        content.add(Box.createRigidArea(new Dimension(0, space)));
        content.add(makeRackInfo);
        content.add(Box.createRigidArea(new Dimension(0, space)));
        content.add(snapInfo);
        content.add(Box.createRigidArea(new Dimension(0, space)));
        content.add(overheadInfo);
        this.add(content);
    }    
}
