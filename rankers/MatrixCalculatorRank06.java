/*
 * Name: rank6
 * Entry Date: 2016/4/26 00:47:37
 * Runtime: 3603 ms
 * 
 * ------- output -------
 * 
 * calc1: 
 * 39 msec
 * 
 * calc2: 
 * 1334 msec
 * 
 * calc3: 
 * 2155 msec
 * 
 */
package jp.co.interprism.pg_contest.matrix_calculator;

import java.io.*;
import java.util.*;
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

	/**
	 * マトリクス（入力）ファイルと問題ファイルを読み取り、問題の回答を結果ファイルに出力する
	 */
	public void run() {
		List<Question> questionList = new ArrayList<>();
        Map<Integer, RowMatrix> rowNumberAndMatrixMappings = new HashMap<>();

		try (BufferedReader questionReader = new BufferedReader(new  FileReader(new File(problemFilePath)), 200000)) {
			String line;
			while ((line = questionReader.readLine()) != null) {
				Question question = new Question(line);
				int leftRow = question.getLeft().getRow();
				int rightRow = question.getRight().getRow();

				if (rowNumberAndMatrixMappings.get(leftRow) == null) {
					rowNumberAndMatrixMappings.put(leftRow, new RowMatrix());
				}
				rowNumberAndMatrixMappings.get(leftRow).addMatrix(question.getLeft());

				if (rowNumberAndMatrixMappings.get(rightRow) == null) {
					rowNumberAndMatrixMappings.put(rightRow, new RowMatrix());
				}
				rowNumberAndMatrixMappings.get(rightRow).addMatrix(question.getRight());

				questionList.add(question);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (BufferedReader matrixReader = new BufferedReader(new FileReader(new File(matrixFilePath)), 10000000)) {
			int rowNumber = 0;
			String line;
			while ((line = matrixReader.readLine()) != null) {
				RowMatrix rowMatrix = rowNumberAndMatrixMappings.get(rowNumber);
				if (rowMatrix != null) {
                    rowMatrix.setMatrixValue(line);
				}
				rowNumber++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (PrintWriter resultWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(resultFilePath))))) {
			for (Question question : questionList) {
				resultWriter.println(question.answer());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 問題
	 */
	private class Question {

		// 問題文のフォーマット
		private static final String FORMAT_REGEX = "^\\[(\\d+),(\\d+)\\]\\+\\[(\\d+),(\\d+)\\]";

		// 加算の左辺
		private final Matrix left;

		// 加算の右辺
		private final Matrix right;

		public Question(String questionSentence) {
			Pattern p = Pattern.compile(FORMAT_REGEX);
			Matcher m = p.matcher(questionSentence);
			if (!m.find()) {
                System.out.println("questionSentence = [" + questionSentence + "]");
                throw new RuntimeException("問題文のフォーマットが間違っています。出題者にクレームの電話を入れてください。");
			}
			left = new Matrix(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
			right = new Matrix(Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)));
		}

		public Matrix getRight() {
			return right;
		}

		public Matrix getLeft() {
			return left;
		}

		public int answer() {
			return right.getValue() + left.getValue();
		}

		@Override
		public String toString() {
			return "left : " + left.toString() + ", right : " + right.toString();
		}
	}

	/**
	 * 問題文の計算対象となる行列
	 */
	private class Matrix {
		private final int row;
		private final int column;

		private int value;

		public Matrix(int row, int column) {
			this.row = row;
			this.column = column;
		}

		public int getRow() {
			return row;
		}

		public int getColumn() {
			return column;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return "[" + row + "," + column + "]";
		}
	}

    /**
     * 行内の必要な値を管理する
     */
    private class RowMatrix {
        private int maxColumnIndex;
        private Map<Integer, Set<Matrix>> columnNumberAndMatrixSetMappings;

        public RowMatrix() {
            maxColumnIndex = 0;
            columnNumberAndMatrixSetMappings = new HashMap<>();
        }

        public void setMatrixValue(String rowString) {
            int index = 0;
            int next;
            int off = 0;
            int value;
            while ((next = rowString.indexOf(",", off)) != -1) {
                if (index <= maxColumnIndex) {
                    if (columnNumberAndMatrixSetMappings.containsKey(index)) {
                        value = Integer.parseInt(rowString.substring(off, next));
                        for (Matrix matrix : columnNumberAndMatrixSetMappings.get(index)) {
                            matrix.setValue(value);
                        }
                    }
                    off = next + 1;
                    index++;
                } else {
                    break;
                }
            }
            if (next == -1 && maxColumnIndex == index) {
                value = Integer.parseInt(rowString.substring(off));
                for (Matrix matrix : columnNumberAndMatrixSetMappings.get(index)) {
                    matrix.setValue(value);
                }
            }
        }

        public void addMatrix(Matrix matrix) {
            int columnIndex = matrix.getColumn();
            if (columnNumberAndMatrixSetMappings.get(columnIndex) == null)  {
                columnNumberAndMatrixSetMappings.put(columnIndex, new HashSet<>());
            }
            columnNumberAndMatrixSetMappings.get(columnIndex).add(matrix);
            if (columnIndex > maxColumnIndex) {
                maxColumnIndex = columnIndex;
            }
        }

    }
}
