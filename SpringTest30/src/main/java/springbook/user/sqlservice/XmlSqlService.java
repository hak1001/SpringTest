package springbook.user.sqlservice;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import springbook.user.dao.UserDao;
import springbook.user.sqlservice.jaxb.SqlType;
import springbook.user.sqlservice.jaxb.Sqlmap;

public class XmlSqlService implements SqlService, SqlRegistry, SqlReader{
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
	
	
	// sqlMap은 SqlRegistry 구현의 일부가 된다. 따라서 외부에서 직접 접근할 수 없다
	private Map<String, String> sqlMap = new HashMap<String, String>();
	public String findSql(String key) throws SqlNotFoundException{
		String sql = sqlMap.get(key);
		if(sql == null){
			throw new SqlNotFoundException(key + "에 대한 SQL을 찾을 수 없습니다.");
		}else{
			return sql;
		}
	}
	public void registerSql(String key, String sql){
		sqlMap.put(key, sql);
	}
	
	
	// sqlmapFile은 SqlReader 구현의 일부가 된다. 따라서 SqlReader 구현 메소드를 통하지 않고는 접근하면 안된다. 
	private String sqlmapFile;
	public void setSqlmapFile(String sqlmapFile){
		this.sqlmapFile = sqlmapFile;
	}
	public void read(SqlRegistry sqlRegistry){
		// JAXB API를 이용해 XMl 문서를 오브젝트 트리로 읽어온다. 
		String contextPath = Sqlmap.class.getPackage().getName();
		try {
			JAXBContext context = JAXBContext.newInstance(contextPath);
			Unmarshaller unmashaller = context.createUnmarshaller();
			// 프로퍼티 설정을 통해 제공받은 파일 이름을 사용 
			InputStream is = UserDao.class.getResourceAsStream(this.sqlmapFile);
			Sqlmap sqlmap = (Sqlmap)unmashaller.unmarshal(is);
			
			// 읽어온 SQL을 맵으로 저장해둔다.
			for(SqlType sql : sqlmap.getSql()){
				// SQL 저장 로직 구현에 독립적인 인터페이스 메소드를 통해 읽어들인 SQL과 키를 전달한다. 
				sqlRegistry.registerSql(sql.getKey(), sql.getValue());
			}
		} catch (JAXBException e) {
			throw new RuntimeException();	// JAXBException은 복구 불가능한 예외다. 불필요한 throws를 피하도록 런타임 예외로 포장해서 던진다. 
		}
	}
	
}
