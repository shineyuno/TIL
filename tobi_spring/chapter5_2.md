# 5.2 트랜잭션 서비스 추상화

## 5.2.1 모 아니면 도
트랜잭션이란 더 이상 나눌 수 없는 단위 작업을 말한다. 작업을 쪼개서 작은 단위로 만들수 
없다는 것은 트랜잭션의 핵심 속성인 원자성을 의미한다.

## 5.2.2 트랜잭션 경계설정
DB는 그 자체로 완벽한 트랜잭션을 지원한다. SQL을 이용해 다중 로우의 수정이나
삭제를 위한 요청을 했을 때 일부 로우만 삭제되고 나머지는 안 된다거나, 일부 필드는 수정했는데 나머지 필드는 수정이 안되고
실패로 끝나는 경우는 없다. 하나의 SQL 명령을 처리하는 경우는 DB가 트랜잭션을 보장해준다고 믿을 수 있다. 
하지만 여러 개의 SQL이 사용되는 작업을 하나의 트랜잭션으로 취급해야 하는 경우 도 있다.

여러개의 SQL을 하나의 트랜잭션으로 처리하는 경우에 모든 SQL 수행 작업이 다 성공적으로 마무리 됐다고 DB에 알려줘서
작업을 확정시켜야 한다. 이것을 트랜잭션 커밋 (transaction commit)이라고 한다.

### JDBC 트랜잭션의 경계설정
모든 트랜잭션은 시작하는 지점과 끝나는 지점이 있다. 시작하는 방법은 한가지이지만 끝나는 방법은 두가지다.
모든 작업을 무효화하는 롤백과 모든작업을 다 확정하는 커밋이다. 애플리케이션 내에서 트랜잭션이 시작되고 끝나는 위치를
트랜잭션의 경계라고 부른다.

트랜잭션을 사용한 jdbc코드
```java
Connection c = dataSource.getConnection();

c.setAutoCommit(false); // 트랜잭션 시작

try {       
  //하나의 트랜잭션으로 묶인 단위작업
  PreparedStatement st1 = c.prepareStatement("update users ...");
  st1.executeUpdate();
  
  PreparedStatement st2 = c.prepareStatement("delete users ...");
  st2.executeUpdate();
  
  c.commit();  //트랜잭션 커밋
}
catch(Exception e) {
  c.rollback(); // 트랜잭션 롤백
}

c.close();
```
JDBC의 트랜잭션은 하나의 Connection을 가져와 사용하다가 닫는 사이에 일어난다.
트랜잭션의 시작과 종료는 Connection 오브젝트를 통해 이뤄지기 때문이다. JDBC에서 트랜잭션을 시작하려면
자동커밋 옵션을 false로 만들어주면 된다. JDBC의 기본 설저은 DB작업을 수행한 직후에 자동으로 커밋이 되도록 되어 있다.

트랜잭션이 한번 시작되면 commit()또는 rollback() 메소드가 호출될 때까지의 작업이 하나의 트랜잭션으로 묶인다.

이렇게 setAutoCommit(false)로 트랜잭션의 시작을 선언하고 commit() 또는 rollback()으로 트랜잭션을 종료하는 작업을
트랜잭션의 경계설정(transaction demarcation)이라고 한다. 트랜잭션의 경계는 하나의 Connection이 만들어 지고 닫히는 범위 안에 존재
한다는 점도 기억해두자. 이렇게 하나의 DB 커넥션 안에서 만들어지는 트랜잭션을 로컬 트랜잭션이라고도 한다.


### UserService와 UserDao의 트랜잭션 문제
템플릿 메소드 호출 한번에 한개의 DB 커넥션이 만들어지고 닫히는 일까지 일어나는 것이다.
일반적으로 트랜잭션은 커넥션보다도 존재 범위가 짧다. 따라서 템플릿 메소드가 호출될 때마다 트랜잭션이 새로 만들어지고 
메소드를 빠져나오기 전에 종료된다. 결국 JdbcTemplate의 메소드를 사용하는 UserDao는 각 메소드마다 하나씩의 독립적인 트랜잭션으로
실행될수 밖에 없다.

어떤 일련의 작업이 하나의 트랜잭션으로 묶이려면 그 작업이 진행되는 동안 DB 커넥션도 하나만 사용돼야 한다.


### 비즈니스 로직 내의 트랜잭션 경계설정
트랜잭션 경계를 upgradeLevels()메소드 안에 두려면 DB 커넥션도 이 메소드 안에서 만들고, 종료시킬 필요가 있다.

Connection을 공유하도록 수정한 UserService 메소드
```java
class UserService {
  public void upgradeLevels() throws Exception {
    Connection c = ...;
    ...
    try{
        ...
        upgradeLevel(c, user);
        ...
    }
    ...
 }
 
 protected void upgradeLevel(Connection c, User user) {
    user.upgradeLevel();
    userDao.update(c,user);
 }
}

interface UserDao {
  public update(Connection c, User user);
  ...
}
```

### UserService 트랜잭션 경계설정의 문제점
첫째는 DB 커넥션을 비롯한 리소스의 깔금한 처리를 가능하게 했던 JdbcTemplate을 더이상 활용할 수 없다는 점
두번째 문제점은 DAO의 메소드와 비즈니스 로직을 담고 있는 UserService의 메소드에 Connection 파라미터가 추가돼야 한다는 점
세 번째 문제는 Connection 파라미터가 UserDao 인터페이스 메소드에 추가되면 UserDao는 더 이상 데이터 액세스 기술에 독립적일 수가 없다는 점
마지막으로 DAO 메소드에 Connection 파라미터를 받게 하면 테스트 코드에도 영향을 미친다.

## 5.2.3 트랜잭션 동기화
### Connection 파라미터 제거 
트랜잭션 동기화(transaction synchronization)란 UserService에서 트랜잭션을 시작하기 위해 만든 Connection 오브젝트를 특별한 저장소에
보관해두고, 이후에 호출되는 DAO의 메소드에서는 저장된 Connection을 가져다 사용하게 하는것
정확히는DAO가 사용하는 JdbcTemplate이 트랜잭션 동기화 방식을 이용하도록 하는것이다.
그리고 트랜잭션이 모두 종료되면, 그때는 동기화를 마치면된다.

트랜잭션 동기화 저장소는 작업 스레드마다 독립적으로 Connection 오브젝트를 저장하고 관리하기 때문에 다중 사용자를 처리하는 
서버의 멀티스레드 환경에서도 충돌이 날 염려는 없다.
이렇게 트랜잭션 동기화 기법을 사용하면 파라미터를 통해 일일이 Connection 오브젝트를 전달할 필요가 없어 진다.

트랜잭션 동기화 방식을 적용한 UserService
```java
private DataSource dataSource;

public void setDataSource(DataSource dataSource){ //Connection을 생성할때 사용할 DataSource를 DI 받도록 한다.
  this.dataSource = dataSource;
}

public  void upgradeLevels() throws Exception {
  TransactionSynchronizationManager.initSynchronization();  //트랜잭션 동기화 관리자를 이용해 동기화 작업을 초기화 한다.
  Connection c = DataSourceUtils.getConnection(dataSource); //DB커넥션 생성과 동기화를 함께 해주는 유틸리티 메소드 
                                                            // DB커넥션을 생성하고 트랜잭션을 시작한다. 이후의 DAO작업은 
  c.setAutoCommit(false);                                   // 모두 여기서 시작한 트랜잭션 안에서 진행한다.
  
  try {
    List<User> users = userDao.getAll();
    for(User user : users){
        if(canUpgradeLevel(user)){
          upgradeLevel(user);
        }
    }
    c.commit(); // 정상적으로 작업을 미치면 트랜잭션 커밋
  } catch (Exception e) {  
    c.rollback(); //예외가 발생하면 롤백한다.
    throw e; 
  } finally {
    DataSourceUtils.releaseConnection(c, dataSource);   //스프링 유틸리티 메소드를 이용해 DB 커넥션을 안전하게 닫는다.
    TransactionSynchronizationManager.unbindResource(this.dataSource);
    TransactionSynchronizationManager.clearSynchronization(); // 동기화 작업 종료 및 정리
  }
}
```
트랜잭션 동기화가 되어 있는 채로 JdbcTemplate을 사용하면 JdbcTemplate의 작업에서 동기화시킨 DB 커넥션을 사용하게 된다.


### JdbcTemplate과 트랜잭션 동기화
JdbcTemplate은 영리하게 동작하도록 설계되어 있다. 만약 미리 생성돼서 트랜잭션 동기화 저장소에 등록된 DB 커넥션이나 트랜잭션이 없는 경우에는
JdbcTemplate이 직접 DB 커넥션을 만들고 트랜잭션을 시작해서 JDBC 작업을 진행한다.
반면에 upgradeLevels() 메소드에서처럼 트랜잭션 동기화를 시작해놓았다면 그때부터 실행되는 JdbcTemplate의 메소드에서는 직접 DB 커넥션을
만드는 대신 트랜잭션 동기화 저장소에 들어 있는 DB커넥션을 가져와서 사용한다. 

## 5.2.4 트랜잭션 서비스 추상화

### 기술과 환경에 종속되는 트랜잭션 경계설정 코드
한개 이상의 DB로의 작업을 하나의 트랜잭션으로 만드는건 JDBC의 Connection을 이용한 트랜잭션 방식인 로컬 트랜잭션으로는 불가능하다.
왜냐하면 로컬 트랜잭션은 하나의 DB Connection에 종속되기 때문이다.
따라서 각 DB와 독립적으로 만들어지는 Connection을 통해서 아니라, 별도의 트랜잭션 관리자를 통해 
트랜잭션을 관리하는 글로벌 트랜잭션(grobal transaction) 방식을 사용해야 한다.

자바는 JDBC 외에 이런 글로벌 트랜잭션을 지원하는 트랜잭션 매니저를 지원하기 위한 API인 JTA(Java Transaction API)를 제공한다.
트랜잭션은 JDBC나 JMS API를 사용해서 직접 제어하지 않고 JTA를 통해 트랜잭션 매니저가 관리하도록 위임한다.
트랜잭션 매니저는 DB와 메시징 서버를 제어하고 관리하는 각각의 리소스 매니저와 XA 프로토콜을 통해 연결된다.
JTA를 이용해 트랜잭션 매니저를 활용하면 여러개의 DB나 메시징 서버에 대한 작업을 하나의 트랜잭션으로 통합하는 분산 트랜잭션 또는
글로벌 트랜잭션이 가능해진다. 


JTA를 이용한 트랜잭션 코드 구조
```java
InitialContext ctx = new InitialContext();
UserTransaction tx = (UserTransaction)ctx.lookup(USER_TX_JNDI_NAME);  //JNDI를 이용해 서버의 UserTransaction 오브젝트를 가져온다.

tx.begin();
Connection c = dataSource.getConnection();  //JNDI로 가져온 dataSource를 사용해야 한다.
try{
  //데이터 액세스 코드
  tx.commit();
} catch (Exception e) {
  tx.rollback();
  throw e;
} finally {
  c.close();
}
```
Connection의 메소드 대신에 UserTransaction의 메소드를 사용한다는 점을 제외하면 트랜잭션 처리 방법은 별로 달라진게 없다.

하이버네이트는 Connection을 직접 사용하지 않고 Session이라는 것을 사용하고, 독자적인 트랜잭션 관리 API를 사용한다.

트랜잭션의 경계설정을 담당하는 코드는 일정한 패턴을 갖는 유사한 구조다. 이렇게 여러기술의 사용 방법에 공통점이 있다면 추상화를 생각해볼수있다.
추상화란 하위 시스템의 공통점 뽑아내서 분리시키는 것을 말한다. 그렇게 하면 하위 시스템이
어떤것인지 알지 못해도, 또는 하위 시스템이 바뀌더라도 일관된 방법으로 접근 할수가 있다.

애플리케이션 코드에서는 트랜잭션 추상계층이 제공하는 API를 이용해 트랜잭션을 이용하게 만들어준다면 특정 기술에 종속되지
않는 트랜잭션 경계설정 코드를 만들 수 있을 것이다. 

### 스프링 트랜잭션 서비스 추상화
스프링은 트랜잭션 기술의 공통점을 담은 트랜잭션 추상화 기술을 제공하고 있다.
이를 이용하면 애플리케이션에서 직접 각 기술의 트랜잭션 API를 이용하지 않고도, 일관된 방식으로 트랜잭션을 제어하는 
트랜잭션 경계설정 작업이 가능해진다.

스프링의 트랜잭션 추상화 API를 적용한 upgradeLevels()
```java
public void upgradeLevels() {
  PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource); //JDBC 트랜잭션 추상오브젝트 생성
  
  TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition()); // 트랜잭션 시작
  try { //트랜잭션 안에서 진행되는 작업
    List<User> users = userDao.getAll();
    for(User user : users) {
      if(canUpgradeLevel(user)){
        upgradeLevel(user);
      }
    }
    transactionManager.commit(status); // 트랜잭션 커밋
  } catch(RuntimeException e) {
    transactionManager.rollback(status); //트랜잭션 커밋
    throw e;
  }
}
```
스프링이 제공하는 트랜잭션 경계설정을 위한 추상 인터페이스는 PlatformTransactionManager다. JDBC의 로컬 트랜잭션을 이용한다면
PlatformTransactionManager를 구현하는 DataSourceTransactionManager를 사용하면 된다.

### 트랜잭션 기술 설정의 분리
JTA를 이용하는 글로벌 트랜잭션으로 변경하려면 PlatformTransactionManager 구현클래스를 DataSourceTransactionManager 에서 
JTATransactionManager로 바꿔주기만 하면된다.

어떤 클래스든 스프링의 빈으로 등록할 때 먼저 검토해야 할 것은 싱글톤으로 만들어져 어러 스레드에서 동시에 사용해도 괜찮은가 하는점이다
상태를 갖고 있고, 멀티스레드 환경에서 안전하지 않은 클래스를 빈으로 무작정 등록하면 심각한 문제가 발생하기 때문이다.
