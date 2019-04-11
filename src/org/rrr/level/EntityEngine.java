package org.rrr.level;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.print.attribute.standard.Finishings;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.rrr.RockRaidersRemake;

public class EntityEngine {
	
	private RockRaidersRemake par;
	private Globals globals;
	private Entity entity;
	private float delta;
	
	public EntityEngine(RockRaidersRemake par) {
		this.par = par;
		
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
		globals.set("moveNoclip", new ThreeArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1, LuaValue arg2) {
				entity.pos.add(arg0.tofloat(), arg1.tofloat(), arg2.tofloat());
				return null;
			}
		});
		globals.set("translate", new ThreeArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1, LuaValue arg2) {
				entity.pos.add(arg0.tofloat(), arg1.tofloat(), arg2.tofloat());
				return null;
			}
		});
		globals.set("hasPath", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1) {
				if(entity.curPath != null)
					return LuaValue.TRUE;
				else
					return LuaValue.FALSE;
			}
		});
		globals.set("removePath", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1) {
				entity.curPath = null;
				return null;
			}
		});
		globals.set("pathFind", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1) {
				Level l = par.getCurrentLevel();
				Vector2i mapPos = l.toMapPos(entity.pos);
				entity.curPath = l.pathFind(mapPos.x, mapPos.y, arg0.toint(), arg1.toint(), entity.walkables);
				return null;
			}
		});
		globals.set("getNextPathStep", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				Vector2i v = entity.curPath.tiles.get(entity.curPathStep);
				entity.curPathStep++;
				if(entity.curPathStep >= entity.curPath.tiles.size()) {
					entity.curPath = null;
					entity.curPathStep = 0;
				}
				LuaTable t = new LuaTable();
				t.set("x", (v.x + 0.5f)*40.0f); // TODO: remove hardcoded unit distance
				t.set("y", 90);
				t.set("z", (v.y + 0.5f)*40.0f);
				return t;
			}
		});
		globals.set("getCurrentPathStep", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				Vector2i v = entity.curPath.tiles.get(entity.curPathStep);
				LuaTable t = new LuaTable();
				t.set("x", (v.x + 0.5f)*40.0f); // TODO: remove hardcoded unit distance
				t.set("y", 90);
				t.set("z", (v.y + 0.5f)*40.0f);
				return t;
			}
		});
		globals.set("finishedPathStep", new ZeroArgFunction() {
			@Override
			public LuaValue call() {
				Vector2i v = entity.curPath.tiles.get(entity.curPathStep);
				Vector3f stepPos = new Vector3f((v.x + 0.5f)*40.0f, 90, (v.y + 0.5f)*40.0f);
				if(entity.pos.distance(stepPos) < 0.5f) {
					return LuaValue.TRUE;
				} else {
					return LuaValue.FALSE;
				}
			}
		});
		globals.set("lookInDirection", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				
				if(!(arg0 instanceof LuaTable))
					return LuaValue.FALSE;
				
				LuaTable t = (LuaTable) arg0;
				
				Vector3f dir = new Vector3f(	t.get("x").tofloat(),
												t.get("y").tofloat(),
												-t.get("z").tofloat());
				
				entity.rot.identity();
				entity.rot.lookAlong(dir, new Vector3f(0, 1, 0));
				
				return LuaValue.TRUE;
			}
		});
		globals.set("getNormalizedDifference", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue v1, LuaValue v2) {
				if(v1.istable() && v2.istable()) {
					LuaTable t1 = (LuaTable) v1;
					LuaTable t2 = (LuaTable) v2;
					LuaTable t = new LuaTable();
					Vector3f diff = new Vector3f(	t2.get("x").tofloat()-t1.get("x").tofloat(),
													t2.get("y").tofloat()-t1.get("y").tofloat(),
													t2.get("z").tofloat()-t1.get("z").tofloat());
					if(diff.x == 0 && diff.y == 0 && diff.z == 0)
						return LuaValue.NIL;
					diff.normalize();
					t.set("x", diff.x);
					t.set("y", diff.y);
					t.set("z", diff.z);
					return t;
				} else {
					return LuaValue.NIL;
				}
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
			globals.set("m", e.mVars);
			e.script.call();
		}
	}
	
}
