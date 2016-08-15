/*
 * Name: rank3
 * Entry Date: 2016/4/26 22:56:17
 * Runtime: 1624 ms
 * 
 * ------- output -------
 * 
 * calc1: 
 * 18 msec
 * 
 * calc2: 
 * 374 msec
 * 
 * calc3: 
 * 1132 msec
 * 
 */
package jp.co.interprism.pg_contest.matrix_calculator;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class MatrixCalculator implements Runnable {
	private String matrixFilePath = null;
	private String problemFilePath = null;
	private String resultFilePath = null;

	public MatrixCalculator(String matrixFilePath, String problemFilePath, String resultFilePath) {
		this.matrixFilePath = matrixFilePath;
		this.problemFilePath = problemFilePath;
		this.resultFilePath = resultFilePath;
	}

	public void run() {
		WriteTask tatsk = new WriteTask();
		Thread ｔ = new Thread(tatsk);
		ｔ.start();

		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(problemFilePath, "r");
			FileChannel fch = raf.getChannel();
			MappedByteBuffer buffer = fch.map(MapMode.READ_ONLY, 0, raf.length());

			byte c;
			while (buffer.position() < buffer.capacity()) {
				if (buffer.get() == '[') {
					Q q = new Q();

					while ((c = buffer.get()) != ',') {
						q.lrow = q.lrow * 10 + Character.getNumericValue(c);
					}

					while ((c = buffer.get()) != ']') {
						q.lcol = q.lcol * 10 + Character.getNumericValue(c);
					}

					while (buffer.get() != '[') {
					}

					while ((c = buffer.get()) != ',') {
						q.rrow = q.rrow * 10 + Character.getNumericValue(c);
					}

					while ((c = buffer.get()) != ']') {
						q.rcol = q.rcol * 10 + Character.getNumericValue(c);
					}

					tatsk.offer(q);
				}
			}
			IO.close(tatsk);
			ｔ.join();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IO.close(raf, tatsk);
		}
	}

	class MatrixCache implements Closeable {
		int[] map = new int[100000];

		RandomAccessFile raf = null;
		MappedByteBuffer buffer = null;

		public MatrixCache() {
			open();
		}

		private void open() {
			try {
				raf = new RandomAccessFile(matrixFilePath, "r");
				FileChannel fch = raf.getChannel();
				buffer = fch.map(MapMode.READ_ONLY, 0, raf.length());
				buffer.mark();

				int i = 1;

				while (buffer.position() < buffer.capacity()) {
					if (buffer.get() == '\n') {
						map[i++] = buffer.position();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public int get(int row, int col) {
			try {
				buffer.position(map[row]);

				int colcount = 0;
				byte c;
				int res = 0;
				while (buffer.position() < buffer.capacity()) {
					c = buffer.get();
					if (c == '\n') {
						break;
					}
					if (colcount == col) {
						if (c == ',') {
							break;
						}
						res = res * 10 - Character.getNumericValue(c);
					} else if (c == ',') {
						colcount++;
					}
				}

				return -res;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 0;
		}

		@Override
		public void close() throws IOException {
			IO.close(raf);
		}
	}

	class WriteTask implements Runnable, Closeable {

		private boolean done = false;
		final private Queue<Q> queue = new LinkedBlockingQueue<Q>();

		public void offer(Q q) {
			queue.offer(q);
		}

		@Override
		public void run() {
			BufferedWriter os = null;
			MatrixCache cache = null;
			try {
				cache = new MatrixCache();
				os = new BufferedWriter(new FileWriter(resultFilePath));

				while (!done || 0 < queue.size()) {
					Q q;
					if ((q = queue.poll()) != null) {
						os.write(String.valueOf(cache.get(q.lrow, q.lcol) + cache.get(q.rrow, q.rcol)) + "\n");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				IO.close(os, cache);
			}
		}

		@Override
		public void close() throws IOException {
			done = true;
		}
	}

	static class Q {
		public int lrow;
		public int lcol;
		public int rrow;
		public int rcol;
	}

	static class IO {
		static void close(Closeable... cs) {
			for (Closeable c : cs) {
				if (c != null) {
					try {
						c.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
