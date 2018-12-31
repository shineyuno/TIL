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
