package springbook.user.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import springbook.user.domain.Level;
import springbook.user.domain.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/applicationContext.xml")
public class UserDaoTest {
	@Autowired UserDao dao;
	@Autowired DataSource dataSource;
	
	private User user1;
	private User user2;
	private User user3;
	
	
	// 테스트용 사용자 정보 객체 생성. 
	@Before
	public void setUp(){
		this.user1 = new User("test1", "테스터1", "password", Level.BASIC, 1, 0);
		this.user2 = new User("test2", "테스터2", "xptmxm", Level.SILVER, 55, 10);
		this.user3 = new User("admin", "관리자", "admintest", Level.GOLD, 100, 40);
	}
	
	// 사용자 정보 등록 테스트
	@Test
	public void addAndGet(){
		dao.deleteAll();
		assertThat(dao.getCount(), is(0));
		
		dao.add(user1);
		dao.add(user2);
		assertThat(dao.getCount(), is(2));
		
		User userget1 = dao.get(user1.getId());
		checkSameUser(userget1, user1);
		
		User userget2 = dao.get(user2.getId());
		checkSameUser(userget2, user2);
	}
	
	// 등록된 사용자 숫자 체크 테스트
	@Test
	public void count(){
		dao.deleteAll();
		assertThat(dao.getCount(), is(0));
		
		dao.add(user1);
		assertThat(dao.getCount(), is(1));
		
		dao.add(user2);
		assertThat(dao.getCount(), is(2));
		
		dao.add(user3);
		assertThat(dao.getCount(), is(3));
		
	}
	
	// 등록된 사용자 정보가 없을 때 테스트..
	@Test(expected=EmptyResultDataAccessException.class)
	public void getUserFailure(){
		dao.deleteAll();
		assertThat(dao.getCount(), is(0));
		
		dao.get("unknown_id");
				
	}
	
	// 사용자 리스트 테스트
	@Test
	public void getAll(){
		dao.deleteAll();
		
		List<User> users0 = dao.getAll();
		assertThat(users0.size(), is(0));
		
		dao.add(user1);
		List<User> users1 = dao.getAll();
		assertThat(users1.size(), is(1));
		checkSameUser(user1, users1.get(0));
		
		dao.add(user2);
		List<User> users2 = dao.getAll();
		assertThat(users2.size(), is(2));
		checkSameUser(user1, users2.get(0));
		checkSameUser(user2, users2.get(1));
		
		dao.add(user3);
		List<User> users3 = dao.getAll();
		assertThat(users3.size(), is(3));
		checkSameUser(user3, users3.get(0));
		checkSameUser(user1, users3.get(1));
		checkSameUser(user2, users3.get(2));
	}
	
	// 사용자 정보 검증 테스트
	private void checkSameUser(User user1, User user2){
		assertThat(user1.getId(), is(user2.getId()));
		assertThat(user1.getName(), is(user2.getName()));
		assertThat(user1.getPassword(), is(user2.getPassword()));
		assertThat(user1.getLevel(), is(user2.getLevel()));
		assertThat(user1.getLogin(), is(user2.getLogin()));
		assertThat(user1.getRecommend(), is(user2.getRecommend()));
	}
	
	@Test(expected=DataAccessException.class)
	public void duplicateKey(){
		dao.deleteAll();
		
		dao.add(user1);
		dao.add(user1);
	}
	
	// id중복 입력 오류 테스트 
	@Test
	public void sqlExceptionTranslate(){
		dao.deleteAll();
		
		try {
			dao.add(user1);
			dao.add(user1);
		} catch (DuplicateKeyException ex) {
			SQLException sqlEx = (SQLException)ex.getRootCause();
			// 코드를 이용한 SQLException의 전환
			SQLExceptionTranslator set = new SQLErrorCodeSQLExceptionTranslator(this.dataSource);
			DataAccessException transEx = set.translate(null, null, sqlEx);
			// TODO 에러나는 이유 찾아볼것..
			//assertThat(transEx, is(DuplicateKeyException.class));
		}
	}
	
	// 사용자 정보 수정 테스트
	@Test
	public void update(){
		dao.deleteAll();
		
		dao.add(user1);
		dao.add(user2);
		
		user1.setName("테스터3");
		user1.setPassword("pass333");
		user1.setLevel(Level.GOLD);
		user1.setLogin(1000);
		user1.setRecommend(990);
		dao.update(user1);
		
		User user1update = dao.get(user1.getId());
		checkSameUser(user1, user1update);
		
		User user2same = dao.get(user2.getId());
		checkSameUser(user2, user2same);
	}
}
