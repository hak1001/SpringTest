package springbook.user.sqlservice.updatable;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import springbook.user.sqlservice.SqlNotFoundException;
import springbook.user.sqlservice.SqlUpdateFailureException;
import springbook.user.sqlservice.UpdatableSqlRegistry;

public abstract class AbstractUpdatableSqlRegistryTest {
	UpdatableSqlRegistry sqlRegistry;
	
	@Before
	public void setUp(){
		sqlRegistry = createUpdatableSqlRegistry();
		sqlRegistry.registerSql("KEY1", "SQL1");
		sqlRegistry.registerSql("KEY2", "SQL2");
		sqlRegistry.registerSql("KEY3", "SQL3");
	}
	
	// 테스트 픽스처를 생성하는 부분만 추상 메소드로 생성.
	abstract protected UpdatableSqlRegistry createUpdatableSqlRegistry();
	
	@Test
	public void find(){
		checkFindResult("SQL1", "SQL2", "SQL3");
	}
	
	// 서브 클래스에서 접근이 가능하도록 protected로 변환
	protected void checkFindResult(String expected1, String expected2, String expected3){
		assertThat(sqlRegistry.findSql("KEY1"), is(expected1));
		assertThat(sqlRegistry.findSql("KEY2"), is(expected2));
		assertThat(sqlRegistry.findSql("KEY3"), is(expected3));
	}
	
	// 주어진 키에 해당하는 SQL을 찾을 수 없을 때 예외가 발생하는지 테스트
	@Test(expected=SqlNotFoundException.class)
	public void unkonwnKey(){
		sqlRegistry.findSql("SQL9999!@#$");
	}

	// 하나의 SQL을 변경하는 기능에 대한 테스트
	@Test
	public void updateSingle(){
		sqlRegistry.updateSql("KEY2", "Modified2");
		checkFindResult("SQL1", "Modified2", "SQL3");
	}

	// 한번에 여러 개의 SQL을 변경하는 기능에 대한 테스트
	@Test
	public void updateMulti(){
		Map<String, String> sqlmap = new HashMap<String, String>();
		sqlmap.put("KEY1", "Modified1");
		sqlmap.put("KEY3", "Modified3");
		
		sqlRegistry.updateSql(sqlmap);
		checkFindResult("Modified1", "SQL2", "Modified3");
	}
	
	// 존재하지 않는 SQL을 변경하려고 시도할 때 예외가 발생하는지 테스트
	@Test(expected=SqlUpdateFailureException.class)
	public void updateWithNotExistingKey(){
		sqlRegistry.updateSql("SQL9999!@#$", "Modified2");
	}
}
