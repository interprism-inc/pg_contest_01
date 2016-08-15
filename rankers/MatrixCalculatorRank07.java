/*
 * Name: rank7
 * Entry Date: 2016/4/28 14:06:51
 * Runtime: 5990 ms
 * 
 * ------- output -------
 * 
 * calc1: 
 * 34 msec
 * 
 * calc2: 
 * 2036 msec
 * 
 * calc3: 
 * 3836 msec
 * 
 */
package jp.co.interprism.pg_contest.matrix_calculator;

import sun.security.pkcs11.P11TlsKeyMaterialGenerator;

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

	private static Pattern p = Pattern.compile("^\\[(\\d+),(\\d+)\\]\\+\\[(\\d+),(\\d+)\\]");

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
		//TODO 実装してください
		System.out.println(this.matrixFilePath);
		System.out.println(this.problemFilePath);
		System.out.println(this.resultFilePath);
		BufferedReader br = null;
		this.readProblemFile();

//		try {
//			br = new BufferedReader(new FileReader(this.matrixFilePath));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//		try (LineNumberReader lnr = new LineNumberReader(br)) {
//			String line = null;
//			int lnum = 0;
//
//			while ((line = lnr.readLine()) != null
//					&& (lnum = lnr.getLineNumber()) < 7) {
//			}
//
//			switch (lnum) {
//				case 0:
//					System.out.println("the file has zero length");
//					break;
//				case 7:
//					boolean empty = "".equals(line);
//					System.out.println("line 7: " + (empty ? "empty" : line));
//					break;
//				default:
//					System.out.println("the file has only " + lnum + " line(s)");
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	private void readProblemFile() {
        Date start = new Date();
		try (BufferedReader pbr = new BufferedReader(new FileReader(this.problemFilePath));
			 BufferedReader mbr = new BufferedReader(new FileReader(this.matrixFilePath))) {

            System.out.println("File 準備:"+(new Date().getTime() - start.getTime()));
			String line = null;
//			Pattern p = Pattern.compile("^\\[(\\d+),(\\d+)\\]\\+\\[(\\d+),(\\d+)\\]");

            // 一旦問題データをmapに詰める
            List<String> problemData = new ArrayList<>();
			Map<Integer, TreeSet<Integer>> map = new HashMap<Integer, TreeSet<Integer>>();
            int maxY = 0;
			while ((line = pbr.readLine()) != null) {
				int[] lineData = this.readProblemFileLine(line);
				int p1Y = lineData[0];
				int p1X = lineData[1];
				int p2Y = lineData[2];
				int p2X = lineData[3];
                TreeSet<Integer> cache = map.get(p1Y);
				if (cache == null) {
					cache = new TreeSet<Integer>();
				}
				cache.add(p1X);
				map.put(p1Y, cache);
				cache = map.get(p2Y);
				if (cache == null) {
					cache = new TreeSet<Integer>();
				}
				cache.add(p2X);
				map.put(p2Y, cache);
                problemData.add(line);
//                maxY = maxY < p1Y ? p1Y : maxY;
//                maxY = maxY < p2Y ? p2Y : maxY;
//				lnr.setLineNumber(p1Y);
//				String matrixRow1 = lnr.readLine();
//				System.out.println(lnr.getLineNumber());
//				lnr.setLineNumber(p2Y);
//				String matrixRow2 = lnr.readLine();
//				System.out.println(lnr.getLineNumber());
//
//				System.out.println(p1Y+":"+matrixRow1);
//				System.out.println(p1Y + "," + p1X + ":" + matrixRow1.split(",")[p1X]);
//
//				System.out.println(p2Y+":"+matrixRow2);
//				System.out.println(p2Y+","+p2X+":"+matrixRow2.split(",")[p2X]);


//				break;
			}
            System.out.println(maxY);
            System.out.println("problem read:"+(new Date().getTime() - start.getTime()));

            // Matrixから読む
            Map<String, Integer> matrixCache = new HashMap<String, Integer>();
            String mline = null;
            int rowNum = 0;
            while ((mline = mbr.readLine()) != null) {
                if (map.get(rowNum) != null) {
                    String[] splitLines = mline.split(",", map.get(rowNum).last() + 2);
//                    String[] splitLines = mline.split(",");
                    for (Iterator<Integer> j = map.get(rowNum).iterator();j.hasNext();) {
                        int posX = j.next();
//                        System.out.println(rowNum+","+posX+":"+mline.split(",")[posX]);
//                        matrixCache.put("["+rowNum+","+posX+"]", Integer.parseInt("1"));
                        matrixCache.put("["+rowNum+","+posX+"]", Integer.parseInt(splitLines[posX]));
                    }
                }
                rowNum++;
//                if (rowNum > maxY) {
//                    break;
//                }
            }
            System.out.println("Matrix read:"+(new Date().getTime() - start.getTime()));

            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(this.resultFilePath));

            // もう１度問題を読みながら答えをセット(無駄な気がする)
//            Pattern problemPattern = Pattern.compile("^([^\\+]+)\\+([^=]+)=$]");
            Pattern problemPattern = Pattern.compile("([^\\+]+)\\+([^=]+)");
            StringBuilder result = new StringBuilder();
            for (Iterator<String> i = problemData.iterator();i.hasNext();) {
                String problem = i.next();
                Matcher m = problemPattern.matcher(problem);
                m.find();
//                m.find();
//                System.out.println(m.group(1) + ":" + matrixCache.get(m.group(1)));
//                System.out.println(m.group(2) + ":" + matrixCache.get(m.group(2)));
                String answer = matrixCache.get(m.group(1)) + matrixCache.get(m.group(2)) + "";
                result.append(answer+"\n");
//                System.out.println(answer);
//                osw.write(answer + "\n");

//                break;
            }
            System.out.println("get Answer:" + (new Date().getTime() - start.getTime()));
            osw.write(result.toString());
            osw.flush();
            osw.close();
            System.out.println("File Write:" + (new Date().getTime() - start.getTime()));
//			System.out.println(map.size());
//            System.out.println(map.keySet().size());
//            for (Iterator<Integer> i = map.keySet().iterator();i.hasNext();) {
//                int k = i.next();
//                System.out.print(k + ":");
//
//
//                for (Iterator<Integer> j = map.get(k).iterator();j.hasNext();) {
//                    System.out.print(j.next()+",");
//                }
//                System.out.println();
//            }

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int[] readProblemFileLine(String line) {

		int[] ret = {-1, -1, -1, -1};
		Matcher matcher = p.matcher(line);
		if (!matcher.find()) {
			return ret;
		}
		ret[0] = Integer.parseInt(matcher.group(1));
		ret[1] = Integer.parseInt(matcher.group(2));
		ret[2] = Integer.parseInt(matcher.group(3));
		ret[3] = Integer.parseInt(matcher.group(4));

		return ret;
	}
}
