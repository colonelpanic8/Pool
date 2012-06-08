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
    PoolSettings settings = PoolSettings.getSettings();
        
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
    JButton snapButton;
    JButton settingsButton = new JButton("Settings");
    JButton overheadViewButton = new JButton("Overhead");
    
    public PoolFrame(String str) {
        super(str);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
                
        //Component Configuration
        powerSlider.setOrientation(JSlider.VERTICAL);
        powerSlider.setPaintTicks(true);
        powerSlider.setMajorTickSpacing(20);
        powerSlider.setMinorTickSpacing(5);
        powerSlider.setBorder(BorderFactory.createTitledBorder("Power"));
        
        angleSlider.setBorder(BorderFactory.createTitledBorder("Angle"));
        
        spinSlider.setPaintTicks(true);
        spinSlider.setSnapToTicks(true);
        spinSlider.setMajorTickSpacing(24);
        spinSlider.setMinorTickSpacing(3);
        spinSlider.setBorder(BorderFactory.createTitledBorder("Spin"));
        
        
        snapButton = new JButton(new ImageIcon(toolkit.createImage("images/SnapButton.jpg")));        
	        
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
        toolbar.add(settingsButton);
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
        val = powerSlider.getValue();
        poolPanel.setPower(1.5);
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
        settingsButton.addActionListener(this);
    }
    
    @Override
    public void actionPerformed(ActionEvent evt){        
        Object source = evt.getSource();
        if       (source == shootButton)         {
            poolPanel.shoot();
        } else if(source == snapButton)          {
            poolPanel.mouseController.snapToShootingBall();
        } else if(source == selectionModeButton) {
            poolPanel.flipSelectionMode();
        } else if(source == makeRackButton)      {
            poolPanel.new9BallRack();
        } else if(source == overheadViewButton)  {
            poolPanel.mouseController.overheadView();
        }  else if(source == settingsButton)     {
            settings.setVisible(true);
        }
    }
    
    @Override
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