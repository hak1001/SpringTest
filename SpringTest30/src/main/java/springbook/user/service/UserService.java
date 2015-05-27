package springbook.user.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import springbook.user.domain.User;

@Transactional
public interface UserService {
	// <tx:method name="*" />
	void add(User user);
	void deleteAll();
	void update(User user);
	void upgradeLevels();

	// <tx:method name="get*" read-Only="true"/>
	@Transactional(readOnly=true)
	User get(String id);
	
	@Transactional(readOnly=true)
	List<User> getAll();
}
