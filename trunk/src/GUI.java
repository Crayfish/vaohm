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
	private Videoplayer vplayer = new Videoplayer();
	private JPanel vpanel = new JPanel();

	public GUI() {
		super("Motion Tracking");

		setSize(400, 300);
		setLocation(
				(Toolkit.getDefaultToolkit().getScreenSize().width - getWidth()) / 2,
				(Toolkit.getDefaultToolkit().getScreenSize().height - getHeight()) / 2);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		init();

		vpanel.setSize(400, 300);
		getContentPane().add(vpanel);
		vpanel.add(vplayer);
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
