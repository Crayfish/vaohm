import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Graphical user Interface
 * 
 * @author Mark Ormos, Thomas Mayr
 * 
 */
public class GUI1 extends JFrame implements ActionListener {
	/* GUI Components */
	private JMenuBar menuBar = new JMenuBar();
	private JMenu mnFile = new JMenu("File");
	private JMenuItem mntmOpenFromFile = new JMenuItem("Open from file...");
	private JMenuItem mntmSquash1 = new JMenuItem("Squash1.avi");
	private JMenuItem mnSave = new JMenuItem("Show data");
	private JPanel vpanel = new JPanel();
	private ImgPanel imagePanel = new ImgPanel("output");
	private ImgPanel ipBlobs = new ImgPanel("blob view");
	private ImgPanel ipCollide = new ImgPanel("collision view");

	private Videoplayer vplayer;
	private ImageProcessor processor;
	private boolean playing = false;
	private List<Data> dataCollector = new LinkedList<Data>();

	public GUI1() {
		super("Motion Tracking - Squash");

		this.setPreferredSize(new Dimension(810, 650));
		this.setLocation(
				(Toolkit.getDefaultToolkit().getScreenSize().width - 800) / 2,
				(Toolkit.getDefaultToolkit().getScreenSize().height - 650) / 2);

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

		mnFile.add(mnSave);
		mnSave.addActionListener(this);
		mnSave.setActionCommand("save");

		/* Layout issues */
		vplayer = new Videoplayer(imagePanel, ipBlobs, ipCollide,
				processor = new ImageProcessor(dataCollector));
		getContentPane().setLayout(new FlowLayout());
		vpanel.setPreferredSize(new Dimension(388, 288));
		getContentPane().add("North", vpanel);
		vpanel.add(vplayer);

		imagePanel.setPreferredSize(new Dimension(388, 288));
		getContentPane().add("North", imagePanel);

		ipBlobs.setPreferredSize(new Dimension(388, 288));
		getContentPane().add("North", ipBlobs);

		ipCollide.setPreferredSize(new Dimension(388, 288));
		getContentPane().add("North", ipCollide);

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
		if (e.getActionCommand().equals("save")) {

			String[] data = new String[dataCollector.size() + 1];
			Iterator<Data> it = dataCollector.iterator();
			data[0] = "Time   Player1   Player2";
			for (int i = 1; it.hasNext(); i++) {
				data[i] = it.next().print();
			}

			JFrame dataFrame = new JFrame("Squash data");

			JList<String> list = new JList<String>(data);
			JScrollPane listScroller = new JScrollPane(list);
			dataFrame.getContentPane().add(listScroller);

			dataFrame.setSize(new Dimension(400, 600));
			dataFrame.setVisible(true);
			pack();
		}

	}

}
