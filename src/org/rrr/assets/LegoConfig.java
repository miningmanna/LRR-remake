package org.rrr.assets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

public class LegoConfig {
	
	private Node superNode;
	
	public LegoConfig() {
		superNode = new Node(null, null, 0);
	}
	
	public static LegoConfig getConfig(InputStream in, String relPath, AssetManager am) throws IOException {
		LegoConfig cfg = new LegoConfig();
		
		LinkedList<String> scripts = new LinkedList<String>();
		getToNode(relPath, in, cfg.superNode, 0, am, scripts);
		
		Globals globals = JsePlatform.standardGlobals();
		globals.set("setValue", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0, LuaValue arg1) {
				String path = arg0.tojstring();
				path.replace('\\', '/');
				String nodePath = path.substring(0, path.lastIndexOf('/'));
				Node n = (Node) cfg.get(nodePath);
				if(n == null)
					return LuaValue.FALSE;
				
				String valName = path.substring(path.lastIndexOf('/')+1).toUpperCase();
				n.values.put(valName, arg1.tojstring());
				return LuaValue.TRUE;
			}
		});
		globals.set("getValue", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg0) {
				Object o = cfg.get(arg0.tojstring());
				if(o instanceof String)
					return LuaValue.valueOf((String) o);
				return LuaValue.NIL;
			}
		});
		
		for(String script : scripts) {
			globals.load(script).call();
		}
		
		return cfg;
		
	}

	public void printTree() {
		superNode.printTree();
	}
	public static String EXTERN_REGEX = "[\\t ]*(;#extern:)([\\w\\.]+).*";
	public static String SCRIPT_START_REGEX = "[\\t ]*(;#runAfter).*";
	public static String SCRIPT_END_REGEX = "[\\t ]*(;#endRunAfter).*";
	public static void getToNode(String relPath, InputStream in, Node parent, int depth, AssetManager am, LinkedList<String> scripts) throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		String lastLine = null;
		
		Node currentNode = parent;
		
		boolean isScript = false;
		String curScript = "";
		while((line = br.readLine()) != null) {
			if(isScript) {
				if(line.matches(SCRIPT_END_REGEX)) {
					isScript = false;
					scripts.add(curScript);
					curScript = "";
					continue;
				}
				curScript += line.substring(line.lastIndexOf(';')+1) + "\n";
				continue;
			}
			if(line.matches(EXTERN_REGEX)) {
				try {
					String path = relPath + line.split(":")[1];
					File f = new File(path);
					InputStream extIn = am.getAsset(path);
					System.out.println(path + " : " + extIn);
					getToNode(f.getParent(), extIn, currentNode, depth, am, scripts);
					extIn.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
				continue;
			} else if(line.matches(SCRIPT_START_REGEX)) {
				isScript = true;
				continue;
			}
			
			if(line.contains(";")) {
				String[] split = line.split(";");
				if(split.length != 0)
					line = split[0];
				else
					continue;
			}
			if(line.contains("//")) {
				String[] split = line.split("//");
				if(split.length != 0)
					line = split[0];
				else
					continue;
			}
			// Wonky Tab replacement
			line = line.replace((char) 9, ' ');
			line = line.trim();
			line = removeDoubleSpaces(line);
			
			if(line.equals(""))
				continue;
			
			if(line.contains(" ")) {
				String[] split = line.split(" ");
				if(split.length > 2)
					System.out.println("WHAT??");
				
				if(split[1].equals("{")) {
					depth++;
					if(currentNode.subNodes.containsKey(split[0].toUpperCase())) {
						currentNode = currentNode.getSubNode(split[0].toUpperCase());
					} else {
						Node n = new Node(split[0].toUpperCase(), currentNode, depth);
						currentNode.subNodes.put(split[0].toUpperCase(), n);
						currentNode = n;
					}
				} else {
					currentNode.values.put(split[0].toUpperCase(), split[1]);
				}
			} else {
				if(line.equals("}")) {
					depth--;
					currentNode = currentNode.parent;
				} else if(line.equals("{")) {
					depth++;
					if(currentNode.subNodes.containsKey(lastLine.toUpperCase())) {
						currentNode = currentNode.getSubNode(lastLine.toUpperCase());
					} else {
						Node n = new Node(lastLine.toUpperCase(), currentNode, depth);
						currentNode.subNodes.put(lastLine.toUpperCase(), n);
						currentNode = n;
					}
				}
			}
			
			lastLine = line;
		}
		
	}
	
	private static String removeDoubleSpaces(String str) {
		
		char[] chars = new char[str.length()];
		int newLen = 0;
		for(int i = 0; i < str.length(); i++) {
			if(str.charAt(i) == ' ') {
				for(int j = i+1; j < str.length(); j++) {
					if(str.charAt(j) != ' ')
						break;
					i++;
				}
				chars[newLen] = ' ';
			} else {
				chars[newLen] = str.charAt(i);
			}
			newLen++;
		}
		
		return new String(chars, 0, newLen);
		
	}
	
	public Object get(String path) {
		path = path.toUpperCase();
		String[] split = path.split("/");
		
		Node curNode = superNode;
		for(int i = 0; i < split.length-1; i++) {
			Node n = curNode.getSubNode(split[i]);
			if(n == null)
				return null;
			curNode = n;
		}
		
		String name = split[split.length-1];
		Object res = curNode.getValue(name);
		if(res == null)
			res = curNode.getSubNode(name);
		
		return res;
	}
	
	public boolean getBoolean(String path) {
		path = path.toUpperCase();
		Object o = (String) get(path);
		if(o == null)
			return false;
		if(!(o instanceof String))
			return false;
		String val = (String) o;
		if(val.equalsIgnoreCase("FALSE"))
			return false;
		if(val.equalsIgnoreCase("TRUE"))
			return true;
		
		return true;
	}
	
	public int getInteger(String path) {
		path = path.toUpperCase();
		Object o = (String) get(path);
		if(o == null)
			return 0;
		if(!(o instanceof String))
			return 0;
		try {
			return Integer.parseInt((String) o);
		} catch(Exception e) {
			return 0;
		}
		
	}
	
	public double getDouble(String path) {
		path = path.toUpperCase();
		Object o = (String) get(path);
		if(o == null)
			return 0;
		if(!(o instanceof String))
			return 0;
		try {
			return Double.parseDouble((String) o);
		} catch(Exception e) {
			return 0;
		}
		
	}
	
	public float getFloat(String path) {
		path = path.toUpperCase();
		Object o = (String) get(path);
		if(o == null)
			return 0;
		if(!(o instanceof String))
			return 0;
		try {
			return Float.parseFloat((String) o);
		} catch(Exception e) {
			return 0;
		}
		
	}
	
	
	public static final String INDENT = "\t";
	public static class Node {
		
		private String name;
		private Node parent;
		private int depth;
		private HashMap<String, String> values;
		private HashMap<String, Node> subNodes;
		
		public Node(String name, Node parent, int depth) {
			this.name = name;
			this.parent = parent;
			this.depth = depth;
			values = new HashMap<>();
			subNodes = new HashMap<>();
		}
		
		public String getPath() {
			String res = new String(name);
			Node curNode = this;
			while((curNode = curNode.getParent()) != null) {
				if(curNode.name != null)
					res = curNode.name + "/" + res;
				else
					break;
			}
			return res;
		}
		
		public Node getParent() {
			return parent;
		}
		
		public Node getSubNode(String name) {
			name = name.toUpperCase();
			return subNodes.get(name);
		}
		
		public Set<String> getValueKeys() {
			return values.keySet();
		}
		
		public Set<String> getSubNodeKeys() {
			return subNodes.keySet();
		}
		
		public String getValue(String name) {
			name = name.toUpperCase();
			return values.get(name);
		}
		
		public String getOptValue(String name, String alt) {
			name = name.toUpperCase();
			if(values.get(name) == null)
				return alt;
			return values.get(name);
		}
		
		public boolean getBoolean(String name) {
			name = name.toUpperCase();
			String val = values.get(name);
			if(val == null)
				return false;
			if(val.equalsIgnoreCase("FALSE"))
				return false;
			if(val.equalsIgnoreCase("TRUE"))
				return true;
			
			return false;
		}
		
		public boolean getOptBoolean(String name, boolean alt) {
			name = name.toUpperCase();
			String val = values.get(name);
			if(val == null)
				return alt;
			if(val.equalsIgnoreCase("FALSE"))
				return false;
			if(val.equalsIgnoreCase("TRUE"))
				return true;
			
			return alt;
		}
		
		public int getInteger(String name) {
			name = name.toUpperCase();
			String val = values.get(name);
			if(val == null)
				return 0;
			
			try {
				return Integer.parseInt(val);
			} catch(Exception e) {
				return 0;
			}
			
		}
		
		public int getOptInteger(String name, int alt) {
			name = name.toUpperCase();
			String val = values.get(name);
			if(val == null)
				return alt;
			
			try {
				return Integer.parseInt(val);
			} catch(Exception e) {
				return alt;
			}
			
		}
		
		public double getDouble(String name) {
			name = name.toUpperCase();
			String val = values.get(name);
			if(val == null)
				return 0;
			
			try {
				return Double.parseDouble(val);
			} catch(Exception e) {
				return 0;
			}
			
		}
		
		public double getOptDouble(String name, double alt) {
			name = name.toUpperCase();
			String val = values.get(name);
			if(val == null)
				return alt;
			
			try {
				return Double.parseDouble(val);
			} catch(Exception e) {
				return alt;
			}
			
		}
		
		public float getFloat(String name) {
			name = name.toUpperCase();
			String val = values.get(name);
			if(val == null)
				return 0;
			
			try {
				return Float.parseFloat(val);
			} catch(Exception e) {
				return 0;
			}
			
		}
		
		public float getOptFloat(String name, float alt) {
			name = name.toUpperCase();
			String val = values.get(name);
			if(val == null)
				return alt;
			
			try {
				return Float.parseFloat(val);
			} catch(Exception e) {
				return alt;
			}
			
		}
		
		public void printTree() {
			
			String ind = "";
			for(int i = 0; i < depth; i++)
				ind += INDENT;
			
			for(String key : subNodes.keySet()) {
				System.out.println(ind + key);
				subNodes.get(key).printTree();
			}
			
			for(String key : values.keySet()) {
				System.out.println(ind + key + ":   " + values.get(key));
			}
		}
	}
}
