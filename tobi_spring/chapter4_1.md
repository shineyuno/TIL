# 4장 예외
4장에서는 JdbcTemplate을 대표로 하는 스프링의 데이터 액세스 기능에 담겨 있는 예외처리와 관련된 접근 방법에 대해 알아본다.

## 4.1 사라진 SQLException

### 4.1.1 초난감 예외처리
#### 예외 블래홀
초난감 예외처리 코드
```java
try {
...
}
catch(SQLException e) { // 예외를 잡고는 아무것도 하지 않는다. 예외 발생을 무시해버리고 정상적인 상황인 것처럼 다음 라인으로 
                        // 넘어가겠다는 분명한 의도가 있는게 아니라면 연습중에도 절대 만들어서는 안되는 코드다.
}
```
예외가 발생하면 그것을 catch 블록을 써서 잡아내는 것까지는 좋은데 그리고 아무것도
하지 않고 별문제 없는 것처럼 넘어가는 버리는 건 정말 위험한 일이다.
문제는 그 시스템 오류나 이상한 결과의 원인이 무엇인지 찾아내기가 매우 힘들다는 점이다. 

예외를 처리할 때 반드시 지켜야 할 핵심 원칙은 한 가지다. 모든 예외는 적절하게 복구 되든지 아니면
작업을 중단시키고 운영자 또는 개발자에게 분명하게 통보돼야 한다.

#### 무의미하고 무책임한  throws
메소드 선언에 throws Exception을 기계적으로 붙이기

예외 블랙홀 보다는 조금 낫긴 하지만 정말 무엇인가 실행 중에 예외적인 상황이 발생할  수 있다는 것인지,
아니면 그냥 습관적으로 복사해서 붙여 놓은 것인지 알수가 없다. 결국 이런 메소드를 사용하는 메소드에서도 역시
throws Exception을 따라서 붙이는 수밖에 없다. 결과적으로 적절한 처리를 통해 복구될 수 있는 예외상황도 제대로 다룰 수 있는 기회를 박탈당한다.

### 4.1.2 예외의 종류와 특징
예외 처리에 관해서는 체크 예외(checked exception)라고 불리는  명시적인 처리가 필요한 예외를 사용하고 다루는 방법이다.
자바에서  throw를 통해 발생시킬수 있는 예외는 크게 세가지가 있다.

#### Error
java.lang.Error클래스의 서브클래스들이다. 에러는 시스템에 뭔가 비정상적인 상황이 발생했을 경우에 사용된다.
그래서 주로 자바 VM에서 발생시키는 것이고 애플리케이션 코드에서 잡으려고 하면 안된다.
OutOfMemoryError나 ThreadDeath 같은 에러는 catch 블록으로 잡아봤자 아무런 대응 방법이 없기 때문이다.

#### Exception과 체크에러
java.lang.Exception 클래스와 그 서브클래스로 정의되는 예외들은 에러와 달리 개발자들이 만든
애플리케이션 코드의 작업중에 예외상황이 발생했을 경우에 사용된다.
Exception 클래스는 다시 체크 예외와 언체크 예외로 구분된다.
전자는 Exception 클래스의 서브클래스이면서  RuntimeException 클래스를 상속하지 않은 것들이고,
후자는 RuntimeException을 상속한 클래스들을 말한다.
체크 예외가 발생할 수 있는 메소드를 사용할 경우 반드시 예외를 처리하는 코드를 함께 작성해야 한다.
사용할 메소드가 체크 예외를 던진다면 이를 catch 문으로 잡든지, 아니면 다시 throws를 정의해서 메소드 밖으로 던져야 한다.
그렇지 않으면 컴파일 에러가 발생한다.
IOException이나 SQLException을 비롯해서 예외적인 상황에서 던져질 가능성이 있는 것들 대부분이 체크 예외로 만들어져 있다.

#### RuntimeException과 언체크/런타임 예외
java.lang.RuntimeException 클래스를 상속한 예외들은 명시적인 예외처리를 강제 하지 않기 때문에 언체크 예외라고 불린다.

런타임 예외는 주로 프로그램의 오류가 있을 때 발생하도록 의도된 것들이다. 
대표적으로 오브젝트를 할당하지 않은 레퍼런스 변수를 사용하려고 시도했을 때 발생하는 NullPointerException이나,
허용되지 않는 값을 사용해서 메소드를 호출할 때 발생하는 IllegalArgumentException등이 있다.
이런 예외는 코드에서 미리 조건을 체크하도록 주의 깊게 만든다면 피할 수 있다.
피할 수 있지만 개발자가 부주의해서 발생할 수 있는 경우에 발생하도록 만든 것이 런타임 예외다.
따라서 런타임 예외는 예상하지 못했던 예외상황에서 발생하는 게 아니기 때문에 굳이 catch나 throws를 사용하지 않아도 되도록 만든것이다.

### 4.1.3 예외처리 방법

#### 예외복구
재시도를 통해 예외를 복구하느코드
```java
int maxretry = MAX_RETRY
while(maxretry-- > 0 ){
  try {
    ...     //예외가 발생할 가능성이 있는 시도
    return; // 작업성공
  }
  catch(SomeException e){
    // 로그 출력. 정해진 시간만큼 대기
  }
  finally {
    // 리소스 반납, 정리 작업
  } 
}
throw new RetryFailedException(); // 최대 재시도 회수를 넘기면 직접 예외 발생

```

#### 예외처리 회피
예외처리를 자신이 담당하지 않고 자신을 호출한 쪽으로 던져버리는 것
throws 문으로 선언해서 예외가 발생하면 알아서 던져지게 하거나 catch문으로 일단 예외를 잡은 후에
로그를 남기고 다시 예외를 던지는 rethrow 것이다.

#### 예외 전환
예외 회피와 비슷하게 예외를 복구해서 정상적인 상태로는 만들 수 없기 때문에 예외를 메소드 밖으로 던지는 것이다.
하지만 예외 회피와 달리, 발생한 예외를 그대로 넘기는게 아니라 적절한 예외로 전환해서 던진다는 특징이 있다.

예외 전환 기능을 가진 DAO 메소드
```java
public void add(User user) throws DuplicateUserIdException, SQLException {
  try {
    // JDBC를 이용해 user 정보를 DB에 추가하는 코드 또는
    // 그런 기능을 가진 다른 SQLException을 던지는 메소드를 호출하는 코드
  }
  catch (SQLException e) {
    // ErrorCode 가 MySQL의 "Duplicate Entry(1062)"이면 예외 전환
    if (e.getErrorCode() == MysqlErrorNumbers.ER_DUP_ENTRY)
      throw DuplicateUserIdException(e);
    else
      throw e; // 그 외의 경우는 SQLException 그대로
  }
}
```

### 4.1.4 예외처리 전략
#### 런타임 예외의 보편화
 자바의 환경이 서버로 이동하면서 체크 예외의 활용도와 가치는 점점 떨어지고 있다.
 자칫하면 throws Exception으로 점철된 아무런 의미도 없는 메소드들을 낳을 뿐이다. 
 그래서 대응이 불가능한 체크 예외라면 빨리 **런타임 예외** 로 전환해서 던지는게 낫다.
 
#### add() 메소드의 예외처리 
아이디 중복 시 사용하는 예외
```java
public class DuplicateUserIdException extends RuntimeException {
  public DuplicateUserIdException(Throwable cause){
    super(cause);
  }
}
```
중첩 예외를 만들수 있도록 생성자를 추가 

언체크 예외로 만들어지긴 했지만 add() 메소드를 사용하는 쪽에서 아이디 중복 예외를 처리하고 싶은 경우 활용할 수 있음을 알려주도록
DuplicateUserIdException을 메소드의 throws 선언에 포함시킨다.

예외처리 전략을 적용한 add()
```java
public void add() throws DuplicateUserIdException(){
  try {
    // jdbc를 이용해 user 정보를 db에 추가하는 코드 또는
    // 그런 기능이 있는 다른 SQLException을 던지는 메소드를 호출하는 코드
  }
  catch (SQLException e) {
    if (e.getErrorCode() == MysqlErrorNumbers.ER_DUP_ENTRY)
      throw new DuplicateUserIdException(e); // 예외 전환
    else
      throw new RuntimeException(e); // 예외 
  }
}
```

### 4.1.5 SQLException은 어떻게 됐나?
대부분의 SQLException은 복구가 불가능하다.
필요도 없는 기계적인 throws 선언이 등장하도록 방치하지 말고 가능한 빨리 언체크/런타임 예외로 전환해줘야 한다.
스프링의 JdbcTemplate은 바로 이 예외처리 전략을 따르고 있다. JdbcTemplate 템플릿과 콜백안에서 발생하는
모든 SQLException을 런타임 예외인 DataAccessException으로 포장해서 던져준다.
따라서 JdbcTemplate을 사용하는 UserDao 메소드에선 꼭 필요한 경우엠만 런타임 예외인 DataAccessException을 잡아서 처리하면 되고
그 외의 경우에는 무시해도 된다
