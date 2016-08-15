/*
 * Name: rank4
 * Entry Date: 2016/5/2 02:50:21
 * Runtime: 1666 ms
 * 
 * ------- output -------
 * 
 * calc1: 
 * 13 msec
 * 
 * calc2: 
 * 415 msec
 * 
 * calc3: 
 * 1146 msec
 * 
 */
package jp.co.interprism.pg_contest.matrix_calculator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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

	private final static byte ZERO = '0';
	private final static byte NINE = '9';
	private final static byte LINE = '\n';
	private final static byte COMMA = ',';
	private final static byte EXIT = 0;
	private final static int BUFFER_SIZE = 1000 * 1024;

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
	public void run() {
		List<Element> problemData = getProblemData();
		List<Element> sortedData = new ArrayList<Element>(problemData);
		listSort(sortedData);
		setElementData(sortedData);
		writeAnswer(problemData);
	}

	/**
	 * 問題ファイルからデータを取得する。 数値の後に文字が来ることを前提とする。
	 * 
	 * @return 取得データ
	 */
	private List<Element> getProblemData() {
		BufferedReader br = null;
		List<Element> elementList = new ArrayList<Element>();
		char c;
		int readTempNumber = 0;
		int rowTempNumber = 0;
		boolean isReadingRow = false;
		boolean isReadingNumeric = false;
		String line;
		try {
			br = new BufferedReader(new FileReader(problemFilePath), BUFFER_SIZE);
			while ((line = br.readLine()) != null) {
				for (int i = 0; i < line.length(); i++) {
					c = line.charAt(i);
					if (c >= ZERO && c <= NINE) {
						isReadingNumeric = true;
						readTempNumber = readTempNumber * 10 + (c - ZERO);
					} else {
						if (isReadingNumeric) {
							if (isReadingRow) {
								elementList.add(new Element(rowTempNumber, readTempNumber));
								isReadingRow = false;
							} else {
								isReadingRow = true;
								rowTempNumber = readTempNumber;
							}
							isReadingNumeric = false;
							readTempNumber = 0;
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return elementList;
	}

	/**
	 * マトリクスファイルから問題ファイルで使用するデータのみを取得し、各Elementに設定する。
	 * 
	 * @param sortedList
	 *            行インデックス・列インデックスでソートしたリスト
	 * @return 問題ファイルで使用するデータのみ格納したマップ
	 */
	private void setElementData(List<Element> sortedList) {
		int readRowIndex = 0;
		int readColumnIndex = 0;
		int readTempNumber = 0;
		int listIndex = 0;
		Element element = sortedList.get(listIndex);
		int columnMax = 0;
		int halfColumnMax = 0;
		int elementRow = element.getRow();
		int elementColumn = element.getColumn();
		FileInputStream inputStream = null;
		boolean exitFlag = false;
		boolean isFirstReadLine = true;
		int skipBufferIndicies = 0;
		int halfSkipBufferIndicies = 0;
		try {
			inputStream = new FileInputStream(matrixFilePath);
			byte[] buffer = new byte[BUFFER_SIZE];
			while (!exitFlag) {
				int readNum = inputStream.read(buffer, 0, BUFFER_SIZE);
				if (readNum == -1) {
					break;
				} else if (readNum != BUFFER_SIZE) {
					Arrays.fill(buffer, readNum, BUFFER_SIZE, EXIT);
				}
				for (int bufferIndex = 0; bufferIndex < BUFFER_SIZE; bufferIndex++) {
					byte target = buffer[bufferIndex];
					if (readColumnIndex == elementColumn && readRowIndex == elementRow) {
						if (target >= ZERO && target <= NINE) {
							readTempNumber = readTempNumber * 10 + (target - ZERO);
						} else {
							int columnIndexTemp = readColumnIndex;
							int readIndexTemp = readRowIndex;
							do {
								element.setValue(readTempNumber);
								if (++listIndex < sortedList.size()) {
									element = sortedList.get(listIndex);
									elementRow = element.getRow();
									elementColumn = element.getColumn();
								} else {
									break;
								}
								// 同じ要素考慮
							} while (columnIndexTemp == elementColumn && readIndexTemp == elementRow);
							readTempNumber = 0;
							if (!isFirstReadLine && elementRow != readRowIndex) {
								if (readColumnIndex < halfColumnMax) {
									bufferIndex += halfSkipBufferIndicies + 2 * (halfColumnMax - readColumnIndex);
								} else {
									bufferIndex += 2 * (columnMax - readColumnIndex);
								}
								if (bufferIndex >= BUFFER_SIZE) {
									bufferIndex = BUFFER_SIZE - 1;
								}
							}
						}
					}
					if (target == LINE) {
						readRowIndex++;
						if (isFirstReadLine) {
							skipBufferIndicies = bufferIndex * 9 / 10;
							halfSkipBufferIndicies = skipBufferIndicies / 2;
							isFirstReadLine = false;
							columnMax = readColumnIndex;
							halfColumnMax = columnMax / 2;
						}
						if (readRowIndex != elementRow) {
							bufferIndex += skipBufferIndicies;
						}
						readColumnIndex = 0;
					} else if (target == COMMA) {
						readColumnIndex++;
					} else if (target == EXIT) {
						exitFlag = true;
						break;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 回答を計算して出力する
	 * 
	 * @param problemData
	 *            問題データ
	 * @param elementMap
	 *            使用データ格納マップ
	 */
	private void writeAnswer(List<Element> problemData) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < problemData.size(); i = i + 2) {
			sb.append(problemData.get(i).getValue() + problemData.get(i + 1).getValue());
			sb.append(System.lineSeparator());
		}
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(resultFilePath));
			bw.write(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 要素クラスのリストを行・列のインデックス順に並び替える
	 * 
	 * @param list
	 */
	private void listSort(List<Element> list) {
		Collections.sort(list, new Comparator<Element>() {
			@Override
			public int compare(Element e1, Element e2) {
				int r = e1.getRow() - e2.getRow();
				return r == 0 ? (e1.getColumn() - e2.getColumn()) : r;
			}
		});
	}

	/**
	 * 要素クラス
	 */
	private class Element {
		private final int row;
		private final int column;
		private int value;

		public Element(int row, int column) {
			this.row = row;
			this.column = column;
		}

		public final int getRow() {
			return row;
		}

		public final int getColumn() {
			return column;
		}

		public final int getValue() {
			return value;
		}

		public final void setValue(int value) {
			this.value = value;
		}

		@Override
		public int hashCode() {
			return Objects.hash(row, column);
		}
	}
}
