package springbook.user.sqlservice;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import springbook.user.dao.UserDao;
import springbook.user.sqlservice.jaxb.SqlType;
import springbook.user.sqlservice.jaxb.Sqlmap;

public class JaxbXmlSqlReader implements SqlReader{
	private static final String DEFAULT_SQLMAP_FILE="sqlmap.xml";
	
	private String sqlmapFile = DEFAULT_SQLMAP_FILE;
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
