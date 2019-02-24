package org.rrr.assets.sound;

import static org.lwjgl.openal.AL10.*;

import org.joml.Vector3f;

public class Source {
	
	public int id;
	public Vector3f pos;
	public AudioSystem parent;
	public SoundStream stream;
	
	public Source(AudioSystem parent) {
		this.parent = parent;
		pos = new Vector3f();
	}
	
	public void play(SoundClip clip) {
		if(clip == null) {
			alSourceStop(id);
			return;
		}
		alSourceStop(id);
		alSourceUnqueueBuffers(id);
		alSourceQueueBuffers(id, clip.buffer);
		alSourcePlay(id);
		
	}
	
	public void play(SoundStream stream) {
		this.stream = stream;
		int[] buffs = stream.fillAll();
		for(int i = 0; i < buffs.length; i++)
			if(buffs[i] != -1)
				alSourceQueueBuffers(id, buffs[i]);
		parent.registerStream(this);
		alSourcePlay(id);
	}
	
	public void stop() {
		alSourceStop(id);
	}
	
	public void update() {
		if(!stream.finished()) {
			if(alGetSourcei(id, AL_SOURCE_STATE) != AL_STOPPED) {
				while(alGetSourcei(id, AL_BUFFERS_PROCESSED) != 0) {
					int b = alSourceUnqueueBuffers(id);
					if(stream.fillBuffer(b))
						alSourceQueueBuffers(id, b);
					else
						break;
				}
			} else {
				int[] buffs = new int[alGetSourcei(id, AL_BUFFERS_PROCESSED)];
				alSourceUnqueueBuffers(id, buffs);
				for(int i = 0; i < buffs.length; i++) {
					if(stream.fillBuffer(buffs[i]))
						alSourceQueueBuffers(id, buffs[i]);
					else
						break;
				}
				alSourcePlay(id);
			}
		}
	}
}
