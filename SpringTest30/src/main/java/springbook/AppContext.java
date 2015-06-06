package springbook;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import springbook.user.dao.UserDao;
import springbook.user.service.DummyMailSender;
import springbook.user.service.UserService;
import springbook.user.service.UserServiceTest.TestUserService;

import com.mysql.jdbc.Driver;

@Configuration
@EnableTransactionManagement	// <tx:annotation-driven /> 을 대신하는 애노테이션
@ComponentScan(basePackages="springbook.user")
@EnableSqlService
@PropertySource("/database.properties")
public class AppContext implements SqlMapConfig{
	/**
	 * @Value 필드 사용
	 */
	@Value("${db.driverClass}") Class<? extends Driver> driverClass;
	@Value("${db.url}") String url;
	@Value("${db.username}") String username;
	@Value("${db.password}") String password;
	
	@Override
	public Resource getSqlMapResource(){
		return new ClassPathResource("sqlmap.xml", UserDao.class);
	}
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer placeholderConfigurer(){
		return new PropertySourcesPlaceholderConfigurer();
	}
	
	@Bean
	public DataSource dataSource(){
		SimpleDriverDataSource ds = new SimpleDriverDataSource();
		
		ds.setDriverClass(this.driverClass);
		ds.setUrl(this.url);
		ds.setUsername(this.username);
		ds.setPassword(this.password);
		
		return ds;
	}
	
	/**
	 * Environment 사용
	@Autowired Environment env;
	
	@Bean
	public DataSource dataSource(){
		SimpleDriverDataSource ds = new SimpleDriverDataSource();
		
		try {
			ds.setDriverClass((Class<? extends java.sql.Driver>)Class.forName(env.getProperty("db.driverClass")));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		ds.setUrl(env.getProperty("db.url"));
		ds.setUsername(env.getProperty("db.username"));
		ds.setPassword(env.getProperty("db.password"));
		
		return ds;
	}
	*/
	
	@Bean
	public PlatformTransactionManager transactionManager(){
		DataSourceTransactionManager tm = new DataSourceTransactionManager();
		tm.setDataSource(dataSource());
		return tm;
	}
	
	@Configuration
	@Profile("test")
	public static class TestAppContext {
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
	
	@Configuration
	@Profile("production")
	public static class ProductionAppContext {
		@Bean
		public MailSender mailSender(){
			JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
			mailSender.setHost("localhost");
			return mailSender;
		}
	}
}
