package org.rrr.assets.sound;

import static org.lwjgl.openal.ALC11.*;

import java.util.ArrayList;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;

public class AudioSystem {
	
	private long device;
	private long context;
	private ArrayList<Integer> sources;
	private ArrayList<Source>  streams;
	private Source publicSource;
	
	public AudioSystem() {
		sources = new ArrayList<>();
		streams = new ArrayList<>();
	}
	
	public void init() {
		String soundDefDevice = alcGetString(0, ALC_DEFAULT_ALL_DEVICES_SPECIFIER);
		device = alcOpenDevice(soundDefDevice);
		
		int[] attribs = {0};
		context = alcCreateContext(device, attribs);
		alcMakeContextCurrent(context);
		
		ALCCapabilities	alcCap	= ALC.createCapabilities(device);
		ALCapabilities	alCap	= AL.createCapabilities(alcCap);
		
		if(!alCap.OpenAL10) {
			System.err.println("No OpenAL 1.0 capabilities!");
			System.exit(-1);
		}
		
		publicSource = getSource();
		
	}
	
	public Source getSource() {
		
		int id = AL10.alGenSources();
		Source source = new Source(this);
		source.id = id;
		sources.add(id);
		return source;
		
	}
	
	public void playPublic(SoundClip clip) {
		publicSource.play(clip);
	}
	
	public void stopPublic() {
		publicSource.stop();
	}
	
	public void update() {
		synchronized (streams) {
			for(Source s : streams)
				s.update();
		}
	}
	
	public void registerStream(Source source) {
		synchronized (streams) {
			streams.add(source);
		}
	}
	
	public void removeStream(Source source) {
		synchronized (streams) {
			streams.remove(source);
		}
	}
	
	public void destroy() {
		alcDestroyContext(context);
		alcCloseDevice(device);
	}
	
}
