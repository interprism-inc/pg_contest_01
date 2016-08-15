/*
 * Name: rank8
 * Entry Date: 2016/4/27 00:09:07
 * Runtime: 7250 ms
 * 
 * ------- output -------
 * 
 * calc1: 
 * 114 msec
 * 
 * calc2: 
 * 2002 msec
 * 
 * calc3: 
 * 5057 msec
 * 
 */
package jp.co.interprism.pg_contest.matrix_calculator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

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
		//TODO 実装してください
//		System.out.println(matrixFilePath);
//		System.out.println(problemFilePath);
//		System.out.println(resultFilePath);
		
		final Integer DUMMY_INT = Integer.MIN_VALUE;
		final long LINE_MAX = 100000;
		
		// インデックス用キー
		final TreeMap<Integer, TreeSet<Integer>> indecies = new TreeMap<Integer, TreeSet<Integer>>();
		
		// 必要な数を取得する
		final SortedMap<Long, Integer> cache = new TreeMap<Long, Integer>();
		
		// cache用のキーを作る
		final BiFunction<Integer, Integer, Long> createKey = (i,j)-> (i+1) * LINE_MAX + j;

		
		// 問題の行から数字部分を読み取る関数
		Function<Reader, Integer> scanner = r->{
			int val = 0;

			try {
				int singleChar;
				while( (singleChar = r.read()) != -1 && 
						singleChar != ']' && singleChar != ',' && 
						singleChar != '\n' && singleChar != '\0' &&
						singleChar != ' ' ){
					val *= 10;
					val += ( singleChar - '0' );
				}
				return val;
			} catch (Exception e) {
			}
			
			return -1;
		};
		
		
		// キャッシュ用のマップを作る
		Consumer<SortedMap<Long,Integer>> makeCache = (map) -> {
			
			ExecutorService executor = Executors.newFixedThreadPool(4);
						
			try (BufferedReader reader = new BufferedReader(new FileReader(matrixFilePath))) {

				final Iterator<Integer> itr = indecies.keySet().iterator();
					
				int target_i = itr.next();
				
				int i = 0;
				String line;
				while( (line = reader.readLine()) != null ){
					if( target_i == i ){
						
						final int closured_i = target_i;
						final String closuredLine = line;
	
						executor.submit(()->{
							//-------------------------------
							// 調査対象の行
							String[] elems = closuredLine.split(",");

							indecies.get( closured_i ).forEach(j->{
//								synchronized(cache){
									cache.put(createKey.apply(closured_i, j), Integer.parseInt(elems[j]));
//								}
							});
							//-------------------------------					
						});

						if( itr.hasNext()){
							target_i = itr.next();
						}else{
							// すべてのエントリを見つけた場合はループ全体を終了
							executor.shutdown();
							break;
						}
					}
					
					i++;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				executor.awaitTermination(1, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};

		
		//System.out.println("ans:" + findMatrix.applyAsInt(2, 99));
		
		// 問題を読み、解答を書く
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultFilePath))) {
			try (BufferedReader reader = new BufferedReader(new FileReader(problemFilePath))) {

				long start;
				long end;
				
				start = System.currentTimeMillis();

				ArrayList<Long> inputList = new ArrayList<Long>();
				
				
				
				// インデックスキーに格納する
				BiConsumer<Integer, Integer> putIndecies = (i,j)->{
					if( !indecies.containsKey(i) ){
						indecies.put(i, new TreeSet<Integer>());
					}
					indecies.get(i).add(j);
				};
				
				
				
				
				// 問題を読む
				do{					
					// '['の分を読み飛ばす
					if( reader.read() == -1 ){
						// ファイルの終端は改行のみ一行含まれているデータのため、その場合はbreakする
						break;
					}

					int i1 = scanner.apply(reader);
					int j1 = scanner.apply(reader);
					long longA = createKey.apply(i1, j1);
					
					putIndecies.accept(i1, j1);
					
					// '+['を読み飛ばす
					reader.skip(2);
					int i2 = scanner.apply(reader);
					int j2 = scanner.apply(reader);
					long longB = createKey.apply(i2, j2);
					
					putIndecies.accept(i2, j2);
					
					
					cache.put(longA, DUMMY_INT);
					cache.put(longB, DUMMY_INT);

					inputList.add(longA);
					inputList.add(longB);
					
					// 最後の'='の分を読み飛ばす
					reader.skip(1);
				}while( reader.read() != -1 );
				
//				end = System.currentTimeMillis();
//				System.out.println("問題構築 :" + (end - start)  + "ms");
				
	
//				start = System.currentTimeMillis();
				
				makeCache.accept(cache);
				
//				end = System.currentTimeMillis();
//				System.out.println("chache構築 :" + (end - start)  + "ms");
//				
//				
//				start = System.currentTimeMillis();
				
				
				
				// 解答処理
				for( int i=0; i<inputList.size(); ){
					
					long longA = inputList.get(i++);
					long longB = inputList.get(i++);
					
//					System.out.println(" a,b = " + longA + " " + longB);
					
					int ans = cache.get(longA) + cache.get(longB);
//					System.out.println(""+ ans);
					
					writer.write("" + ans);
					writer.newLine();
				}
				
//				end = System.currentTimeMillis();
//				System.out.println("解答処理:" + (end - start)  + "ms");
				
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
//		System.out.println("finish !!");
	}

}
