/*
 * Name: rank9
 * Entry Date: 2016/4/27 07:53:36
 * Runtime: 7764 ms
 * 
 * ------- output -------
 * 
 * calc1: 
 * 128 msec
 * 
 * calc2: 
 * 2008 msec
 * 
 * calc3: 
 * 5515 msec
 * 
 */
package jp.co.interprism.pg_contest.matrix_calculator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	 * @param matrixFilePath マトリクス（入力）ファイル
	 * @param problemFilePath 問題ファイル
	 * @param resultFilePath 回答結果ファイル
	 */
	public MatrixCalculator(String matrixFilePath, String problemFilePath, String resultFilePath) {
		this.matrixFilePath = matrixFilePath;
		this.problemFilePath = problemFilePath;
		this.resultFilePath = resultFilePath;
	}

	private static class Index implements Comparable<Index> {
		public int r;
		public int c;

		public Index(int r, int c) {
			this.r = r;
			this.c = c;
		}

		@Override
		public int hashCode() {
			return r + 10000 + c;
		}

		@Override
		public int compareTo(Index o) {
			if (this.r < o.r) {
				return -1;
			} else {
				if (this.r == o.r) {
					return this.c - o.c;
				} else {
					return 1;
				}
			}
		}

		@Override
		public boolean equals(Object obj) {
			Index tgt = (Index)obj;
			return this.r == tgt.r && this.c == tgt.c;
		}
	}

	/**
	 * マトリクス（入力）ファイルと問題ファイルを読み取り、問題の回答を結果ファイルに出力する
	 */
	public void run() {
		try {
			File resultFile = new File(resultFilePath);
			if (resultFile.exists()) {
				resultFile.delete();
			}

			start();
			HashMap<Index, Integer> problemCacheMap = new HashMap<>(10000);
			System.out.println("init hash map: " + finish());

			start();
			final Pattern problemIndexPattern = Pattern.compile("\\[(\\d+),(\\d+)\\]\\+\\[(\\d+),(\\d+)\\]=");
			try (BufferedReader problemReader = new BufferedReader(new FileReader(problemFilePath))) {
				while (true) {
					String problem = problemReader.readLine();
					if (problem == null) {
						break;
					}
					Matcher indexMatcher = problemIndexPattern.matcher(problem);
					if (indexMatcher.find()) {
						Index left = new Index(Integer.parseInt(indexMatcher.group(1)), Integer.parseInt(indexMatcher.group(2)));
						Index right = new Index(Integer.parseInt(indexMatcher.group(3)), Integer.parseInt(indexMatcher.group(4)));

						if (!problemCacheMap.containsKey(left)) {
							problemCacheMap.put(left, -1);
						}
						if (!problemCacheMap.containsKey(right)) {
							problemCacheMap.put(right, -1);
						}
					}
				}
			} finally {

			}
			System.out.println("prepare problem index cache: " + finish());

			start();
			try (BufferedReader matrixReader = new BufferedReader(new FileReader(matrixFilePath))) {
				int currentRow = -1;
				String[] splittedMatrixLine = null;

				start2();
				Index[] problemIndexes = problemCacheMap.keySet().stream().sorted().toArray(i -> new Index[i]);
				System.out.println("sort index: " + finish2());

				int i = 0;
				while (true) {
					final String matrixLine = matrixReader.readLine();
					if (matrixLine == null) {
						break;
					}
					splittedMatrixLine = null;
					currentRow++;
					while (true) {
						if (i >= problemIndexes.length) {
							break;
						}
						Index index = problemIndexes[i];
						if (index.r > currentRow) {
							break;
						} else if (index.r == currentRow) {
							if (splittedMatrixLine == null) {
								splittedMatrixLine = matrixLine.split(",");
							}
							int value = Integer.parseInt(splittedMatrixLine[index.c]);
							problemCacheMap.put(index, value);
							i++;
						} else {
							break;
						}
					}
					if (i == problemIndexes.length) {
						break;
					}
				}
			} catch (IOException ioe2) {
			}
			System.out.println("create cache: " + finish());


//			printCache(problemCacheMap);

			start();
			try (BufferedReader problemReader = new BufferedReader(new FileReader(problemFilePath))) {
				try (BufferedWriter resultWriter = new BufferedWriter(new FileWriter(resultFilePath))) {
					while (true) {
						String problem = problemReader.readLine();
						if (problem == null) {
							break;
						}
						Matcher indexMatcher = problemIndexPattern.matcher(problem);
						if (indexMatcher.find()) {
							Index left = new Index(Integer.parseInt(indexMatcher.group(1)), Integer.parseInt(indexMatcher.group(2)));
							Index right = new Index(Integer.parseInt(indexMatcher.group(3)), Integer.parseInt(indexMatcher.group(4)));
							int result = problemCacheMap.get(left) + problemCacheMap.get(right);

//							System.out.println("[" + left.r + "," + left.c + "] + [" + right.r + "," + right.c + "] = " + result);

							resultWriter.write(String.valueOf(result));
							resultWriter.newLine();
						}
					}
				}
			} finally {

			}
			System.out.println("write result: " + finish());

		} catch (IOException ieo) {
			System.err.println(ieo);
		}
	}

	private void printCache(HashMap<Index, Integer> cache) {
		cache.entrySet().stream().forEach(e -> {
			System.out.println("[" + e.getKey().r + "," + e.getKey().c + "] = " + e.getValue());
		});
	}

	private static Date started;
	private static void start() {
		started = new Date();
	}
	private static long finish() {
		return new Date().getTime() - started.getTime();
	}

	private static Date started2;
	private static void start2() {
		started2 = new Date();
	}
	private static long finish2() {
		return new Date().getTime() - started2.getTime();
	}
}
