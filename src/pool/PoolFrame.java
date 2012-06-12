package pool;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PoolFrame extends JFrame implements ChangeListener, ActionListener{
    
    PoolPanel poolPanel = PoolPanel.getPoolPanel();    
    JPanel content = new JPanel();
    JToolBar toolbar = new JToolBar();
    SpinController spinController = new SpinController(32, poolPanel);
    
    PoolSettings settings = PoolSettings.getSettings();
    PoolHelp help;
    
    int powerRange = 100;
    int spinRange = 100;
    
    //Buttons and components
    JSlider powerSlider = new JSlider(0, powerRange, powerRange/4);
    JSlider spinSlider = new JSlider(-spinRange, spinRange, 0);
        
    JButton settingsButton = new JButton("Settings");
    JButton helpButton = new JButton("Help");
    
    JButton snapButton;
    ImageIcon snapIcon;
    
    JButton overheadViewButton;
    ImageIcon overheadViewIcon;
    
    JButton makeRackButton;
    ImageIcon makeRackIcon;
    
    JButton selectionModeButton;
    ImageIcon selectionModeIcon;
    ImageIcon stopIcon;
    
    public PoolFrame(String str) {
        URL filename = this.getClass().getResource("/images/Icon.jpg");
        Toolkit toolkit = Toolkit.getDefaultToolkit();        
        this.setIconImage(toolkit.createImage(filename));
                
        //Component Configuration
        powerSlider.setPaintTicks(true);
        powerSlider.setMajorTickSpacing(20);
        powerSlider.setMinorTickSpacing(5);
        powerSlider.setBorder(BorderFactory.createTitledBorder("Power"));                
        
        spinSlider.setPaintTicks(true);
        spinSlider.setSnapToTicks(true);
        spinSlider.setMajorTickSpacing(24);
        spinSlider.setMinorTickSpacing(3);
        spinSlider.setBorder(BorderFactory.createTitledBorder("Spin"));
        
        
        //Button construction.
        filename = this.getClass().getResource("/images/SnapIcon.jpg");
        snapIcon = new ImageIcon(toolkit.createImage(filename));        
        snapButton = new JButton(snapIcon);
        snapButton.setMaximumSize(new Dimension(64,64));
        
        filename = this.getClass().getResource("/images/NewRackIcon.jpg");
        makeRackIcon = new ImageIcon(toolkit.createImage(filename));
        makeRackButton = new JButton(makeRackIcon);
        makeRackButton.setMaximumSize(new Dimension(64,64));
        
        filename = this.getClass().getResource("/images/SelectionModeIcon.jpg");
        selectionModeIcon = new ImageIcon(toolkit.createImage(filename));
        filename = this.getClass().getResource("/images/StopIcon.jpg");
        stopIcon = new ImageIcon(toolkit.createImage(filename));
        selectionModeButton = new JButton(selectionModeIcon);
        selectionModeButton.setMaximumSize(new Dimension(64,64));
        
        filename = this.getClass().getResource("/images/OverheadViewIcon.jpg");
        overheadViewIcon = new ImageIcon(toolkit.createImage(filename));
        overheadViewButton = new JButton(overheadViewIcon);
        overheadViewButton.setMaximumSize(new Dimension(64,64));
	        
        //Add listeners
        startListening();
        
        //Add content to frame.
	content.setLayout(new BorderLayout());
	content.add(poolPanel, BorderLayout.CENTER);	
        
        //Toolbar setup.
        toolbar.add(snapButton);
        toolbar.add(overheadViewButton);
        toolbar.add(makeRackButton);
	toolbar.add(selectionModeButton);
        //toolbar.add(spinSlider);
        toolbar.add(powerSlider);
        toolbar.add(spinController);
        toolbar.add(settingsButton);
        toolbar.add(helpButton);
	content.add(toolbar, BorderLayout.NORTH);
        
        //Finalize
        Dimension dim = toolkit.getScreenSize();
	this.setContentPane(content);
	this.setSize(dim.width, dim.height);
	this.setLocation(0,0);
	this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        this.validate();
	this.setVisible(true);
        
        //Init proper shooting values
        float val = powerSlider.getValue();
        poolPanel.setPower(val/powerRange);
        help = new PoolHelp(this);
    }
    
    private void startListening() {
        selectionModeButton.addActionListener(this);
        makeRackButton.addActionListener(this);
        snapButton.addActionListener(this);
	powerSlider.addChangeListener(this);
        spinSlider.addChangeListener(this);       
        overheadViewButton.addActionListener(this);
        settingsButton.addActionListener(this);
        helpButton.addActionListener(this);
    }
    
    @Override
    public void actionPerformed(ActionEvent evt){        
        Object source = evt.getSource();
        if(source == snapButton)          {
            poolPanel.mouseController.snapToShootingBall();
        } else if(source == selectionModeButton) {
            if(poolPanel.flipSelectionMode()) {
                selectionModeButton.setIcon(stopIcon);
            } else {
                selectionModeButton.setIcon(selectionModeIcon);
            }
        } else if(source == makeRackButton)      {
            poolPanel.new9BallRack();
        } else if(source == overheadViewButton)  {
            poolPanel.mouseController.overheadView();
        } else if(source == settingsButton)      {
            settings.setVisible(true);
        } else if(source == helpButton)          {
            help.setVisible(true);
        }
    }
    
    @Override
    public void stateChanged(ChangeEvent ce) {
        Object source = ce.getSource();
            
        if(source == powerSlider)               {
            double val = powerSlider.getValue();
            poolPanel.setPower(val/powerRange);
        } else if(source == spinSlider)         {
            double val = spinSlider.getValue();
            poolPanel.setSpin(val/spinRange, 0.0);
        }        
    }    
}
class SpinController extends JPanel implements MouseListener {
    int radius;
    Point click = new Point();
    Point aim = new Point(0,0);
    PoolPanel poolPanel;
    
    
    public SpinController(int s, PoolPanel pp) {
        radius = s;
        this.setPreferredSize(new Dimension(2*radius,2*radius));
        this.addMouseListener(this);
        aim.setLocation(radius, radius);
        poolPanel = pp;
    }
    
    
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.white);
        g.fillOval(0, 0, 2*radius, 2*radius);
        g.setColor(Color.BLACK);
        g.fillOval(aim.x-2, aim.y-2, 4, 4);
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        click.setLocation(me.getX(), me.getY());
        if(click.distance(radius, radius) <= radius) {
            aim.setLocation(click);
            this.repaint();
            poolPanel.setSpin((double)(click.x-radius)/radius, (double)-(click.y-radius)/radius);
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {

    }

    @Override
    public void mouseReleased(MouseEvent me) {

    }

    @Override
    public void mouseEntered(MouseEvent me) {

    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

}