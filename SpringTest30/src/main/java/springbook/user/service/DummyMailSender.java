package springbook.user.service;

import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

public class DummyMailSender implements MailSender{
	public void send(SimpleMailMessage mailMessage) throws MailException{
		System.out.println("=================== 메일발송 테스트 시작 ===================");
		System.out.println("from === " + mailMessage.getFrom());
		System.out.println("to === " + mailMessage.getTo());
		System.out.println("subject === " + mailMessage.getSubject());
		System.out.println("text === " + mailMessage.getText());
		System.out.println("=================== 메일발송 테스트 완료 ===================");
	}
	
	public void send(SimpleMailMessage[] mailMessage) throws MailException{
		
	}
}
