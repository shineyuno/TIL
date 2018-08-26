# 6.3 다이내믹 프록시와 팩토리 빈
## 6.3.1 프록시와 프록시 패턴, 데코레이터 패턴
자신이 클라이언트가 사용하려고 하는 실제 대상인 것처럼 위장해서 클라이언트의
요청을 받아주는 것을 대리자, 대리인과 같은 역할을 한다고 해서 *프록시(proxy)* 라고 부른다.
프록시를 통해 최종적으로 요청을 위임받아 처리하는 실제 오브젝트를 *타깃(taget)* 또는 *실체(real subeject)* 라고 부른다.

프록시 특징
- 타깃과 같은 인터페이스 구현
- 프록시가 타깃을 제어할 수 있는 위치에 있다는것

프록시 사용목적으로 구분
- 첫째 클라이언트가 타깃에 접근하는 방법을 제어하기 위해
- 둘째 타깃에 부가적인 기능을 부여해 주기 위해

### 데코레이터 패턴
데코레이터 패턴은 타깃에 부가적인 기능을 런타임 시 다이내미가하게 부여해주기 위해 프록시를 사용하는 패턴

자바 IO 패키지의 InputStream과 OutputStream 구현 클래스는 데코레이터 패턴이 사용된 대표적인 예
다음 코드는 InputStream이라는 인터페이스를 구현한 타깃인 FileInputStream에 버퍼 읽기 기능을 제공해주는  BufferedInputStream이라는
데코레이터를 적용한 예다.
```java
InputStream is = new BufferedInputStream(new FileInputStream("a.txt"));
```

### 프록시 패턴
프록시 패턴의 프록시는 타깃의 기능을 확장하거나 추가하지 않는다. 대신 클라이언트가 타깃에 접근하는 방식을 변경해준다.

프록시 사용시 장점이 되는예
- 타깃 오브젝트 생성지연시 
- 원격 오브젝트 이용시
- 타깃에 대한 접근권한 제어

## 6.3.2 다이내믹 프록시
일일이 프록시 클래스를 정의하지 않고도 몇 가지 API를 이용해 프록시처럼 동작하는 오브젝트를 다이내믹하게 생성하는것

### 프록시의 구성과 프록시 작성의 문제점
프록시의 역활은 위임과 부가작업이라는 두가지로 구분할수 있다.

프록시를 만들기 번거로운 이유
- 타깃의 인터페이스를 구현하고 위임하는 코드를 작성하기가 번거롭다. 부가기능이 필요 없는 메소드도 구현해서 타깃으로 위함하는 코드를 일일이 만들어줘야한다.
- 부가기능 코드가 중복될 가능성이 많다. 트랜잭션은 DB를 사용하는 대부분의 로직에 적용될 필요가 있다.

### 리플렉션
다이내믹 프록시는 리플렉션 기능을 이용해서 프록시를 만들어준다. 리플렉션은 자바의 코드 자체를 추상화해서 접근하도록 만든것이다.

리플렉션 API 중에서 메소드에 대한 정의를 담은 Method라는 인터페이스에 정의된  invoke()메소드를  이용해 메소드를 호출할수있다.

리플렉션 학습테스트
```java
public class ReflectionTest {
  @Test
  publick void invokeMethod() throws Exception{
    String name = "Spring";
    
    //length()
    assertThat(name.length(), is(6));
    
    Method lengthMethod = String.class.getMethod("length");
    assertThat((Intger))lengthMethod.invoke(name), is(6));
    
    // charAt()
    assertThat(name.chartAt(0), is('S'));
    
    Method charAtMethod = String.class.getMethod("chartAt", int.class);
    assertThat((Character)charAtMethod.invoke(name,0), is('S'));
  }
}
```

### 프록시 클래스
다이내믹 프록시를 이용한 프록시를 만들어 보자. 
프록시를 적용할 간단한 타깃 클래스와 인터페이스를 다음과 같다.

Hello 인터페이스
```java
interface Hello {
  String sayHello(String name);
  String sayHi(String name);
  String sayThankYou(String name);
}

```

타깃 클래스
```java
public class HelloTarget implements Hello{
  public String sayHello(String name){
    return "Hello" + name;
  }
  
  public String sayHi(String name){
    return "Hi" + name;
  }
  
  public String sayThankYou(String name){
    return "Thank You" + name;
  }  
}
```

Hello 인터페이스를 구현한 프록시
데코레이터 패턴을 적용해 타깃인 HelloTarget에 부가기능 추가
```java
public class HelloUppercase implements Hello{
  Hello hello; //위임할 타깃 오브젝트, 여기서는 타깃 클래스의 오브젝트인 것은 알지만 
               //다른 포록시를 추가할수도 있으므로 인터페이스로 접근한다.

  public HelloUppercase(Hello hello){
    this.hello = hello;
  }
  public String sayHello(String name){
    return hello.sayHello(name).toUpperCase(); // 위임과 부가기능 적용
  }
  
  public String sayHi(String name){
    return hello.sayHi(name).toUpperCase();
  }
  
  public String sayThankYou(String name){
    return hello.sayThankYou(name).toUpperCase();
  }  
}
```

HelloUppercase 프록시 테스트
```java
Hello proxiedHello = new HelloUppercase(new HelloTarget()); //프록시를 통해 타기 오브젝트에 접근하도록 구성한다.
assertThat(proxiedHello.sayHello("Toby"),is("HELLO TOBY"));
```

이 프록시는 프록시 적용의 일반적인 문제점 두 가지를 모두 갖고 있다. 
인터페이스의 모든 메소드를 구현해 위함도록 코드를 만듦
부가기능인 리턴값을 대문자로 바구는 기능이 모든 메소드에 중복돼서 나타남


### 다이내믹 프록시 적용
다이내믹 프록시는 프록시 팩토리에 의해 런타임 시 다이내믹하게 만들어지는 오브젝트다. </br>
다이내믹 프록시 오브젝트는 타깃의 인터페이스와 같은 타입으로 만들어진다. </br>
클라이언트는 다이내믹 프록시 오브젝트를 타깃 인터페이스를 통해 사용할 수 있다. </br>
이 덕분에 프록시를 만들때 인터페이스를 모두 구현해가면서 클래스를 정의하는 수고를 덜수 있다. </br>
프록시 팩토리에게 인터페이스 정보만 제공해주면 해당 인터페이스를 구현한 클래스의 오브젝트를 자동으로 만들어주기 때문이다. </br>

다이내믹 프록시가 인터페이스 구현 클래스의 오브젝트는 만들어주지만, 프록시로서 필요한 부가기능 제공 코드는 직접작성해야 한다. </br>
부가기능은 프록시 오브젝트와 독립적으로 InvocationHandler를 구현한 오브젝트에 담는다. 
InvocationHandler인터페이스는 다음과 같은 메소드 한 개만 가진 간단한 인터페이스다.
```java
public Object invoke(Object proxy, Method method, Object[] args)
```

InvocationHandler 구현 클래스
```java
public class UppercaseHandler implements InvocationHandler{
  Hello targer;
  
  public UppercaseHandler(Hello target){  //다이내믹 프록시로부터 전달받은 요청을 다시 타깃 오브젝트에 위임해야 하기 때문에 타깃 오브젝트를 주입받아둔다.
    this.target = target;
  }
  
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String ret = (String)method.invoke(target, args); // 타깃으로 위임, 인터페이스의 메소드 호출에 모두 적용된다.
    return ret.toUpperCase(); //부가기능 제공
  }
}
```

### 다이내믹 프록시의 확장

확장된 UppercaseHandler
```java
public class UppercaseHandler implements InvocationHandler{
  Object targer;
  
  private UppercaseHandler(Object target){  //어떤 종류의 인터페이스를 구현한 타깃에도 적용가능하도록  Object 타입으로 수정
    this.target = target;
  }
  
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    
    Object ret = method.invoke(target, args);
    if(ret instanceOf String && method.getName().startWith("say")){ // 리턴타입과 메소드 이름이 일치하는 경우에만 부가기능 적용
      return ret.toUpperCase();
    }else {
      return ret; //조건이 일치하지 않으면 타깃 오브젝트의 호출 결과를 그대로 리턴한다.
    }
  }
}
```


## 6.3.3 다이내믹 프록시를 이용한 트랜잭션 부가기능

다이내믹 프록시를 위한 트랜잭션 부가기능
```java
public class TransactionHandler implements InvocationHandler {
  private Object target; //부가기능을 제공할 타깃 오브젝트 어떤 타입의 오브젝트에도 적용 가능하다.
  private PlatformTransactionManager transactionManager;  //트랜잭션 기능을 제공하는데 필요한 트랜잭션 매니져
  private String pattern; //트랜잭션을 적용할 메소드 이름 패턴
  
  public void setTarget(Object target){
    this.target = target;
  }
  
  public void setTransactionManager(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }
  
  public void setPattern(String pattern){
    this.pattern = pattern;
  }
  
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if(method.getName().startWith(pattern)){    //트랜잭션 적용 대상 메소드를 선별해서 트랜잭션 경계설정 기능을 부여해 준다.
      return invokeInTransaction(method, args);
    } else {
      return method.invoke(target, args);
    }
  }
  
  private Object invokeTnTransaction(Method method, Object[] args) throws Throwable {
    TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
    
    try {
      Object ret = method.invoke(target, args); //트랜잭션을 시작하고 타기 오브젝트의 메소드를 호출한다. 예외가 발생하지 않았다면 커밋한다.
      this.transactionManager.commit(status);
      return ret;
    } catch (InvocationTargetException e) {
      this.transactionManager.rollback(status); //예외가 발생하면 트랜잭션을 롤백한다. 
      throw e.getTargetException();
    }
  }
}
```

리플렉션 메소드인  Method.invoke()를 이용해 타깃 오브젝트의 메소드를 호출할때는 타깃 오브젝트에서 발생하는 예외가
InvocationTargetException으로 한번 포장돼서 전달된다.
