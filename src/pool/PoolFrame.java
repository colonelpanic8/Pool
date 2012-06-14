package pool;


import com.jogamp.opengl.util.Animator;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PoolFrame extends JFrame implements ChangeListener, ActionListener, GLEventListener {
    
    //JOGL Stuff
    GLU glu = new GLU();
    static GLCanvas canvas = new GLCanvas();
    Animator animator = new Animator();
    
    PoolSimulation simulation = PoolSimulation.getPoolSimulation();    
    JPanel content = new JPanel();
    JToolBar toolbar = new JToolBar();
    
    PoolSettings settings = PoolSettings.getSettings();
    PoolHelp help;
    
    int powerRange = 100;
    int spinRange = 100;
    
    //Buttons and components
    JSlider powerSlider = new JSlider(0, powerRange, powerRange/4);
    JSlider spinSlider = new JSlider(-spinRange, spinRange, 0);
        
    JButton settingsButton = new JButton("Settings");
    JButton helpButton = new JButton("Help");
    JButton undoButton = new JButton("Undo");
    
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
	content.add(canvas, BorderLayout.CENTER);	
        
        //Toolbar setup.
        toolbar.add(snapButton);
        toolbar.add(overheadViewButton);
        toolbar.add(makeRackButton);
	toolbar.add(selectionModeButton);
        //toolbar.add(spinSlider);
        toolbar.add(powerSlider);
        //toolbar.add(spinController);
        toolbar.add(undoButton);
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
        simulation.setPower(val/powerRange);
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
        undoButton.addActionListener(this);
    }
    
    @Override
    public void actionPerformed(ActionEvent evt){        
        Object source = evt.getSource();
        if(source == snapButton)          {
            simulation.mouseController.snapToShootingBall();
        } else if(source == selectionModeButton) {
            if(simulation.flipSelectionMode()) {
                selectionModeButton.setIcon(stopIcon);
            } else {
                selectionModeButton.setIcon(selectionModeIcon);
            }
        } else if(source == makeRackButton)      {
            simulation.new9BallRack();
        } else if(source == overheadViewButton)  {
            simulation.mouseController.overheadView();
        } else if(source == settingsButton)      {
            settings.setVisible(true);
        } else if(source == helpButton)          {
            help.setVisible(true);
        } else if(source == undoButton) {
            simulation.rewind();
        }
    }
    
    @Override
    public void stateChanged(ChangeEvent ce) {
        Object source = ce.getSource();
            
        if(source == powerSlider)               {
            double val = powerSlider.getValue();
            simulation.setPower(val/powerRange);
        } else if(source == spinSlider)         {
            double val = spinSlider.getValue();
            simulation.setSpin(val/spinRange, 0.0);
        }        
    }

    @Override
    public void init(GLAutoDrawable glad) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void dispose(GLAutoDrawable glad) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void display(GLAutoDrawable glad) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reshape(GLAutoDrawable glad, int i, int i1, int i2, int i3) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}