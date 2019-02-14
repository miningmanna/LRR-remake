package org.rrr.assets.sound;

import org.joml.Vector3f;
import org.lwjgl.openal.AL10;

public class Source {
	
	public int id;
	public Vector3f pos;
	
	public Source() {
		pos = new Vector3f();
	}
	
	public void play(SoundClip clip) {
		if(clip == null)
			return;
		AL10.alSourceStop(id);
		AL10.alSourceUnqueueBuffers(id);
		AL10.alSourceQueueBuffers(id, clip.buffer);
		AL10.alSourcePlay(id);
		
	}
}
