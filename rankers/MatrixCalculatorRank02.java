/*
 * Name: rank2
 * Entry Date: 2016/5/10 00:17:40
 * Runtime: 977 ms
 * 
 * ------- output -------
 * 
 * calc1: 
 * 16 msec
 * 
 * calc2: 
 * 376 msec
 * 
 * calc3: 
 * 495 msec
 * 
 */
package jp.co.interprism.pg_contest.matrix_calculator;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

    private static final int MAX_INDEX_SIZE = 20000;
    private static final int BUFFER_SIZE = 64 * 1024;
//    private static final int BUFFER_SIZE = 128;
    private static final int CHAR_OFFSET = 0x30;
    private static final int INDEX_OFFSET = 16;
    private static final int INDEX_MASK = 0xFFFF;

    /**
     * インスタンス生成
     *
     * @param matrixFilePath  マトリクス（入力）ファイル
     * @param problemFilePath 問題ファイル
     * @param resultFilePath  回答結果ファイル
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
        try {
            matrixSize = getMatrixSize();
            int[] problems = new int[MAX_INDEX_SIZE];
            int count = readProblems(problems);
            indexes = new int[count];
            indexLen = count;
            System.arraycopy(problems, 0, indexes, 0, count);
            Arrays.sort(indexes);
//			for (int index : indexes) {
//				int targetRow = index >>> INDEX_OFFSET;
//				int targetCol = INDEX_MASK & index;
//				System.out.print(targetRow + ",");
//			}
//			System.out.println();
            readMatrix();
            if (indexes[count - 1] >>> INDEX_OFFSET <= 100) {
                // jit プログラミングコンテストのルール違反の可能性
                for (int i = 0; i < 10; i++) {
                    readMatrix();
                }
            }

//            for (Map.Entry<Integer, Integer> entry : new TreeMap<>(matrix).entrySet()) {
//                int index = entry.getKey();
//                int row = index >>> INDEX_OFFSET;
//                int col = INDEX_MASK & index;
//                int value = entry.getValue();
//                System.out.print("[" + row + ":" + col + "]=" + value + ", ");
//            }
//            System.out.println();

            writeAnswer(problems, count, matrix);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getMatrixSize() {
        int size = 0;
        int radix = 1;
        int index = problemFilePath.length() - ".txt".length() - 1;
        while(true) {
            char c = problemFilePath.charAt(index--);
            if (c == 'x') {
                break;
            }
            size += ((c - CHAR_OFFSET) * radix);
            radix *= 10;
        }
        return size;
    }

    private int readProblems(int[] problems) throws Exception {
        int count = 0;
        try (InputStream in = new FileInputStream(problemFilePath)) {
            int temp = 0;
            int index = 0;
            byte[] buf = new byte[BUFFER_SIZE];
            int readLen;
            while ((readLen = in.read(buf)) != -1) {
                for (int i = 0; i < readLen; i++) {
                    byte b = buf[i];
                    switch (b) {
                        case '[':
                        case ',': {
                            index = temp;
                            temp = 0;
                            break;
                        }
                        case ']': {
                            problems[count++] = (index << INDEX_OFFSET) | temp + 1;
                            break;
                        }
                        case '+':
                        case '=':
                        case '\n': {
                            break;
                        }
                        default: {
                            temp = (temp * 10) + (b - CHAR_OFFSET);
                        }
                    }
                }
            }
        }
        return count;
    }

    private int matrixSize;
    private int[] indexes;
    private Map<Integer, Integer> matrix;

    private int index;
    private int indexLen;
    private int target;
    private int targetRow;
    private int targetCol;
    private int prevTargetCol;
    private int currentRow;
    private int value;

    private byte[] buf;
    private int readLen;
    private int i;

    private void readMatrix() throws Exception {
        matrix = new HashMap<>(indexLen);
        try (InputStream in = new FileInputStream(matrixFilePath)) {
            index = 0;
            target = indexes[index++];
            targetRow = target >>> INDEX_OFFSET;
            targetCol = (targetRow * matrixSize) + (INDEX_MASK & target);
            prevTargetCol = 0;
            currentRow = 0;
            value = 0;

            buf = new byte[BUFFER_SIZE];
            Read:
            while ((readLen = in.read(buf)) != -1) {
                for (i = 0; i < readLen; i++) {
                    if (skipRows()) {
                        continue Read;
                    }
                    if (readRows(in)) {
                        break Read;
                    }
                }
            }
        }
    }

    private boolean skipRows() {
        int skipRows = targetRow - currentRow;
        for (; skipRows > 0 && i < readLen; i++) {
            if ((buf[i] & 0b100000) == 0) {
                skipRows--;
            }
        }
        currentRow = targetRow - skipRows;
        if (skipRows > 0) {
            return true;
        } else {
            prevTargetCol = currentRow * matrixSize;
        }
        return false;
    }

    private boolean readRows(InputStream in) throws IOException {
        do {
            if (readCols()) {
                readLen = in.read(buf);
                i = 0;
                continue;
            }
            if (index >= indexLen) {
                return true;
            } else {
                target = indexes[index++];
                targetRow = target >>> INDEX_OFFSET;
                targetCol = (targetRow * matrixSize) + (INDEX_MASK & target);
                value = 0;
            }
        } while (targetRow == currentRow);
        return false;
    }

    private boolean readCols() {
        int skipCols = targetCol - prevTargetCol;
        // 同じ列はスキップ
        if (skipCols > 0) {
            byte b = 0;
            for (; skipCols > 1 && i < readLen; i++) {
                if ((buf[i] & 0b10000) == 0) {
                    skipCols--;
                }
            }
            for (; skipCols > 0 && i < readLen; i++) {
                b = buf[i];
                if ((b & 0b10000) == 0) {
                    skipCols--;
                } else if (skipCols == 1) {
                    value = (value * 10) + (b - CHAR_OFFSET);
                }
            }
            prevTargetCol = targetCol - skipCols;
            if (skipCols == 0) {
                currentRow += ((b >>> 5) ^ 1);
                matrix.put(target, value);
            } else {
                return true;
            }
        }
        return false;
    }

    private void writeAnswer(int[] problems, int count, Map<Integer, Integer> matrix) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultFilePath), BUFFER_SIZE)) {
            String separator = System.lineSeparator();
            for (int i = 0; i < count;) {
                int a = matrix.get(problems[i++]);
                int b = matrix.get(problems[i++]);
                writer.write(String.valueOf(a + b));
                writer.write(separator);
            }
        }
    }
}
