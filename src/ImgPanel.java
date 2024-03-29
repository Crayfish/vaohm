
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImgPanel extends JPanel {

	private BufferedImage buffImg = null;
	

	public ImgPanel() {
		super();

	}

	public void setImg(BufferedImage buff) {
		buffImg = buff;
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void paint(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, 200, 200);
		if (buffImg != null) {
			g.drawImage(buffImg, 0, 0, this);
		}
	}

}
