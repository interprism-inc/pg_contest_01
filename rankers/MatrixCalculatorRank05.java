/*
 * Name: rank5
 * Entry Date: 2016/4/27 09:41:40
 * Runtime: 3276 ms
 * 
 * ------- output -------
 * 
 * calc1: 
 * 21 msec
 * 
 * calc2: 
 * 1179 msec
 * 
 * calc3: 
 * 1957 msec
 * 
 */
package jp.co.interprism.pg_contest.matrix_calculator;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;

/**
 * 
 */
public class MatrixCalculator {
	/**
	 * マトリクス（入力）ファイル（読み取り用）
	 */
	private String matrixFilePath = null;

	/**
	 * 問題ファイル（読み取り用）
	 */
	private String problemFilePath = null;

	/**
	 * 回答結果ファイル（出力用）
	 */
	private String resultFilePath = null;

	/**
	 * インスタンス生成
	 * 
	 * @param matrixFilePath
	 *            マトリクス（入力）ファイル
	 * @param problemFilePath
	 *            問題ファイル
	 * @param resultFilePath
	 *            回答結果ファイル
	 */
	public MatrixCalculator(String matrixFilePath, String problemFilePath, String resultFilePath) {
		this.matrixFilePath = matrixFilePath;
		this.problemFilePath = problemFilePath;
		this.resultFilePath = resultFilePath;
	}

	/**
	 * マトリクス（入力）ファイルと問題ファイルを読み取り、問題の回答を結果ファイルに出力する
	 */

	private MatrixReader matrixReader;
	private ProblemReader problemReader;
	private CalculateProblem calculateProblem;

	public void run() {
		method();
	}

	private void method() {
		try {
			problemReader = new ProblemReader(new FileReader(problemFilePath), 16384);
			problemReader.readProblem();		
			matrixReader = new MatrixReader(new FileReader(matrixFilePath), 8192, problemReader.getNeedElem());
			Entry[] entries = matrixReader.readMatrix();
			calculateProblem = new CalculateProblem(new FileWriter(resultFilePath), 8192, problemReader.getProblem(),
					entries);
			calculateProblem.execute();
		} catch (IOException e) {
			System.out.println(e);
		}

	}

}

class Entry {
	Entry next;
	int row;
	int colmn;
	char[] value;
}

// =============== ProblemReader Class ====================
class ProblemReader {
	private Reader in;
	private char[] cb;
	private int nChars;
	private int nextChar;
	private char[] result;
	private boolean flag;
	private int colmn;
	private int row;
	private boolean[][] needElem;
	private int[][] problemInt;
	private int pbcolmn;
	public static boolean isLongM;
	public static int rowMax = 0;
	public static int colmnMax = 0;

	public ProblemReader(Reader in, int sz) {
		this.in = in;
		cb = new char[sz];

	}

	public boolean[][] getNeedElem() {
		return needElem;
	}

	public int[][] getProblem() {
		return problemInt;
	}

	public void pushNeed(int row, int colmn) {
		if (needElem == null)
			needElem = new boolean[10000][];
		if (needElem[row] == null)
			needElem[row] = new boolean[10000];
		if (needElem[row][colmn] == false) {
			needElem[row][colmn] = true;
		}
	}

	public void pushProblem(int row, int colmn, boolean isfirst, int pbColm) {
		if (problemInt == null)
			problemInt = new int[10000][];
		if (problemInt[pbColm] == null)
			problemInt[pbColm] = new int[4];

		if (isfirst) {
			problemInt[pbColm][0] = row;
			problemInt[pbColm][1] = colmn;
		} else {
			problemInt[pbColm][2] = row;
			problemInt[pbColm][3] = colmn;
		}
	}

	public void readProblem() throws IOException {
		isLongM = false;
		flag = true;
		pbcolmn = 0;
		while (true) {
			Object object = readProblem2();
			if (object == null) {
				break;
			}
		}
	}

	public Object readProblem2() throws IOException {
		int startChar;
		int n;
		int i;
		char[] newc;
		for (;;) {
			if (nextChar >= nChars) {
				do {
					n = in.read(cb, 0, cb.length);
				} while (n == 0);
				if (n > 0) {
					nChars = n;
					nextChar = 0;
				}
				if (nextChar >= nChars) { /* EOF */
					return null;
				}
			}

			char c = 0;
			boolean eol = false;
			boolean comma = false;
			boolean left = false;
			boolean right = false;
			boolean plus = false;
			boolean eq = false;
			charLoop: for (i = nextChar; i < nChars; i++) {
				c = cb[i];
				switch (c) {
				case ',':
					comma = true;
					break charLoop;
				case ']':
					right = true;
					break charLoop;
				case '\n':
					eol = true;
					break charLoop;
				case '=':
					eq = true;
					break charLoop;
				case '[':
					left = true;
					break charLoop;
				case '+':
					plus = true;
					break charLoop;
				}
			}
			startChar = nextChar;
			nextChar = i;

			if (right) {
				if (result != null) {
					// 結合
					newc = copyOfRange(cb, startChar, i);
					int len = newc.length;
					int count = result.length;
					result = copyOf(result, count + len);
					System.arraycopy(newc, 0, result, count, len - 0);
				} else {
					result = copyOfRange(cb, startChar, i);
				}
				colmn = parseInt(result);
				if (colmnMax < colmn) {
					colmnMax = colmn;
				}
				if (flag) {
					// 最初
					pushProblem(row, colmn, true, pbcolmn);
					pushNeed(row, colmn);
					flag = false;
					nextChar += 3;
				} else {
					// 最後
					pushProblem(row, colmn, false, pbcolmn++);
					pushNeed(row, colmn);
					flag = true;
					nextChar += 4;
				}
				result = null;
				return 1;
			}

			if (comma) {
				if (result != null) {
					// 結合
					newc = copyOfRange(cb, startChar, i);
					int len = newc.length;
					int count = result.length;
					result = copyOf(result, count + len);
					System.arraycopy(newc, 0, result, count, len - 0);
				} else
					result = copyOfRange(cb, startChar, i);

				row = parseInt(result);
				if (rowMax < row) {
					rowMax = row;
				}
				if (!isLongM && row > 100) {
					isLongM = true;
				}
				nextChar++;
				result = null;
				return 1;
			}
			if (left) {
				nextChar++;
				return 1;
			}
			if (eq) {
				flag = true;
				nextChar += 2;
				return 1;
			}

			if (eol) {
				flag = true;
				nextChar += 2;
				return 1;
			}
			if (plus) {
				flag = false;
				nextChar += 2;
				return 1;
			}
			result = copyOfRange(cb, startChar, i);
		}

	}

	public static char[] copyOfRange(char[] original, int from, int to) {
		int newLength = to - from;
		char[] copy = new char[newLength];
		System.arraycopy(original, from, copy, 0, Math.min(original.length - from, newLength));
		return copy;
	}

	public static char[] copyOf(char[] original, int newLength) {
		char[] copy = new char[newLength];
		System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
		return copy;
	}

	public static int parseInt(char[] s) throws IOException {
		return parseInt(s, 10);
	}

	public static int parseInt(char[] s, int radix) throws IOException {
		int result = 0;
		boolean negative = false;
		int i = 0, len = s.length;
		int digit;
		while (i < len) {
			digit = Character.digit(s[i++], radix);
			result *= radix;
			result -= digit;
		}
		return negative ? result : -result;
	}
}

// =============== MatrixReader Class =====================
class MatrixReader{
	public static int defaultLen = 16384;// 65536;//
											// 1024,2048,4096,8192,16384,32768,65536
	private Reader in;
	Entry[] entrys;
	private int nextChar;
	private int nChars;
	private char[] cb;
	private int colmn;
	private int row;
	private char[] result;
	private boolean[][] needelem;
	char[] newc;
	private boolean isLongM;
	private boolean isMax = false;

	private int rowMax;
	private int a;
	private int b;
	private static final int LEN1 = 9999;
	private static final int LEN2 = 99;

	public MatrixReader(Reader in, int sz, boolean[][] needelem) {
		this.in = in;
		nextChar = nChars = 0;
		this.needelem = needelem;
		this.isLongM = ProblemReader.isLongM;

		if (isLongM) {
			cb = new char[sz];
			a = LEN1 - 1;
		} else {
			cb = new char[(int) sz / 10];
			a = LEN2 - 1;
		}
		b = a - 8;
		rowMax = ProblemReader.rowMax;

	}

	public Entry[] readMatrix() throws IOException {
		row = 0;
		colmn = 0;
		Object object;

		while (true) {
			object = readMatrixL();
			if (object == null) {
				in.close();
				return entrys;
			}
		}

	}

	private void push(int row, int colmn, char[] value) {
		final int enint = (row + colmn) % defaultLen;
		if (entrys == null)
			entrys = new Entry[defaultLen];
		if (entrys[enint] != null) {
			Entry temp = entrys[enint];
			entrys[enint] = new Entry();
			entrys[enint].next = temp;
		} else {
			entrys[enint] = new Entry();
		}
		entrys[enint].row = row;
		entrys[enint].colmn = colmn;
		entrys[enint].value = value;
	}

	public Object readMatrixL() throws IOException {
		int startChar;
		int n;
		int i;

		for (;;) {
			if (nextChar >= nChars) {
				do {
					n = in.read(cb, 0, cb.length);// cbに読み込み
				} while (n == 0);
				if (n > 0) {
					nChars = n;
					nextChar = 0;
				}
				if (nextChar >= nChars) /* EOF */
					return null;

			}
			char c = 0;
			boolean eol = false;
			boolean comma = false;
			if (!isMax && !rowisnull) {
				charLoop: for (i = nextChar; i < nChars; i++) {
					c = cb[i];
					if (c == 0x2c) {
						comma = true;
						break charLoop;
					}
				}
			} else {
				charLoop: for (i = nextChar; i < nChars; i++) {
					c = cb[i];
					if (c == 0x0A) {
						eol = true;
						isMax = false;
						break charLoop;
					}
				}
			}
			startChar = nextChar;
			nextChar = i;
			if (comma) {
				if (needelem[row] != null) {
					if (needelem[row][colmn]) {
						if (result != null) {
							// 結合
							newc = copyOfRange(cb, startChar, i);
							int len = newc.length;
							int count = result.length;
							result = copyOf(result, len + count);
							System.arraycopy(newc, 0, result, count, len - 0);

						} else {
							result = copyOfRange(cb, startChar, i);
						}
						push(row, colmn, result);
					}
				} else {
					if (row == rowMax + 1)
						return null;
					rowisnull = true;
					nextChar += b;// buffer reRead
				}
				nextChar++;
				colmn++;
				result = null;
				if (colmn > a)
					isMax = true;
				return 1;

			}

			if (eol) {
				if (needelem[row] != null) {
					if (needelem[row][colmn]) {
						if (result != null) {
							// 結合
							newc = copyOfRange(cb, startChar, i);
							int len = newc.length;
							int count = result.length;
							result = copyOf(result, len + count);
							System.arraycopy(newc, 0, result, count, len - 0);
						} else {
							result = copyOfRange(cb, startChar, i);
						}
						push(row, colmn, result);
					}
				} else {
					rowisnull = false;
				}
				nextChar++;
				colmn = 0;
				row++;
				result = null;
				return 1;
			}
			result = copyOfRange(cb, startChar, startChar + i - startChar);
		}
	}

	boolean rowisnull = false;

	public static char[] copyOfRange(char[] original, int from, int to) {
		int newLength = to - from;
		if (newLength < 0)
			throw new IllegalArgumentException(from + " > " + to);
		char[] copy = new char[newLength];
		System.arraycopy(original, from, copy, 0, Math.min(original.length - from, newLength));
		return copy;
	}

	public static char[] copyOf(char[] original, int newLength) {
		char[] copy = new char[newLength];
		System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
		return copy;
	}


}

class CalculateProblem extends BufferedWriter {
	private int[][] problem;
	private Entry[] entries;
	private static int defaultCharBufferSize = 8192;

	public CalculateProblem(Writer out, int sz, int[][] problem, Entry[] entries) {
		super(out, defaultCharBufferSize);
		this.problem = problem;
		this.entries = entries;

	}

	public int get(int row, int colmn) throws IOException {
		final int intR = (row + colmn) % MatrixReader.defaultLen;
		Entry entry = entries[intR];
		while (true) {
			if (entry != null) {
				if (entry.row == row && entry.colmn == colmn) {
					int returnInt = parseInt(entry.value);
					return returnInt;
				} else {
					entry = entry.next;
				}
			}
		}
	}

	public static int parseInt(char[] s) throws IOException {
		return parseInt(s, 10);
	}

	public static int parseInt(char[] s, int radix) throws IOException {
		int result = 0;
		boolean negative = false;
		int i = 0, len = s.length;
		int digit;
		while (i < len) {
			digit = Character.digit(s[i++], radix);
			result *= radix;
			result -= digit;
		}
		return negative ? result : -result;
	}

	public void execute() throws IOException {

		execute(true);

		close();
	}

	public void execute(boolean flag) throws IOException {
		int result, result1, result2 = 0;
		for (int i = 0; i < problem.length; i++) {
			if (problem[i] == null)
				break;
			result1 = get(problem[i][0], problem[i][1]);
			result2 = get(problem[i][2], problem[i][3]);
			result = result1 + result2;
			super.write(String.valueOf(result) + "\n");
		}
	}

}


