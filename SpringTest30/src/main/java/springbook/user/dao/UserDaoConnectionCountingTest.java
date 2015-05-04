package springbook.user.dao;

import java.sql.SQLException;
import java.util.Date;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import springbook.user.domain.User;

public class UserDaoConnectionCountingTest {
	public static void main(String[] args) throws ClassNotFoundException, SQLException{
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(CountingDaoFactory.class);
		UserDao dao = context.getBean("userDao", UserDao.class);
		
		Date ndate = new Date();
		String sMi = String.format("%02d", ndate.getMinutes());
		String sSe = String.format("%02d", ndate.getSeconds());
		
		for(int i=0; i<10; i++) {
			String sId = "hak" + sMi + "_" + sSe + i;
			User user = new User();
			user.setId(sId);
			user.setName("±èÇÐ¿­");
			user.setPassword("passhak");
			
			dao.add(user);
		}
		
		CountingConnectionMaker ccm = context.getBean("connectionMaker", CountingConnectionMaker.class);
		System.out.println("Connection counter : " + ccm.getCounter());
		
	}
}
