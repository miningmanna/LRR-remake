package org.rrr.assets.nerp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class NerpLuaCompiler {
	
	public static void main(String[] args) {
		
		if(args.length != 2)
			System.out.println("Arguments: <NerpInputFile> <LuaOutputFile>");
		
		BufferedReader in = null;
		BufferedWriter out = null;
		try {
			in = new BufferedReader(new FileReader(args[0]));
			out = new BufferedWriter(new FileWriter(args[1]));
		} catch (FileNotFoundException e) {
			System.out.println("Could not find: " + args[0]);
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		String line = null;
		try {
			while((line = in.readLine()) != null) {
				compileLine(out, line);
			}
		} catch (Exception e1) {
			System.out.println("An error happened during converting:");
			e1.printStackTrace();
			
		} finally {
			try {
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		
	}
	
	static String indent = "";
	public static void compileLine(BufferedWriter bw, String s) throws Exception {
		
		if(s.equals("")) {
			bw.write("\n");
		} else if(s.startsWith("//") || s.startsWith(";")) {
			if(s.startsWith(";"))
				s = s.substring(1);
			else
				s = s.substring(2);
			bw.write("--" + s + "\n");
		} else if(count(s, '?') == 1) {
			String[] split = s.split(" \\? ");
			String[] half1 = stripComments(split[0].trim()).split(" ");
			String[] half2 = stripComments(split[1].trim()).split(" ");
			if(half1.length == 1 || half1.length == 3) {
				
				String appendix = "";
				String prefix = "";
				if(half1.length == 1) {
					if(!half1[0].equals("TRUE")) {
						bw.write(indent + "if " + half1[0] + "() then\n");
						prefix = indent + "  ";
						appendix = indent + "end\n";
					}
				} else {
					if(!isOperand(half1[1])) {
						System.out.println("Missing operand: " + s);
						throw new Exception();
					}
					String op = " " + getLuaOperand(half1[1]) + " ";
					String v1 = half1[0];
					String v2 = half1[2];
					if(!isValue(v1))
						v1 += "()";
					if(!isValue(v2))
						v2 += "()";
					
					bw.write(indent + "if " + v1 + op + v2 + " then\n");
					prefix = indent + "  ";
					appendix = indent + "end\n";
				}
				
				if(half2[0].startsWith(":")) {
					bw.write(prefix + "goto " + half2[0].substring(1) + "\n");
					if(half2.length == 2) {
						if(!half2[half2.length-1].endsWith(":")) {
							System.out.println("No Label?");
							throw new Exception();
						}
						bw.write("::" + half2[1] + ":\n");
					}
				} else {
					int paramCount = half2.length-1;
					if(half2[half2.length-1].endsWith(":")) {
						paramCount--;
						appendix = "::" + half2[half2.length-1] + ":\n" + appendix;
					}
					if(half2[0].equals("Stop")) {
						bw.write(prefix + "return\n");
					} else {
						bw.write(prefix + half2[0] + "(");
						for(int i = 1; i < paramCount-1; i++) {
							bw.write(half2[i]);
							if(!isValue(half2[i]))
								bw.write("()");
							bw.write(", ");
						}
						if(paramCount != 0) {
							bw.write(half2[1 + paramCount-1]);
							if(!isValue(half2[1 + paramCount-1]))
								bw.write("()");
						}
						bw.write(")\n");
					}
				}
				bw.write(appendix);
			} else {
				System.out.println("Invalid statement: " + s);
				throw new Exception();
			}
		} else if(count(s, '?') > 1) {
			
		} else if(s.trim().endsWith(":")) {
			bw.write("::" + s + ":\n");
		}
		
	}
	
	public static int count(String str, char c) {
		int count = 0;
		for(int i = 0; i < str.length(); i++) {
			if(str.charAt(i) == c)
				count++;
		}
		return count;
	}
	
	public static String[] OPS = {
			"=",
			"!=",
			">",
			"<"
	};
	public static String[] LUA_OPS = {
			"==",
			"~=",
			">",
			"<"
	};
	public static boolean isOperand(String s) {
		for(int i = 0; i < OPS.length; i++)
			if(OPS[i].equals(s))
				return true;
		return false;
	}
	public static String getLuaOperand(String s) {
		for(int i = 0; i < OPS.length; i++)
			if(OPS[i].equals(s))
				return LUA_OPS[i];
		return null;
	}
	public static boolean isValue(String s) {
		try {
			Integer.parseInt(s);
		} catch(Exception e) {
			return false;
		}
		return true;
	}
	public static String stripComments(String s) {
		int i = s.length();
		if(s.contains("//")) {
			i = s.indexOf("//");
		}
		if(s.contains(";")) {
			int i2 = s.indexOf(";");
			if(i2 < i)
				i = i2;
		}
		return s.substring(0, i);
	}
}
