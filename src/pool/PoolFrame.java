package pool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PoolFrame {
    public static class AngleListener implements ChangeListener{
	PoolPanel pp;
	JSlider slider;
	public AngleListener(PoolPanel a){
	    pp=a;
	}
	public void stateChanged(ChangeEvent evt){
			slider = (JSlider)evt.getSource();
			double aind = slider.getValue();
			pp.aim.x = Math.cos((aind)*2*Math.PI/5000);
			pp.aim.y = Math.sin((aind)*2*Math.PI/5000);
			pp.repaint();
	}
    }
    
    public static class SpinListener implements ChangeListener {
        PoolPanel pp;
	public SpinListener(PoolPanel a){
	    pp=a;
	}
	public void stateChanged(ChangeEvent evt){
            JSlider slider = (JSlider)evt.getSource();
            pp.spin = (double)(slider.getValue())/1000;
	}
    }
    
    public static class PowerListener implements ChangeListener{
	PoolPanel pp;
	JSlider slider;
	SListener slistener;
	public PowerListener(PoolPanel a, SListener b){
	    pp = a; slistener = b;
	}
	public void stateChanged(ChangeEvent evt){
	    slider = (JSlider)evt.getSource();
	    pp.power = (float)(slider.getValue())/100;
	    
	}
    }
    
    public static class SListener implements ActionListener{
	PoolPanel pp;
	public SListener(PoolPanel a){
	    pp = a;
	}
	public void actionPerformed(ActionEvent evt){
            pp.shoot();
	}
    }
    
    public static class ModeListener implements ActionListener{
	PoolPanel pp;
	boolean enabled;
	public ModeListener(PoolPanel a){
	    pp = a;
	}
	public void actionPerformed(ActionEvent evt){
	    JButton b = (JButton)evt.getSource();
	    pp.selMode = !pp.selMode;
	    if(pp.selMode) {
		b.setLabel("Stop");
	    } else {
		b.setLabel("Selection Mode");
	    }
	}
    }
    
    public static class ShootingModeListener implements ActionListener{
	PoolPanel pp;
	boolean enabled;
	public ShootingModeListener(PoolPanel a){
	    pp = a;
	}
	public void actionPerformed(ActionEvent evt){
	    JButton b = (JButton)evt.getSource();
	    pp.sliderPower = !pp.sliderPower;
	    if(pp.sliderPower) {
		b.setLabel("Slider Mode");
	    } else {
		b.setLabel("Drag Mode");
	    }
	}
    }
    
    public static class SnapListener implements ActionListener{
	PoolPanel pp;
	boolean enabled;
	public SnapListener(PoolPanel a){
	    pp = a;
	}
	public void actionPerformed(ActionEvent evt){
            pp.cameraController.snapToShootingBall();
	}
    }        
    
    public static class MakeRackListener implements ActionListener{
	PoolPanel pp;
	boolean enabled;
	public MakeRackListener(PoolPanel a){
	    pp = a;
	}
	public void actionPerformed(ActionEvent evt){
            pp.newRack();
	}
    }
    
    public static class NBControl implements ActionListener{
	PoolPanel msp;
	JComboBox cc;
	public NBControl(PoolPanel a, JComboBox c){
	    msp = a;
	    cc= c;
	}
	public void actionPerformed(ActionEvent evt){
	    int width = 1;
	    int height = 2;
	    switch (cc.getSelectedIndex()) {
	    case 1:
		msp.addBall(-width, height, 0, 0, msp.ballSize);
		break;
	    case 2:
		msp.addBall(msp.borderSize + msp.railSize, 400, 0, 0, msp.ballSize);
		break;
	    case 3:
		msp.addBall(width, -height, 0, 0, msp.ballSize);
		break;
		
	    default:
		msp.addBall(-width, -height, 0, 0, msp.ballSize);
		break;
	    }
	    msp.repaint();
	}
    }
    public static void main(String[] args) {
	JFrame window = new JFrame("Pool");
	PoolPanel poolpanel = new PoolPanel(.3,.35, 25, 13);
	JPanel content = new JPanel();
	JPanel south = new JPanel();
	
	JComboBox colorChoice = new JComboBox();
	colorChoice.addItem("Red");
	colorChoice.addItem("Blue");
	colorChoice.addItem("Green");
	colorChoice.addItem("Yellow");
	
	JSlider angle = new JSlider(0,5000,0);//Defines angle of shot
	AngleListener alist = new AngleListener(poolpanel);
	angle.addChangeListener(alist);
	
	JButton button = new JButton("New Ball");
	NBControl nbcontrol = new NBControl(poolpanel, colorChoice);
	button.addActionListener(nbcontrol);
	
	JButton selMode = new JButton("Selection Mode");
	ModeListener mode = new ModeListener(poolpanel);
	selMode.addActionListener(mode);
        
        JButton powerMode = new JButton("Slider Mode");
	ShootingModeListener shootingMode = new ShootingModeListener(poolpanel);
	powerMode.addActionListener(shootingMode);
	
	JButton shoot = new JButton("Shoot");
	SListener slistener = new SListener(poolpanel);
	shoot.addActionListener(slistener);
        
        JButton makeRack = new JButton("Make Rack");
	MakeRackListener rackListener = new MakeRackListener(poolpanel);
	makeRack.addActionListener(rackListener);
	
	JSlider power = new JSlider(0,100,50);
	PowerListener plist = new PowerListener(poolpanel, slistener);
	power.addChangeListener(plist);
	power.setOrientation(JSlider.VERTICAL);
        
        JSlider spin = new JSlider(-25,25,0);
        SpinListener spinListener = new SpinListener(poolpanel);
        spin.addChangeListener(spinListener);
        
        JButton snap = new JButton("Snap");
	SnapListener snapListener = new SnapListener(poolpanel);
	snap.addActionListener(snapListener);
        
        
                
	
	
	
	south.setBackground(Color.GRAY);
	content.setLayout(new BorderLayout());
	content.add(poolpanel, BorderLayout.CENTER);
	content.add(angle, BorderLayout.NORTH);
	content.add(power, BorderLayout.EAST);
        south.add(snap);
        south.add(makeRack);
        south.add(powerMode);
	south.add(selMode);
	south.add(button);
	south.add(shoot);
	south.add(colorChoice);
        south.add(spin);
	content.add(south, BorderLayout.SOUTH);
	window.setContentPane(content);
	window.setSize(1200, 700);
	window.setLocation(100,100);
	window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	window.setVisible(true);
    }
}
