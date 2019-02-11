package org.rrr.assets.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class MapVis extends JPanel {
	
	private static final long serialVersionUID = -7895659645758561024L;
	
	public int[][] data;
	private JLabel surfNum;
	
	public MapVis(int[][] data) {
		setLayout(null);
		surfNum = new JLabel("asdasf");
		surfNum.setBounds(0, 0, data.length*10, 20);
		this.data = data;
		setPreferredSize(new Dimension(this.data[0].length*10, this.data.length*10+20));
		addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				int x = (int) Math.floor(e.getX() / 10.0f);
				int y = (int) Math.floor((e.getY() - 20) / 10.0f);
				if(y < 0)
					return;
				surfNum.setText("" + MapVis.this.data[y][data[0].length-1-x]);
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
				
				g.setColor(new Color(data[i][data[0].length-1-j]*5, data[i][data[0].length-1-j]*5, data[i][data[0].length-1-j]*5));
				g.fillRect(j*10, 20+i*10, 10, 10);
				
			}
			
		}
		
	}
	
}
