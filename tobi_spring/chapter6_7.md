# 6.7 애노테이션 트랜잭션 속성과 포인트 컷

## 6.7.1 트랜잭션 애노테이션

### @Transactional

리스트 6-84 @Transaction 애노테이션
```java
package org.springframework.transaction.annotation;
...

@Target({ElementType.METHOD, ElementType.TYPE}) //애노테이션을 사용할 대상을 지정한다. 여기에 사용된 `메소드와 타입(클래스, 인터페이스)`처럼 
                                                // 한 개 이상의 대상을 지정할 수 있다.
@Retention(RetentionPolicy.RUNTIME) // 애노테이션 정보가 언제까지 유지되는지를 지정한다. 이렇게 설정하면 런타임 때도 애노테이션 정보를
                                    // 리플렉션을 통해 얻을 수 있다.
@Inherited  // 상속을 통해서도 애노테이션 정보를 얻을 수 있게 한다.
@Documented
public @inteface Transactional {  //트랜잭션 속성의 모든 항목을 엘리먼트로 지정할 수 있다. 디폴트 값이 설정되어 있으므로 모두 생략 가능하다.
  String value() default "";
  Propagation propagation() default Propagation.REQUIRED;
  Isolation isolation() default Isolation.DEFAULT;
  int timeout() default TransactionDefinition.TIMEOUT_DEFAULT;
  boolean readOnly() default false;
  Class<? extends Throwalbe>[] rollbackFor() default {};
  String[] rollbackForClassName() default {};
  Class<? extends Throwable>[] noRollbackFor() default {};
  String[] noRollbackForClassName() default {};
}
```
@Transactional이 부여된 모든 오브젝트를 자동으로 타깃 오브젝트로 인식한다.
@Transactional은 기본적으로 트랜잭션 속성을 정의하는 것이지만, 동시에 포인트컷의 자동등록에도 사용된다.

### 트랜잭션 속성을 이용하는 포인트컷
@Transactional은 메소드마다 다르게 설정할 수도 있으므로 매우 유연한 트랜잭션 속성 설정이 가능해진다.

### 대체 정책
스프링은 @Transactional을 적용할 때 4단계의 대체(fallback) 정책을 이용하게 해준다. 
메소드의 속성을 확인할 때 타깃메소드, 타깃클래스, 선언 메소드, 선언 타입(클래스, 인터페이스)의 순서에 따라서 
@Transactional이 적용됐는지 차례로 확인하고, 가장 먼저 발견되는 속성정보를 사용하게 하는 방법이다.

리스트 6-85 @Transactional 대체 정책의 예
```java
//[1]
public interface Service {
  //[2]
  void method1();
  
  //[3]
  void method2();
}

//[4]
public class ServiceImpl implements Service {
  //[5]
  public void method1(){}
  
  //[6]
  public void method2(){}
}  
```

method1,2 @Transactional 적용순서
```
[5],[6](1순위) > [4](2순위) >  [2],[3] (3순위) > [1] (4순위) 
```

인터페이스를 사용하는 프록시 방식의 AOP가 아닌 방식으로 트랜잭션을 적용하면 인터페이스에 정의한 @Transactional은 무시되기
때문에 안전하게 타깃 클래스에 @Transactional을 두는 방법을 권장한다.

### 트랜잭션 애노테이션을 사용을 위한 설정
이 태그 하나로 트랜잭션 애노테이션을 이용하는 데 필요한 어드바이저, 어드바이스, 포인트컷, 애노테이션을 이용하는 트랜잭션 속성정보가 등록된다.

```xml
<tx:annotation-driven />
```

## 6.7.2 트랜잭션 애노테이션 적용
인터페이스 방식의 프록시를 사용하는 경우에는 인터페이스 @Transactional을 적용해도 상관없다.
UserService에는 get으로 시작하지 않는 메소드가 더 많으므로 6-87처럼 인터페이스 레벨에 디폴트 속성을 부여해주고,
읽기 적용 속성을 지정할 get으로 시작하는 메소드에는 읽기 전용 트랜잭션 속성을 반복해서 지정해야 한다.

리스트 6-87 @Transactional 애노테이션을 이용한 속성 부여
```java
@Transactional // <tx:method name="*" />과 같은 설정 효과를 가져온다. 메소드 레벨 애노테이션이 없으므로 대체정책에 따라
               // 타입 레벨에 부여된 디폴트 속성이 적용된다.
public interface UserService {
  void add(User user);
  void deleteAll();
  void update(User user);
  void upgradeLevels();
  
  @Transactional(readOnly=true) // <tx:method name="get*" read-only="true" />를 애노테이션 방식으로 변경한것이다.
                                //메소드 단위로 부여된 트랜잭션의 속성이 타입 레벨에 부여된 것에 우선해서 적용된다.
                                //같은 속성을 가졌어도 메소드 레벨에 부여될 때는 메소드마다 반복될수 밖에 없다. 
  User get(String id);
  
  @Transactional(readOnly=true)
  List<User> getAll();
}
```
