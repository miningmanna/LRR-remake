package org.rrr;

import java.util.ArrayList;

public class DelayedProcessor {
	
	private ArrayList<Action> actions;
	
	public DelayedProcessor() {
		actions = new ArrayList<>();
	}
	
	public void update(float delta) {
		for(int i = 0; i < actions.size(); i++) {
			Action a = actions.get(i);
			a.timeLeft -= delta;
			if(a.timeLeft < 0) {
				actions.remove(a);
				i--;
				a.r.run();
			}
		}
	}
	
	public Action queue(float time, Runnable r) {
		Action a = new Action();
		a.timeLeft = time;
		a.r = r;
		actions.add(a);
		return a;
	}
	
	public void dequeue(Action a) {
		actions.remove(a);
	}
	
	public static class Action {
		public float timeLeft;
		public Runnable r;
	}
	
}
