package springbook.learningtest.template;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Calculator {
	public Integer calcSum(String filepath) throws IOException{
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(new FileReader(filepath)); // 한 줄씩 일기 편하게 BufferedReader로 파일을 가져온다.
			Integer sum = 0;
			String line = null;
			
			while((line = br.readLine()) != null){
				sum += Integer.valueOf(line);
			}
			return sum;
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
}
