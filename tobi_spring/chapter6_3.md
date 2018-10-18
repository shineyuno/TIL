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

다이내믹 프록시를 이용한 트랜잭션 테스트
```java
@Test
public void upgradeAllOrNothing() throws Exception {
  ...
  TransactionHandler txHandler = new TransactionHandler();
  txHandler.setTarget(testUserService);
  txHandler.setTransactionManager(transactionManager); //트랜잭션 핸들러가 필요한 정보와 오브젝트를 DI 해준다.
  txHandler.setPattern("upgradeLevels");
  
  UserService txUserService = (UserService)Proxy.newProxyInstance( // UserService 인터페이스 타입의 다이내믹 프록시 생성 
    getClass().getClassLoader(), new Class[] {UserService.class}, txHandler); 
    
  ...
}
```

## 6.3.4 다이내믹 프록시를 위한 팩토리 빈
DI의 대상이 되는 다이내믹 프록시 오브젝트는 일반적인 스프링의 빈으로는 등록할 방법이 없다. 
스프링의 빈은 기본적으로 클래스 이름과 프로퍼티로 정의된다. 
스프링은 지정된 클래스 이름을 가지고 리플렉션을 이용해서 해당 클래스의 오브젝트를 만든다.
클래스의 이름을 갖고 있다면 다음과 같은 방법으로 새로우 오브젝트를 생성할수 있다.
Class의 newInstance() 메소드는 해당 클래스의 파라미터가 없는 생성자를 호출하고, 그 결과 생성되는 오브젝트를 돌려주는 리플렉션 API다
```java
Date now = (Date) Class.forName("java.util.Date").newInstance
```
스프링은 내부적으로 리플렉션 API를 이용해서 빈 정의에 나오는 클래스 이름을 가지고 빈 오브젝트를 생성한다.


### 팩토리 빈
팩토리 빈이란 스프링을 대신해서 오브젝트의 생성로직을 담당하도록 만들어진 특별한 빈을 만든다.
팩토리 빈을 만드는 가장 간단한 방법은 스프링의 FactoryBean이라는 인터페이스를 구현하는 것이다.
FactoryBean 인터페이스는 아래에 나와 있는 대로 세가지 메소드로 구성되어 있다.


FactoryBean 인터페이스
```java
package org.springframework.beans.factory;

public interface FactoryBean<T> {
  T getObject() throws Exception; // 빈 오브젝트를 생성해서 돌려준다.
  Class<? extends T> getObjectType(); // 생성되는 오브젝트의 타입을 알려준다.
  boolean isSingleton(); // getObject()가 돌려주는 오브젝트가 항상 같은 싱글톤으로 오브젝트인지 알려준다.
}
```

FactoryBean 인터페이스를 구현한 클래스를 스프링의 빈으로 등록하면 팩토리 빈으로 동작한다.

생성자를 제공하지 않는 클래스
```java
public class Message {
  String text;
  
  private Message(String text){ //생성자가 private으로 선언되어 있어서 외부에서 생성자를 통해 오브젝트를 만들 수 없다.
   this.text = text;
  }
  
  public String getText(){
    return text;
  }
  
  public static Message newMessage(String text) { //생성자 대신 사용할수 있는 스태틱 팩토리 메소드를 제공한다.
    return new Message(text);
  }
}
```

위 Message 클래스는 다음과 같은 방식으로 사용하면 안된다
```xml
<bean id="m" class="....factorybean.Message">  ///private 생성자를 가진 클래스의 직접 사용금지
```

Message 클래스의 오브젝트를 생성해주는 팩토리 빈클래스를 만들어 보자.
FactoryBean 인터페이스를 구현해서 아래와 같이 만들면 된다.

Message의 팩토리 빈 클래스
```java
public class MessageFactoryBean implements FactoryBean<Message> {
  String text;
  
  public void setText(String text){ // 오브젝트를 생성할 때 필요한 정보를 팩토리 빈의 프로퍼티로 설정해서 대신 DI 받을수 있게 한다.
    this.text = text;               // 주입된 정보는 오브젝트 생성중에 사용된다.
  }
  
  public Message getObject() throws Exception { //실제 빈으로 사용될 오브젝트를 직접 생성한다. 코드를 이용하기 때문에 복잡한 방식의 
    return Message.newMessage(this.text);       // 오브젝트 생성과 초기화 작업도 가능하다.
  }
  
  public Class<? extends Message> getObjectType() {
    return Message.class;
  }
  
  public boolean isSingleton() {  // getObject()메소드가 돌려주는 오브젝트가 싱글톤인지를 알려준다. 이 팩토리 빈은 매번요청할 때마다
    return false;                 // 새로운 오브젝트를 만들므로 false로 설정한다. 이것은 팩토리 빈의 동작방식에 관한 설정이고 만들어진
  }                               // 빈 오브젝트는 싱글톤으로 스프링이 관리해줄 수 있다.
}
```

팩토리 빈은 전형적인 팩토리 메소드를 가진 오브젝트다. 
스프링은 FactoryBean인터페이스를 구현한 클래스가 빈의 클래스로 지정되면, 팩토리 빈 클래스의 오브젝트의 getObject()메소드를 이용해
오브젝트를 가져오고, 이를 빈 오브젝트로 사용한다. *빈의 클래스로 등록된 팩토리 빈은 빈 오브젝트를 생성하는 과정에서만 사용*될 뿐이다.


### 팩토리 빈의 설정 방법
팩토리 빈설정
```xml
<bean id="message" class=".....factorybean.MessageFactoryBean">
  <property name="text" value="Factory Bean" />
</bean>  
```
여타 빈 설정과 다른점은 message 빈 오브젝트의 타입이 class 애트리뷰트에 정의된 MessageFactoryBean이 아니라 Message 타입이라는 것이다.
Messsage 빈의 타입은 MessageFactoryBean의 getObjectType() 메소드가 돌려주는 타입으로 결정된다. 
또, getObject()메소드가 생성해주는 오브젝트가 message 빈의 오브젝트가 된다.

팩토리 빈이 만들어주는 빈 오브젝트가 아니라 팩토리 빈 자체를 가져오고 싶을 경우도 있다.
이럴 때를 위해 스프링은 '&'를 빈 이름 앞에 붙여주면 팩토리 빈 자체를 돌려준다. 
```java
@Test
  public void getFactoryBean()throws Exception {
    Object factory = context.getBean("&message"); //&이 붙고 안 붙고에 따라 getBean()메소드가 돌려주는 오브젝트가 달라진다.
    assertThat(factory, is(MessageFactoryBean.class));
  }
```

### 다이내믹 프록시를 만들어주는 팩토리 빈 
Proxy의 newProxyInstance() 메소드를 통해서만 생성이 가능한 다이내믹 프록시 오브젝트는 일반적인 방법으로는 스프링의 빈으로 등록할 수 없다.
대신 팩토리 빈을 사용하면 다이내믹 프록시 오브젝트를 스프링의 빈으로 만들어줄 수가 있다. 
팩토리 빈의 getObject() 메소드에 다이내믹 프록시 오브젝트를 만들어주는 코드를 넣으면 되기 때문이다.


### 트랜잭션 프록시 팩토리빈
트랜잭션 프록시 팩토리 빈
```java
public class TxProxyFactoryBean implements FactoryBean<Object> { // 생성할 오브젝트 타입을 지정할 수도 있지만 범용적으로 사용하기 위해 Object로 했다.

  Object target;
  PlatformTransactionManager transactionManager; // TransactionHandler를 생성할 때 필요
  String pattern;
  
  Class<?> serviceInterface;  // 다이내믹 프록시를 생성할 때 필요하다. UserServie 외의 인터페이스를 가진 타깃에도 적용할 수 있다.
  
  public void setTarget(Object target) {
    this.target = target;
  }
  
  public void setTransactionManager(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }
  
  public void setPattern(String pattern) {
    this.pattern = pattern;
  }
  
  public void setServiceInterface(Class<?> serviceInterface) {
    this.serviceInterface = serviceInterface;
  }
  
  // FactoryBean 인터페이스 구현 메소드
  public Object getObject() throws Exception {  // DI 받은 정보를 이용해서 TransactionHandler를 사용하는 다이내믹 프록시를 생성한다.
    TransactionHandler txHandler = new TransactionHandler();
    txHandler.setTarget(target);
    txHandler.setTransactionManager(transactionManager);
    txHandler.setPattern(patter);
    return Proxy.newProxyInstance(
      getClass().getClassLoader(), new Class[] { serviceInterface}, 
      txHandler);
  }
  
  public Class<?> getObjectType() { 
    return serviceInterface;   // 팩토리 빈이 생성하는 오브젝트의 타입은 DI 받은 인터페이스 타입에 따라 달라진다. 따라서 다양한 타입의  
  }                            // 프록시 오브젝트 생성에 재사용 할 수 있다.
  
  public boolean isSingleton() {
    return false; // 싱글톤 빈이 아니라는 뜻이 아니라 getObject()가 매번 같은 오브젝트를 리턴하지 않는다는 의미다.
  }
}
```

UserService에 대한 트랜잭션 프록시 팩토리 빈
```xml
<bean id="userService" class="springbook.user.serivce.TxProxyFactoryBean">
  <property name="target" ref="userSeirviceImpl" />
  <property name="transactionManger" ref="transactionManager" />
  <property name="pattern" value="upgradeLevels" />
  <property name="serviceInterface" value="springbook.user.service.UserService" />
</bean>  
```
target, transactionManger 프로퍼티는 다른빈을 가리키는 것이니 ref 애트리뷰트로 설정
Class 타입은 value를 이용해 클래스 또는 인터페이스의 이름을 넣어주면된다.
