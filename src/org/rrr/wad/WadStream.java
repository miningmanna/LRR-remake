package org.rrr.wad;

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
		return 0;
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		raf.seek(start+prog);
		int toRead = b.length;
		if(prog+b.length > len) {
			toRead = (int) (len-prog);
			if(toRead <= 0)
				return -1;
		}
		int r = raf.read(b, 0, toRead);
		prog += r;
		return r;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		raf.seek(start+prog);
		int toRead = len;
		if(prog+len > this.len) {
			toRead = (int) (this.len-prog);
			if(toRead <= 0)
				return -1;
		}
		int r = raf.read(b, off, toRead);
		prog += r;
		return r;
	}

}
