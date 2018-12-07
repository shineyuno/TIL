# 6.6 트랜잭션 속성

리스트 6-69 트랜잭션 경계설정 코드
```java
public Object invoke(MethodInvocation invocation) throws Throwable {
  TransactionStatus status = this.transactionManager.getTransaction(new DefaultTranscationDefinition()); //트랜잭션 시작?
  
  try {
    Object ret = invocation.proceed();
    this.transactionManager.commit(status); // 트랜잭션 종료
    return ret;
  }catch (RuntimeException e) {
    this.transactionManager.rollback(status); // 트랜잭션 종료
    throw e;
  }
}
```
 트랜잭션의 경계는 트랜잭션 매니저에게 트랜잭션을 가져오는 것과 commit(), rollback() 중의 하나를 호출하는 것으로 설정되고 있다.
 
## 6.6.1 트랜잭션 정의
트랜잭션이라고 모두 같은 방식으로 동작하는것은 아니다. 물론 트랜잭션의 기본 개념인 더 이상 조갤 수 없는 최소 단위의 작업이라는 개념은 항상 유효하다.
DefaultTransactionDefinition이 구현하고 있는 TransactionDefinition 인터페이스는 트랜잭션의 동작방식에 영향을 줄 수 있는 네가지 속성을 정의하고 있다.

### 트랜잭션 전파
트랜잭션 전파 transaction propagation란 트랜잭션의 경계에서 이미 진행 중인 트랜잭션이 있을 때 또는 없을 때 어떻게 동작할 것인가를
결정하는 방식을 말한다. 

#### PROPAGATION_REQUIRED
 가장 많이 사용되는 트랜잭션 전파 속성이다. 진행중인 트랜잭션이 없으면 새로 시작하고, 이미 시작된 트랜잭션이 있으면 이에 참여한다.
DefaultTransactionDefinition의 트랜잭션 전파 속성은 바로 이 PROPAGATION_REQUIRED다

#### PROPAGATION_REQUIRES_NEW
항상 새로운 트랜잭션을 시작한다. 독립적인 트랜잭션이 보장돼야 하는 코드에 적용할수 있다.

#### PROPAGATION_NOT_SUPPORTED
이 속성을 사용하면 트랜잭션 없이 동작하도록 만들 수도 있다. 진행중인 트랜잭션이 있어도 무시한다.
트랜잭션 경계설정은 보통 AOP를 이용해 한 번에 많은 메소드에 동시에 적용하는 방법을 사용한다. 
그중에서 특별한 메소드만 트랜잭션 적용에서 제외하려면 특정 메소드의 트랜잭션 전파 속성만 PROPAGATION_NOT_SUPPORTED로 설정해서 
트랜잭션없이 동작하게 만드는 편이 낫다.

트랜잭션 매니저를 통해 트랜잭션을 시작하려고 할 때 getTransaction() 메소드는 항상 트랜잭션을 새로 시작하는것이 아니다.
트랜잭션 전파 속성과 현재 진행 중인 트랜잭션이 존재하는지 여부에 따라 다르다.
진행중인 트랜잭션에 참여하는 경우는 트랜잭션 경계의 끝에서 트랜잭션을 커밋시키지도 않는다. 최초로 트랜잭션을 시작한 경계까지
정상적으로 진행돼야 비로소 커밋될 수 있다.

### 격리수준
모든 DB 트랜잭션은 격리수준 isolation level을 갖고 있어야 한다.
격리수준은 기본적으로 DB에 설정되어 있지만 JDBC 드라이버나 DataSource 등에서 재설정할 수 있고, 필요 하다면
트랜잭션 단위로 격리수준을 조정할수 있다. DefaultTransactionDefinition에 설정된 격리수준은 ISOLATION_DEFAULT다.
이는 DataSource에 설정되어 있는 디폴트 격리수준을 그대로 따른다는 뜻이다.

### 제한시간
트랜잭션을 수행하는 제한시간 timeout을 설정할수 있다.
DefaultTransactionDefinition의 기본 설정은 제한시간이 없는것이다.
제한시간은 트랜잭션을 직접 시작 할수 있는 PROPAGATION_REQUIRED나 PROPAGATION_REQUIRES_NEW와 함께 사용해야만 의미가 있다.

### 읽기 전용
읽기전용 read only으로 설정해두면 트랜잭션 내에서 데이터를 조작하는 시도를 막아줄수 있다. 또한 데이터 액세스 기술에 따라서 성능이 향상될 수도 있다.


## 6.6.2 트랜잭션 인터셉터와 트랜잭션 속성
메소드별로 다른 트랜잭션 정의를 적용하려면 어드바이스의 기능을 확장해야 한다.


### TransactionInterceptor
스프링에는 편리하게 트랜잭션 경계설정 어드바이스로 사용할 수 있도록 만들어진 TransactionInterceptor가 존재 

TransactionAdvice는 RuntimeException이 발생하는 경우에만 트랜잭션을 롤백시킨다.

TransactionAttribute는 rollbackOn()이라는 속성을 둬서 기본원칙과 다른 예외처리가 가능하게 해준다. 이를 활용하면 특정 체크 예외의 경우는
트랜잭션을 롤백시키고, 특정 런타임 예외에 대해서는 트랜잭션을 커밋시킬 수도 있다.

TransactionInterceptor는 이런 TransactionAttribute를 Properties라는 일종의 맵타입 오브젝트로 전달 받는다. 
컬렉션을 사용하는 이유는 메소드 패턴에 따라서 각기 다른 트랜잭션 속성을 부여할 수 있게 하기 위해서다.

### 메소드 이름 패턴을 이용한 트랜잭션 속성 지정
Properties 타입의 transactionAttributes 프로퍼티는 메소드 패턴과 트랜잭션 속성을 키와 값으로 갖는 컬렉션이다.
트랜잭션 속성은 다음과 같은 문자열로 정의할수 있다.

* PROPAGATION_NAME : 트랜잭션 전파 방식, 필수 항목이다. PROPAGATION_으로 시작한다.
* ISOLATION_NAME : 격리수준
* readOnly : 읽기전용 항목, 디폴트는 읽기 적용이 아니다.
* timeout_NNNN : 제한시간, 초단위 시간을 뒤에 붙인다.
* -Exception1 : 체크예외중에서 롤백 대상으로 추가할 것을 넗는다. 한 개이상을 등록할 수 있다.
* +Exception2 : 런타임 예외지만 롤백시키지 않을 예외들을 넣는다. 한 개이상을 등록할 수 있다.

이 중에서 트랜잭션 전파 항목만 필수이고 나머지는 다 생략 가능한다.
생략하면 모두 DefaultTransactionDefinition에 설정된 디폴트 속성이 부여된다.

리스트 6-71 트랜잭션 속성의 예
```xml
<bean id="transactionAdvice" class="org.springframework.transaction.interceptor.TransactionInterceptor" >
  <property name="transactionManager" ref="transactionManager" />
  <property name="transactionAtrributes">
    <props>
      <prop key="get*">PROPAGATION_REQUIRED,readOnly,timeout_30</prop>
      <prop key="upgrade*">PROPAGATION_REQUIRES_NEW,ISOLATION_SERIALIZABLE</prop>
      <prop key="*">PROPAGATION_REQUIRED</prop>
    <props>
  </property>
</bean>      
```
세가지 메소드 이름 패턴에 대한 트랜잭션 속성이 정의되어 있다.
트랜잭션 속성 중 readOnly나 timeout등은 트랜잭션이 처음 시잘될때가 아니라면 적용되진 않는다.
때로는 메소드 이름이 하나 이상의 패턴과 일치하는 경우가 있다. 이때는 메소드 이름 팬턴중에서 가정 정확히 일치하는것이 적용된다.


### tx 네임스페이스를 이용한 설정 방법
TransactionInterceptor 타입의 어드바이스 빈과 TransactionAttribute 타입의 속성 정보도 tx스키마의 전용 태그를 이용해 정의할 수 있다.

리스트 6-72 tx 스키마의 전용태그로 정의한 6-71
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns=http://www.springframework.org/schema/beans" 
...
    xmlns:aop="http://www.springframework.org/schema/aop"   
    xmlns:tx="http://www.springframework.org/schema/tx"  ## tx 네임스페이스 선언                                                   
    xsi:schemaLocation=" ...
                        http://www.springframework.org/schema/aop
                        http://www.springframework.org/schema/aop/spring-aop-3.0.xsd
                        http://www.springframework.org/schema/tx            ## tx 스키마 위치지정
                        http://www.springframework.org/schema/tx/spring-tx-2.5.xsd">
...                                                                                   
<tx:advice id="transactionAdvice  ## 이 태그에 의해 TransactionInterceptor 빈이 등록된다.
       transaction-manager="transactionManager">  ##트랜잭션 매니저의 빈 아이디가 transactionManager라면 생략가능
  <tx:attributes>
       <tx:method name="get*" propagation="REQUIRED" read-only="true" timeout="30" /> ## Enumeration 으로 스키마에 값이 정의되어 있으므로 오타가 있으면 xml유효성 검사만으로 확인가능한다.
       <tx:method name="ugrade*" propagation="REQUIRED_NEW" isolation="SERIALIZABLE" />
       <tx:method name="*" propagation="REQUIRED" /> ## 디폴트값이 스키마에 정의되어 있으므로 REQUIRED라면 아예 생략도 가능하다. 
  </tx:attributes>
  </tx:advie>  
...
</beans>
```

## 6.6.3 포인트컷과 트랜잭션 속성의 적용 전략
포인트컷 표현식과 트랜잭션 속성을 정의할때 따르면 좋은 몇가지 전략

### 트랜잭션 포인트컷 표현식은 타입 패턴이나 빈 이름을 이용한다.
일반적으로 트랜잭션을 적용할 타깃 클래스의 메소드는 모두 트랜잭션 적용 후보가 되는 것이 바람직하다.

* 트랜잭션용 포인트컷 표현식에는 메소드나 파라미터, 예외에 대한 패턴을 정희하지 않는것이 바람직하다.
* 가능하면 클래스보다는 인터페이스 타입을 기준으로 타입패턴을 적용하는 것이좋다. 
  * 인터페이스는 클래스에 비해 변경 빈도가 적고 일정한 패턴을 유지하기 쉽기 때문이다.
* bean() 표현식은 빈 이름을 기준으로 선정하기 때문에 클래스나 인터페이스 이름에 일정한 규칙을 만들기가 어려운 경우에 유용하다.

### 공통된 메소드 이름 규칙을 통해 최소한의 트랜잭션 어드바이스와 속성을 정의한다.
가장 간단한 트랜잭션 속성 부여 방법은 모든메소드에 대해 디폴트 속성을 지정하는 것이다.
일단 트랜잭션 속성의 종류와 상관없이 메시지 패턴이 결정되지 않았으면 리스트 6-73과 같이 가장 단순한 디폴트 속성으로부터 출발하면 된다.
개발이 진행됨에 따라 단계적으로 속성을 추가해주면 된다.

리스트 6-73 디폴트 트랜잭션 속성 부여
```xml
<tx:advice id="transactionAdvice">
  <tx:attributes>
    <tx:method name="*" /> ## 모든 타킷 메소드에 기본 트랜잭션 속성 지정
  </tx:attributes>
</tx:advice>  
```

리스트 6-74 읽기전용 속성추가
```xml
<tx:advice id="transactionAdvice">
  <tx:attributes>
    <tx:method name="get*" read-only="true" />  ## get으로 시작하는 메소드에 대해서는 읽기전용 속성을 부여한다.
                                                ## 이메소드가 트랜잭션의 실제 시작 위치가 아니라면 읽기전용 속성은 무시된다.
    <tx:method name="*" /> ## get으로 시작하지 않는 나며지 메소드에는 기본 트랜잭션속성을 지정한다. 순서가 뒤바뀌지 않도록 주의한다.
  </tx:attributes>
</tx:advice>  
```


일반화하기에는 적당하지 않은 특별한 트랜잭션 속성이 필요한 타깃 오브젝트에 대해서는 별도의 어드바이스와
포인트컷 표현식을 사용하는 편이 좋다.

리스트 6-75 두 가지 트랜잭션 속성 패턴을 사용한 예
```xml
<aop:config>
  <aop:advisor advice-ref="transactionAdvice" pointcut="bean(*Service)" />
  <aop:advisor advice-ref="batchTxAdvice" pointcut="execution(a.b.*BatchJob.*.(..))" />
</aop:config>

<tx:advice id="transactionAdvice"> ## 비즈니스 로직 서비스 클래스에 적용되는 기본 트랜잭션 속성
  <tx:attributes>...</tx:attributes>
</tx:advice>  

<tx:advice id="batchTxAdvice"> ## 배치작업 클래스에 적용되는 특별한 트랜잭션 속성
  <tx:attributes>...</tx:attributes>
</tx:advice>  
```

### 프록시 방식 AOP는 같은 타깃 오브젝트 내의 메소드를 호출할 때는 적용되지 않는다.
프록시 방식의 AOP에서는 프록시를 통한 부가 기능의 적용은 클라이언트로부터 호출이 일어날 때만 가능하다.
여기서 클라이언트는 인터페이스를 통해 타깃 오브젝트를 사용하는 다른 모든 오브젝트를 말한다.
반대로 타깃 오브젝트가 자기 자신의 메소드를 호출할 때는 프록시를 통한 부가기능의 적용이 일어나지 않는다.

만약 update() 메소드에 대핸 트랜잭션의 전파 속성을 REQUIRES_NEW라고 해놨더라도 같은 타깃 오브젝트에 있는 delete() 메소드를
통해 update()가 호출되면 트랜잭션 전파 속성이 적용되지 않으므로 REQUIRES_NEW는 무시되고 프록시의 delete() 메소드에서
시작한 트랜잭션에 단순하게 참여하게 될 뿐이다. 
또는 트랜잭션이 아예 적용되지 않는 타깃의 다른메소드에서 update()가 호출된다면 그때는 트랜잭션이 없는 채로 update() 메소드가 실행될것이다.

타깃 안에서의 호출에는 프록시가 적용되지 않은 문제를 해결할수 있는 방법은 두가지가 있다.
* 스프링 API를 이용해 프록시 오브젝트에 대한 레퍼런스를 가져온뒤에 같은 오브젝트의 메소드 호출도 프록시를 이용하도록 강제하는 방법
* AspectJ와 같은 타깃의 바이트코드를 직접조작하는 방식의 AOP 기술을 적용

## 6.6.4 트랜잭션 속성 적용
### 트랜잭션 경계설정의 일원화
특정 계층의 경계를 트랜잭션의 경계와 일치시키는 것이 바람직하다.
비즈니스 로직을 담고 있는 서비스 계층 오브젝트의 메소드가 트랜잭션 경계를 부여하기에 가장 적절한 대상이다.
서비스 계층을 트랜잭션이 시작되고 종료되는 경계로 정했다면 다른계층이나 모듈에서 DAO에 직접 접근하는 것은 차단해야 한다.
가능하면 다른 모듈의 DAO에 접근할 때는 서비스 계층을 거치도록 하는게 바람직 하다.
그래야야 UserService의 add()처럼 부가 로직을 적용할 수도 있고, 트랜잭션 속성도 제어할수 있기 때문이다. 


UserDao 인터페이스에 정의된 메소드 중에서 서비스계층에 새로 추가할 메소드
단순히 레코드 개수를 리턴하는 getCount()를 제외하면 나머지는 독자적인 트랜잭션을 가지고 사용될 가능성이 높다.

리스트 6-76 UserService에 추가된 메소드
```java
public interface UserService {
  void upgradeLevels();
  
  
  // DAO 메소드와 1:1대응되는 CRUD 메소드이지만 add()처럼 단순 위임 이상의 로직을 가질수 있다.
  void add(User user);
  //신규 추가 메소드 
  User get(String id);
  List<User> getAll();
  void deleteAll();
  void update(User user);
  
}
```

리스트 6-77 추가 메소드 구현
```java
public class UserServiceImpl implements UserService {
  UserDao userDao;
  
  ...
  
  //DAO로 위임하도록 만든다. 필요한 부가 로직을 넣어도 좋다.
  public User get(String id) { return userDao.get(id); }
  public List<User> getAll() { return userDao.getAll(); }
  public void deleteAll(){ userDao.deleteAll(); }
  public void update(User user){ userDao.update(user); }
  
  ...
}
```
