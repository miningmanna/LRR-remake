package org.rrr.assets.sound;

import static org.lwjgl.openal.AL10.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.lwjgl.BufferUtils;
import org.rrr.assets.LegoConfig.Node;

public class SoundLoader {
	
	private HashMap<String, File> sounds;
	private ArrayList<Integer> buffers;
	
	public SoundLoader(Node cfg) {
		sounds = new HashMap<>();
		buffers = new ArrayList<>();
		
		for(String key : cfg.getValueKeys()) {
			String val = cfg.getValue(key);
			key = key.replaceAll("[!@\\*]", "");
			System.out.println("with special: " + val);
			val = val.replaceAll("[!@\\*]", "");
			System.out.println("fixed: " + val);
			
			
			if(val.contains(","))
				val = val.split(",")[0]; // TODO Use all listed files
			
			System.out.println("VAL: " + val);
			File f = findFile("LegoRR0/" + val);
			if(f == null)
				f = findFile("LegoRR1/" + val);
			System.out.println(key + ": " + f);
			sounds.put(key, f);
		}
	}
	
	private File findFile(String path) {
		
		String[] split = path.split("[\\\\/]");
		File f = new File("./");
		for(int i = 0; i < split.length; i++) {
			String next = split[i];
			System.out.println(split[i]);
			if(f.isDirectory()) {
				if(i == split.length-1) {
					boolean foundNext = false;
					for(File nextFile : f.listFiles()) {
						String[] nameSplit = nextFile.getName().split("\\.");
						String nextFileName = nameSplit[0];
						if(nameSplit.length > 2)
							for(int j = 1; j < nameSplit.length-1; j++)
								nextFileName += "." + split[j];
						
						if(nextFileName.equalsIgnoreCase(next)) {
							System.out.println(nextFile);
							f = nextFile;
							foundNext = true;
							break;
						}
					}
					if(foundNext)
						continue;
					System.out.println("Found no next :(");
					return null;
				} else {
					boolean foundNext = false;
					for(File nextFile : f.listFiles()) {
						if(nextFile.getName().equalsIgnoreCase(next)) {
							System.out.println(nextFile);
							f = nextFile;
							foundNext = true;
							break;
						}
					}
					if(foundNext)
						continue;
					System.out.println("Found no next :(");
					return null;
				}
			} else {
				return null;
			}
				
		}
		
		return f;
	}
	
	public File getSample(String key) {
		return sounds.get(key);
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
		
		byte[] b = new byte[(int) size];
		System.out.println("Reading " + in.read(b) + " byte into buffer");
		System.out.println("Avaliable: " + in.available());
		System.out.println("Framesize: " + in.getFrameLength());
		ByteBuffer buff = BufferUtils.createByteBuffer((int) size).put(b);
		buff.flip();
		
		int buffer = alGenBuffers();
		buffers.add(buffer);
		alBufferData(buffer, openAlFormat, buff, (int) in.getFormat().getSampleRate());
		int realSize = alGetBufferi(buffer, AL_SIZE);
		System.out.println("REAL BUFFER SIZE: " + realSize + "/" + size);
		SoundClip c = new SoundClip();
		c.buffer = buffer;
		
		return c;
	}
	
}

