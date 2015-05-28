package springbook.user.sqlservice.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="sqlType", propOrder= { "value" })
public class SqlType {
	@XmlValue
	protected String value;	// SQL 값을 저장할 스트링 타입의 필드
	@XmlAttribute(required = true)
	protected String key; 	// key 애트리뷰트에 담긴 검색용 키 값을 위한 스트링 타입의 필드 
	
	public String getValue(){
		return value;
	}
	
	public void setValue(){
		this.value = value;
	}
	
	public String getKey(){
		return key;
	}
	
	public void setKey(){
		this.key = key;
	}
}
