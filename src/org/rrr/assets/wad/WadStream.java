package org.rrr.assets.wad;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class WadStream extends InputStream {
	
	private RandomAccessFile raf;
	private long start;
	private long len;
	private long prog;
	
	public WadStream(RandomAccessFile raf, long start, long len) {
		this.raf = raf;
		this.start = start;
		this.len = len;
		this.prog = 0;
	}
	
	@Override
	public int read() throws IOException {
		raf.seek(start+prog);
		prog++;
		if(prog > len)
			return -1;
		return raf.read();
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		if(prog >= len)
			return -1;
		raf.seek(start+prog);
		if(b.length > available()) {
			int l = raf.read(b, 0, available());
			prog += l;
			return l;
		} else {
			int l = raf.read(b);
			prog += l;
			return l;
		}
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if(prog >= this.len)
			return -1;
		raf.seek(start+prog);
		if(b.length > available()) {
			int l = raf.read(b, 0, available());
			prog += l;
			return l;
		} else {
			int l = raf.read(b, 0, len);
			prog += l;
			return l;
		}
	}
	
	@Override
	public int available() throws IOException {
		return (int) (len-prog);
	}
	
	@Override
	public long skip(long n) throws IOException {
		if(n >= (len-prog)) {
			prog = len-1;
			return (len-prog-1);
		} else {
			prog += n;
			return n;
		}
	}
}
