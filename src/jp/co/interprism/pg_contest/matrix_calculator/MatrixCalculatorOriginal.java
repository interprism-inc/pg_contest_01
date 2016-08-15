package jp.co.interprism.pg_contest.matrix_calculator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hirano
 */
public class MatrixCalculatorOriginal {
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
	public MatrixCalculatorOriginal(String matrixFilePath, String problemFilePath, String resultFilePath) {
		this.matrixFilePath = matrixFilePath;
		this.problemFilePath = problemFilePath;
		this.resultFilePath = resultFilePath;
	}

	/**
	 * マトリクス（入力）ファイルと問題ファイルを読み取り、問題の回答を結果ファイルに出力する
	 */
	public void run() {
		try {
			final Pattern problemIndexPattern = Pattern.compile("\\[(\\d+),(\\d+)\\]\\+\\[(\\d+),(\\d+)\\]=");
			try (BufferedReader problemReader = new BufferedReader(new FileReader(problemFilePath))) {
				File resultFile = new File(resultFilePath);
				if (resultFile.exists()) {
					resultFile.delete();
				}
				try (BufferedWriter resultWriter = new BufferedWriter(new FileWriter(resultFilePath))) {
					while (true) {
						String problem = problemReader.readLine();
						if (problem == null) {
							break;
						}
						Matcher indexMatcher = problemIndexPattern.matcher(problem);
						if (indexMatcher.find()) {
							int rowLeft = Integer.parseInt(indexMatcher.group(1));
							int columnLeft = Integer.parseInt(indexMatcher.group(2));
							int rowRight = Integer.parseInt(indexMatcher.group(3));
							int columnRight = Integer.parseInt(indexMatcher.group(4));

							int valueLeft = valueAt(rowLeft, columnLeft);
							int valueRight = valueAt(rowRight, columnRight);

							System.out.println(valueLeft + valueRight);
							resultWriter.write(String.valueOf(valueLeft + valueRight));
							resultWriter.newLine();
						}
					}
				}
			} finally {

			}
		} catch (IOException ieo) {
			System.err.println(ieo);
		}
	}

	private int valueAt(int row, int column) throws IOException {
		try (BufferedReader matrixReader = new BufferedReader(new FileReader(matrixFilePath))) {
			String matrixLine = null;
			for (int i=0; i<=row; i++) {
				matrixLine = matrixReader.readLine();
			}

			int value = Integer.parseInt(matrixLine.split(",")[column]);

//            System.out.println("[" + row + "," + column + "]=" + value);

			return value;
		}
	}
}
