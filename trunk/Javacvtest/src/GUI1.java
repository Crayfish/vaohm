import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class GUI1 extends JFrame implements ActionListener {
	/* GUI Components */
	private JMenuBar menuBar = new JMenuBar();
	private JMenu mnFile = new JMenu("File");
	private JMenuItem mntmOpenFromFile = new JMenuItem("Open from file...");
	private JMenuItem mntmSquash1 = new JMenuItem("Squash1.avi");
	private JPanel vpanel = new JPanel();
	private ImgPanel imagePanel = new ImgPanel();
	private JPanel settingsPanel = new JPanel();
	private JLabel blobs = new JLabel("Blobs: ");
	private JLabel time = new JLabel(": ");

	private Videoplayer vplayer;
	private ImageProcessor processor;
	private boolean playing = false;

	public GUI1() {
		super("Motion Tracking");

		this.setPreferredSize(new Dimension(800, 500));
		this.setLocation(
				(Toolkit.getDefaultToolkit().getScreenSize().width - 800) / 2,
				(Toolkit.getDefaultToolkit().getScreenSize().height - 700) / 2);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		init();

		setVisible(true);
		pack();

	}

	/***
	 * Initialize GUI Components
	 */
	private void init() {

		/* Menu issues */
		setJMenuBar(menuBar);
		menuBar.add(mnFile);

		mnFile.add(mntmOpenFromFile);
		mntmOpenFromFile.addActionListener(this);
		mntmOpenFromFile.setActionCommand("open");

		mnFile.add(mntmSquash1);
		mntmSquash1.addActionListener(this);
		mntmSquash1.setActionCommand("squash1");

		/* Layout issues */
		vplayer = new Videoplayer(imagePanel, processor = new ImageProcessor());
		getContentPane().setLayout(new FlowLayout());
		vpanel.setPreferredSize(new Dimension(384, 315));
		getContentPane().add("North", vpanel);
		vpanel.add(vplayer);

		imagePanel.setPreferredSize(new Dimension(384, 288));
		getContentPane().add("North", imagePanel);

		getContentPane().add("North", settingsPanel);
		// settingsPanel.add(threshold);
		// settingsPanel.add(thresholdSldr);
		// settingsPanel.add(thresholdValue);
		// settingsPanel.add(blobs);
		settingsPanel.add(time);

	}

	/**
	 * 
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("open")) {

			try {
				if (vplayer.openURL(null)) {
					pack();
					playing = true;
				}
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(this,
						"Cannot open the video file.");
				// e1.printStackTrace();
			}

		}
		if (e.getActionCommand().equals("squash1")) {

			try {
				if (vplayer
						.openURL(new URL(
								"file:/C:/Users/Márk/Documents/UNI/Visual Analisis of Human Motion/squash1.avi"))) {
					pack();
					playing = true;
				}
			} catch (Exception e1) {
				// e1.printStackTrace();
				JOptionPane.showMessageDialog(this,
						"Cannot open the video file.");
			}

		}

	}

	public void paint(Graphics g) {
		super.paint(g);
		if (playing)

			repaint();
	}
}
