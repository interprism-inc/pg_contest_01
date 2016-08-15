package jp.co.interprism.pg_contest.matrix_calculator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

public class Main {

    //TODO 自分の環境に合わせて変えてください
    // example for windows
    //public static String BASE_PATH = "C:\\path\\to\\materials";
    // example for mac
    public static String BASE_PATH = "/path/to/materials";


    public static String SMALL_INPUT = BASE_PATH + File.separator + "input_100x100.txt";
    public static String LARGE_INPUT = BASE_PATH + File.separator + "input_10000x10000.txt";
    public static String SMALL_PROBLEM = BASE_PATH + File.separator + "problem_100_from_100x100.txt";
    public static String MEDIUM_PROBLEM = BASE_PATH + File.separator + "problem_100_from_10000x10000.txt";
    public static String LARGE_PROBLEM = BASE_PATH + File.separator + "problem_10000_from_10000x10000.txt";
    public static String ANSWER1 = BASE_PATH + File.separator + "answer01.txt";
    public static String ANSWER2 = BASE_PATH + File.separator + "answer02.txt";
    public static String ANSWER3 = BASE_PATH + File.separator + "answer03.txt";
    public static String RESULT1 = BASE_PATH + File.separator + "result01.txt";
    public static String RESULT2 = BASE_PATH + File.separator + "result02.txt";
    public static String RESULT3 = BASE_PATH + File.separator + "result03.txt";

    public static void main(String[] args) {
        long lapsedTime;

        // 参考実行時間 128 msec
        System.out.println("\ncalc1: ");
        start();
        MatrixCalculatorOriginal calc1 = new MatrixCalculatorOriginal(SMALL_INPUT, SMALL_PROBLEM, RESULT1);
        calc1.run();
        lapsedTime = finish();

        if (isCorrect(RESULT1, ANSWER1)) {
            System.out.println(lapsedTime + " msec");
        } else {
            System.out.println("FAILED!");
        }

        // 参考実行時間 142305 msec (142 seconds)
        System.out.println("\ncalc2: ");
        start();
        MatrixCalculatorOriginal calc2 = new MatrixCalculatorOriginal(LARGE_INPUT, MEDIUM_PROBLEM, RESULT2);
        calc2.run();
        lapsedTime = finish();

        if (isCorrect(RESULT2, ANSWER2)) {
            System.out.println(lapsedTime + " msec");
        } else {
            System.out.println("FAILED!");
        }

        // 参考実行時間 13895213 msec (231 minutes)
        System.out.println("\ncalc3: ");
        start();
        MatrixCalculatorOriginal calc3 = new MatrixCalculatorOriginal(LARGE_INPUT, LARGE_PROBLEM, RESULT3);
        calc3.run();
        lapsedTime = finish();
        if (isCorrect(RESULT3, ANSWER3)) {
            System.out.println(lapsedTime + " msec");
        } else {
            System.out.println("FAILED!");
        }
    }

    private static Date started;
    private static void start() {
        started = new Date();
    }
    private static long finish() {
        return new Date().getTime() - started.getTime();
    }

    private static boolean isCorrect(final String resultFilePath, final String answerFilePath) {
        try (BufferedReader answerReader = new BufferedReader(new FileReader(answerFilePath))) {
            try (BufferedReader resultReader = new BufferedReader(new FileReader(resultFilePath))) {
                while (true) {
                    final String answer = answerReader.readLine();
                    final String result = resultReader.readLine();
                    if (answer != null && result != null) {
                        if (!answer.trim().equals(result.trim())) {
                            return false;
                        }
                    } else if (answer == null && result == null) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } catch(IOException ioe) {
            return false;
        }
    }
}
