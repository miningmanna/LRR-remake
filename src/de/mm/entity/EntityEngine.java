package de.mm.entity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.joml.Vector3f;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

public class EntityEngine {
	
	private Globals globals;
	private Entity entity;
	private float delta;
	
	public EntityEngine() {
		
		globals = JsePlatform.standardGlobals();
		
		globals.set("setPosition", new ThreeArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1, LuaValue arg2) {
				entity.pos.set(arg0.tofloat(), arg1.tofloat(), arg2.tofloat());
				return null;
			}
		});
		globals.set("getPosition", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				
				LuaTable t = new LuaTable();
				t.set("x", entity.pos.x);
				t.set("y", entity.pos.y);
				t.set("z", entity.pos.z);
				
				return t;
			}
		});
		globals.set("move", new ThreeArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1, LuaValue arg2) {
				entity.pos.add(arg0.tofloat(), arg1.tofloat(), arg2.tofloat());
				return null;
			}
		});
		globals.set("delta", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				return LuaValue.valueOf(delta);
			}
		});
		Vector3f yAxis = new Vector3f(0, 1, 0);
		globals.set("turn", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				entity.rot.rotate(arg0.tofloat(), yAxis);
				return null;
			}
		});
		
	}
	
	public void bindScript(Entity e, String s) {
		LuaValue script = globals.load(s);
		e.script = script;
	}
	
	public void bindScript(Entity e, File f) throws FileNotFoundException {
		LuaValue script = globals.load(new FileReader(f), f.getName());
		e.script = script;
	}
	
	public void step(float delta) {
		this.delta = delta;
	}
	
	public void call(Entity e) {
		entity = e;
		if(e.script != null) {
			e.script.call();
		}
	}
	
}
