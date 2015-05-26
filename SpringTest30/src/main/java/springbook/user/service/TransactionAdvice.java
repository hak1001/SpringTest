package springbook.user.service;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionAdvice implements MethodInterceptor{
	PlatformTransactionManager transactionManager;
	
	public void setTransactionManager(PlatformTransactionManager transactionManager){
		this.transactionManager = transactionManager;
	}
	
	// 타깃을 호출하는 기능을 가진 콜백 오브젝트를 프록시로부터 받는다. 
	public Object invoke(MethodInvocation invocation) throws Throwable{
		TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
		
		try {
			// 콜백을 호출해서 타깃의 메소드를 실행. 타깃 메소드 호출 전후로 필요한 부가기능을 넣을 수 있다. 
			Object ret = invocation.proceed();	 
			this.transactionManager.commit(status);
			return ret;
		} catch (RuntimeException e) {	// JDK 다이내믹 프록시가 제공하는 Method와는 달리 스프링으 MethodInvocation을 통한 타깃 호출은 예외가 포장되지 않고 타깃에서 보낸 그대로 전달된다. 
			this.transactionManager.rollback(status);
			throw e;
		}
	}
}
