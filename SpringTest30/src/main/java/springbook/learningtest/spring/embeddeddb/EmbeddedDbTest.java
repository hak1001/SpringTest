package springbook.learningtest.spring.embeddeddb;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.HSQL;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

public class EmbeddedDbTest {
	EmbeddedDatabase db;
	SimpleJdbcTemplate template;
	
	@Before
	public void setUp(){
		db = new EmbeddedDatabaseBuilder()
		.setType(HSQL)	// HSQL, DERBY, H2 세 가지 중 하나를 선택할 수 있다. 
		.addScript("classpath:/springbook/learningtest/spring/embeddeddb/schema.sql")
		.addScript("classpath:/springbook/learningtest/spring/embeddeddb/data.sql")
		.build();
		
		template = new SimpleJdbcTemplate(db);
	}
	
	// 매 테스트를 진행한 뒤에 DB를 종료. 내장형 메모리 DB는 따로 저장하지 않는 한 애플리케이션과 함께 매번 새롭게 DB가 만들어지고 제거되는 생명주기를 갖는다. 
	@After
	public void tearDown(){
		db.shutdown();
	}
	
	@Test
	public void initData(){
		assertThat(template.queryForInt("select count(*) from sqlmap"), is(2));
		
		List<Map<String, Object>> list = template.queryForList("select * from sqlmap order by key_");
		assertThat((String)list.get(0).get("key_"), is("KEY1"));
		assertThat((String)list.get(0).get("sql_"), is("SQL1"));
		assertThat((String)list.get(1).get("key_"), is("KEY2"));
		assertThat((String)list.get(1).get("sql_"), is("SQL2"));
	}
	
	@Test
	public void insert(){
		template.update("insert into sqlmap(key_, sql_) values(?,?)", "KEY3", "SQL3");
	}
	
}
