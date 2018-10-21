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
