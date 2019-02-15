package org.rrr.assets.sound;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_MONO8;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO8;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioInputStream;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

public abstract class SoundStream {
	
	private int format;
	private int curBuff;
	private int[] buffs;
	private byte[] lBuff;
	
	private AudioInputStream in;
	
	public SoundStream(AudioInputStream in, int lBuffSize) {
		curBuff = 0;
		buffs = new int[2];
		if(lBuffSize % 4 != 0) {
			System.err.println("Invalid buffer size!");
			System.exit(-1);
		}
		lBuff = new byte[lBuffSize];
		this.in = in;
		
		format = -1;
		switch (in.getFormat().getChannels()) {
		case 1:
			switch (in.getFormat().getSampleSizeInBits()) {
				case 8:
					format = AL_FORMAT_MONO8;
					break;
				case 16:
					format = AL_FORMAT_MONO16;
					break;
				default:
					break;
			}
			break;
		case 2:
			switch (in.getFormat().getSampleSizeInBits()) {
				case 8:
					format = AL_FORMAT_STEREO8;
					break;
				case 16:
					format = AL_FORMAT_STEREO16;
					break;
				default:
					break;
			}
			break;
		default:
			break;
	}
	
	if(format == -1) {
		System.err.println("Invalid format!");
	}
	}
	
	public int getNextBuffer() {
		int res = buffs[curBuff];
		curBuff = (curBuff+1)%buffs.length;
		
		try {
			int l = in.read(lBuff);
			if(l != -1) {
				in.close();
				return -1;
			}
			
			ByteBuffer b = BufferUtils.createByteBuffer(l);
			b.put(lBuff, 0, l);
			
			AL10.alBufferData(res, format, b, (int) in.getFormat().getSampleRate());
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return res;
	}
	
	public void close() {
		try {
			if(in != null)
				in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
