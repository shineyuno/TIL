# 7장 스프링 핵심 기술의 응용
## 7.1 SQL과 DAO의 분리

### 7.1.1 XML 설정을 이용한 분리

#### 개별 SQL 프로퍼티 방식
리스트 7-1 add()메소드를 위한 SQL 필드
```java
public class UserDaoJdbc implements UserDao {
  private String sqlAdd;
  
  public void setSqlAdd(String sqlAdd){
    this.sqlAdd = sqlAdd;
  }
  ...
}
```

리스트 7-2 주입받은 SQL사용
```java
public void add(User user){
  this.jdbcTemplate.update(
    this.sqlAdd, // "insert into users..." 를 제거하고 오비ㅜ에서 주입받은 SQL을 사용하게 한다.
    user.getId(), user.getName() ...
  )
}
```

리스트 7-3 설정파일에 넣은 SQL 문장
```java
<bean id="userDao" class="springbook.user.dao.UserDaoJdbc">
  <property name="dataSource" ref="dataSoure" />
  <property name="sqlAdd" value="insert into users(id, name, password, 
  email,level,login,recommend) values(?,?,?,?,?,?)" />
...
```
스프링에서는 스트링 값을 외부에서 DI해서 사용할 수 있기 때문에 손쉽게 SQL을 분리하는데 성공했다.
하지만 이방법은 매번 새로운 SQL이 필요할 때마다 프로퍼티를 추가하고 DI를 위한 변수와 수정자 메소드도 만들어줘야 한다.

#### SQL 맵 프로퍼티 방식
리스트 7-4 맵 타입의 SQL 정보 프로퍼티 
```java
public class UserDaoJdbc implements UserDao {
...
  private Map<String, String> sqlMap;
  
  public void setSqlMap(Map<String, String> sqlMap){
    this.sqlMap = sqlMap;
  }
  ...
}
```

리스트 7-5 sqlMap을 사용하도록 수정한 add()
```java
public void add(User user){
  this.jdbcTemplate.update(
    this.sqlMap.get("add"), // "프로퍼티로 제공 받은 맵으로 부터 키를 이용해서 필요한 SQL을 가져온다.
    user.getId(), user.getName() ...
  );
}
```

리스트 7-6 맵을 이용한 SQL 설정
```java
<bean id="userDao" class="springbook.user.dao.UserDaoJdbc">
  <property name="dataSource" ref="dataSoure" />
  <property name="sqlMap">
    <map>
      <entry key="add" value="insert into users(id, name, password, email,level,login,recommend) values(?,?,?,?,?,?)" />
      <entry key="get" value="select * from users where id = ?" />
      ...
   </map>
  </property>
</bean>
```
맵으로 만들어 두면 새로운 SQL이 필요할 때 설정에 <entry>만 추가해주면 되니 모든 SQL을 일일이 프로퍼티로 등록하는 방법에
비해 작업량도 적고 코드도 간단해서 좋다.


### 7.1.2 SQL 제공 서비스
#### SQL 서비스 인터페이스
리스트 7-10 sqlService를 사용하도록 수정한 메소드
```java
public void add(User user){
  this.jdbcTemplate.update(this.sqlService.getSql("userAdd"),
    user.getId(), user.getName(), ... );
}
...
```
#### 스프링 설정을 사용하는 단순 SQL 서비스
리스트 7-11 맵을 이용한 SqlService의 구현
```java
public class SimpleSqlService implements SqlService {
  private Map<String, String> sqlMap; //설정파일에 <map>으로 정의된 SQL정보를 가져오도록 프로퍼티로 등록해둔다.
  
  public void setSqlMap(Map<String, String> sqlMap){
    this.sqlMap = sqlMap;
  }
  
  public String getSql(String key) throws SqlRetrievalFailureException {
    String sql = sqlMap.get(key); // 내부 SqlMap에서 SQL을 가져온다.
    if (sql == null) //인터페이스에 정의한 규약대로 SQL을 가져오는데 실패하면 예외를 던진다.
      throw new SqlRetrievalFailureException( key + "에 대한 SQL을 찾을 수 없습니다"); 
    else
      return sql;
  }
}
```
