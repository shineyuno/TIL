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
