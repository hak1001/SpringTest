package springbook.user.service;

import java.util.List;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

public class UserService {
	public static final int MIN_LOGCOUNT_FOR_SILVER = 50;
	public static final int MIN_RECOMMEND_FOR_GOLD = 30;

	private UserDao userDao;
	
	public void setUserDao(UserDao userDao){
		this.userDao = userDao;
	}
	
	private PlatformTransactionManager transactionManager;
	
	public void settransactionManager(PlatformTransactionManager transactionManager){
		this.transactionManager = transactionManager;
	}
	
	private MailSender mailSender;
	
	public void setMailSender(MailSender mailSender){
		this.mailSender = mailSender;
	}
	
	// 레벨 업그레이드 메소드
	public void upgradeLevels() throws Exception{
		// 스프링 트랜잭션 추상 인터페이스
		TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			List<User> users = userDao.getAll();
			for(User user : users){
				// 업그레이드 가능 확인용 메소드와 업그레이드 작업 메소드로 리팩토링
				if(canUpgradeLevel(user)){
					upgradeLevel(user);
				}
			}
			transactionManager.commit(status);
		} catch (Exception e) {
			transactionManager.rollback(status);
			throw e;
		} 
		
	}
	
	// 레벨 업그레이드 가능 확인 메소드
	private boolean canUpgradeLevel(User user){
		Level currentLevel = user.getLevel();
		switch(currentLevel){
			case BASIC	: return (user.getLogin() >= MIN_LOGCOUNT_FOR_SILVER);
			case SILVER	: return (user.getRecommend() >= MIN_RECOMMEND_FOR_GOLD);
			case GOLD	: return false;
			default	: throw new IllegalArgumentException("Unknown Level: " + currentLevel);
		}
				
	}
	
	// 레벨 업그레이드 작업용 메소드
	protected void upgradeLevel(User user){
		user.upgradeLevel();
		userDao.update(user);
		sendUpgradeEmail(user);
	}
	
	private void sendUpgradeEmail(User user){
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(user.getEmail());
		mailMessage.setFrom("hak1001@naver.com");
		mailMessage.setSubject("Upgrade 안내");
		mailMessage.setText(user.getName() + "님의 등급이 " + user.getLevel().name() + "(으)로 업그레이드 되었습니다.");
		
		this.mailSender.send(mailMessage);
	}
	
	// 사용자 추가 메소드
	public void add(User user){
		if(user.getLevel() == null) user.setLevel(Level.BASIC);
		userDao.add(user);
	}
}
