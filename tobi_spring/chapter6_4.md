# 스프링의 프록시 팩토리 빈

## 6.4.1 ProxyFactoryBean
스프링은 일관된 방법으로 프록시를 만들 수 있게 도와주는 추상레이어를 제공한다.
생성된 프록시는 스프링의 빈으로 등록돼야 한다. 
스프링은 프록시 오브젝트를 생성해주는 기술을 추상화한 팩토리 빈을 제공해준다.
스프링의 ProxyFactoryBean은 프록시를 생성해서 빈 오브젝트로 등록하게 해주는 팩토리 빈이다.
ProxyFactoryBean은 순수하게 프록시를 생성하는 작업만을 담당하고 프록시를 통해 제공해줄 부가기능은 별도의 빈에 둘 수있다. 
ProxyFactoryBean이 생성하는 프록시에서 사용할 부가기능은 MethodInterceptor인터페이스를 구현해서 만든다.
MethodInterceptor의 invoke() 메소드는 ProxyFactoryBean으로부터 타깃 오브젝트에 대한 정보까지도 함께 제공받는다.
그 덕분에 MethodInterceptor는 타깃 오브젝트에 상관없이 독립적으로 만들어질 수 있다. MethodInterceptor 오브젝트는 타깃이
다른 여러 프록시에서 함께 사용할 수 있고, 싱글톤 빈으로 등록 가능하다.

스프링 ProxyFactoryBean을 이용한 다이내믹 프록시 테스트
```java
public class DynamicProxyTest {
  @Test
  public void simpleProxy() {
    Hello proxieHello = (Hello)Proxy.newProxyInstance(  // JDK 다이내믹 프록시 생성
      getClass().getClassLoader(),
      new Class[] { Hello.class},
      new UppercaseHandler (new HelloTarget()));
      ...
  }
  
  @Test
  public void proxyFactoryBean() {
    ProxyFactoryBean pfBean = new ProxyFactoryBean();
    pfBean.setTarget(new HelloTarget());  // 타깃설정
    pfBean.addAdvice(new UppercaseAdvice());  // 부가기능을 담은 어드바이스를 추가한다., 여러개를 추가할 수도 있다.
    
    Hello proxieHello = (Hello) pfBean.getObject(); // FactoryBean이므로 getObject()로 생성된 프록시를 가져온다.
    
    asserThat(proxieHello.sayHello("Toby"), is("HELLO TOBY"));
  }
  
  static class UppercaseAdvie implements MethodInterceptor {
    public Object invoke(MethodInvocation invocation) throws Throwable {
      String ret = (String)invocation.proceed();  // 리플렉션의 Method와 달리 메소드 실행 시 타기 오브젝트를 전달할 필요가 없다.
                                                  // MethodInvocation은 메소드 정보와 함께 타깃 오브젝트를 알고 있기 때문이다.
      return ret.toUpperCase(); // 부가기능 적용
    }
  }
  
  static interface Hello {  // 타깃과 프록시가 구현할 인터페이스
    String sayHello(String name);
  }
  
  static class HelloTarget implements Hello { // 타깃 클래스 
    public String sayHello(String name) { return "Hello " + name; }
  }
}

```

### 어드바이스: 타깃이 필요 없는 순수한 부가기능
MethodInvocation은 일종의 콜백 오브젝트로, proceed() 메소드를 실행하면 타깃 오브젝트의 메소드를 내부적으로 실행해주는 기능이 있다.
MethodInterceptor 오브젝트를 추가하는 메소드 이름은 addMethodInterceptor가 아니라 addAdvice다. MethodInterceptor는 Advice 인터페이스를 상송하고
있는 서브인터페이스이기 때문이다.
MethodInterceptor처럼 타깃 오브젝트에 적용하는 부가기능을 담은 오브젝트를 스프링에서는 어드바이스advide 라고 부른다.
ProxyFactoryBean을 적용한 코드에는 프록시가 구현해야하는 Hello라는 인터페이스를 제공해주는 부분이 없다.


### 포인트컷 : 부가기능 적용 대상 메소드 선정 방법
여러 프록시가 공유하는 MethodInterceptor에 특정 프록시에만 적용되는 패턴을 넣으면 문제가 된다.
프록시에 부가기능 적용 메소드를 선택하자.
프록시의 핵심가치는 타깃을 대신해서 클라이언트의 요청을 받아 처리하는 오브젝트로서의 존재 자체이므로, 메소드를 선별하는 기능은
프록시로부터 다시 분리하는 편이 낫다.

스프링은 부가기능을 제공하는 오브젝트를 *어드바이스* 라고 부르고 
메소드 선정 알고리즘을 담은 오브젝트를 *포인트컷* 이라고 부른다.
어드바이스는 JDK의 다이내믹 프록시의 InvocationHandler와 달리 직접 타깃을 호출하지 않는다.
자신이 공유돼야 하므로 타깃 정보라는 상태를 가질 수 없다. 따라서 타깃에 직접 의존하지 않도록 일종의 템플릿 구조로 설계되어 있다.
어드바이스가 부가기능을 부여하는 중에 타깃 메소드의 호출이 필요하면 프록시로부터 전달받은 MethodInvocation 타입 콜백 오브젝트의 proceed()
메소드를 호출해주기만 하면 된다.
재사용 가능한 기능을 만들어두고 바뀌는 부분(콜백 오브젝트와 메소드 호출정보)만 외부에서 주입해서 이를 작업 흐름(부가기능 부여)중에
사용하도록 하는 전형적인 템플릿/콜백 구조다.

포인트컷까지 적용한 ProxyFactoryBean
```java
@Test
public void pointcutAdvisor(){
  ProxyFactoryBean pfBean = new ProxyFactoryBean();
  pfBean.setTarget(new HelloTarget());  
  
  NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut(); //메소드 이름을 비교해서 대상을 선정하는 알고리즘을 제공하는 포인트컷 생성
  pointcut.setMappedName("sayH*");  //이름 비교조건 설정. sayH로 시작하는 모든 메소드를 선택하게 한다.
  
  pfBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdvice())); //포인트컷과 어드바이스를 Advisor로 묶어서 한번에 추가
  
  Hello proxiedHello = (Hello) pfBean.getObject();
  
  assertThat(proxiedHello.sayHello("Toby"), is("HELLO TOBY"));
  
}
```

포인트컷을 함께 등록할때는 어드바이스와 포인트컷을 Advisor 타입으로 묶어서 addAdvisor() 메소드를 호출해야한다.
포인트컷과 어드바이스를 따로 등록하면 어떤 어드바이스에 대해 어떤 포인트컷을 적용할지 애매해지기 때문

어드바이져 = 포인트컷(메소드 선정 알고리즘) + 어드바이스(부가기능)

## 6.4.2 ProxyFactoryBean 적용

### TransactionAdvice 

트랜잭션 어드바이스 
```java
public class TransactionAdvice implements MethodInterceptor { // 스프링의 어드바이스 인터페이스 구현
  PlatformTransactionManager transactionManager;
  
  public void setTransactionManager(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }
  
  public Object invoke(MethodInvocation invocation) throws Throwable { // 타깃을 호출하는 기능을 가진 콜백 오브젝트를 프록시로부터 받는다.
                                                                      // 덕분에 어드바이스는 특정 타깃에 의존하지 않고 재사용 가능하다.
      TransactionStatus status = this.transactionManager.getTransaction(new DefalutTransactionDefinition());
      
      try {
        Object ret = invocation.proceed();  //콜백을 호출해서 타깃의 메소드를 실행한다. 타깃 메소드 호출 전후로 필요한 부가기능을 넣을수 있다.
                                            //경우에 따라서 타깃이 아예 호출되지 않게 하거나 재시도를 위한 반복적인 호출도 가능하다.
        this.transactionManager.commit(status);
        return ret;
      }catch (RuntimeException e) { // JDK 다이내믹 프록시가 제공하는 Method와는 달리 스프링의 MethodInvocation을 통한 타깃 호출은 예외가
                                    // 포장되지 않고 타깃에서 보낸 그대로 전달된다.
        this.transactionManager.rollback(status);
        throw e;
      }
  }
}
```

리플렉션을 통한 타깃 메소드 호출 작업의 번거로움은 MethodInvocation타입의 콜백을 이용한 덕분에 대부분 제거할 수 있다. 
타깃 메소드가 던지는 예외도 InvocationTargetException으로 포장돼서 오는 것이 아니기 때문에 그대로 잡아서 처리하면 된다. 
