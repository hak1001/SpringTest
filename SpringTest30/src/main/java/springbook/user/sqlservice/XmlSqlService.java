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

public class XmlSqlService implements SqlService{
	private Map<String, String> sqlMap = new HashMap<String, String>();
	
	// 맵 파일이름 프로퍼티 추가
	private String sqlmapFile;
	
	public void setSqlmapFile(String sqlmapFile){
		this.sqlmapFile = sqlmapFile;
	}
	
	// 생성자를 대신할 초기화 메소드
	@PostConstruct	// loadSql() 메소드를 빈의 초기화 메소드로 지정
	public void loadSql(){
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
				sqlMap.put(sql.getKey(), sql.getValue());
			}
		} catch (JAXBException e) {
			throw new RuntimeException();	// JAXBException은 복구 불가능한 예외다. 불필요한 throws를 피하도록 런타임 예외로 포장해서 던진다. 
		}
	}
	
	public String getSql(String key) throws SqlRetrievalFailureException{
		String sql = sqlMap.get(key);
		if(sql == null){
			throw new SqlRetrievalFailureException(key + "를 이용해서 SQL을 찾을 수 없습니다.");
		}else{
			return sql;
		}
	}
	
}
