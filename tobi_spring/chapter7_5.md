# 7.5 DI를 이용해 다양한 구현 방법 적용하기

## 7.5.1 ConcurrentHashMap을 이용한 수정 가능 SQL 레지스트리
JDK의 HashMap으로는 멀티스레드 환경에서 동시에 수정을 시도하거나 수정과 동시에 요청하는 경우 예상하지 못한 결과를 발생할 수있다.

ConcurrentHashMap은 데이터 조작시 전체 데이터에 대해 락을 걸지 않고 조회는 락을 아예 사용하지 않는다. 
그래서 어느 정도 안전하면서 성능이 보장되는 동기화된 HashMap으로 이용하기에 적당하다.

리스트 7-66 ConcurrentHashMap을 사용하는 SQL 레지스트리
```java
public class ConcurrentHashMapSqlRegistry implements UpdatableSqlRegistry {
  private Map<String, String> sqlMap = new ConcurrentHashMap<String, String>();
  ...
  
  public void updateSql(String key, String sql) throws SqlUpdateFailureException {
    if(sqlMap.get(key) == null) {
      throw new SqlUpdateFailureException(key + "에 해당하는 SQL을 찾을 수 없습니다.");
    }
    sqlMap.put(key, sql);
  }
  
  public void updateSql(Map<String,String> sqlmap) throws SqlUpdateFailureException {
    for(Map.Entry<String, String> entry : sqlmap.entrySet()){
      updateSql(entry.getKey(), entry.getValue());
    }
  } 
}
```

리스트 7-67 ConcurrentHashMapSqlRegistry를 적용한 설정
```xml
<bean id="sqlService" class="springbook.user.sqlservice.OxmSqlService">
  <property name="unmarshaller" ref="unmarshaller" />
  <property name="sqlRegistry" ref="sqlRegistry" />
</bean>

<bean id="sqlRegistry" class="springbook.user.sqlservice.updatable.ConcurrentHashMapSqlRegistry" />
```

## 7.5.2 내장형 데이터베이스를 이용한 SQL 레지스트리 만들기
 내장형 DB는 애플리케이션에 내장돼서 애플리케이션과 함께 시작되고 종료되는 DB를 말한다.
 데이터는 메모리에 저장되기 때문에 IO로 인해 발생하는 부하가 적어서 성능이 뛰어나다.
 동시에 Map과 같은 컬렉션이나 오브젝트를 이용해 메모리에 데이터를 저장해두는 방법에 비해 매후 효과적이고
 안정적인 방법으로 등록, 수정, 검색이 가능하다. 최적화된 락킹, 격리수준, 트랜잭션을 적용할 수도 있다.
 
### 스프링의 내장형 DB 지원 기능
스프링의 내장형 DB 빌더는 모든 준비가 끝나면 내장형 DB에 대한 DataSource 오브젝트를 돌려준다.

내장형 DB는 애플리케이션 안에서 직접 DB종료를 요청할 수도 있어야 한다.
이를 위해 스프링은 DataSource 인터페이스를 상속해서 shutdown()이라는 내장형 DB용 메소드를 추가한 EmbeddedDatabase 인터페이스를 제공한다.

### 내장형 DB 빌더 학습 테스트
내장형 DB는 애플리케이션을 통해 DB가 시작될 때마다 매번 테이블을 새롭게 생성한다.
따라서 지속적으로 사용 가능한 테이블 생성 SQL 스크립트를 준비해둬야 한다. 

SQL 문장을 저장할 필드를 가진 SQLMAP이라는 이름의 테이블을 리스트 7-68과 같이 작성해서 schema.sql이라는 이름으로 저장해둔다.

리스트 7-68 테이블 생성 SQL 
```sql
CREATE TABLE SQLMAP (
  KEY_ VARCHAR(100) PRIMARY KEY,    --key와 sql 모두 일반적으로 DB에서 키워드로 사용되기 때문에 그대로 필드이름으로 쓸 수 없다.
  SQL_ VARCHAR(100) NOT NULL        -- 번거롭게 앞뒤에 "를 붙여서 사용하는 것을 피하기 위해 뒤에 _를 추가했다.
)
```

리스트 7-69 초기 데이터를 위한 SQL을 담은 data.sql
```sql
INSERT INTO SQLMAP(KEY_, SQL_) values('KEY1', 'SQL1');
INSERT INTO SQLMAP(KEY_, SQL_) values('KEY2', 'SQL2');
```
내장형 DB빌더는 DB엔진을 생성하고 초기화 스크립트를 실행해서 테이블과 초기 데이터를 준비한뒤에
DB에 접근할 수 있는 Connection을 생성해주는 DataSource 오브젝트를 돌려주게 된다.
정확히는 DB 셧다운 기능을 가진 EmbeddedDatabase 타입 오브젝트다.

스프링이 제공하는 내장형 DB 빌더는 EmbeddedDatabaseBuilder다.
다음은 EmbeddedDatabaseBuilder를 사용하는 전형적인 방법을 보여준다.

```java
new EmbeddedDatabaseBuilder() //빌드 오브젝트 생성
.setType(내장형DB종류) // EmbeddedDatabaseType의 HSQL, DERBY, H2 중에 하나를 선택한다.
.addScript(초기화에 사용할 DB 스크립트 리소스)  // 테이블 생성과 데이터 초기화를 위해 사용할 SQL 문장을 담은 SQL 스크립트의 위치를
                                              // 지정한다. SQL스크립트는 하나 이상을 지정할 수 있다.
...
.build(); // 주어진 조건에 맞는 내장형 DB를 준비하고 초기화 스크립트를 모두 실행한 뒤에 이에 접근할 수 있는 EmbeddedDatabase를 돌려준다.
```

EmbeddedDatabaseBuilder 빌더가 최종적으로 만들어주는 오브젝트는 DataSource 인터페이스를 상속한 EmbeddedDatabase 타입이다.
따라서 DataSource의 일반적인 사용 방법을 그대로 적용할 수 있다. 예를 들어 DataSource를 DI 받는 JdbcTemplate을 사용할 수도 있다.

리스트 7-70 내장형 DB 학습 테스트
```java
import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.HSQL;
...

public class EmbeddedDbTest {
  EmbeddedDatabase db;
  SimpleJdbcTemplate template; // JdbcTemplate를 더 편리하게 사용할 수 있게 확장한 템플릿
  
  @Before
  public void setUp(){
    db = new EmbeddedDatabaseBuilder()
    .setType(HSQL)  //HSQL, DERBY, H2 세 가지 중 하나를 선택할 수 있다. 초기화 SQL이 호환만 된다면 DB종류는 언제든지 바꿀 수 있다.
    .addScript("classpath:/springbook/learningtest/spring/embeddeddb/schema.sql") //테이블 생성과
    .addScript("classpath:/springbook/learningtest/spring/embeddeddb/data.sql") //초기 데이터를 넣기 위한 스크립트 지정
    .build(); 
    
    template = new SimpleJdbcTemplate(db);  //EmbeddedDatabase는 DataSource의 서브 인터페이스이므로 DataSource를 필요로 하는
                                            // SimpleJdbcTemplate을 만들 때 사용할 수 있다.
  }
  
  @After
  public void tearDown() {
    db.shutdown();  // 매 테스트를 진행한 뒤에 DB를 종료한다. 내장형 메모리 DB는 따로 저장하지 않는 한 애플리케이션과 함께 매번
                    // 새롭게 DB가 만들어지는고 제거되는 생명주기를 갖는다.
  }
  
  @Test
  public void initData() { //초기화 스크립트를 통해 등록된 데이터를 검증하는 테스트다.
    assertThat(template.queryForInt("select count(*) from sqlmap"), is(2));
    
    List<Map<String, Object>> list = template.queryForList("select * from sqlmap order by key_");
    assertThat((String)list.get(0).get("key_"), is("KEY1"));
    assertThat((String)list.get(0).get("sql_"), is("SQL1"));
    assertThat((String)list.get(1).get("key_"), is("KEY2"));
    assertThat((String)list.get(1).get("sql_"), is("SQL2"));
  }
  
  @Test
  public void insert(){ //새로운 데이터를 추가하고 이를 확인해본다.
    template.update("insert into sqlmap(key_, sql_) values(?,?)", "KEY3", "SQL3");
    
    assertThat(template.queryForInt("select count(*) from sqlmap"), is(3));  
  }
}
```

### 내장형 DB를 이용한 SqlRegistry 만들기

리스트 7-71 HSQL 내장형 DB설정 예
```xml
<jdbc:embedded-database id="embeddedDatabase" type="HSQL" >
  <jdbc:script location="classpath:schema.sql" />
</jdbc:embedded-database>  
```
embeddedDatabase 아이디를 가진 빈이 등록되며, 빈의 타입은 EmbeddedDatabase다.
내장형 DB를 시작하고 초기화를 마쳤으니 EmbeddedDatabas 타입 빈 오브젝트를 이용해 내장형 DB를 자유롭게 사용할 수 있다. 

### UpdatableSqlRegistry 테스트 코드의 재사용
리스트 7-73 테스트 코드에서 ConcurrentHashMapSqlRegistry에 의존하는 부분
```java
public class ConcurrentHashMapSqlRegistryTest {
  UpdatableSqlRegistry sqlRegistry; //테스트에서 사용할 픽스쳐는 인터페이스로 정의해두길 잘했다.
  
  @Before
  public void setUp(){
    sqlRegistry = new ConcurrentHashMapSqlRegistry(); // 오직 이문장만 ConcurrentHasMapSqlRegistry라는 특정클래스에 의존하고 있다.
  ...
  }
}
```

리스트 7-74 UpdatatableSqlRegsitry에 대한 테스트 추상 클래스
```java
  public abstract class AbstractUpdatableSqlRegistryTest {  //UpdatableSqlRegistry 인터페이스를 구현한 모든클래스에 대한 테스트를 만들때
                                                            //사용할수 있는 추상 테스트 클래스다.
    UpdatableSqlRegistry sqlRegistry;
    
    @Before
    public void setUp() {
      sqlRegistry = createUpdatableSqlRegistry();
      ...
    }
    
    //테스트 픽스처를 생성하는 부분만 추상 메소드로 만들어두고 서브클래스에서 이를 구현하도록 만든다.
    abstract protected UpdatableSqlRegistry createUpdatableSqlRegistry(); 
    
    //서브 클래스에 테스트를 추가한다면 필요할 수 있다. 따라서 서브클래스에서 접근이 가능하도록 protected로 변경한다.
    protected void checkfind(String expected1, String expected2, Stirng expected3)  {
      ...
    }
    
    @Test
    public void find(){
      ...
    }
    
    //나머지 테스트 생략
  }
```

리스트 7-75 변경된 ConcurrentHashMapSqlRegistryTest
```java
public class ConcurrentHashMapSqlRegistryTest extends AbstractUpdatableSqlRegistryTest{
  protected UpdatableSqlRegistry createUpdatableSqlRegistry (){
    return new ConcurrentHashMapSqlRegistry();
  }
}
 
```

리스트 7-76 EmbeddedDbSqlRegistry에 대한 테스트 클래스
```java
public class EmbeddedDbSqlRegistryTest extends AbstractUpdatableSqlRegistryTest {
  EmbeddedDatabase db;
  
  protected UpdatableSqlRegistry createUpdatableSqlRegistry (){
    db = new EmbeddedDatabaseBuilder()
    .setType(HSQL)  
    .addScript("classpath:/springbook/learningtest/spring/embeddeddb/schema.sql")
    .build(); 
    
    EmbeddedDbSqlRegistry embeddedDbSqlRegistry = new EmbeddedDbSqlRegistry();
    embeddedDbSqlRegistry.setDataSource(db);
    
    return embeddedDbSqlRegistry;
  }
  
  @After
  public void tearDown(){
    db.shutdown();
  }
  
}
```

### XML 설정을 통한 내장형 DB의 생성과 적용

리스트 7-79 EmbeddedDbSqlRegistry 클래스를 이용한 빈등록
```xml
<bean id="sqlService" class="springbook.user.sqlservice.OxmSqlService">
  <property name="unmarshaller" ref="unmarshaller" />
  <property name="sqlRegistry" ref="sqlRegistry" />
</bean>

<bean id="sqlRegistry" class="springbook.user.sqlservice.updatable.EmbeddedDbSqlRegistry">
  <property name="dataSource" ref="embeddedDatabase" />
</bean>
```

<jdbc:embedded-database> 태그에 의해 만들어지는 EmbeddedDatabase 타입 빈은 스프링 컨테이너가 종료될때 자동으로 
shutdown() 메소드가 호출되도록 설정되어 있다. 따라서 내장형 DB를 종료시키기 위한 별도의 코드나 설정은 필요하지 않다. 
