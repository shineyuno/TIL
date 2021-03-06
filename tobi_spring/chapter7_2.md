# 7.2 인터페이스의 분리와 자기참조 빈
## 7.2.1 XML 파일매핑
### JAXBJ
XML에 담긴 정보를 파일에서 읽어오는 방법은 다양하다.

JAXB(Java Architecture for XML Binding)의 장점은 XML 문서정보를 거의 동일한 구조의 오브젝트로 직접 매핑해준다.

### SQL 맵을 위한 스키마 작성과 컴파일

### 언마샬링
* 언마샬링(unmarshalling) XML 문서를 읽어서 자바의 오브젝트로 변환하는것
* 마샬링(marshalling) 바인딩 오브젝트를 XML 문서로 변환하는것

자바 오브젝트를 바이트 스트림으로 바꾸는걸 직렬화(serialization)라고 부르는것과 비슷

## 7.2.2 XML 파일을 이용하는 SQL 서비스
### SQL 맵 XML 파일

### XML SQL 서비스
스프링이 언제 어떻게 빈 오브젝트를 생성할지 알 수 없으니 SQL을 읽는 초기 작업을 어디서 어떻게 해야 할지 조금 막막하다.
일단은 간단히 생성자에서 SQL을 읽어와 내부에 저장해두는 초기 작업을 하자.
항상 그래 왔듯이 일단 가장 간단한 방법으로 기능이 동작하게 만든 다음, 차근차근 좀 더 나은 구조와 코드로 개석ㄴ해나가면 된다.

## 7.2.3 빈의 초기화 작업
 생성자에서 예외가 발생할 수도 있는 복잡한 초기화 작업을 다루는 것은 좋지 않다.
오브젝트를 생성하는 중에 생성자에서 발생하는 예외는 다루기 힘들고, 상속하기 불편하며, 보안에도 문제가 생길 수 있다.
일단 초기 상태를 가진 오브젝트를 만들어 놓고 별도의 초기화 메소드를 사용하는 방법이 바람직하다.

코드의 로직과 여타 이유로 바뀔 가능성이 있는 내용은 외부에서 DI로 설정해줄 수 있게 만들어야 한다.

스프링은 빈 오브젝트를 생성하고 DI 작업을 수행해서 프로퍼티를 모두 주입해준 뒤에 미리 지정한 초기화 메소드를 호출 해주는 기능을 갖고 있다.
 
 context 네임 스페이스를 사용해서 <context:annotation-config/> 태그를 만들어 설정파일에 넣어주면 빈 설정 기능에 사용할 수 있는 특별한
 애노테이션 기능을 부여해주는 빈 후처리기들이 등록된다.
 
 리스트 7-24 context 네임스페이스 선언과 annotiation-config 태그 설정
 ```xml
 <beans xmlns="http://www.springframework.org/schema/beans"
  ...
  ## context 스키마에 정의된 태그를 context 네임스페이스를 통해 사용하도록 정의한다.
      xmlns:context="http://www.springframework.org/schema/context" 
  ...
      xsi:schemaLocation="http://www.springframework.org/schema/beans
      ...
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context-3.0.xsd
     ... ">
     
     
     <tx:annotation-driven /> ## @Transactional 붙은 타입과 메소드에 트랜잭션 부가기능을 담은 프록시를 추가하도록 만들어주는 후처리기 등록
     
     <context:annotation-config /> ## 코드의 애노테이션을 이용해서 부가적인 빈 설정 또는 초기화 작업을 해주는 후처리기 등록
           
 ```
 <context:annotation-config /> 태그에 의해 등록되는 빈 후처리기는 몇 가지 특별한 빈 설정에 사용되는 애노테이션을 제공한다.
 <tx:annotation-driven /> 선언에 의해 @Transactional을 사용 할수 있었던 것과 빅슷하다고 보면된다.
 
 스프링은 @PostConstruct 애노테이션을 빈 오브젝트의 초기화 메소드를 지정하는데 사용한다.
 
 생성자와 달리 프로퍼티까지 모두 준비된 후에 실행된다는 면에서 @PostConstruct 초기화 메소드는 매우 유용하다.
 
 리스트 7-25 @PostConstruct 초기화 메소드
 ```java
 public class XmlSqlService implements SqlService {
  ...
  @PostConstruct // loadSql()메소드를 빈의 초기화 메소드로 지정한다.
  public void loadSql() {...}
 }
 ```

## 7.2.4 변화를 위한 준비: 인터페이스 분리
SQL을 가져오는 것과 보관해두고 사용하는 것은 충분히 독자적인 이유로 변경 가능한 독립적인 전략이다.
서로 변하는 시기와 성질이 다른 것, 변하는 것과 변하지 않는 것을 함께 두는 건 바람직한 설계구조가 아니다.
지금까지 해왔듯이 서로 관심이 다른 코드들을 분리하고, 서로 코드에 영향을 주지 않으면서 유연하게 확장 가능하도록 DI를 적용해 보자.

### 책임에 따른 인터페이스 정의
XmlSqlService 구현을 참고해서 독립적으로 변경 가능한 책임을 뽑아보자
* 첫째는 SQL정보를 외부의 리소스로부터 읽어오는것
* 두 번째 책임은 읽어온 SQL을 보관해두고 있다가 필요할 때 제공해주 주는것

변경 가능은 기능은 전략 패턴을 적용해 별도의 오브젝트로 분리해줘야 한다.

리스트 7-27 SqlService 구현 클래스 코드
```java
Map<String, String> sqls = sqlReader.readSql();  // Map이라는 구체적인 전송타입을 강제하게 된다.
sqlRegistry.addSqls(sqls);
```
구현 방식이 다양한 두개의 오브젝트 사이에 복잡한 정보를 전달하기 위해서는 전달 과정중에 일정한 포멧으로 변환하도록
강제하는 것은 피할수 없는것일까?

리스트 7-28
```java
sqlReader.readSql(sqlRegistry); // SQL을 저장할 대상인 sqlRegistry 오브젝트를 전달한다.
```

## 7.2.5 자기참조 빈으로 시작하기
### 다중 인터페이스 구현과 간접 참조
인터페이스에만 의존하도록 만들어야 스프링의 DI를 적용할 수 있다.
굳이 DI를 적용하지 않았더라도 자신이 사용하는 오브젝트의 클래스가 어떤 것인지 알지 못하게 만드는 것이 좋다.
그래야 구현 클래스를 바꾸고 의존 오브젝트를 변경해서 자유롭게 확장할 기회를 제공해주는 것이다.

자바는 extends를 이용해 다른 클래스이 구현 내용을 상속하려는 경우에는 하나의 클래스만 상속하게 한다.
인터페이스는 한 개 이상을 상속하는것이 허용된다.

인터페이스 구현은 타입을 상속하는 것이다.

### 인터페이스를 이용한 분리

### 자기참조 빈 설정

리스트 7-36 자신을 참조하는 sqlService 빈 설정
```xml
<bean id="sqlService" class="springbook.user.sqlservice.XmlSqlService">
 <property  name="sqlReader" ref="sqlService"/> ## 프로퍼티는 자긴 자신을 참조 할 수 있다. 수정자 메소드로 주입만 가능하면 된다.
 <property  name="sqlRegistry" ref="sqlService"/>
 <property  name="sqlmapFile" value="sqlamp.xml" />
</bean> 
```

스프링은 프로퍼티의 ref 항목에 자기 자신을 넣는 것을 허용한다.
인터페이스를 사용하고 DI를 이용하면 이렇게 특별한 구조까지도 유연하게 구성할 수 있다.

## 7.2.6 디폴트 의존관계
### 확장 가능한 기반 클래스
### 디폴트 의존관계를 갖는 빈 만들기
 확장을 고려해서 기능을 분리하고, 인터페이스와 전략 패턴을 도입하고, DI를 적용한다면 늘어난 클래스와 인터페이스 구현과 의존관계
 설정에 대한 부담은 감수해야 한다.
 
 특정 의존 오브젝트가 대부분의 환경에서 거의 디폴트라고 해도 좋을 만큼 기본적으로 사용될 가능성이 있다면, 디폴트 의존관계를
 갖는 빈을 만드는 거슬 고려해볼 필요가 있다.
  디폴트 의좐관계란 외부에서 DI 받지 않는 경우 기본적으로 자동 적용되는 의존관계를 말한다.
  
  리스트 7-41 생성자를 통한 디폴트 의존관계 설정
  ```java
  public class DefaultSqlService extends BaseSqlService {
   public DefaultSqlService(){ //생성자에서 디폴트 의존 오브젝트를 직접 만들어서 스스로 DI 해준다.
    setSqlReader(new JaxbXmlSqlReader()); 
    setSqlRegistry(new HashMapSqlRegistry());
   }
  }
  ```
  
  리스트 7-43 디폴트 값을 갖는 JaxbXmlSqlReader
  ```java
  public class JaxbXmlSqlReader implements SqlReader {
   private static final String DEFAULT_SQLMAP_FILE = "sqlmap.xml"; //굳이 상수로 만들지 않고 바로 sqlmapFile의 값으로 넣어도 상관없지만
                                                                   // 이렇게 해주면 의도가 코드에 분명히 드러나고 코드도 폼이 난다.
   private String sqlmapFile = DEFAULT_SQLMAP_FILE;
   
   //sqlmapFile 프로퍼티를 지정하면 지정된 파일이 사용되고, 아니라면 디폴트로 넣은파일이 사용된다.
   public void setSqlmapFile(String sqlmapFile) { this.sqlmapFile = sqlmapFile; } 
 ...
 ```
 DI를 사용한다고 해서 항상 모든 프로퍼티 값을 설정에 넣고, 모든 의존 오브젝트를 빈으로 일일이 지정할 필요는 없다.
 먼저 BaseSqlService와 같이 의존 오브젝트를 DI해줌으로써 기능의 일부를 자유롭게 확장해줄 수 있는 기반을 만들어 둬야 하지만,
 DefaultSqlService처럼 자주 사용되는 의본 오브젝트는 미리 지정한 디폴트 의존 오브젝트를 설정 없이도 사용할 수 있게 만드는것도 좋은 방법이다.
 
 디폴트 의존 오브젝트 대신 사용하고 싶은 구현 오브젝트가 있다면 7-44와 같이 설정에 프로퍼티를 추가해주면 된다.
 설정하지 않은 부분은 디폴트가 사용된다.
 
 리스트 7-44 디폴트 의존 오브젝트 대신 사용할 빈 선언
 ```xml
 <bean id="sqlService" class="springbook.user.sqlservice.DefaultSqlService" >
  <property name="sqlRegistry" ref="ultraSuperFastSqlRegistry" />
</bean>  
 ```
