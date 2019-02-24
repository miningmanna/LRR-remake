package org.rrr.assets.sound;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_MONO8;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO8;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

public class SoundStream {
	
	private int format;
	private int curBuff;
	private int[] buffs;
	private byte[] lBuff;
	private boolean finished;
	
	private AudioInputStream _in;
	private AudioInputStream in;
	
	public SoundStream(SoundLoader loader, File f, int lBuffSize) throws UnsupportedAudioFileException, IOException {
		
		finished = false;
		
		_in = AudioSystem.getAudioInputStream(f);
		String[] split = f.getName().split("\\.");
		String ext = "";
		if(split.length > 1)
			ext = split[split.length-1];
		if(ext.equalsIgnoreCase("ogg")) {
			System.out.println("CONVERTING SREAM FOR OGG");
			AudioFormat base = _in.getFormat();
			AudioFormat target = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED,
					base.getSampleRate(),
					16,
					base.getChannels(),
					base.getChannels()*2,
					base.getSampleRate(),
					false);
			in = AudioSystem.getAudioInputStream(target, _in);
		} else {
			in = _in;
		}
		System.out.println(in.getFormat());
		System.out.println("BIG_ENDIAN: " + in.getFormat().isBigEndian());
		System.out.println("CHANNELS:   " + in.getFormat().getChannels());
		System.out.println("BITS:       " + in.getFormat().getSampleSizeInBits());
		curBuff = 0;
		buffs = new int[2];
		for(int i = 0; i < buffs.length; i++)
			buffs[i] = loader.getBuffer();
		if(lBuffSize % 4 != 0) {
			System.err.println("Invalid buffer size!");
			System.exit(-1);
		}
		lBuff = new byte[lBuffSize];
		
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
			in.close();
			finished = true;
		}
	}
	
	public int[] fillAll() {
		
		int[] ret = new int[buffs.length];
		for(int i = 0; i < buffs.length; i++) {
			if(fillBuffer(buffs[i]))
				ret[i] = buffs[i];
			else
				ret[i] = -1;
		}
		
		return ret;
	}
	
	public boolean fillBuffer(int buff) {
		if(finished)
			return false;
		
		int res = buffs[curBuff];
		curBuff = (curBuff+1)%buffs.length;
		
		try {
			int l;
			while((l = in.read(lBuff)) == 0) {}
			if(l == -1) {
				_in.close();
				finished = true;
				return false;
			}
			
			ByteBuffer b = BufferUtils.createByteBuffer(l);
			b.put(lBuff, 0, l);
			b.flip();
			
			AL10.alBufferData(res, format, b, (int) in.getFormat().getSampleRate());
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public void close() {
		System.out.println("CLOSE!=!=!=!");
		try {
			if(in != null) {
				System.out.println("CLOSING STREAM");
				finished = true;
				_in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getBufferCount() {
		return buffs.length;
	}

	public boolean finished() {
		return finished;
	}
	
}
