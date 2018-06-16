# 3장 템플릿
 템플릿이란 바뀌는 성질이 다른 코드중에서 변경이 거의 일어나지 않으며
일정한 패턴으로 유지되는 특성을 가진 부분을 자유롭게 변경되는 성질을 가진 부분으로부터 독립시켜서 효과적으로 활용할 수 있도록 하는 방법이다.

## 3.1 다시보는 초난감 DAO

예외 발생시에도 리소스를 반환하도록 수정한 deleteAll()

```java
public void deleteAll() throws SQLException {
  Connection c = null;
  PreparedStatement ps = null;
  
  try {
    c = dataSource.getConnection();
    ps = c.prepareStatement("delete from users");
    ps.executeUpdate();
  }catch (SQLException e){
    throw e;
  } finally {
    if(ps != null){
      try{
        ps.close();
      } catch (SQLException e){
      }
    }
    if(c != null){
      try{
        c.close();  //Connection 반환
      } catch (SQLException e){
      }
    }
  }
}
```

## 3.2 변하는 것과 변하지 않는것 


### 3.2.1 JDBC try/catch/finally 코드의 문제점
모든 메소드마다 반복된다.

### 3.2.2 분리와 재사용을 위한 디자인 패턴 적용
 변하는 부분을 변하지 않는 나머지 코드에서 분리하는것이 어떨까? 그렇게 할수  있다면 변하지 않는 부분을 재사용할 수 있는 방법이 있지 않을까?

#### 메소드 추출
 변하는 부분을 메소드로 빼는것이다.
 변하지 않는 부분이 변하는 부분을 감싸고 있어서 변하지 않는 부분을 추출하기가 어려워 보이기 때문에 반대로 했다.
변하는 부분을 메소드로 추출한 후의 deleteAll()
```java
public void deleteAll() throws SQLException {
  ...
  try {
    c = dataSource.getConnection();
    
    ps = c.prepareStatement("delete from users");
    
    ps.executeUpdate();
  }catch (SQLException e)
  ... 
}

private PreparedStatement makeStatement(Connection c) throws SQLException {
 PreparedStatement ps;
 ps = c.prepareStatement("delete from users");
 return ps;
}
```
자주 바뀌는 부분을 메소드로 독립시켜봤는데 당장 봐서는 별 이득이 없어 보인다. 왜냐하면 보통 메소드 추출 리팩토링을 적용하는 경우에는
분리시킨 메소드를 다른곳에서 재사용할 수 있어야 하는데, 이건 반대로 분리시키고 남은 메소드가 재사용이 필요한 부분이고, 분리된 메소드는
DAO 로직마다 새롭게 만들어서 확장돼야 하는 부분이기 때문이다. 뭔가 반대로 됐다. 

#### 템플릿 메소드 패턴의 적용
 템플릿 메소드 패턴은 상속을 통해 기능을 확정해서 사용하는 부분이다. 변하지 않는 부분은 슈퍼클래스에 두고 변하는 부분은
 추상 메소드로 정의해둬서 서브클래스에서 오버라이드하여 새롭게 정의해 쓰도록 하는것이다. 

추출해서 별도의 메소드로 독립시킨 makeStatement() 메소드를 다음과 같이 추상 메소드 선언으로 변경한다.
물론 UserDao 클래스도 추상 클래스가 돼야 할 것이다.
```java
abstract protected PreparedStatement makeStatement(Connection c) throws SQLException
```
고정된 JDBC try/catch/finally 블록을 가진 슈퍼클래스 메소드와 필요에 따라서 상속을 통해 구체적인 PreparedStatement를
바꿔서 사용할수 있게 만드는 서브클래스로 깔끔하게 분리할수 있다.


makeStatement()를 구현한 UserDao 서브 클래스
```java
public class UserDaoDeleteAll extends UserDao {

  protected PreparedStatement makeStatement(Connection c) throws SQLException {
  PreparedStatement ps = c.prepareStatement("delete from users");
  return ps;
 }
}
```
템플릿 메소드 패턴으로의 접근은 제한이 많다. 가장큰 문제는 DAO 로직마다 상속을 통해 새로운 클래스를 만들어야 한다는점이다.

#### 전략 패턴의 적용
 개방 폐쇄 원칙을 잘 지키는 구조이면서도 템플릿 메소드 패턴보다 유연하고 확장성이 뚸어난 것이,
 오브젝트를 아예 둘로 분리하고 클래스 레벨에서는 인터페이스를 통해서만 의존하도록 만드는 전략 패턴이다.
 
 deleteAll()은 JDBC를 이용해 DB를 업데이트하는 작업이라는 변하지 않는 맥락context을 갖는다.
 
StatementStrategy 인터페이스
```java
public interface StatementStrategy {
 PreparedStatement makePreparedStatement(Connection c) throws SQLException;
}
```

deleteAll() 메소드의 기능을 구현한 StatementStrategy 전략 클래스
```java
public class DeleteAllStatement implements StatementStrategy{
 public PreparedStatement makePreparedStatement(Connection c) throws SQLException{
  PreparedStatement ps = c.prepareStatement("delete from users");
  return ps;
 }
}
```

전략 패턴을 따라 DeleteAllStatement가 적용된 deleteAll() 메소드
```java
public void deleteAll() throws SQLException{
 ...
 try {
 
  c = dataSource.getConnection();
  
  StatementStrategy strategy = new DeleteAllStatement();
  ps = strategy.makePreparedStatement(c)
  
  ps.executeUpdate();
 } catch (SQLException e){
 ...
}
```
이럲게 컨텍스트 안에서 이미 구체적인 전략 클래스인 DeleteAllStatement를 사용하도록 고정되어 있다면 뭔가 이상하다.
컨텍스트가 StatementStrategy 인터페이스뿐 아니라 특정 구현 클래스인 DeleteAllStatement를 직접 알고 있다는건, 전략 패턴에도 
OCP에도 잘 들어맞는다고 볼수 없기때문이다.


#### DI 적용을 위한 클라이언트/ 컨텍스트 분리
 전략 패턴에 따르면 Context가 어떤 전략을 사용하게 할 것인가는 Context를 사용하는 앞단의
 Client가 결정하는 게 일반적이다. Client가 구체적인 전략의 하나를 선택하고 오브젝트로 만들어서 Context에 전달하는 것이다.
 Context는 전달받은 Strategy구현 클래스의 오브젝트를 사용한다. 

메소드로 분리한 try/catch/finally 컨텍스트 코드

```java
public void jdbcContextWithStatementStrategy(StatementStrategy stmt) throws SQLException {
  Connection c = null;
  PreparedStatement ps = null;
  
  try {
    c = dataSource.getConnection();
    
    ps = stmt.makePreparedStatement(c);
    
    ps.executeUpdate();
  }catch (SQLException e){
    throw e;
  } finally {
    if(ps != null){
      try{
        ps.close();
      } catch (SQLException e){
      }
    }
    
    if(c != null){
      try{
        c.close();  //Connection 반환
      } catch (SQLException e){
      }
    }
    
  }
}
```
모든 JDBC 코드의 틀에 박힌 작업은 이 컨텍스트 메소드안에 잘 담겨 있다.

**클라이언트 책임을 담당할 deleteAll() 메소드**

```java
public void deleteAll() throws SQLException {
 StatementStratege st = new DeleteAllStatement(); // 선정한 전략 클래스의 오브젝트 생성
 jdbcContextWithStatementStrategy(st); // 컨텍스트 호출, 전략 오브젝트 전달
}
```


#### 마이크로 DI
DI의 가장 중요한 개념은 제3자의 도움을 통해 두 오브젝트 사이의 유연한 관계가 설정되도록 만든다는 것이다.
일반적으로 DI는 의존관계에 있는 두 개의 오브젝트와 이 관계를 다이내믹하게 설정해주는 오브젝트 팩토리(DI 컨테이너), 그리고 이를 사용하는
클라이언트라는 4개의 오브젝트 사이에서 일어난다. 
IoC 컨테이너의 도움없이 코드 내에서 적용한 경우르르 마이크로 DI라고도 한다. 
