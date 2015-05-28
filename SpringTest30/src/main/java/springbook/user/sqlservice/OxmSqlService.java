package springbook.user.sqlservice;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.springframework.oxm.Unmarshaller;

import springbook.user.dao.UserDao;
import springbook.user.sqlservice.jaxb.SqlType;
import springbook.user.sqlservice.jaxb.Sqlmap;

public class OxmSqlService implements SqlService{
	// SqlService의 실제 구현 부분을 위윔할 대상인 BaseSqlService를 인스턴스 변수로 정의
	private final BaseSqlService baseSqlService = new BaseSqlService();
	// OxmSqlService와 OxmSqlReader는 강하게 결합돼서 하나의 빈으로 등록되고 한 번에 설정할 수 있다. 
	private final OxmSqlReader oxmSqlReader = new OxmSqlReader();
	
	private SqlRegistry sqlRegistry =  new HashMapSqlRegistry();
	public void setSqlRegistry(SqlRegistry sqlRegistry){
		this.sqlRegistry = sqlRegistry;
	}
	
	public void setUnmarshaller(Unmarshaller unmarshaller){
		this.oxmSqlReader.setUnmarshaller(unmarshaller);
	}
	
	public void setSqlmapFile(String salmapFile){
		this.oxmSqlReader.setSqlmapFile(salmapFile);
	}
	
	@PostConstruct
	public void loadSql(){
		this.baseSqlService.setSqlReader(this.oxmSqlReader);
		this.baseSqlService.setSqlRegistry(this.sqlRegistry);
		
		this.baseSqlService.loadSql();
	}
	
	public String getSql(String key) throws SqlRetrievalFailureException{
		return this.baseSqlService.getSql(key);
	}
	
	private class OxmSqlReader implements SqlReader{
		private Unmarshaller unmarshaller;
		private final static String DEFAULT_SQLMAP_FILE = "sqlmap.xml";
		private String sqlmapFile = DEFAULT_SQLMAP_FILE;
		
		public void setUnmarshaller(Unmarshaller unmarshaller) {
			this.unmarshaller = unmarshaller;
		}

		public void setSqlmapFile(String sqlmapFile) {
			this.sqlmapFile = sqlmapFile;
		}
		
		public void read(SqlRegistry sqlRegistry){
			try {
				Source source = new StreamSource(UserDao.class.getResourceAsStream(sqlmapFile));
				Sqlmap sqlmap = (Sqlmap)this.unmarshaller.unmarshal(source);
				for(SqlType sql : sqlmap.getSql()){
					sqlRegistry.registerSql(sql.getKey(), sql.getValue());
				}
			} catch (IOException e) {
				// 언마샬 작업 중 IO 에러가 났다면 설정을 통해 제공받은 XML파일 일므이나 정보가 잘못됐을 가능성이 제일 높다. 이런 경우에 가장 적합한 런타임 에러 중 하나인 IllegalArgumentException으로 포장해서 던진다.				
				throw new IllegalArgumentException(this.sqlmapFile + "을 가져올 수 없습니다.", e);
			}
		}
	}
}
