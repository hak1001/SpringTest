package springbook.learningtest.spring.factorybean;

import org.springframework.beans.factory.FactoryBean;

public class MessageFactoryBean implements FactoryBean<Message>{
	String text;
	
	// 오브젝트를 생성할 때 필요한 정보를 팩토리 빈의 프로퍼티로 설정해서 대신 DI 받을 수 있게 한다. 
	public void setText(String text){
		this.text = text;
	}
	
	// 실제 빈으로 사용될 오브젝트를 직접 생산. 생성과 초기화 가능
	public Message getObject() throws Exception{
		return Message.newMessage(text);
	}
	
	public Class<? extends Message> getObjectType(){
		return Message.class;
	}
	
	public boolean isSingleton(){
		return false;
	}
	
}
