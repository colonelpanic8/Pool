/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author ivanmalison
 */
public class PoolApplet extends JApplet {
    public static class AngleListener implements ChangeListener{
		PoolPanel pp;
		JSlider slider;
		public AngleListener(PoolPanel a){
			pp=a;
		}
		public void stateChanged(ChangeEvent evt){
			slider = (JSlider)evt.getSource();
			double aind = slider.getValue();

			pp.repaint();
		}
	}

	public static class PowerListener implements ChangeListener{
		PoolPanel pp;
		JSlider slider;
		SListener slistener;
		public PowerListener(PoolPanel a, SListener b){
			pp=a; slistener=b;
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
			pp.cueball.vel.x = pp.aimer.aim.x*pwr;						//power
			pp.cueball.vel.y = pp.aimer.aim.y*pwr;
			pp.gball = false;
		}
	}
	private static class NBControl implements ActionListener{
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
					msp.balls[msp.numberofballs] = new Ball(Color.BLUE, width, height, 0, 0, 42);
					break;
				case 2:
					msp.balls[msp.numberofballs] = new Ball(Color.GREEN, width, height, 0, 0, 42);
					break;
				case 3:
					msp.balls[msp.numberofballs] = new Ball(Color.BLACK, width, height, -2, 1, 42);
					break;

				default:
					msp.balls[msp.numberofballs] = new Ball(Color.RED, width, height, 0, 0, 42);
					break;
			}
			msp.numberofballs++;
			msp.repaint();
		}
	}
    @Override
	public void init() {
		PoolPanel poolpanel = new PoolPanel();
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
