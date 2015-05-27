package springbook.user.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static springbook.user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static springbook.user.service.UserServiceImpl.MIN_RECOMMEND_FOR_GOLD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
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
	@Autowired ApplicationContext context;
	@Autowired UserService userService;
	@Autowired UserService testUserService;
	@Autowired UserDao userDao;
	@Autowired PlatformTransactionManager transactionManager;
	
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
		UserServiceImpl userServiceImpl = new UserServiceImpl();
		
		// 1. 레벨 업그레이드 확인을 위해 목 오브젝트 DI
		MockUserDao mockUserDao = new MockUserDao(this.users);
		userServiceImpl.setUserDao(mockUserDao);
		
		// 2. 메일 발송 여부 확인을 위해 목 오베젝트 DI
		MockMailSender mockmailSender = new MockMailSender();
		userServiceImpl.setMailSender(mockmailSender);
		
		// 3. 테스트 대상 실행
		userServiceImpl.upgradeLevels();

		// 4. 목 오브젝트를 이용한 결과 확인(레벨 업그레이드)
		List<User> updated = mockUserDao.getUpdated();
		assertThat(updated.size(), is(2));
		checkUserAndLevel(updated.get(0), "master", Level.SILVER);
		checkUserAndLevel(updated.get(1), "tuser2", Level.GOLD);
		
		// 5. 목 오브젝트를 이용한 결과 확인(메일 발송)
		List<String> request = mockmailSender.getRequests();
		assertThat(request.size(), is(2));
		assertThat(request.get(0), is(users.get(1).getEmail()));
		assertThat(request.get(1), is(users.get(3).getEmail()));
	}
	
	private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel){
		assertThat(updated.getId(), is(expectedId));
		assertThat(updated.getLevel(), is(expectedLevel));
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
	public void mockUpgradeLevels() throws Exception{
		UserServiceImpl userServiceImpl = new UserServiceImpl();
		
		UserDao mockUserDao = mock(UserDao.class);
		when(mockUserDao.getAll()).thenReturn(this.users);
		userServiceImpl.setUserDao(mockUserDao);
		
		MailSender mockMailSender = mock(MailSender.class);
		userServiceImpl.setMailSender(mockMailSender);
		
		userServiceImpl.upgradeLevels();
		
		verify(mockUserDao, times(2)).update(any(User.class));
		verify(mockUserDao, times(2)).update(any(User.class));
		verify(mockUserDao).update(users.get(1));
		assertThat(users.get(1).getLevel(), is(Level.SILVER));
		verify(mockUserDao).update(users.get(3));
		assertThat(users.get(3).getLevel(), is(Level.GOLD));
		
		ArgumentCaptor<SimpleMailMessage> mailMessageArg = 
				ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mockMailSender, times(2)).send(mailMessageArg.capture());
		List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
		assertThat(mailMessages.get(0).getTo()[0], is(users.get(1).getEmail()));
		assertThat(mailMessages.get(1).getTo()[0], is(users.get(3).getEmail()));
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
	@DirtiesContext // 다이내믹 프록시 팩토리 빈을 직접 만들어 사용할 때는 없앴다가 다시 등장한 컨텍스트 무효화 애노테이션
	public void upgradeAllOrNothing() throws Exception{
		userDao.deleteAll();
		for(User user : users) userDao.add(user);
		
		try {
			this.testUserService.upgradeLevels();
			fail("TestUserServiceException expected");
		} catch (TestUserServiceException e) {
		}
		
		// 예외가 발생하기 전에 레벨 변경이 있었던 사용자의 레벨이 처음 상태로 바뀌었나 확인 
		checkLevelUpgrade(users.get(1), false);
	}
	
	@Test(expected=TransientDataAccessResourceException.class)
	public void readOnlyTransactionAttribute(){
		testUserService.getAll();
	}
	
	// 트랜잭션 테스트용 스태틱 클래스
	static class TestUserService extends UserServiceImpl{
		private String id = "tuser2";
		
		protected void upgradeLevel(User user){
			// 지정된 id의 User 오브젝트가 발견되는 예외발생하여 작업강제 중단..
			if(user.getId().equals(this.id)) throw new TestUserServiceException();
			super.upgradeLevel(user);
		}
		
		// 읽기전용 트랜잭션의 대상인 get으로 시작하는 메소드 오버라이드
		@Override
		public List<User> getAll(){
			for(User user : super.getAll()){
				super.update(user);	// 강제로 쓰기를 시도. get*메소드는 읽기전용 속성으로 인한 예외 발생
				
			}
			return null;	// 메소드가 끝나기 전에 예외가 발생하지만 컴파일 에러 방지. 
		}
	}
	
	static class TestUserServiceException extends RuntimeException{
		
	}
	
	@Test
	public void advisorAutoProxyCreator(){
		// 프록시로 변경된 오브젝트인지 확인
		assertThat(testUserService, is(java.lang.reflect.Proxy.class));
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
	
	static class MockUserDao implements UserDao{
		private List<User> users; // 레벨 업그레이드 후보 User 오브젝트 목록
		private List<User> updated = new ArrayList(); // 업그레이드 대상 오브젝트를 저장할 목록
		
		private MockUserDao(List<User> users){
			this.users = users;
		}
		
		public List<User> getUpdated(){
			return this.updated;
		}
		
		// 스텁 기능 제공
		public List<User> getAll(){
			return this.users;
		}
		
		// 목 오브젝트 기능 제공 
		public void update(User user){
			updated.add(user);
		}
		
		// 사용하지 않는 메소드는 UnsupportedOperationException을 던지게 해서 지원하지 않는 기능이라는 예외가 발생하도록 처리..
		public void add(User user) {throw new UnsupportedOperationException(); }
		public void deleteAll() {throw new UnsupportedOperationException(); }
		public User get(String id) {throw new UnsupportedOperationException(); }
		public int getCount() {throw new UnsupportedOperationException(); }
	}
	
}
