# 7.6 스프링 3.1의 DI
스프링을 이용해서 만들어지는 애플리케이션 코드가 DI 패턴을 이용해서 안전하게 발전하고 확장할 수 있는 것처럼 
스프링 프레임워크 자체도 DI원칙을 충실하게 따라서 만들어졌기 때문에 기존 설계와 코드에 영향을 주지 않고도 꾸준히
새로운 기능을 추가하고 확장해나가는 일이 가능했다.

### 자바 언어의 변화와 스프링

#### 애노테이션의 메타정보 활용
자바코드의 메타 정보를 이용한 프로그래밍
자바는 소스코드가 컴파일된후 클래스 파일에 저장됐다가, JVM에 의해 메모리로 로딩되어 실행된다.
그런데 때로는 자바코드가 실행되는 것이 목적이 아니라 다른 자바 코드에 의해 데이터처럼 취급되기도 한다.
자바 코드의 일부를 리플렉션 API 등을 이용해 어떻게 만들었는지 살펴보고 그에 따라 동작하는 기능이 점점 많이 사용되고 있다.

**애노테이션**은 옵션에 따라 컴파일된 클래스에 존재하거나 애플리케이션이 동작할때 메모리에 로딩되기도 하지만 자바 코드가
실행되는데 직접 참여하지 못한다.
인터페이스처럼 오브젝트에 타입을 부여하는 것도 아니고, 그 자체로 상속이나 오버라이딩이 가능하지도 않다.
동작하는 코드를 넣을 수 없는 것은 물로이고, 코드에서 간단히 참조하거나 활용할 수가 없다.
복잡한 리플렉션 API를 이용해 애노테이션의 메타정보를 조회하고, 애노테이션 내에 설정된 값을 가져와 참고하는 방법이 전부다.

애노테이션의 활용이 늘어난 이유는 무엇일까?
애노테이션은 애플리케이션을 핵심 로직을 담은 자바 코드와 이를 지원하는 IoC 방식의 프레임워크,
그리고 프레임워크가 참조하는 메타정보라는 세가지로 구성하는 방식에 잘 어울리기 때문일 것이다.
애노테이션은 프레임워크가 참조하는 메타정보로 사용되기에 여러 가지 유리한 점이 많다. 

DI의 기본원리를 소개했던 1장의 내용을 다시 생각해보자 
핵심코드가 런타임 시 동적으로 관계를 맺고 동작하도록 만들어주는 DaoFatory, 그리고 DaoFactory를 활용해 핵심 로직 코드가 서로 관계를 맺고
동작하는 모든 과정을 제어하는 클라이언트 코드, 
핵심 로직을 담은 오브젝트가 클라이언트에 의해 생성되고, 관계를 맺고 제어되는 구조다. 따라서 이때의 클라이언트는 일종의 IoC 프레임워크로 볼수 있다.
또한 DaoFactory는 IoC 프레임워크가 참고하는 일종의 메타정보로 의미가 있다.

런타임 의존관계 정보를 담고 있는 DaoFactory는 처음엔 평범한 자바코드로 작성됐다.
UserDao 한가지가 아니라 애플리케이션을 구성하는 많은 오브젝트의 관계를 IoC/DI를 이용해서 프레임워크와 
메타정보를 활용하는 방식으로 작성하도록 발전시키려면 DaoFactory와 같은 단순한 자바 코드로 만들어진 관계 설정 책임을 담은 코드는 불편하다.
그래서 1.8절에서는 DaoFactory의 내용을 XML로 전환해서 좀 더 간결한 형태로 전환했다.
어차피 DaoFactory는 애플리케이션의 로직을 담은 코드에서 사용될 일이 없기때문에 형태가 어떻든지 상관없다.
XML이라면 DaoFactory 같은 자바 코드를 이용할 때보다 프레임워크가 할 일이 좀 더 많아지긴 하지만, 작성하기 편하고 빌드 과정이 필요 없으며,
AOP를 위해 빈생성과 관계 설정을 재구성하는 경우를 고려하면 자바코드보다 유리했기 때문에 스프링 초창기부터 XML이 프레임워크가 사용하는
오브젝트 관계 설정요 DI 메타정보로 적극 활용돼 왔다.


**애노테이션**은 XML이나 여타 외부파일과 달리 자바코드의 일부로 사용된다. 코드의 동작에 직접 영향을 주지는 못하지만
**메타정보**로서 활용되는 데는 XML 비해 유리한점이 많다.
다음과 같이 간단한 애노테이션이 사용된 코드를 살펴보자

```java
@Special
public class MyClass {
  ...
}
```
@Special 애노테이션이 타입 레벨, 즉 클래스에 부여했다는 사실을 알수 있다.
애노테이션은 정의하기에 따라서 타입, 필드, 메소드, 파라미터, 생성자, 로컬변수의 한군데 이상 적용 가능하다.
위의 코드에선 클래스 레벨에 적용한다는 정보를 얻을수 있다.
애노테이션이 위치한 MyClass 클래스이 메타정보를 얻을수 있다.
애노테이션이 부여된 클래스의 패키지, 클래스 이름, 접근 제한자 상속한 클래스나 구현 인터페이스가 무엇인지 알수 있다.
원한다면 클래스의 필드나 메소드 구성도 확인할수 있다.

단순한 애노테이션 하나를 자바코드에 넣는 것만으로도, 애노테이션을 참고하는 코드에서는 이렇게  다양한 부가정보를 얻어 낼수 있다.

반면에 동일한 정보를 XML로 표현하려면 모든 내용을 명시적으로 나타내야 한다. 
간단히 클래스가 무엇인지만 지정하려고 해도 다음과 같이 작성해야 한다.
```xml
<x:special target="type" class="com.mycompany.myproject.MyClass" />
```

객체지향 언어의 기본에 충실하게 작성된 자바 코드 형태로 시작됐던 DI 패턴은 프레임워크의 발전과 함께
자바코드와 프레임워크, XML 메타정보의 형태로 진행되다가 스프링 3.1 이르러서는 핵심 로직을 담은 자바 코드와 DI 프레임워크,
그리고 DI를 위한 메타데이터로서의 자바 코드로 재구성되고 있다.

#### 정책과 관례를 이용한 프로그래밍
애노테이션 같은 메타정보를 활요하는 프로그래밍 방식은 코드를 이용해 명시적으로 동작 내용을 기술하는 대신 코드 없이도 미리 약속한
규칙 또는 관례를 따라서 프로그램이 동작하도록 만드는 프로그래밍 스타일을 적극적으로 포용하게 만들어왔다.
DaoFactory 같은 자바 코드를 대체한 스프링의 XML도 미리 정의한 정책을 이용해서 특정 기능이 동작하게 만든것이라고 볼수 있다.
<bean> 태그를 작성해두면 그에 따라 하나의 오브젝트가 만들어진다. new 키워드를 이용한 인스턴스 생성 코드가 동작하는 세임이다.
미리 정의된 규칙을 따라서 프레임워크가 작업을 수행한다.
이런 스타일의 프로그래밍 방식은 자바 코드로 모든 작업 과정을 직접 표현했을 때에 비해서 작성해야 할 내용이 줄어든다는 장점이 있다.
좀 더 지능적으로, 자주 반복되는 부분을 관례화 하면 더 많은 내뇽을 생략할 수도 있다.

@Transactional을 제대로 활용하려면 관례화된 이 정책을 기억하고 코드를 작성해야 한다.
애노테이션을 메타정보 이용하면서도 명시적으로 정보를 넣도로 하지 않았다. 
그덕분에 코드는 간결해진다. 하지만 정책을 기억 못하거나 잘못 알고 있을 경우 의도한 대로 동작하지 않는 코드가 만들어질 수 있다.
트랜잭션 속성의 문제 같은 경우는 디버깅도 매우 어렵다.

스프링 3.1은 스프링 하면 제일 먼저 떠오르는 것 중의 하나였던 XML을 전혀 사용하지 않고도 스프링 애플리케이션을 만들 수 있다

스프링이 DI의 원리와 다양한 패턴을 빈과 DI 설정정보를 담은 자바 코드와 애노테이션 등에 어떻게 적용했는지도 눈여겨 보자.
리팩토링을 진행할 때 중요한 것은 테스트를 준비하는 일이다.

## 7.6.1 자바코드를 이용한 빈 설정
### 테스트 컨텍스트의 변경
스프링 3.1은 애노테이션과 자바 코드로 만들어진 DI설정정보와 XML을 동시에 사용할수 있는 방법을 제공한다.

리스트 7-82 XML 파일을 사용하는 UserDaoTest
```java
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(location="/test-applicationContext.xml")
public class UserDaoTest {
...
}
```
@ContextConfiguration은 스프링 테스트가 테스트용 DI 정보를 어디서 가져와야 하는지 지정할 때 사용하는 애노테이션이다.

DI 설정정보를 담은 클래스는 평범한 자바 클래스에 @Configuration 애노테이션을 달아주면 만들수 있다.

리스트 7-83 DI 메타정보로 사용될 TestApplicationContext클래스
```java
@Configuration
public class TestApplicationContext {
}
```

리스트 7-84 TestApplicationContext를 테스트 컨텍스트로 사용하도록 변경한 UserDaoTest
```java
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(calsses=TestApplicationContext.class)
public class UserDaoTest {
...
}
```

테스트를 성공시키려면 XML에 있던 빈 설정정보를 모두 TestApplicationContext로 옮겨야 한다. 
그런데 한번에 다 옮기자니 부담스럽다. 이럴때는 TestApplicationContext에 모든 빈 정보를 담는 대신 XML의 도움을 받도록 만드는게 좋겠다.

자바 클래스로 만들어진 DI 설정정보에서 XML의 설정정보를 가져오게 만들수 있다.
리스트 7-85와 같이 @ImportResource 애노테이션을 이용하면 된다.

리스트 7-85 TestApplicationContext를 테스트 컨텍스트로 사용하도록 변경한 UserDaoTest
```java
@Configuration
@ImportResource("/test-applicationContext.xml")
public class TestApplicationContext {
}
```

### <context:annotation-config />제거
<context:annotation-config />은 @PostConstruct를 붙인 메소드가 빈이 초기화된후에 자동으로 실행되도록 사용했다.
<context:annotation-config />에 의해 등록되는 빈 후처리기가 @PostConstruct와 같은 표준 애노테이션을 인식해서 자동으로 메소드를 실행해준다.

TestApplicationContext처럼 @Configuration이 붙은 설정클래스를 사용하는 컨테이너가 사용되면 더이상 <context:annotation-config />을 넣을필요가 없다.
컨테이너가 직접 @PostConstruct 애노테이션을 처리하는 후처리기를 등록해주기 때문이다.

### \<bean\>의 전환
@Bean은 @Configuration이 붙은 DI 설정용 클래스에서 주로 사용되는 것으로, 메소드를 이용해서 빈 오브젝트의 생성과 의존관계 주입을 
직접 자바코드로 작성할 수 있게 해준다.

리스트 7-87 XML을 이용한 dataSource 빈의 정의
```xml
<bean id="dataSource" class="org.springframework.jdbc.datasource.SimpleDriverDataSource">
 <property name="driverClass" vlaue="com.mysql.jdbc.Driver" />
 <property name="url" value="jdbc:mysql://localhost/springbook?characterEncoding=UTF-8" />
 <property name="username" value="spring" />
 <property name="password" value="book" />
</bean>  
```

빈의 의존관계가 인터페이스를 통해 안전하게 맺어지도록 dataSource 빈의 리터 값 타입은 DataSource 인터페이스로 하는것이 좋다.

가장 먼저 할일은 빈 오브젝트를 만드는 것이다.
@Bean 메소드에서는 빈 인스턴스 생성과 프로퍼티 설정등을 모두 실제 동작하는 코드로 만들 필요가 있다.

리스트 7-88자바 코드로 작성한 dataSource빈
```java
import com.mysql.jdbc.Driver;

@Bean
public DataSource dataSource() {
  SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
  
  dataSource.setDriverClass(Driver.class);
  dataSource.setUrl("jdbc:mysql://localhost/springbook?characterEncoding=UTF-8");
  dataSource.setUsername("spring");
  dataSource.setPassword("book");
  
  return dataSource
}
```

스프링의 \<bean\>에 넣는 클래스는 굳이 public이 아니어도 된다.
내부적으로 리플렉션 API를 이용하기 때문에 private으로 접근을 제한해도 빈의 클래스로 사용할 수 있다.
반면에 직접 자바 코드에서 참조할 때는 패키지가 다르면 public으로 접근 제한자를 바꿔줘야 한다.


자바 코드로 빈을 정의할 때 다른 빈을 프로퍼티에 넣어주려면 빈의 아이디와 같은 이름의 메소드를 호출하면 된다.
```java
dao.setSqlService(sqlService());
```
XML에 정의된 빈은 sqlService()처럼 같은 메소드를 호출하는 방법으로는 가져올 수가 없다.
이런 경우엔 클래스에 @Autowired가 붙은 필드를 선언해서 XML에 정의된 빈을 컨테이너가 주입해주게 해야 한다.

@Autowired가 붙은 필드의 타입과 같은 빈이 있으면 해당 빈을 필드에 자동으로 넣어준다.

리스트 7-93 @Autowired를 이용해서 XML 빈을 참조하게 만든 userDao()메소드
```java
@Autowired SqlService sqlService;

@Bean
public UserDao userDao(){
  UserDaoJdbc dao =  new UserDaoJdbc();
  dao.setDataSource(dataSource());
  dao.setSqlService(this.sqlService);
  return dao;
}
```

@Resource는 @Autowired와 유사하게 필드에 빈을 주입 받을때 사용한다.
차이점은 @Autowired는 필드의 타입을 기준을 빈을 찾고 @Resouce는 필드 이름을 기준으로 한다.

XML에서 사용한\<jdbc:embedded-database\> 전용 태그는 DataSource 타입의 빈을 생성한다.
그런데 이미 TestApplicationContext에 DataSource타입의 dataSource 빈이 존재하므로 타입을 기준으로 주입받게 만들면 혼란이 발생할수 있다.
그래서 필드 이름과 일치하는 빈 아이디를 가진 빈을 주입받을때 사용하는 @Resource를 이용했다.

### 전용태그 
 전용 태그도 \<bean\>과 마찬가지로 빈을 등록하는 데 사용된다. 
 그런데 내부에서 실제로 어떤빈이 만들어지는지 파악하기가 쉽지 않다.
 전용태그 하나에 여러 개의 빈이 만들어질 수도 있다.
 
 #### \<tx:annotation-driven /\> 
 @Transactional을 이용한 트랜잭션 AOP기능을 지원하는 \<tx:annotation-driven /\>  전용태그 </br>
 트랜잭션 AOP를 적용하려면 제법 복잡하고 많은 빈이 동원돼야한다. </br>
 AOP를 위해 기본적으로 어드바이스와 포인트컷이 필요하고, 애노테이션 정보에서 트랜잭션속성을 가져와서 어드바이스에서 사용하게 
 해주는 기능도 필요하다.
 
 \<tx:annotation-driven /\>  은 옵션을 주지 않는다면 기본적으로 다음 네가지 클래스를 빈으로 등록해준다.
 ```java
 org.springframework.aop.framework.autoporxy.InfrastructureAdvisorAutoProxyCreator
 org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
 org.springframework.transaction.interceptor.TransactionInterceptor
 org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor
 ```
 
 XML에서는 용도가 잘 설명되는 전용태그로 정의돼서 손쉽게 사용했는데 자바코드에서는 복잡한 로우 레벨의 클래스를 여러개나 사용해서 빈을 정의해야
 한다면 부담스럽다.
 
 스프링 3.1에서는 \<tx:annotation-driven /\> 과 같이 특별한 목적을 위해 만들어진, 내부적으로 복잡한 로우 레벨의 빈을 
 등록해주는 전용태그에 대응되는 애노테이션을 제공
 
 스프링 3.1은 XML에서 자주 사용되는 전용 태그를 @Enable로 시작하는 애노테이션으로 대체할 수 있게 다양한 애노테이션을 제공한다.
 가장 대표적으로 사용되는것이 \<tx:annotation-driven /\>를 대체할수있는 **@EnableTransactionManagement** 다
 
 ## 7.6.2 빈 스캐닝과 자동와이어링
 ### @Autowired를 이용한 자동와이어링
 빈의 프로퍼티에 다른 빈을 넣어서 런타임 관계를 맺어주려면 <bean>의 <property>를 사용해 빈을 정의하거나 
 자바코드로 직접 수정자 메소드를 호출해줘야했다.
  
 @Autowired는 자동와이어링 기법을 이용해서 조건에 맞는 빈을 찾아 자동으로 **수정자 메소드** 나 **필드** 에 넣어준다. 
 
 자동 와이어링을 이용하면 컨테이너가 이름이나 타입을 기준으로 주입될 빈을 찾아주기 때문에 빈의 프로퍼티 설정을 직접해주는
 자바코드나 XML의 양을 대폭 줄일 수 있다. 컨테이너가 자동으로 주입할 빈을 결정하기 어려운 경우도 있다.
 이럴땐 직접 프로퍼티에 주입할 대상을 지정하는 방법을 병행하면 된다.
 
 리스트 7-100 dataSource 수정자에 @Autowired 적용
 ```java
 public class UserDaoJdbc implements UserDao {
  
  @Autowired
  public void setDataSource(DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }
  ...
 }
 ```
 스프링은 @AutoWired가 붙은 수정자 메소드가 있으면 파라미터 타입을 보고 주입가능한 타입의 빈을 모두 찾는다.
 
 리스트 7-101 sqlService 필드에 @Autowired 적용
```java
 public class UserDaoJdbc implements UserDao {
  
  @Autowired
  private SqlService sqlService;
  
  public void setSqlService(SqlService sqlService) {
    this.sqlService = sqlSerivce;
  }
 }
```
 필드에 직접 값을 넣을 수 있다면 수정자 메소드는 없어도 된다. 리스트 7-101의 setSqlService()는 메소드는 생략해도 좋다.
 
 반면에 리스트 7-100의 setDataSource() 수정자 메소드를 없애고 필드에 @Autowired를 적용하는 건 불가능하다. 왜냐하면 setDataSource() 
 메소드는 여타 수정자 메소드처럼 주어진 오브젝트를 그대로 필드에 저장하는 대신 JdbcTemplate을 생성해서 저장해주기 때문이다.
 
 
 스프링과 무관하게 직접 오브젝트를 생성하고 다른 오브젝트를 주입해서 테스트하는 순수한 단위 테스트를 만드는 경우에는 수정자 메소드가 필요하다.
 예를 들어 UserSerivceTest의 upgradeLevels() 테스트 메소드는 목 오브젝트를 만들어서 UserSerivceImpl의 프로퍼티 필드에 @Autowired를
 적용했다고 수정자 메소드를 제거하면 곤란해진다.

### @Componet를 이용한 자동 빈 등록
@Component 또는 @Component를 메타 애노테이션으로 갖고 있는 애노테이션이 붙은 클래스가 자동 빈 등록 대상이 된다.

@Component는 빈으로 등록될 후보 클래스에 붙여주는 일종의 마커(marker)라고 보면된다.

리스트 7-104 @Component적용
```java
@Component
public class UserDaoJdbc implements UserDao {
```
+ @Component 애노테이션이 달린 클래스를 자동으로 찾아서 빈을 등록해주게 하려면 빈 스캔 기능을 사용하겠다는 애노테이션 정의가 필요.
+ 빈 자동등록이 컨테이너가 디폴트로 제공하는 기능은 아님.
+ 프로젝트 내의 모든 클래스패스를 다 뒤져서 @Component 애노테이션이 달린 클래스를 찾는 것은 부담이 많이 가는 작업이다.
+ 특정 패키지 아래서만 찾도록 기준이 되는 패키지를 지정해줄 필요가 있다.
+ 이때 사용되는 애노테이션은 @ComponentScan이다.

리스트 7-105 @ComponentScan 적용
```java
@Configuration
@EnableTransactionManagement
@ComponetScan(basePackages="springbook.user")
public class TestApplicationContext {
```
+ **@Component**의 basePackages 엘리먼트는 @Component가 붙은 클래스를 스캔할 기준 패키지를 지정할때 사용 
+ 기준 패키지는 여러개 넣어도 된다.
+ 지정한 패키지 아래의 모든 서브패키지를 다 검색

@Componentr가 붙은 클래스이고, 빈의 아이디는 따로 지정하지 않았으면 클래스 이름의 첫 글자를 소문자로 바꿔서 사용한다.

자동 빈 등록을 이용하는 경우 빈의 의존관계를 담은 프로퍼티를 따로 지정할 방법이 없다.
그래서 프로퍼티 설정에 @Autowired와 같은 자동와이어링 방식을 적용해야 한다.

@Component가 붙은 클래스의 이름 대신 다른 이름을 빈의 아이디로 사용하고 싶다면 다음과 같이 애노테이션에 이름을 넣어주면된다.
```java
@Component("userDao")
```

애노테이션은 @interface 키워드를 이용해 정의한다.
@Component 애노테이션은 다음과 같이 정의되어 있다.
```java
public @interface Componet {
...
}
```
애노테이션은 상속을 할수 없고, 인터페이스를 구현할 수도 없다.
여러 개의 애노테이션에 공통적인 속성을 부여하려면 메타 애노테이션을 이용한다.

#### 메타 애노테이션
메타애노테이션은 애노테이션의 정의에 부여된 애노테이션을 말한다.

애노테이션을 정의할 때 메타 애노테이션으로 @Component를 부여해주면 클래스마다 @Component를 따로 붙여주지 않아도 자동 빈 등록 
대상으로 만들 수 있다.

리스트 7-106 @Component메타 애노테이션을 가진 애노테이션 정의
```java
@Component
public @interface SnsConnector {
...
}
```

#### @Repository 
스프링은 DAO 빈을 자동등록 대상으로 만들때 사용할 수 있게 @Repository 애노테이션을 제공한다.
@Component를 부여하는 것만으로도 등록 대상으로 만드는 데 충분하지만 스프링은 DAO 기능을 제공하는 클래스에는 @Repository
애노테이션을 이용하도록 권장한다. @Repository는 @Component를 메타 애노테이션으로 갖고 있다.


#### @Service
@Service도 @Repository처럼 스프링이 제공하는 빈 자동등록용 애노테이션인데, 이 애노테이션은 비즈니스 로직을 담고 있는
서비스 계층의 빈을 구분하기 위해 사용된다. 서비스 계층은 트랜잭션 경계가 되는 곳이라 @Transactional이 함꼐 사용되는 경우가 많다.

## 7.6.3 컨텍스트 분리와 @Import

### 테스트용 컨텍스트 분리
TestApplicationContext 클래스의 이름을 AppContext라고 바꾸자.
테스트 정보를 분리하고 남은 애플리케이션의 핵심 DI 정보를 남겨둘 클래스이므로 이름에서 Test를 빼는게 좋겠다.
문제는 Test를 빼고 나면 이름이 ApplicationContext가 돼서 스프링 컨테이너의 핵심 인터페이스인 ApplicationContext와 
혼동된다는 점으로, **자주 쓰는 인터페이스와 이름이 겹치는 건 별로 좋지 않다.** 그래서 앞부분을 줄여서 AppContext로 바꾸겠다. 

테스트용으로 특별히 만든 빈은 설정정보에 내용이 드러나 있는 편이 좋다.

하나 이상의 설정 클래스가 스프링 테스트에서 사용되게 하려면 리스트 7-111과 같이 classes에 적용할 설정 클래스를 모두 나열해주면 된다.

리스트 7-111 테스트 컨텍스트의 DI 설정 클래스 정보 수정
```java
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestAppContext.class, AppContext.class})
public class UserDaoTest {
```

### @Import
+ 테스트용 설정정보는 애플리케이션 핵심 설정정보와 깔끔하게 분리되는 편이 낫다.
+ 반면에 SQL서비스와 관련된 빈 설정은 별도 클래스로 분리하더라도 애플리케이션이 동작할때 항상 필요한 정보다.
+ 그래서 파일을 구분했더라도 애플리케이션 설정정보의 중심이되는 AppContext와 긴밀하게 연결해주는 게 좋다. 

AppContext가 메인 설정정보가 되고, SqlServiceContext는 AppContext에 포함되는 보조 설정정보로 사용
자바 클래스로 된 설정정보를 가져올 때는 @ImportResource 대신 @Import를 이용한다.

리스트 7-113 @Import 적용
```java
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages="springbook.user")
@Import(SqlServiceContext.class)
public class AppContext {
```

## 7.6.4 프로파일
같은 타입이면서 아이디도 같은 두개의 빈이 있으면 스프링이 빈 정보를 읽는 순서에 따라 뒤의 빈 설정이 앞에서 발견된 빈 설정에 우선해서 적용된다.

테스트 환경과 운녕환경에서 각기 다른 빈 정의가 필요한 경우가 종종 있다.

mailSender 빈처럼 양쪽 모두 필요하면서 빈의 내용이 달라져야 하는 경우에는 빈 설정정보 작성이 곤란해진다.
mailSender 빈은 @Autowired를 이용한 자동 와이어링 방식으로 주입되기 때문에 빈 아이디를 다르게 설정하는 것으로 간단히 해결 할수도 없다.

리스트 7-115 운영환경에서만 필요한 빈을 담은 빈 설정 클래스
```java
@Configuration
public class ProductionAppContext {
  @Bean
  public MailSender mailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost("localhost");
    return mailSender;
  }
}
```

### @Profile과 @ActivePrfiles
스프링 3.1은 환경에 따라서 빈 설정정보가 달라져야 하는 경우에는 파일을 여러 개로 쪼개고 조합하는 등의 번거로운 방법 대신 간단히
설정정보를 구성할 수 있는 방법을 제공한다.
실행환경에 따라 빈 구성이 달라지는 내용을 프로파일로 정의해서 만들어두고, 실행시점에 어떤 프로파일의 빈 설정을 사용할지 지정한는 것이다.

프로파일은 간단한 이름과 빈설정으로 구성된다.
특정실행환경에만 적용돼야 하는 빈설정은 이미 독립된 클래스로 깔끔히 구분해뒀다.
다만, 명시적으로 파일을 매번 조합해야하는 번거로움이 있다.

프로파일을 적용하면 하나의 설정 클래스만 가지고 환경에 따라 다른 빈 설정 조합을 만들어낼 수있다.

프로파일은 설정 클래스 단위로 지정한다. 리스트 7-116과 같이 @Profile 애노테이션을 클래스 레벨에 부여하고 프로파일 이름을 넣어주면된다.

리스트 7-116 @Profile을 지정한 TestAppContext
```java
@Configuration
@Profile("test")
public class TestAppContext {
```

리스트 7-117 @Import에 모든 설정 클래스 추가
```java
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackage="springbook.user")
@Import({SqlServiceContext.class, TestAppContext.class, ProductionAppContext.class})
public class AppContext {
```

@Profile이 붙은 설정클래스는 @Import로 가져오든 @ContextConfiguration에 직접 명시하든 상관없이 현재 컨테이너의
활성(active) 프로파일 목록에 자신의 프로파일 이름이 들어 있지 않으면 무시된다.

**활성 프로파일**이란 스프링 컨테이너를 실행할때 추가로 지정해주는 속성

프로파일을 지정하려면 @ActiveProfile 애노테이션을 사용하면 된다.

리스트 7-118 활성프로파일을 지정한 UserServiceTest
```java
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes=AppContext.class)
public class UserSerivceTest {
```
프로파일이 일종의 필터처럼 적용된다고 이해해도 좋다.

### 컨테이너의 빈 등록 정보 확인
스프링 컨테이너는 모두 BeanFactory라는 인터페이스를 구현하고 있다.

BeanFactory의 구현 클래스 중에 DefaultListableBeanFactory가 있는데 거의 대부분의 스프링 컨테이너는
이 클래스를 이용해 빈을 등록하고 관리한다.

리스트 7-119 등록된 빈 내역을 조회하는 테스트 메소드
```java
@Autowired DefaultListablesBeanFactory bf;

@Test
public void beans(){
  for(String n : bf.getBeanDefinitionNames()) {
    System.out.println(n + " \t " + bf.getBean(n).getClass().getName()); 
  }
}
```

### 중첩 클래스를 이용한 프로파일 적용

리스트 7-120 TestAppContext와 ProductionAppContext를 중첩 클래스로 만든 AppContext
```java
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackage="springbook.user")
@Import({SqlServiceContext.class, AppContext.TestAppContext.class, AppContext.ProductionAppContext.class})
public class AppContext {

...

@Configuration
@Profile("production")
public static class ProductionAppContext {
...
}

@Configuration
@Profile("test")
public static class TestAppContext {
...
}

```

다음과 같이 @Import에 지정했던 두개의 프로파일 설정 클래스를 아예 제거해도 된다.
```java
@Import(SqlServiceContext.class)
```
스태틱 중첩 클래스로 넣은 @Configuration 클래스는 스프링이 자동으로 포함해주기 때문이다.

## 7.6.5 프로퍼티 소스
DB 드라이버 클래스나, 접속 URL, 로그인 계정 정보 등은 개발환경이나 테스트환경, 운영환경에 따라 달라진다.

외부 서비스 연결에 필요한 정보는 자바 클래스에서 제거하고 손쉽게 편집할수 있고 빌드 작업이 따로 필요 없는 XML이나 프로퍼티 파일 같은
텍스트 파일에 저장해두는 편이 낫다.

### @PropertySource
프로퍼티에 들어갈 DB 연결정보는 텍스트로 된 이름과 값의 쌍으로 구성되면 된다.
복잡한 XML을 정의해서 사용할 것 없이 간단히 자바의 프로퍼티 파일 포멧을 이용하면 충분하다.

프로퍼티 파일의 확장자는 보통 properties이고, 내부에 키=값 형태로 프로퍼티를 정의한다.

다른 종류의 서비스 정보를 추가하면 비슷한 이름의 프로퍼티가 있어서 혼동될 위험이 있으니 앞에 'db.'을 붙여서 구분하자

리스트7-122 database.properties 파일
```java
db.driverClass=com.mysql.jdbc.Driver
db.url=jdbc:mysql://localhost/springbook?characterEncoding=UTF-8
db.username=spring
db.password=book
```

스프링 3.1은 빈 설정 작업에 필요한 프로퍼티 정보를 컨테이너가 관리하고 제공해준다.
스프링 컨테이너가 지정된 정보 소스로부터 프로퍼티 값을 수집하고, 이를 빈 설정 작업중에 사용할 수 있게 해준다.
컨테이너가 프로퍼티 값을 가져오는 대상을 프로퍼티 소소(property source)라고 한다.
환경 변수나 시스템 프로퍼티처럼 디폴트로 프로퍼티 정보를 끌어오는 프로퍼티 소소도 있고, 프로퍼티 파일이나 리소스의 위치를
지정해서 사용되는 프로퍼티 소스도 있다.

DB 연결정보는 database.properties라는 특정 파일에서 프로퍼티 값을 가져와야 하므로 프로퍼티 소스를 등록해줘야 한다.
프로퍼티 등록에는 @PropertySource 애노테이션을 이용한다.

리스트 7-123 @PropertySource 적용
```java
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackage="springbook.user")
@Import(SqlServiceContext.class)
@PropertySource("/database.properties")
public class AppContext {
```

@PropertySource로 등록한 리소스로부터 가져오는 프로퍼티 값은 컨테이너가 관리하는 Environment 타입의 환경 오브젝트에 저장된다.
환경오브젝트는 빈처럼 @Autowired를 통해 필드로 주입받을 수 있다. 
주입받은 Environment 오브젝트의 getProperty() 메소드를 이용하면 프로퍼티 값을 가져올 수 있다.

리스트 7-124 환경 오브젝트로부터 프로퍼티 값을 가져오도록 수정한 dataSource() 메소드
```java
@Autowired Environment env;

@Bean
public DataSource datasource() {
  SimpleDriverDataSource ds = new SimpleDriverDataSource();
  
  try {
    ds.setDriverClass((Class<? extends java.sql.Driver>)Class.forName(env.getProperty("db.driverClass")));
  } catch(ClassNotFoundException e) {
    throw new RuntimeException(e);
  }
  
  ds.setUrl(env.getProperty("db.url"));
  ds.setUsername(env.getProperty("db.username"));
  ds.setPassword(env.getProperty("db.password"));
  
  return ds;
}
```

Environment 오브젝트의 getProperty() 메소드는 프로퍼티 이름을 파라미터로 받아 스트링 타입의 프로퍼티 값을 돌려준다.

### PropertySourcePlaceholderConfigurer
Environment 오브젝트 대신 프로퍼티 값을 직접 DI 받는 방법도 가능하다.

@Value는 이름 그대로 값을 주입받을 때 사용한다. 
@Value의 사용 방법은 여러가지가 있는데, 여기서는 프로퍼티 소스로부터 값을 주입받을 수 있게 치환자(placeholder)를 이용해 보겠다.

@Value에는 프로퍼티 이름을 ${}안에 넣은 문자열을 디폴트 엘리먼트 값으로 지정해준다.

리스트 7-125 @Value를 이용한 프로퍼티 값 주입
```java
@PropertySource("/database.properties")
public class AppContext {
  @Value("${db.driverClass}") Class<? extends Driver> driverClass;
  @Value("${db.url}") String url;
  @Value("${db.username}") String username;
  @Value("${db.password}") String password;
```
XML에서는 다음과 같이 프로퍼티 값에 문자열로 된 치환자를 넣어두면, 컨테이너가 프로퍼티 파일 등에서 가져온 실제 값으로 
바꿔치게 만들 수 있다.
```xml
  <property name="driverClass" value="${db.driverClass}" /> 
```

리스트 7-127 @Value 필드를 사용하도록 수정한 dataSource() 메소드
```java
@Bean
public DataSource datasource() {
  SimpleDriverDataSource ds = new SimpleDriverDataSource();
 
  ds.setDriverClass(this.driverClass);
  ds.setUrl(this.url);
  ds.setUsername(this.username);
  ds.setPassword(thispassword);
  
  return ds;
}
```

@Value를 이용하면 driverClass처럼 문자열을 그대로 사용하지 않고 타입 변환이 필요한 프로퍼티를 스프링이 알아서 처리해준다는 장점이 있다.
지저분한 리플렉션 API나 try/catch가 없어서 깔끔하다.

Environment를 이용해 프로퍼티 값을 가져오는 방법과 @Value를 이용하는 방법중에서 작성하기 편하고 코드를 이해하기 쉽다고 생각되는
방법을 선택하면 되겠다.

## 7.6.6 빈 설정의 재사용과 @Enable*

### 빈 설정자
리스트 7-130 SqlMapConfig 인터페이스
```java
import org.springframework.core.io.Resource;

public interface SqlMapConfig {
  Resource getSqlMapResource();
}
```

리스트 7-131 SqlMapConfig 인터페이스를 구현한 클래스
```java
public class UserSqlMapConfig implements SqlMapConfig {
  @Override
  public Resource getSqlMapResource() {
    return new ClassPathResource("sqlmap.xml", UserDao.class);
  }
}
```
빈으로 정의시 런타임 시 주입되게 만들수 있다.

리스트 7-132 SqlMapConfig 타입 빈에 의존하게 만든 SqlServiceContext
```java
@Configuration
public class SqlServiceContext {
  @Autowired SqlMapConfig sqlMapConfig;
  
  @Bean
  public SqlService sqlService(){
    OxmSqlService sqlService = new OxmSqlService();
    sqlService.setUnmarshaller(unmarshaller());
    sqlService.setSqlRegisttry(sqlRegistry());
    
    //sqlMapConfig 이용
    sqlService.setSqlmap(this.sqlMapConfig.getSqlMapResource());
    
    return sqlService;
  }
}
```

리스트 7-133 sqlMapCofig 빈설정
```java
public class AppContext {
...
  @Bean
  public SqlMapConfig sqlMapConfig() {
    return new UserSqlMapConfig();
  }  
```
SQL 서비스 기능을 사용하려는 애플리케이션은 SqlMapConfig을 구현한 빈을 만들어 원하는 리소스로부터 SQL 매핑정보를 가져오게 만들수 있다.

설정정보를 담은 코드도 리팩토링하면 반복적으로 사용되는 부분은 수정없이 재사용될 수 있고, 적용환경에 따라 바뀌는 부분은 인터페이스로
분리하고 DI를 통해 외부에서 주입되게 만들 수 있다.

@Configuration은 다음과 같이 @Component를 메타 애노테이션으로 갖고 있는 자동 빈 등록용 애노테이션이기도 하다.

빈을 DI 받아서 사용하는 쪽은 빈이 특정 인터페이스를 구현하고 있는지에만 관심이 있다.

리스트 7-134 SqlMapConfig을 구현하게만든 AppContext
```java
public class AppContext implements SqlMapConfig {
  ...
  
  @Overide
  public Resource getSqlMapResource() {
    return new ClassPathResource("sqlmap.xml", UserDao.class);
  }
```

### @Enable* 애노테이션 
 @Component는 빈 자동등록 대상을 지정할 때 사용하는 애노테이션인데, 많은 경우 @Component를 직접 사용하기보다는
 @Repository나 @Service처럼 좀 더 의미 있는 이름의 애노테이션을 만들어 사용한다.
 @Component를 메타 애노테이션으로 넣어서 애노테이션을 정의해주면 @Component와 동일한 빈 등록기능이 적용되면서 자동등록되는
 빈의 종류나 계층이 무엇인지 나타낼 수도 있고, AOP를 이용해 특정 애노테이션이 달린빈만 선정해 부가 기능을 제공하게 만들수도 있다.
 
 @Import도 다른 이름의 애노테이션으로 대체 가능하다. @Import애노테이션과 빈 설정 클래스 값을 메타 애노테이션으로 넣어서 
 리스트 7-135와 같이 애노테이션을 만들어 주면 된다.
 
 리스트 7-135 @Import를 메타 애노테이션으로 넣은 애노테이션 정의
 ```java
 @Import(value=SqlServiceContext.class)
 public @interface EnableSqlService {
 }
 ```
 
 @Import를 메타 애노테이션으로 부여해서 사용하는 방법은 이 밖에도 여러 가지 장점이 있다.
 애노테이션을 정의하면서 엘리먼트를 넣어서 옵션을 지정하게 할 수도 있다.
 SqlMapConfig 인터페이스를 통해 SqlServiceContext에 SQL 매핑파일을 전달하게 했던 방식을 다음과 같이 간결하게 만들 수도 있다.
 ```java
 @EnableSqlService("classpath:/springbook/user/sqlmap.xml")
 ```
