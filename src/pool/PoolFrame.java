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
			pp.aimer.aim.x = Math.cos((aind)*2*Math.PI/2000);
			pp.aimer.aim.y = Math.sin((aind)*2*Math.PI/2000);
			pp.repaint();
	}
    }
    
    public static class PowerListener implements ChangeListener{
	PoolPanel pp;
	JSlider slider;
	SListener slistener;
	public PowerListener(PoolPanel a, SListener b){
	    pp = a; slistener=b;
	}
	public void stateChanged(ChangeEvent evt){
	    slider = (JSlider)evt.getSource();
	    slistener.pwr = (slider.getValue())/2.6;
	    
	}
    }
    
    
    public static class SListener implements ActionListener{
	PoolPanel pp;
	double pwr;
	public SListener(PoolPanel a){
	    pp = a; pwr = 15;
	}
	public void actionPerformed(ActionEvent evt){
	    pp.cueball.vel.x = -pp.aimer.aim.x*pwr;
	    pp.cueball.vel.y = -pp.aimer.aim.y*pwr;
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
    public static class NBControl implements ActionListener{
	PoolPanel msp;
	JComboBox cc;
	public NBControl(PoolPanel a, JComboBox c){
	    msp = a;
	    cc= c;
	}
	public void actionPerformed(ActionEvent evt){
	    int width = msp.getWidth()/2 - 20;
	    int height = msp.getHeight()/2 - 20;
	    switch (cc.getSelectedIndex()) {
	    case 1:
		msp.balls.add(new Ball(Color.BLUE, width, height, 2, 2, 42));
		break;
	    case 2:
		msp.balls.add(new Ball(Color.GREEN, width, height, -1, 1, 42));
		break;
	    case 3:
		msp.balls.add(new Ball(Color.yellow, width, height, -2, 1, 42));
		break;
		
	    default:
		msp.balls.add(new Ball(Color.RED, width, height, -1, -1, 42));
		break;
	    }
	    msp.numberofballs++;
	    msp.repaint();
	}
    }
    public static void main(String[] args) {
	JFrame window = new JFrame("Pool");
	PoolPanel poolpanel = new PoolPanel();
	JPanel content = new JPanel();
	JPanel south = new JPanel();
	
	JComboBox colorChoice = new JComboBox();
	colorChoice.addItem("Red");
	colorChoice.addItem("Blue");
	colorChoice.addItem("Green");
	colorChoice.addItem("Yellow");
	
	JSlider angle = new JSlider(0,2000,0);//Defines angle of shot
	AngleListener alist = new AngleListener(poolpanel);
	angle.addChangeListener(alist);
	
	JButton button = new JButton("New Ball");
	NBControl nbcontrol = new NBControl(poolpanel, colorChoice);
	button.addActionListener(nbcontrol);
	
	JButton selMode = new JButton("Selection Mode");
	ModeListener mode = new ModeListener(poolpanel);
	selMode.addActionListener(mode);
	
	JButton shoot = new JButton("Shoot");
	SListener slistener = new SListener(poolpanel);
	shoot.addActionListener(slistener);
	
	JSlider power = new JSlider(0,100,50);
	PowerListener plist = new PowerListener(poolpanel, slistener);//unmodded
	power.addChangeListener(plist);
	power.setOrientation(JSlider.VERTICAL);
	
	
	
	south.setBackground(Color.GRAY);
	content.setLayout(new BorderLayout());
	content.add(poolpanel, BorderLayout.CENTER);
	content.add(angle, BorderLayout.NORTH);
	content.add(power, BorderLayout.EAST);
	south.add(selMode);
	south.add(button);
	south.add(shoot);
	south.add(colorChoice);
	content.add(south, BorderLayout.SOUTH);
	window.setContentPane(content);
	window.setSize(1200, 700);
	window.setLocation(100,100);
	window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	window.setVisible(true);
    }
}
