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
