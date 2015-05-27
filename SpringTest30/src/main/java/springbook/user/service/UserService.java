package springbook.user.service;

import java.util.List;

import springbook.user.domain.User;

public interface UserService {
	void add(User user);
	
	// 신규 메소드 추가
	User get(String id);
	List<User> getAll();
	void deleteAll();
	void update(User user);
	
	void upgradeLevels();

}
