package org.rrr.assets.sound;

import static org.lwjgl.openal.ALC11.*;

import java.util.ArrayList;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;

import static org.lwjgl.openal.ALC10.*;

public class AudioSystem {
	
	long device;
	long context;
	ArrayList<Integer> sources;
	Source publicSource;
	
	public AudioSystem() {
		sources = new ArrayList<>();
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
		Source source = new Source();
		source.id = id;
		sources.add(id);
		return source;
		
	}
	
	public void playPublic(SoundClip clip) {
		System.out.println("PUBLIC PLAY " + clip);
		publicSource.play(clip);
	}
	
	public void destroy() {
		alcDestroyContext(context);
		alcCloseDevice(device);
	}
	
}
