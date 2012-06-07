package pool;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PoolFrame extends JFrame implements ChangeListener, ActionListener {
    PoolPanel poolPanel = new PoolPanel(.3,.35, 25, 13);
    JPanel content = new JPanel();
    JToolBar toolbar = new JToolBar();
        
    int aimRange = 5000;
    int powerRange = 100;
    int spinRange = 100;
    
    //Buttons and components
    JSlider angleSlider = new JSlider(0, aimRange, 0);//Defines angle of shot
    JSlider powerSlider = new JSlider(0, powerRange, powerRange/4);
    JSlider spinSlider = new JSlider(-spinRange, spinRange, 0);
    JButton selectionModeButton = new JButton("Selection Mode");  
    JButton shootButton = new JButton("Shoot");
    JButton makeRackButton = new JButton("Make Rack");
    JButton snapButton;// = new JButton();
    JButton overheadViewButton = new JButton("Overhead");
    
    public PoolFrame(String str) {
        super(str);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        
        snapButton = new JButton(new ImageIcon(toolkit.createImage("images/SnapButton.jpg")));
        //Component Configuration
        powerSlider.setOrientation(JSlider.VERTICAL);                
	        
        //Add listeners
        startListening();
        
        //Add content to frame.
	content.setLayout(new BorderLayout());
	content.add(poolPanel, BorderLayout.CENTER);
	content.add(angleSlider, BorderLayout.SOUTH);
	content.add(powerSlider, BorderLayout.EAST);
        
        //Toolbar setup.
        toolbar.add(snapButton);
        toolbar.add(overheadViewButton);
        toolbar.add(makeRackButton);
	toolbar.add(selectionModeButton);
	toolbar.add(shootButton);
        toolbar.add(spinSlider);
	content.add(toolbar, BorderLayout.NORTH);
        
        //Finalize
        Dimension dim = toolkit.getScreenSize();
	this.setContentPane(content);
	this.setSize(dim.width, dim.height);
	this.setLocation(0,0);
	this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	this.setVisible(true);
        
        //Init proper shooting values
        double val = angleSlider.getValue();
        poolPanel.setAim(Math.cos((val)*2*Math.PI/aimRange), Math.sin((val)*2*Math.PI/aimRange));
        poolPanel.setPower(val/100);
        val = powerSlider.getValue();
        poolPanel.setPower(val/50);
    }
    
    private void startListening() {
        selectionModeButton.addActionListener(this);
	shootButton.addActionListener(this);
        makeRackButton.addActionListener(this);
        snapButton.addActionListener(this);
	powerSlider.addChangeListener(this);
        spinSlider.addChangeListener(this);
        angleSlider.addChangeListener(this);
        overheadViewButton.addActionListener(this);
    }
    
    public void actionPerformed(ActionEvent evt){        
        Object source = evt.getSource();
        if       (source == shootButton)         {
            poolPanel.shoot();
        } else if(source == snapButton)          {
            poolPanel.cameraController.snapToShootingBall();
        } else if(source == selectionModeButton) {
            poolPanel.flipSelectionMode();
        } else if(source == makeRackButton)      {
            poolPanel.new9BallRack();
        } else if(source == overheadViewButton)  {
            poolPanel.cameraController.overheadView();
        }
    }
    
    public void stateChanged(ChangeEvent ce) {
        Object source = ce.getSource();
            
        if       (source == angleSlider) {
            if(ce.getSource() != poolPanel) {
                double val = angleSlider.getValue();
                poolPanel.setAim(Math.cos((val)*2*Math.PI/aimRange), Math.sin((val)*2*Math.PI/aimRange));
            }
        } else if(source == powerSlider) {
            double val = powerSlider.getValue();
            poolPanel.setPower(val/powerRange);
        } else if(source == spinSlider)  {
            double val = spinSlider.getValue();
            poolPanel.setSpin(val/spinRange);
        }        
    }    
    
}