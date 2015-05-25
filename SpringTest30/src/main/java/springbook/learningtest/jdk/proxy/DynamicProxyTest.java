package springbook.learningtest.jdk.proxy;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.junit.Test;

public class DynamicProxyTest {
	@Test
	public void simpleProxy(){
		Hello hello = new HelloTarget();
		assertThat(hello.sayHello("hak1001"), is("Hello hak1001"));
		assertThat(hello.sayHi("hak1001"), is("Hi hak1001"));
		assertThat(hello.sayThankYou("hak1001"), is("ThankYou hak1001"));
		
		// 프록시 테스트
		//Hello proxiedHello = new HelloUppercase(new HelloTarget());
		Hello proxiedHello = (Hello)Proxy.newProxyInstance(	// 생성된 다이내믹 프록시 오브젝트는 Hello 인터페이스를 구현하고 있으므로 Hello 타입으로 캐스팅해도 안전..
				getClass().getClassLoader(),	// 동적으로 생성되는 다이내믹 프록시 클래스의 로딩에 사용할 클래스 로더
				new Class[] { Hello.class },	// 구현할 인터페이스. 한번에 하나 이상의 인터페이스를 구현할 수도 있기 때문에 인터페이스 배열 사용
				new UppercaseHandler(new HelloTarget()));	// 부가기능과 위임 코드를 담은 InvocationHandler
		
		assertThat(proxiedHello.sayHello("hak1001"), is("HELLO HAK1001"));
		assertThat(proxiedHello.sayHi("hak1001"), is("HI HAK1001"));
		assertThat(proxiedHello.sayThankYou("hak1001"), is("THANKYOU HAK1001"));
	}
	
	static class HelloUppercase implements Hello{
		Hello hello; // 위임할 타깃 오브젝트. 인터페이스로 접근
		
		public HelloUppercase(Hello hello){
			this.hello = hello;
		}
		
		public String sayHello(String name){
			return hello.sayHello(name).toUpperCase(); // 위임과 부가기능 적용
		}
		
		public String sayHi(String name){
			return hello.sayHi(name).toUpperCase();
		}
		
		public String sayThankYou(String name){
			return hello.sayThankYou(name).toUpperCase();
		}
		
	}
	
	// InvocationHandler 구현 클래스
	static class UppercaseHandler implements InvocationHandler{
		// 모든 종류의 인터페이스를 구현한 타깃에도 적용 가능하도록 Object 타입으로 수정
		Object target;	
		
		private UppercaseHandler(Hello target){
			this.target = target;
		}
		
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable{
			Object ret = method.invoke(target, args);	// 타깃으로 위임, 인터페이스의 메소드 호출에 모두 적용됨.
			
			// 호출한 메소드의 리턴 타입이 String인 경우와 메소드 이름이 일치하는 경우만 부가기능 적용
			if(ret instanceof String && method.getName().startsWith("say")){
				return ((String)ret).toUpperCase();
			}else{
				return ret;	// 부가기능 제공
			}
			
		}
	}
	
	// Hello 인터페이스
	static interface Hello{
		String sayHello(String name);
		String sayHi(String name);
		String sayThankYou(String name);
	}
	
	// 타깃 클래스
	static class HelloTarget implements Hello{
		public String sayHello(String name){
			return "Hello " + name; 
		}
		
		public String sayHi(String name){
			return "Hi " + name; 
		}
		
		public String sayThankYou(String name){
			return "ThankYou " + name; 
		}
	}
	
}
