package org.rrr.assets.sound;

import static org.lwjgl.openal.AL10.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.lwjgl.BufferUtils;
import org.rrr.assets.AssetManager;
import org.rrr.assets.LegoConfig.Node;

public class SoundLoader {
	
	private AssetManager am;
	private HashMap<String, String> sounds;
	private ArrayList<Integer> buffers;
	
	public static final String[] EXTENSIONS = {
			"wav",
			"ogg"
	};
	
	public SoundLoader(Node cfg, AssetManager am) {
		this.am = am;
		sounds = new HashMap<>();
		buffers = new ArrayList<>();
		
		for(String key : cfg.getValueKeys()) {
			String val = cfg.getValue(key);
			key = key.replaceAll("[!@\\*]", "");
			val = val.replaceAll("[!@\\*]", "");
			
			
			if(val.contains(","))
				val = val.split(",")[0]; // TODO Use all listed files
			String str = null;
			for(String ext : EXTENSIONS) {
				if(am.exists(val + "." + ext)) {
					str = val + "." + ext;
					break;
				}
			}
			sounds.put(key, str);
		}
	}
	
	public SoundStream getSoundStream(String path) throws UnsupportedAudioFileException, IOException {
		InputStream in = am.getAsset(path);
		SoundStream stream = new SoundStream(this, path, in, 4096*4);
		in.close();
		return stream;
	}
	
	public String getSample(String key) {
		key = key.toUpperCase();
		return sounds.get(key);
	}
	
	public int getBuffer() {
		int b = alGenBuffers();
		buffers.add(b);
		return b;
	}
	
	public void destroy() {
		for(int id : buffers)
			alDeleteBuffers(id);
	}
	
	public SoundClip getSoundClip(InputStream dataIn) throws IOException, UnsupportedAudioFileException {
		
		dataIn = new BufferedInputStream(dataIn);
		AudioInputStream in = AudioSystem.getAudioInputStream(dataIn);
		AudioFormat enc = new AudioFormat(in.getFormat().getSampleRate(), 16, 2, true, false);
		in = AudioSystem.getAudioInputStream(enc, in);
		
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
						size *= 2;
						break;
					case 16:
						openAlFormat = AL_FORMAT_STEREO16;
						size *= 4;
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
		
		if(size < 0)
			size = 44100*4*5;
		byte[] b = new byte[(int) size];
		int read = 0, l = 0;
		while((l = in.read(b, read, b.length-read)) != -1) {
			read += l;
		}
		ByteBuffer buff = BufferUtils.createByteBuffer((int) size).put(b);
		buff.flip();
		
		int buffer = alGenBuffers();
		buffers.add(buffer);
		alBufferData(buffer, openAlFormat, buff, (int) in.getFormat().getSampleRate());
		int realSize = alGetBufferi(buffer, AL_SIZE);
		SoundClip c = new SoundClip();
		c.buffer = buffer;
		
		return c;
	}
	
}

