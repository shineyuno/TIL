# 6.8 트랜잭션 지원 테스트

## 6.8.1 선언적 트랜잭션과 트랜잭션 전파 속성
트랜잭션을 정의할 때 지정할수 있는 트랜잭션 전파 속성은 매우 유용한 개념이다.
예를 들어 REQUIRED로 전파 속성을 지정해줄 경우, 앞에서 진행중인 트랜잭션이  있으면 참여하고
없으면 자동으로 새로운 트랜잭션을 시작해준다. REQUIRED 전파 속성을 가진 메소드를 결합해서 다양한 크기의
트랜잭션 작업을 만들 수 있다. 트랜잭션 적용 때문에 불필요하게 코드를 중복하는 것도 피할 수 있으며, 애플리케이션을
작은 기능 단위로 쪼개서 개발할 수가 있다.

AOP를 이용해 코드 외부에서 트랜잭션의 기능을 부여해주고 속성을 지정할 수 있게 하는 방법을 선언적 트랜잭션(declarative transaction)이라고 한다.
반대로 TransactionTemplate이나 개별 데이터 기술의 트랜잭션 API를 사용해 직접 코드 안에서 사용하는 방법은 
프로그램에 의한 트랜잭션(programmatic transaction)이라고 한다.

## 6.8.2 트랜잭션 동기화와 테스트

### 트랜잭션 매니저와 트랜잭션 동기화
트랜잭션 동기화 기술은 트랜잭션 전파를 위해서도 중요한 역활을 한다. 진행 중인 트랜잭션이 있는지 확인하고, 
트랜잭션 전파 속성에 따라서 이에 참여할 수 있도록 만들어주는 것도 트랜잭션 동기화 기술 덕분이다.

### 트랜잭션 매니저를 이용한 테스트용 트랜잭션 제어
트랜잭션의 전파는 트랜잭션 매니저를 통해 트랜잭션 동기화 방식이 적용되기 때문에 가능

트랜잭션을 시작하기 위해서는 먼저 트랜잭션 정의를 담은 오브젝트를 만들고 이를 트랜잭션 매니저에 제공하면서 새로운 트랜잭션을 요청하면 된다.

리스트 6-92 트랜잭션 매니저를 이용해 트랜잭션을 미리 시작하게 만드는 테스트
```java
@Test
public void transactionSync() {
  DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition(); // 트랜잭션 정의는 기본 값을 사용한다.
  
  //트랜잭션 매니저에게 트랜잭션을 요청한다. 기존에 시작된 트랜잭션이 없으니 새로운 트랜잭션을 시작시키고 트랜잭션 정보를 돌려준다.
  // 동시에 만들어진 트랜잭션을 다른 곳에서도 사용할 수 있도록 동기화 한다.
  TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
  
  //앞에서 만들어진 트랜잭션에 모두 참여한다.
  userService.deleteAll();
  
  userSerivce.add(users.get(0));
  userSerivce.add(users.get(1));
  
  transactionManager.commit(txStatus);  // 앞에서 시작한 트랜잭션을 커밋한다.
}
```

테스트 코드에서 트랜잭션 매니저를 이용해서 트랜잭션을 만들고 그 후에 실행되는 UserService의 메소드들이 같은
트랜잭션에 참여하게 만들 수 있다. 세 계의 메소드 모두 속성이 REQUIRED이므로 이미 시작된 트랜잭션이 있으면 참여하고 새로운 트랜잭션을 
만들지 않는다. 


### 트랜잭션 동기화 검증
트랜잭션 속성 중에서 읽기전용과 제한시간 등은 처음 트랜잭션이 시작할 때만 적용되고 그 이후에 참여하는 메소드의 속성은 무시된다.

리스트 6-93 트랜잭션 동기화 검증용 테스트
```java
public void transactionSync() {
  DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition(); 
  txDefinition.setReadOnly(true); //읽기전용 트랜잭션으로 정의한다.
  
  TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
  
  userService.deleteAll();  // 테스트 코드에서 시작한 트랜잭션에 참여한다면 읽기 전용속성을 위반했으니 예외가 발생해야 한다.
  ...
}
```

JdbcTemplate과 같이 스프링이 제공하는 데이터 액세스 추상화를 적용한 DAO에도 영향을 미친다.
JdbcTemplate은 트랜잭션이 시작된 것이 있으면 그 트랜잭션에 자동으로 참여하고, 없으면 트랜잭션 없이 자동커밋 모드로 JDBC작업을 수행한다.
개념은 조금 다르지만 JdbcTemplate의 메소드 단위로 마치 트랜잭션 전파 속성이 REQUIRED인 것처럼 동작한다고 볼 수 있다.

리스트 6-94 DAO를 사용하는 트랜잭션 동기화 테스트
```java
public void transactionSync() {
  DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition(); 
  txDefinition.setReadOnly(true); 
  
  TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
  
  userDao.deleteAll();  // JdbcTemplate을 통해서 이미 시작된 트랜잭션이 있다면 자동으로 참여한다. 따라서 예외가 발생한다.
  ...
}
```

## 6.8.3 테스트를 위한 트랜잭션 애노테이션
스프링의 컨텍스트 테스트 프레임워크는 애노테이션을 이용해 테스트를 편리하게 만들 수 있는 여러 가지 기능을 추가하게 해준다.
@ContextConfiguration을 클래스에 부여하면 테스트를 실행하기 전에 스프링 컨테이너를 초기화하고, 
@Autowired 애노테이션이 붙은 필드를 통해 테스트에 필요한 빈에 자유롭게 접근할 수 있다.

### @Transactional
테스트에도 @Transactional을 적용할 수 있다. 테스트 클래스 또는 메소드에 @Transactional 애노테이션을 부여해주면
마치 타깃 클래스나 인터페이스에 적용된것처럼 테스트 메소드에 트랜잭션 경계가 자동으로 설정된다.
이를 이용하면 테스트 내에서 진행하는 모든 트랜잭션 관력작업을 하나로 묶어줄 수 있다.
@Transactional에는 모든 종류의 트랜잭션 속성을 지정할 수 있기도 하다.

리스트 6-97 테스트에 적용된 @Transactional
```java
@Test
@Transactional
public void transactionSync() {
  userService.deleteAll();
  userSerivce.add(users.get(0));
  userSerivce.add(users.get(1));
}
```
테스트 메소드안에서 실행되는 deleteAll(), add() 등은 테스트 메소드의 트랜잭션에 참여해서 하나의 트랜잭션으로 실행된다.

트랜잭션 적용 여부를 확인해보고 싶다면 테스트의 트랜잭션을 리스트 6-98과 같이 읽기전용으로 바꾸고 테스트를 실행해
예외가 발생하는지 확인해보면 된다.

리스트 6-98 트랜잭션 적용 확인
```java
@Test
@Transactional(readOnly=true)
public void transactionSync() {
  userService.deleteAll();  //@Transactional에 의해 시작된 트랜잭션에 참여하므로 읽기전용 속성 위반으로 예외가 발생한다.
}
```

### @Rollback
테스트에 적용된 @Transactional은 기본적으로 트랜잭션을 강제 롤백시키도록 설정되어 있다.
@Transactional을 지정해주면 롤백 테스트가 되는 것이다.

@Rollback은 롤백 여부를 지정하는 값을 갖고 있다. @Rollback의 기본 값은 true다.
따라서 트랜잭션은 적용되지만 롤백을 원치 않는다면 @Rollback(false)라고 해줘야 한다.

리스트 6-99와 같이 테스트 메소드를 설정해주면 테스트 전체에 걸쳐 하나의 트랜잭션이 만들어지고 예외가 발생하지 않는 한 트랜잭션은 커밋된다.

리스트 6-99 테스트 트랜잭션을 커밋시키도록 설정한 테스트
```java

@Test
@Transactional
@Rollback(false)
public void transactionSync() {
  ...
}
```

### TransactinConfiguration
@Transactional은 테스트 클래스에 넣어서 모든 테스트 메소드에 일괄 적용할 수 있지만 @Rollback 애노테이션은 메소드 레벨에만 적용할수 있다.

@TransactionConfiguration을 사용하면 롤백에 대한 공통 속성을 지정 할 수 있다.

리스트 6-100 @TransactionConfiguration의 사용 예
```java
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= "/test-applicationContext.xml")
@Transactional
@TransactionConfiguration(defaultRollback=false)  //롤백 여부에 대한 기본 설정과 트랜잭션 매니저 빈을 지정하는데 사용할 수 있다.
                                                  //디폴트 트랜잭션 매니저 아이디는 관례를 따라서 transactionManager로 되어 있다.
public class UserServiceTest {
  @Test
  @Rollback // 메소드에서 디폴트 설정과 그 밖의 롤백 방법으로 재설정할 수 있다.
  public void add() throws SQLException { ... }
  ...
}
```

### NotTransactional과 Propagation.NEVER
```java
@Transactional(propagation=Propagation.NEVER)
```
@Transactional을 다음과 같이 NEVER전파 속성으로 메소드에 부여하면 클래스 레벨의 @Transactional설정을 무시하고
트랜잭셕을 시작하지 않은 채로 테스트를 진행한다. 
물론 테스트안에서 호출하는 메소드드에서 트랜잭션을 사용하는 데는 영향을 주지 않는다.

### 효과적인 DB테스트
일반적으로 의존, 협력 오브젝트를 사용하지 않고 고립된 상태에서 테스트를 진행하는 단위테스트와,
DB 같은 외부의 리소스나 여러 계층의 클래스가 참여하는 통합 테스트는 아예 클래스를 구분해서 따로 만드는게 좋다.

