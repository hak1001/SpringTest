package springbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;

import springbook.user.dao.UserDao;
import springbook.user.service.DummyMailSender;
import springbook.user.service.UserService;
import springbook.user.service.UserServiceTest.TestUserService;

@Configuration
public class TestAppContext {
	/**
	 * 애플리케이션 로직 & 테스트용 빈
	 */
	
	@Bean
	public UserService testUserService(){
		return new TestUserService();
	}
	
	@Bean
	public MailSender mailSender(){
		return new DummyMailSender();
	}
}
