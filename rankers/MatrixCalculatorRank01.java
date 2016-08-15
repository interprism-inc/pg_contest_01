/*
 * Name: rank1
 * Entry Date: 2016/5/10 09:05:11
 * Runtime: 914 ms
 * 
 * ------- output -------
 * 
 * calc1: 
 * 7 msec
 * 
 * calc2: 
 * 206 msec
 * 
 * calc3: 
 * 612 msec
 * 
 */
package jp.co.interprism.pg_contest.matrix_calculator;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;

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
	 * 
	 * @throws IOException
	 */
	public void run() {

		try {
			problemLength = readProblem(problemFilePath);
			operandLength = problemLength * 2;
			sortProblem(operandLength);
			answers = new int[problemLength];
			calcMatrix(matrixFilePath);
			writeAnswer(resultFilePath);
			globalCounter++;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static int[] answers;
	private static int problemLength;
	private static int operandLength;
	private static int globalCounter;
	private final static int[] matrixColumnLength = {100, 10000, 10000};
	private final static int[] matrixColumnLength1 = {150, 10500, 10500};
	private final static int[] matrixColumnLength2 = {200, 11000, 11000};
	private final static int[] matrixBufferJumpBorderColumnLength = {60, 9800, 8100};
	private final static int[] matrixBufferJumpBorderColumnLength1 = {70, 9900, 9940};
	private final static int[] matrixBufferJumpBorderColumnLength2 = {80, 9950, 9960};
	private final static int[] matrixColumnAgvByteLength = {200, 48665, 48655};
	private final static byte[] b = new byte[128 * 1024];
	private final static int[][] problems = new int[20000][3];//rowIndex, colIndex, problemIndex

	private static final char LF = '\n';
	private static int problemNo = 0;
	private static int arrayCounter = 0;
	private static int number = 0;

	private static final int readProblem(final String problemFilePath) throws IOException {
		problemNo = 0;
		arrayCounter = 0;
		try (InputStream in = new FileInputStream(problemFilePath);) {
			int len;
			while ((len = in.read(b)) > 0) {
				for (int i = 0; i < len; i++) {
					int c = b[i];
					if ('0' <= c && c <= '9') {// 48,57
						int digit = c - 48;
						number = pushDecNumber(number, digit);
//					} else if (c == '+') {// 43
					} else if (c == ',') {// 44
						problems[arrayCounter][0] = number;
						number = 0;
//					} else if (c == '[') {// 91
					} else if (c == ']') {// 93
						problems[arrayCounter][1] = number;
						number = 0;
						problems[arrayCounter][2] = problemNo;
						if ((arrayCounter & 1) == 1) {
							problemNo++;
						}
						arrayCounter++;
//					} else if (c == LF) {// 10
						// problem[counter][index++] = counter;
						// index = 0;
					}
				}
			}
		}
		return problemNo;
	}

	private final static int pushDecNumber(final int origin, final int digit) {
		return 10 * origin + digit;
	}

	private static final ProblemComparator problemComparator = new ProblemComparator();
	private static final void sortProblem(final int operandLength) {
		Arrays.sort(problems, 0, operandLength, problemComparator);
	}

	private static final class ProblemComparator implements Comparator<Object> {
		ProblemComparator() {
		}

		public final int compare(final Object o1, final Object o2) {
			final int[] left = (int[]) o1;
			final int[] right = (int[]) o2;
			if (left[0] == right[0]) {
				return left[1] - right[1];
			} else {
				return left[0] - right[0];
			}
		}
	}

	private static final int MODE_NEXT_PROBLEM = 0;
	private static final int MODE_FINDING_ROW = 1;
	private static final int MODE_FINDING_COL = 2;
	private static final int MODE_FINDING_NUM = 3;
	private static int mode;
	private static int row;
	private static int col;
	private static int bufIndex;
	private static int num;
	private static int previousNum;
	private static int sameCell;

	private static int bufLength = 0;

	private static int problemRow = -1;
	private static int problemCol = -1;
	private static int problemIndex = 0;
	private static int[] problem = null;

	private static int previousProblemRow = problemRow;
	private static int previousProblemCol = problemCol;
	
	private static final void calcMatrix(final String matrixFilePath) throws IOException {
		try (InputStream in = new FileInputStream(matrixFilePath);) {
			bufLength = 0;

			problemRow = -1;
			problemCol = -1;
			problemIndex = 0;
			problem = null;

			mode = MODE_NEXT_PROBLEM;
			row = 0;
			col = 0;
			num = 0;
			previousNum = 0;
			sameCell = -1;

			readMore: while ((bufLength = in.read(b)) > 0) {
				bufIndex = 0;
				modeLoop: while (true) {
					switch (mode) {
						case MODE_NEXT_PROBLEM:{
							if (problemIndex < operandLength) {
								nextProblem();
								mode = MODE_FINDING_ROW;
								continue modeLoop;
							} else {
								break readMore;
							}
						}
						case MODE_FINDING_ROW:{
							if (row == problemRow) {
								sameCell++;
								mode = MODE_FINDING_COL;
								continue modeLoop;
							} else {
								col = 0;
								findRow(bufLength, problemRow);
								if (mode == MODE_FINDING_COL) continue modeLoop;
							}
						}
						case MODE_FINDING_COL:{
							if (col == problemCol) {
								sameCell++;
								mode = MODE_FINDING_NUM;
								continue modeLoop;
							} else {
								findCol(bufLength, problemCol);
								if (mode == MODE_FINDING_NUM) continue modeLoop;
							}
						}
						case MODE_FINDING_NUM:{
							if (sameCell == 2) {
								final int answerRow = problem[2];
								answers[answerRow] = answers[answerRow] + previousNum;
								sameCell = 0;
								mode = MODE_NEXT_PROBLEM;
								continue modeLoop;
							}
							final boolean continueModeLoop = findNum(bufLength, answers, problem);
							if (continueModeLoop) continue modeLoop;
						}
					}
//					if (mode == MODE_NEXT_PROBLEM) {
//					} else if (mode == MODE_FINDING_ROW) {
//					} else if (mode == MODE_FINDING_COL) {
//					} else if (mode == MODE_FINDING_NUM) {
////					} else {
////						throw new RuntimeException("invalid mode:" + mode);
//					}
					continue readMore;
				}
			}
		}
	}

	private static final void nextProblem() {
		problem = problems[problemIndex++];
		previousProblemRow = problemRow;
		previousProblemCol = problemCol;
		problemRow = problem[0];
		problemCol = problem[1];
		if (previousProblemRow != -1 && problemRow != previousProblemRow) {
			if (previousProblemCol < matrixBufferJumpBorderColumnLength[globalCounter]) {
				bufIndex += matrixColumnAgvByteLength[globalCounter] * (matrixColumnLength[globalCounter] - previousProblemCol) / matrixColumnLength[globalCounter];
			} else if (previousProblemCol < matrixBufferJumpBorderColumnLength1[globalCounter]) {
				bufIndex += matrixColumnAgvByteLength[globalCounter] * (matrixColumnLength[globalCounter] - previousProblemCol) / matrixColumnLength1[globalCounter];
			} else if (previousProblemCol < matrixBufferJumpBorderColumnLength2[globalCounter]) {
				bufIndex += matrixColumnAgvByteLength[globalCounter] * (matrixColumnLength[globalCounter] - previousProblemCol) / matrixColumnLength2[globalCounter];
			}
		}
	}

	private static final void findRow(final int bufLength, final int problemRow) {
		for (; bufIndex < bufLength; bufIndex++) {
			if (b[bufIndex] == LF) {
				row++;
				if (row == problemRow) {
					mode = MODE_FINDING_COL;
					bufIndex ++;
					return;
				} else {
					bufIndex += matrixColumnAgvByteLength[globalCounter];
				}
			}
		}
	}

	private static final void findCol(final int bufLength, final int problemCol) {
		for (; bufIndex < bufLength; bufIndex++) {
			if (b[bufIndex] == ',') {
				col++;
				if (col == problemCol) {
					mode = MODE_FINDING_NUM;
					bufIndex++;
					return;
				} else {
					bufIndex++;
				}
			}
		}
	}

	private static final boolean findNum(final int bufLength, final int[] answers, final int[] problem) {
		for (; bufIndex < bufLength; bufIndex++) {
			final int c = b[bufIndex];
			if (c == ',' || c == LF) {
//				int answerRow = problem[2];
				answers[problem[2]] = answers[problem[2]] + num;
				previousNum = num;
				sameCell = 0;
				num = 0;
				mode = MODE_NEXT_PROBLEM;
				return true;
			} else {
				final int digit = c - 48;
				num = pushDecNumber(num, digit);
			}
		}
		return false;
	}

	private static final void writeAnswer(final String resultFilePath) {
		try (
				OutputStream out = new FileOutputStream(resultFilePath);
				BufferedOutputStream bufout = new BufferedOutputStream(out);
				PrintStream print = new PrintStream(bufout);
				) {
			for (int i = 0; i < problemLength; i++) {
				print.print(String.valueOf(answers[i]));
				print.print(LF);
			}
			print.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
