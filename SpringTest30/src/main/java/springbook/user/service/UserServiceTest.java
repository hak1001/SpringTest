package springbook.user.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static springbook.user.service.UserService.MIN_LOGCOUNT_FOR_SILVER;
import static springbook.user.service.UserService.MIN_RECOMMEND_FOR_GOLD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.AssertThrows;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/test-applicationContext.xml")
public class UserServiceTest {
	@Autowired UserService userService;
	@Autowired UserDao userDao;
	@Autowired DataSource dataSource;
	@Autowired PlatformTransactionManager transactionManager;
	@Autowired MailSender mailSender;
	
	List<User> users;
	
	@Before
	public void setUp(){
		// Arrays.asList 배열을 리스트로 만들어주는 메소드
		users = Arrays.asList(
			new User("admin", "관리자", "p1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER-1, 0, "admin@test.com"),
			new User("master", "마스터", "p2", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0, "master@test.com"),
			new User("suser1", "사용자1", "p3", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD-1, "suer1@test.com"),
			new User("tuser2", "사용자2", "p4", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD, "tuser@test.com"),
			new User("wuser3", "사용자3", "p5", Level.GOLD, 100, 100, "wuser3@test.com")
		);
	}
	
	@Test
	@DirtiesContext // 컨텍스트의 DI설정을 변경하는 테스트
	public void upgradeLevels() throws Exception{
		userDao.deleteAll();
		for(User user : users) userDao.add(user);
		
		MockMailSender mockmailSender = new MockMailSender();
		userService.setMailSender(mockmailSender);
		
		userService.upgradeLevels();
		
		// 업그레이드 체크 메소드 변경
		checkLevelUpgrade(users.get(0), false);
		checkLevelUpgrade(users.get(1), true);
		checkLevelUpgrade(users.get(2), false);
		checkLevelUpgrade(users.get(3), true);
		checkLevelUpgrade(users.get(4), false);
		
		List<String> request = mockmailSender.getRequests();
		assertThat(request.size(), is(2));
		assertThat(request.get(0), is(users.get(1).getEmail()));
		assertThat(request.get(1), is(users.get(3).getEmail()));
	}
	
	// 다음 레벨로 업그레이드할지 true, false 판단
	private void checkLevelUpgrade(User user, boolean upgraded){
		User userUpdate = userDao.get(user.getId());
		if(upgraded){
			assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
		}else{
			assertThat(userUpdate.getLevel(), is(user.getLevel()));
		}
	}
	
	@Test
	public void add(){
		userDao.deleteAll();
		
		User userWithLevel = users.get(4);
		User userWithoutLevel = users.get(0);
		userWithoutLevel.setLevel(null);
		
		userService.add(userWithLevel);
		userService.add(userWithoutLevel);
		
		User userWithLevelRead = userDao.get(userWithLevel.getId());
		User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());
		
		assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel()));
		assertThat(userWithoutLevelRead.getLevel(), is(userWithoutLevel.getLevel()));
	}
	
	// 트랜잭션 테스트
	@Test
	public void upgradeAllOrNothing() throws Exception{
		UserService testUserService = new TestUserService(users.get(3).getId());
		testUserService.setUserDao(this.userDao);
		testUserService.settransactionManager(transactionManager);
		testUserService.setMailSender(mailSender);
		userDao.deleteAll();
		for(User user : users) userDao.add(user);
		
		try {
			testUserService.upgradeLevels();
			fail("TestUserServiceException expected");
		} catch (TestUserServiceException e) {
		}
		
		// 예외가 발생하기 전에 레벨 변경이 있었던 사용자의 레벨이 처음 상태로 바뀌었나 확인 
		checkLevelUpgrade(users.get(1), false);
	}
	
	// 트랜잭션 테스트용 스태틱 클래스
	static class TestUserService extends UserService{
		private String id;
		
		// 예외를 발생시킬 User 오브젝트의 id지정
		private TestUserService(String id){
			this.id = id;
		}
		
		protected void upgradeLevel(User user){
			// 지정된 id의 User 오브젝트가 발견되는 예외발생하여 작업강제 중단..
			if(user.getId().equals(this.id)) throw new TestUserServiceException();
			super.upgradeLevel(user);
		}
	}
	
	static class TestUserServiceException extends RuntimeException{
		
	}
	
	static class MockMailSender implements MailSender{
		private List<String> requests = new ArrayList<String>();
		
		public List<String> getRequests(){
			return requests;
		}
		
		public void send(SimpleMailMessage mailMessage) throws MailException{
			requests.add(mailMessage.getTo()[0]); 
			System.out.println("=================== SimpleMailMessage 메일발송 테스트 시작 ===================");
			System.out.println("from === " + mailMessage.getFrom());
			System.out.println("to === " + mailMessage.getTo()[0]);
			System.out.println("subject === " + mailMessage.getSubject());
			System.out.println("text === " + mailMessage.getText());
			System.out.println("=================== SimpleMailMessage 메일발송 테스트 완료 ===================\n");
		}
		
		public void send(SimpleMailMessage[] mailMessage) throws MailException{
			
		}
	}
	
}
