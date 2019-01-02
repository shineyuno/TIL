# 7.3 서비스 추상화 적용
## 7.3.1 OXM 서비스 추상화
XML과 자바 오브젝트를 매핑해서 상호 변환해주는 기술을 간단히 OXM Object-XML-Mapping이라고도 한다.

### OXM 서비스 인터페이스
스프링이 제공하는 OXM 추상화 서비스 인터페이스에는 자바오브젝트를 XML로 변환하는 Marshaller와, 
반대로 XML을 자바오브젝트로 변환하는 Unmarshaller가 있다.

### JAXB 구현 테스트
JAXB를 이용하도록 만들어진 Unmarshaller 구현클래스는 Jaxb2Marshaller다.

리스트 7-46 JAXB용 Unmarshaller 빈설정
```xml
<bean id="unmarshaller" class="org.springframework.oxm.jaxb.Jaxb2Marshaller" >
  <property name="contextPath" value="springbook.user.sqlservice.jaxb" />
</bean>  
``` 
Jaxb2Marshaller 클래스를 빈으로 등록하고 바인딩 클래스의 패키지 이름을 지정하는 프로퍼티인 contextPath만 넣어주면 된다.

unmarshaller 빈은 Unmarshaller 타입이다.
따라서 스프링 컨텍스트 테스트의 @Autowired를 이용해 Unmarshaller 타입의 인스턴스 변수를 선언해주면 빈을 가져올수 있다.

## 7.3.2 OXM 서비스 추상화 적용
### 멤버 클래스를 참조하는 통합클래스
OxmSqlService와 OxmSqlReader는 구조적으로는 강하게 결합되어 있지만 논리적으로 명확하게 분리되는 구조다.
자바의 스태틱 멤버 클래스는 이런 용도로 쓰기에 적합하다.

리스트 7-50 OxmSqlService 기본 구조
```java
public class OxmSqlService implements SqlService {
  
  // final 이므로 변경이 불가능하다. OxmSqlService와 OxmSqlReader는 강하게 결합되서 하나의 빈으로 등록되고 한번에 설정할수 있다.
  private final OxmSqlReader oxmSqlReader = new OxmSqlReader();
  
  private class OxmSqlReader implements SqlReader { // private 멤버 클래스로 정의한다. 톱레벨 클래스인 OxmSqlService만이 사용할 수 있다.
  ...
  }
}
```
하나의 클래스로 만들어두기 때문에 빈의 등록과 설정은 단순해지고 쉽게 사용할수 있다.

### 위임을 이용한 BaseSqlService의 재사용

리스트 7-54 BaseSqlService로의 위임을 적용한 OxmSqlService
```java
public class OxmSqlService implements SqlService {

  //sqlservice의 실제 구현 부분을 위임할 대상인 BaseSqlService를 인스턴스 변수로 정의해둔다.
  private final BaseSqlService baseSqlService = new BaseSqlService();
  
  ...
  
  @PostConstruct
  public void loadSql(){
    //OxmSqlService의 프로퍼티를 통해서 초기화된 sqlReader, sqlRegistry를 실제 작업을 위임할 대상인 baseSqlService에 위임한다.
    this.baseSqlService.setSqlReader(this.oxmSqlReader);
    this.baseSqlService.setSqlRegistry(this.sqlRegistry);
    
    //SQL을 등록ㄹ하는 초기화 작업을 baseSqlService에 위임한다.
    this.baseSqlService.loadSql();
  }
  
  public String getSql(String key) throws SqlRetrievalFailureException {
    return this.baseSqlService.getSql(key); // sql을 찾아오는 작업도 baseSqlService에 위임한다.
  }
}

```

## 7.3.3 리소스 추상화

### 리소스
스프링은 자바에 존재하는 일관성 없는 리소스 접근 API를 추상화해서 Resource라는 추상화 인터페이스를 정의했다.

리스트 7-55 Resource 인터페이스
```java
package org.springframework.core.io
...

public interface Resource extends InputStreamSource {
  // 리소스의 존재나 읽기 가능한지 여부를 확인할 수 있다. 또 현재 리소스에 대한 입력 스트림이 열려 있는지도 확인 가능하다.
  boolean exist();
  boolean isReadable();
  boolean isOpen();
  
  
  // JDK의 URL, URI, File 형태로 전환 가능한 리소스에 사용된다.
  URL getUrl() throws IOException;
  URI getURI() throws IOException;
  File getFile() throws IOException;
  
  Resource createRelative(String relativePath) throws IOException;
  
  // 리소스에 대한 이름과 부가적인 정보를 제공한다.
  long lastModified() throws IOException;
  String getFilename();
  String getDescription();
}


public interface InputStreamSource {
  InputStream getInputStream() throws IOException; // 모든 리소스는 inputStream형태로 가져올 수 있다.
}
```

Resource는 스프링에서 빈이 아니라 값으로 취급된다. 단순한 정보를 가진 값으로 지정된다.
그래서 추상화를 적용하는 방법이 문제다. 
빈으로 등록한다면 리소스의 타입에 따라서 각기 다른 Resource 인터페이스의 구현클래스를 지정해주면 된다.
HTTP로 가져올 리소스라면 HttpResource 같은 클래스가 만들어져서 이를 빈의 클래스로 지정하는 것이다.
하지만 Resource는 빈으로 등록하지 않는다고 했으니 기껏 외부에서 지정한다고 해봐야 <property>의 value 애트리뷰트에 넣는 방법밖에 없다. 
하지만 value 애트리뷰트에 넣을 수 있는건 단순한 문자열 뿐이다.
  
  ### 리소스 로더
  스프링에는 URL 클래스와 유사하게 접두어를 이용해 Resource 오브젝트를 선언하는 방법이 있다.
문자열에 안에 리소스의 종류와 위치를 함께 표현하게 해주는것이다.
그리고 이렇게 문자열로 정의된 리소스를 실제 Resource 타입 오브젝트로 변환해주는 ResourceLoader를 제공한다.

리스트 7-56 ResourceLoader 인터페이스
```java
package org.springframework.core.io

public interface ResourceLoader {
  Resource getResource(String location);  // location에 담긴 스트링 정보를 바탕으로 그에 적절한 Resource로 변환해준다.
  ...
}
```

표 7-1 ResourceLoader가 처리하는 접두어의 예

| 접두어    | 예                               | 설명                                                                                                                                              |
|-----------|----------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| file:     | file:/c:/temp/file.txt           | 파일 시스템의 C:/temp 폴더에 있는 file.txt를 리소스로 만들어 준다.                                                                                |
| classpath | classpath:file.txt               | 클래스패스의 루트에 존재하는 file.txt 리소스에 접근하게 해준다.                                                                                   |
| 없음      | WEB-INF/test.dat                 | 접두어가 없는 경우에는 ResourceLoader 구현에 따라 리소스의 위치가 결정된다. ServletResourceLoader라면 서블릿 컨텍스트의 루트를 기준으로 해석한다. |
| http:     | http://www.myserver.com/test.dat | HTTP 프로토콜을 사용해 접근할 수 있는 웹상의 리소스를 지정한다. ftp:도 사용할 수 있다.                                                            |

ResourceLoader의 대표적인 예는 바로 스프링의 애플리케이션 컨텍스트다. 
애플리케이션 컨텍스트가 구현해야 하는 인터페이스인 ApplicationContext는 ResourceLoader를 상속하고 있다.

애플리케이션 컨텍스트가 사용할 스프링 설정정보가 담긴 XML 파일도 리소스 로더를 이용해 Resource 형태로 읽어 온다.

빈의 프로퍼티를 값을 변환할 때도 리소스 로더가 자주 사용된다. 
스프링이 제공하는 빈으로 등록 가능한 클래스에 파일을 지정해주는 프로퍼티가 존재한다면 거의 모두 Resource 타입이다.

Resource 타입은 빈으로 등록하지 않고 <property> 태그의 value를 사용해 문자로열로 값을 넣는데, 이 문자열로 된 리소스 정보를
Resource 오브젝트로 변환해서 프로퍼티에 주입할때도 애플리케이션 컨텍스트 자신이 리소스 로더로서 변환과 로딩 기능을 담당한다.
  
 ex) 만약 myFile이라는 이름의 프로퍼티가 Resource 타입이라고 하면, 다음과 같은 식으로 접두어가 붙은 리소스 문자열을 사용할수 있다는 뜻이다.
```xml
<property name="myFile" value="classpath:com/epril/myproject/myfile.txt" />
<property name="myFile" value="file:/data/myfile.txt" />
<property name="myFile" value="http://www.myserver.com/test/dat" />
```

### Resource를 이용해 XML 파일 가져오기
Resource 타입은 실제소스가 어떤것이든 상관없이 getInputStream()메소드를 이용해 스트림으로 가져올 수 있다.
이를 StreamSource 클래스를 이용해서 OXM 언마샬러가 필요로 하는 Source 타입으로 만들어주면 된다.

리스트 7-57 Resource 적용
```java
public class OxmSqlService implements Sqlservice {
  
  // 이름과 타입을 모두 변경한다. 큰 변화긴 하지만 그만큼 기능을 확장하고 유연성을 얻는 것이니 고감하게 변경한다.
  public void setSqlmap(Resource sqlmap){
    this.oxmSqlReader.setSqlmap(sqlmap);
  }
  
  ...
  
  private class OxmSqlReader implements SqlReader {
    //SQL 매핑정보 소스의 타입을 Resource로 변경한다. 
    private Resource sqlmap = new ClassPathResource("sqlmap.xml", UserDao.class); // 디폴트 파일은 기존과 같지만 이제는 Resource
                                                                                  // 구현 클래스의 ClassPathResource를 이용한다.
                                                                                  
    public void setSqlmap(Resource sqlmap)  {
      this.sqlmap = sqlmap;
    }
    
    public void read(SqlRegistry sqlRegistry) {
      try {
        Source source = new StreamSource(sqlmap.getInputStream());  // 리소스의 종류에 상관없이 스트림으로 가져올 수 있다.
        ...
      } cathch (IOException e) {
        throw new IllegalArgumentException(this.sqlmap.getFilename() + "을 가져올 수 없습니다.", e);
      }
    }
  }
}
```

classpath:를 사용했을 때는 클래스패스 루트로부터 상대적인 위치지만 file:을 사용하면 파일 시스템의 루트 디렉토리로부터 시작하는
파일 위치를 나타낸다.
