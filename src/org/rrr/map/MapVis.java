package org.rrr.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class MapVis extends JPanel {
	
	public int[][] data;
	private Color[] colors = {
			Color.BLACK,
			Color.BLUE,
			Color.RED,
			Color.GREEN,
			Color.MAGENTA,
			Color.YELLOW,
			Color.CYAN,
			Color.PINK,
			Color.GRAY,
			Color.WHITE,
			Color.ORANGE,
			Color.LIGHT_GRAY,
			Color.DARK_GRAY
	};
	
	private JLabel surfNum;
	
	public MapVis(int[][] data) {
		setLayout(null);
		surfNum = new JLabel("asdasf");
		surfNum.setBounds(0, 0, data.length*10, 20);
		this.data = data;
		setPreferredSize(new Dimension(this.data.length*10, this.data[0].length*10+20));
		addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				int x = (int) Math.floor(e.getX() / 10.0f);
				int y = (int) Math.floor((e.getY() - 20) / 10.0f);
				if(y < 0)
					return;
				surfNum.setText("" + MapVis.this.data[x][y]);
				repaint();
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				
			}
		});
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		surfNum.paint(g);
		for(int i = 0; i < data.length; i++) {
			
			for(int j = 0; j < data[i].length; j++) {
				
				g.setColor(new Color(data[i][j]*5, data[i][j]*5, data[i][j]*5));
				g.fillRect(i*10, 20+j*10, 10, 10);
				
			}
			
		}
		
	}
	
}
