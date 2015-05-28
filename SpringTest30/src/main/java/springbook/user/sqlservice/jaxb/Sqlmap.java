package springbook.user.sqlservice.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="sqlmapType", propOrder={ "sql" })	//변환 작업에서 참고할 정보를 애노테이션으로 갖고 있다. 
@XmlRootElement(name="sqlmap")
public class Sqlmap {
	@XmlElement(required = true)
	protected List<SqlType> sql;	// <sql> 태그의 정보를 담은 SqlType 오브젝트
	
	public List<SqlType> getSql(){
		if(sql == null){
			sql = new ArrayList<SqlType>();
		}
		return this.sql;
	}
}
