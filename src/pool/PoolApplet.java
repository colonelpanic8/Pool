/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pool;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.*;
import pool.PoolFrame.AngleListener;
import pool.PoolFrame.PowerListener;
import pool.PoolFrame.SListener;
import pool.PoolFrame.NBControl;

/**
 *
 * @author ivanmalison
 */
public class PoolApplet extends JApplet {
 
    @Override
	public void init() {
		PoolPanel poolpanel = new PoolPanel(45,20, 40, 40);
		JPanel content = new JPanel();
		JPanel south = new JPanel();

		JComboBox colorChoice = new JComboBox();
		colorChoice.addItem("Red");
		colorChoice.addItem("Blue");
		colorChoice.addItem("Green");
		colorChoice.addItem("Black");

		JSlider angle = new JSlider(0,2000,0);//Defines angle of shot
		AngleListener alist = new AngleListener(poolpanel);
		angle.addChangeListener(alist);

		JButton button = new JButton("New Ball");
                NBControl nbcontrol = new NBControl(poolpanel, colorChoice);
		button.addActionListener(nbcontrol);

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
		south.add(button);
		south.add(shoot);
		south.add(colorChoice);
		content.add(south, BorderLayout.SOUTH);
		setContentPane(content);
		setSize(840, 670);
		setVisible(true);
	}
}
