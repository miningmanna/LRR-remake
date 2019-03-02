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
	public int available() throws IOException {
		return (int) (len-prog-1);
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
