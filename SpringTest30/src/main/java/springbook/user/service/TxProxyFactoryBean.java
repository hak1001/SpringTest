package springbook.user.service;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

public class TxProxyFactoryBean implements FactoryBean<Object>{
	// TransactionHandler를 생성할 때 필요한 부분
	Object target;
	PlatformTransactionManager transactionManager;
	String pattern;
	
	// 다이내믹 프록세를 생성할 때 필요.
	Class<?> serviceInterface; 
	
	public void setTarget(Object target){
		this.target = target;
	}
	
	public void setTransactionManager(PlatformTransactionManager transactionManager){
		this.transactionManager = transactionManager;
	}
	
	public void setPattern(String pattern){
		this.pattern = pattern;
	}
	
	public void setServiceInterface(Class<?> serviceInterface){
		this.serviceInterface = serviceInterface;
	}
	
	// FactoryBean 인터페이스 구현 메소드
	public Object getObject() throws Exception{
		TransactionHandler txHandler = new TransactionHandler();
		txHandler.setTarget(target);
		txHandler.setTransactionManager(transactionManager);;
		txHandler.setPattern(pattern);
		return Proxy.newProxyInstance(
				getClass().getClassLoader(), 
				new Class[] { serviceInterface }, 
				txHandler);
	}
	
	// 팩토리 빈이 생성하는 오브젝트의 타입은 DI받은 인터페이스 타입에 따라 달라진다. 따라서 다양한 타입의 프록시 오ㅡ젝트 생성에 재사용 할 수 있다. 
	public Class<?> getObjectType(){
		return serviceInterface;
	}
	
	public boolean isSingleton(){
		return false;
	}
	
}
