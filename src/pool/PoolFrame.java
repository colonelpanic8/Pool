package pool;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JSlider;
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
}
