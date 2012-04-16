import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class GUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JMenuBar menuBar = new JMenuBar();
	private JMenu mnFile = new JMenu("File");
	private JMenuItem mntmOpenFromFile = new JMenuItem("Open from file...");
	private Videoplayer vplayer;
	private JPanel vpanel = new JPanel();
	private ImgPanel imagePanel = new ImgPanel();

	public GUI() {
		super("Motion Tracking");

		this.setPreferredSize(new Dimension(800, 700));
		this.setLocation(
				(Toolkit.getDefaultToolkit().getScreenSize().width - 800) / 2,
				(Toolkit.getDefaultToolkit().getScreenSize().height - 700) / 2);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		init();

		vplayer = new Videoplayer(imagePanel);
		getContentPane().setLayout(new FlowLayout());
		vpanel.setPreferredSize(new Dimension(384, 315));
		getContentPane().add("North", vpanel);
		vpanel.add(vplayer);

		imagePanel.setPreferredSize(new Dimension(384, 288));
		getContentPane().add("North", imagePanel);
		setVisible(true);
		pack();

	}

	private void init() {
		setJMenuBar(menuBar);
		menuBar.add(mnFile);

		mnFile.add(mntmOpenFromFile);
		mntmOpenFromFile.addActionListener(this);
		mntmOpenFromFile.setActionCommand("open");

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("open")) {

			if (vplayer.openURL())
				pack();

		}

	}

}
