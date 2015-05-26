package springbook.user.service;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.util.PatternMatchUtils;

public class NameMatchClassMethodPointcut extends NameMatchMethodPointcut{
	public void setMappedClassName(String mappedClassName){
		this.setClassFilter(new SimpleClassFilter(mappedClassName));
	}
	
	static class SimpleClassFilter implements ClassFilter{
		String mappedName;
		
		private SimpleClassFilter(String mappedName){
			this.mappedName = mappedName;
		}
		
		public boolean matches(Class<?> clazz){
			// simpleMatch - 와일드카드(*)가 들어간 문자열 비교를 지원하는 스프링의 유틸리티 메소드다. *name, name*, *name* 세 가지 방식을 모두 지원
			return PatternMatchUtils.simpleMatch(mappedName, clazz.getSimpleName());
		}
	}
	
}
