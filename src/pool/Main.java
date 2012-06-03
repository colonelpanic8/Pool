package pool;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.*;

public class Main {
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
	PoolFrame.AngleListener alist = new PoolFrame.AngleListener(poolpanel);
	angle.addChangeListener(alist);
	
	JButton button = new JButton("New Ball");
	PoolFrame.NBControl nbcontrol = new PoolFrame.NBControl(poolpanel, colorChoice);
	button.addActionListener(nbcontrol);
	
	JButton selMode = new JButton("Selection Mode");
	PoolFrame.ModeListener mode = new PoolFrame.ModeListener(poolpanel);
	selMode.addActionListener(mode);
        
        JButton powerMode = new JButton("Slider Mode");
	PoolFrame.ShootingModeListener shootingMode = new PoolFrame.ShootingModeListener(poolpanel);
	powerMode.addActionListener(shootingMode);
	
	JButton shoot = new JButton("Shoot");
	PoolFrame.SListener slistener = new PoolFrame.SListener(poolpanel);
	shoot.addActionListener(slistener);
        
        JButton makeRack = new JButton("Make Rack");
	PoolFrame.MakeRackListener rackListener = new PoolFrame.MakeRackListener(poolpanel);
	makeRack.addActionListener(rackListener);
	
	JSlider power = new JSlider(0,100,50);
	PoolFrame.PowerListener plist = new PoolFrame.PowerListener(poolpanel, slistener);
	power.addChangeListener(plist);
	power.setOrientation(JSlider.VERTICAL);
        
        JSlider spin = new JSlider(-25,25,0);
        PoolFrame.SpinListener spinListener = new PoolFrame.SpinListener(poolpanel);
        spin.addChangeListener(spinListener);
        
        JButton snap = new JButton("Snap");
	PoolFrame.SnapListener snapListener = new PoolFrame.SnapListener(poolpanel);
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
