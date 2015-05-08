package springbook.learningtest.template;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Calculator {
	public Integer calcSum(String filepath) throws IOException{
		LineCallback sumcallback = new LineCallback() {
			public Integer doSomethingWithLine(String line, Integer value) {
				return value + Integer.valueOf(line);
			}
		};
		return lineReadTemplate(filepath, sumcallback, 0);
	}
	
	public Integer calcMultiply(String filepath) throws IOException{
		LineCallback muliplycallback = new LineCallback() {
			public Integer doSomethingWithLine(String line, Integer value) {
				return value * Integer.valueOf(line);
			}
		};
		return lineReadTemplate(filepath, muliplycallback, 1);
	}
	
	public Integer fileReadTemplate(String filepath, BufferedReaderCallBack callback) throws IOException{
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(new FileReader(filepath)); // 한 줄씩 일기 편하게 BufferedReader로 파일을 가져온다.
			int ret = callback.doSomethingWithReader(br);
			
			return ret;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw e;
		} finally {
			if(br != null){	// BufferedReader 오브젝트가 생성되기 전에 예외가 발생할 수도 있기 때문에 null 체크 
				try {
					br.close();
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
			
		}
	}
	
	public Integer lineReadTemplate(String filepath, LineCallback callback, int initVal) throws IOException{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filepath)); // 한 줄씩 일기 편하게 BufferedReader로 파일을 가져온다.
			Integer res = initVal;
			String line = null;
			
			while((line = br.readLine()) != null){
				res = callback.doSomethingWithLine(line, res);
			}
			
			return res;
		} catch (IOException e) {
			System.out.println(e.getMessage());
			throw e;
		} finally {
			if(br != null){	// BufferedReader 오브젝트가 생성되기 전에 예외가 발생할 수도 있기 때문에 null 체크 
				try {
					br.close();
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
			
		}
	}
}
