package springbook.user.sqlservice;

import javax.annotation.PostConstruct;

public class BaseSqlService implements SqlService{
	// 의존 오브젝트를 DI 받을 수 있도록 인터페이스 타입의 프로퍼티 선언
	private SqlReader sqlReader;
	private SqlRegistry sqlRegistry;
	
	public void setSqlReader(SqlReader sqlReader){
		this.sqlReader = sqlReader;
	}
	
	public void setSqlRegistry(SqlRegistry sqlRegistry){
		this.sqlRegistry = sqlRegistry;
	}
	
	// 생성자를 대신할 초기화 메소드
	@PostConstruct	// loadSql() 메소드를 빈의 초기화 메소드로 지정
	public void loadSql(){
		this.sqlReader.read(this.sqlRegistry);
	}
	
	public String getSql(String key) throws SqlRetrievalFailureException{
		try {
			return this.sqlRegistry.findSql(key);
		} catch (SqlNotFoundException e) {
			throw new SqlRetrievalFailureException(e);
		}
	}
}
