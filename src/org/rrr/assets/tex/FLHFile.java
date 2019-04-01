package org.rrr.assets.tex;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class FLHFile {
	
	public int width;
	public int height;
	public int lframes;
	public ArrayList<BufferedImage> frames;
	
	public static FLHFile getFLHFile(InputStream in) throws IOException {
		
		FLHFile flh = new FLHFile();
		
		byte[] header = new byte[128];
		in.read(header);
		
		int offset = 0;
		int flength = getIntLE(header, offset);
		offset += 4;
		short type = getShortLE(header, offset);
		offset += 2;
		if(type != (short) 0xAF43)
			System.out.println("WARNING: is not a LRR FLH file!");
		
		flh.lframes = getShortLE(header, offset);
		offset += 2;
		flh.frames = new ArrayList<>(flh.lframes);
		flh.width = getShortLE(header, offset);
		offset += 2;
		flh.height = getShortLE(header, offset);
		offset += 2;
		short depth = getShortLE(header, offset);
		if(depth != 16)
			System.out.println("Expected 16 bit colors");
		offset += 68; // Unimportend data
		
		int offset1stFrame = getIntLE(header, offset);
		offset += 4;
		
		offset = offset1stFrame;
		
		byte[] segHeader = new byte[6];
		int imageIndex = 0;
		while((flength - offset) > 0) {
			in.read(segHeader);
			int segLen = -6 + getIntLE(segHeader, 0);
			byte[] seg = new byte[segLen];
			in.read(seg);
			offset += segLen + 6;
			short segType = getShortLE(segHeader, 4);
			
			switch (segType) {
			case (short) 0xF1FB:
				System.out.println("Segment table present!");
				break;
			case (short) 0xF1FA:
				imageIndex += parseFRAME_TYPE(flh, seg, imageIndex);
				break;
			default:
				System.out.println("Unknown chunk type: " + Integer.toHexString(segType));
				break;
			}
		}
		
		in.close();
		
		return flh;
	}
	
	static int _curFrame = 0;
	private static int parseFRAME_TYPE(FLHFile flh, byte[] seg, int imageIndex) {
		int offset = 0;
		short lchunks = getShortLE(seg, offset);
		_curFrame++;
		if(lchunks > 1)
			System.out.println("More than one sub-chunk");
		offset += 2;
		offset += 2;
		offset += 2; // reserved = 0
		offset += 4; // width and height should be 0
		
		if(seg.length < 16) {
			flh.lframes -= 1;
			return 0;
		}
		
		int subLen = getIntLE(seg, offset);
		subLen -= 6;
		offset += 4;
		short chunkType = getShortLE(seg, offset);
		offset += 2;
		
		switch (chunkType) {
		case 25:
			parseDTA_BRUN(flh, seg, offset, subLen, imageIndex);
			break;
		case 27:
			parseDELTA_FLC(flh, seg, offset, subLen, imageIndex);
			break;
		default:
			System.out.println("Unsupported sub-chunk type: " + chunkType);
			break;
		}
		return 1;
	}
	
	private static void parseDTA_BRUN(FLHFile flh, byte[] seg, int offset, int len, int imageIndex) {
		
		BufferedImage res = new BufferedImage(flh.width, flh.height, BufferedImage.TYPE_INT_ARGB);
		
		int x = 0;
		int y = 0;
		int w = flh.width;
		
		offset += 1;
		System.out.println(res);
		while((len-offset) > 0) {
			byte repeat = seg[offset];
			if(repeat < 0) {
				repeat = (byte) (repeat * -1);
				for(int i = 0; i < repeat; i++) {
					int rgb = getARGBFrom555RGB(seg, offset+i*2+1);
					
					res.setRGB(x, y, rgb);
					x++;
				}
				offset += repeat*2+1;
			} else {
				
				int rgb = getARGBFrom555RGB(seg, offset+1);
				
				for(int i = 0; i < repeat; i++) {
					res.setRGB(x, y, rgb);
					x++;
				}
				offset += 3;
			}
			
			if(x >= w) {
				x %= w;
				y++;
				if(y > flh.height)
					break;
				offset++;
			}
			
		}
		for(; y < flh.height; y++) {
			for(; x < w; x++) {
				res.setRGB(x, y, 0xFF000000);
			}
			x = 0;
		}
		
		flh.frames.add(imageIndex, res);
		
	}
	
	private static void parseDELTA_FLC(FLHFile flh, byte[] seg, int offset, int len, int imageIndex) {
		BufferedImage res = new BufferedImage(flh.width, flh.height, BufferedImage.TYPE_INT_ARGB);
		if(imageIndex != 0)
			res.getGraphics().drawImage(flh.frames.get(imageIndex-1), 0, 0, null);
		
		short llines = getShortLE(seg, offset);
		offset += 2;
		
		int y = 0;
		int linesDone = 0;
		while((len - offset) > 0) {
			
			if(llines == linesDone) {
				System.out.println("Line already done. Unexpected data: " + (len-offset));
				break;
			}
			
			int packCount = -1;
			while(packCount == -1) {
				short opcode = getShortLE(seg, offset);
				offset += 2;
				int optype = (0x0000C000 & opcode) >> 14;
				switch (optype) {
				case 0:
					packCount = opcode;
					break;
				case 2:
					System.out.println("Last Pixel?");
					break;
				case 3:
					y += Math.abs(opcode) & 0x000000FF;
					break;
				default:
					System.out.println("Unknown opcode: " + opcode);
					break;
				}
			}
			
			int x = 0;
			for(int i = 0; i < packCount; i++) {
				x += 0x000000FF & seg[offset];
				offset++;
				byte repeat = seg[offset];
				offset++;
				if(repeat < 0) {
					repeat = (byte) (-1*repeat);
					
					int rgb = getARGBFrom555RGB(seg, offset);
					for(int j = 0; j < repeat; j++) {
						res.setRGB(x, y, rgb);
						x++;
					}
					
					offset += 2;
				} else {
					for(int j = 0; j < repeat; j++) {
						int rgb = getARGBFrom555RGB(seg, offset+j*2);
						res.setRGB(x, y, rgb);
						x++;
					}
					offset += repeat*2;
					
				}
			}
			y++;
			
//			if(hasLastPixel)
//				offset--;
			
			linesDone++;
			
		}
		
		
		flh.frames.add(imageIndex, res);
	}
	
	private static int getARGBFrom555RGB(byte[] a, int offset) {
		
		int rgb = 0x000000FF;
		rgb &= a[offset+1];
		rgb = rgb << 8;
		rgb |= 0x000000FF & a[offset];
		
		int r = (int) ((rgb >> 10) * (255.0f/31.0f));
		int g = (int) (((rgb >> 5) & 0b00011111) * (255.0f/31.0f));
		int b = (int) ((rgb & 0b00011111) * (255.0f/31.0f));
		
		rgb = 0x0000FF00 | r;
		rgb = rgb << 8;
		rgb |= g;
		rgb = rgb << 8;
		rgb |= b;
		
		return rgb;
	}
	
	private static short getShortLE(byte[] b, int off) {
		
		int res = 0;
		for(int i = 0; i < 2; i++) {
			res = res | (0x000000FF & b[off+1-i]);
			if(i != 1)
				res = res << 8;
		}
		
		return (short) res;
		
	}
	
	private static int getIntLE(byte[] b, int off) {
		
		int res = 0;
		for(int i = 0; i < 4; i++) {
			res = res | (0x000000FF & b[off+3-i]);
			if(i != 3)
				res = res << 8;
		}
		
		return res;
		
	}
	
	static int curFrame = 0;
	static BufferedImage img;
	public static void main(String[] args) {
		
		try {
			FileInputStream fin = new FileInputStream("LMS.flh");
			FLHFile f = getFLHFile(fin);
			fin.close();
			
			img = f.frames.get(0);
			
			JFrame frame = new JFrame("FLH animations");
			
			
			frame.add(new JPanel() {
				private static final long serialVersionUID = 1L;

				@Override
				public Dimension getPreferredSize() {
					return new Dimension(f.width, f.height);
				}
				
				@Override
				public void paint(Graphics arg0) {
//					arg0.drawImage(img, 0, 0, null);
					arg0.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
//					frame.repaint();
				}
				
			});
			
			frame.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void keyReleased(KeyEvent arg0) {
					if(arg0.getKeyChar() == 'e') {
						curFrame++;
						curFrame %= f.lframes;
						img = f.frames.get(curFrame);
						frame.repaint();
					} else if(arg0.getKeyChar() == 'q') {
						curFrame += f.lframes-1;
						curFrame %= f.lframes;
						img = f.frames.get(curFrame);
						frame.repaint();
					}
				}
				
				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method stub
					
				}
			});
			
			Thread t = new Thread(new Runnable() {
				
				@Override
				public void run() {
					while(true) {
						try {
							Thread.sleep(1000/20);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						curFrame += f.lframes+1;
						curFrame %= f.lframes;
						img = f.frames.get(curFrame);
						frame.repaint();
					}
				}
			});
			t.setDaemon(true);
			t.start();
			
			frame.pack();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
			
			FileOutputStream out = new FileOutputStream("out.png");
			ImageIO.write(img, "png", out);
			out.close();
			
		} catch(IOException e) { 	
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
