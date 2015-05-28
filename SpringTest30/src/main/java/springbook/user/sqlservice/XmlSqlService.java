package springbook.user.sqlservice;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import springbook.user.dao.UserDao;
import springbook.user.sqlservice.jaxb.SqlType;
import springbook.user.sqlservice.jaxb.Sqlmap;

public class XmlSqlService implements SqlService{
	private Map<String, String> sqlMap = new HashMap<String, String>();
	
	// 스프링이 오브젝트를 만드는 시점에서 SQL을 읽어오도록 생성자를 이용
	public XmlSqlService(){
		// JAXB API를 이용해 XMl 문서를 오브젝트 트리로 읽어온다. 
		String contextPath = Sqlmap.class.getPackage().getName();
		try {
			JAXBContext context = JAXBContext.newInstance(contextPath);
			Unmarshaller unmashaller = context.createUnmarshaller();
			// UserDao와 같은 클래스패스의 sample.xml 파일을 변환한다. 
			InputStream is = UserDao.class.getResourceAsStream("sqlmap.xml");
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
