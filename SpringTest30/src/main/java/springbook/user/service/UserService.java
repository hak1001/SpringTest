package springbook.user.service;

import java.util.List;

import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

public class UserService {
	UserDao userDao;
	
	public void setUserDao(UserDao userDao){
		this.userDao = userDao;
	}
	
	// 레벨 업그레이드 메소드
	public void upgradeLevels(){
		List<User> users = userDao.getAll();
		for(User user : users){
			// 업그레이드 가능 확인용 메소드와 업그레이드 작업 메소드로 리팩토링
			if(canUpgradeLevel(user)){
				upgradeLevel(user);
			}
		}
	}
	
	// 레벨 업그레이드 가능 확인 메소드
	private boolean canUpgradeLevel(User user){
		Level currentLevel = user.getLevel();
		switch(currentLevel){
			case BASIC	: return (user.getLogin() >= 50);
			case SILVER	: return (user.getRecommend() >=30);
			case GOLD	: return false;
			default	: throw new IllegalArgumentException("Unknown Level: " + currentLevel);
		}
				
	}
	
	// 레벨 업그레이드 작업용 메소드
	private void upgradeLevel(User user){
		user.upgradeLevel();
		userDao.update(user);
	}
	
	// 사용자 추가 메소드
	public void add(User user){
		if(user.getLevel() == null) user.setLevel(Level.BASIC);
		userDao.add(user);
	}
}
