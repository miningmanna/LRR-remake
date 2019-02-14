package org.rrr.assets.sound;

import static org.lwjgl.openal.AL10.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.lwjgl.BufferUtils;
import org.newdawn.slick.openal.WaveData;

public class SoundLoader {
	
	ArrayList<Integer> buffers;
	
	public SoundLoader() {
		buffers = new ArrayList<>();
	}
	
	public void destroy() {
		for(int id : buffers)
			alDeleteBuffers(id);
	}
	
	public SoundClip getWavClip(File f) throws IOException, UnsupportedAudioFileException {
		
		AudioInputStream in = AudioSystem.getAudioInputStream(f);
		
		if(in.getFormat().isBigEndian()) {
			System.err.println("Invalid format!");
			in.close();
			return null;
		}
		
		long size = in.getFrameLength();
		int openAlFormat = -1;
		switch (in.getFormat().getChannels()) {
			case 1:
				switch (in.getFormat().getSampleSizeInBits()) {
					case 8:
						openAlFormat = AL_FORMAT_MONO8;
						break;
					case 16:
						openAlFormat = AL_FORMAT_MONO16;
						size *= 2;
						break;
					default:
						break;
				}
				break;
			case 2:
				switch (in.getFormat().getSampleSizeInBits()) {
					case 8:
						openAlFormat = AL_FORMAT_STEREO8;
						break;
					case 16:
						openAlFormat = AL_FORMAT_STEREO16;
						size *= 2;
						break;
					default:
						break;
				}
				break;
			default:
				break;
		}
		
		if(openAlFormat == -1) {
			System.err.println("Invalid format!");
			in.close();
			return null;
		}
		
		byte[] b = new byte[(int) size];
		in.read(b);
		ByteBuffer buff = BufferUtils.createByteBuffer((int) size).put(b);
		buff.flip();
		
		int buffer = alGenBuffers();
		buffers.add(buffer);
		alBufferData(buffer, openAlFormat, buff, (int) in.getFormat().getSampleRate());
		SoundClip c = new SoundClip();
		c.buffer = buffer;
		
		return c;
	}
	
}

