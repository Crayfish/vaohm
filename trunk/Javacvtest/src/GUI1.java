import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;

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
		start();

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

		// mnFile.add(mntmSquash1);
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

	private void start() {
		try {
			if (vplayer.openURL(null)) {
				pack();
			}
		} catch (Exception e2) {
			JOptionPane.showMessageDialog(this, "Cannot open the video file.");
			// e1.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("open")) {

			try {
				if (vplayer.openURL(null)) {
					pack();
				}
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(this,
						"Cannot open the video file.");
				// e1.printStackTrace();
			}

		}
		if (e.getActionCommand().equals("squash1")) {

			try {
				if (vplayer.openURL(new URL(new URL("file:"), "./Squash1.avi")))
					;
				{
					pack();
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
			dataFrame.getContentPane().setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();

			gbc.gridx = 1;
			gbc.gridy = 1;

			gbc.weightx = 1;
			gbc.weighty = 1;
			gbc.fill = GridBagConstraints.BOTH;
			dataFrame.getContentPane().add(listScroller, gbc);

			JButton save = new JButton("save to file");
			gbc.gridy = 2;
			gbc.weightx = 0;
			gbc.weighty = 0;
			dataFrame.getContentPane().add(save, gbc);

			save.addActionListener(this);
			save.setActionCommand("save data");

			dataFrame.setSize(new Dimension(400, 600));
			dataFrame.setVisible(true);
			pack();
		}
		if (e.getActionCommand().equals("save data")) {
			System.out.println("saving data to file...");
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					".txt", "txt");
			fc.addChoosableFileFilter(filter);

			int ret = fc.showSaveDialog(this);

			if (ret == JFileChooser.APPROVE_OPTION) {
				saveData(fc.getSelectedFile());

			}
		}

	}

	private void saveData(File file) {
		String filename = file.getPath();
		if (!filename.endsWith(".txt"))
			filename = filename + ".txt";
		if (new File(filename).exists()) {
			Object[] options = { "Replace", "Cancel" };
			int n = JOptionPane.showOptionDialog(this,
					"File already exists! Would you like to replace it?",
					"Replacement", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 0) {
				new File(filename).delete();

			} else
				return;

		}
		String Content = "";
		String[] data = new String[dataCollector.size() + 1];
		Iterator<Data> it = dataCollector.iterator();
		data[0] = "Time   Player1   Player2";

		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			while (it.hasNext()) {
				out.write(it.next().print());
			}
			// out.write(Content);
			out.close();

			JOptionPane.showMessageDialog(this, "File was saved!");
		} catch (IOException ex1) {

		}
	}

}
