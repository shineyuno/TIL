# 3.3 jdbc 전략패턴의 최적화

### 중첩 클래스의 종류
 다른 클래스 내부에 정의되는 클래스를 중첩 클래스(nested class)라고 한다. 
중첩 클래스는 독립적으로 오브젝트로 만들어질 수 있는 스태틱 클래스(static class)와 자신이 정의된 클래스의
오브젝트 안에서만 만들어질 수 있는 내부 클래스(inner class)로 구분된다.
 내부 클래스는 다시 범위(scope)에 따라 세 가지로 구분된다. 멤버 필드처럼 오브젝트 레벨에 정의되는 멤버 내부 클래스(member inner class)와
메소드 레벨에 정의되는 로컬 클래스(local class), 그리고 이름을 갖지 않는 익명 내부 클래스(anonymous inner class)다. 익명 내부 클래스의 
범위는 선언된 위치에 따라서 다르다.

내부 클래스에서 외부의 변수를 사용할 때는 외부 변수는 반드시 final로 선언해줘야 한다. user 파라미터는 메소드 내부에서 변경될 일이
없으므로 final로 선언해도 무방하다.

### 익명 내부 클래스
익명 내부 클래스(anonymous inner class)는 이름을 갖지 않는 클래스다. 클래스 선언과 오브젝트 생성이 결합된 형태로 만들어지며,
상속할 클래스나 구현할 인터페이스를 생성자 대신 사용해서 다음과 같은 형태로 만들어 사용한다. 클래스를 재사용할 필요가 없고, 구현한 
인터페이스 타입으로 사용할 경우에 유용한다.
new 인터페이스이름() {클래스 본문}

익명 내부 클래스를 적용한 deleteAll()메소드
```java
public void deleteAll() throws SQLException {
 jdbcContextWithStatementStrategy (
  new StatementStrategy() {
    public PreparedStatement makePreparedStatement(Connection c) throws SQLException {
      return c.prepareStatement("delete from users");
    }
  }
 )
}
```

# 3.4 컨텍스트와 DI
## 3.4.1 JdbcContext의 분리 

JDBC 작업 흐름을 분리해서 만든 jdbcContext 클래스
```java

public class JdbcContext {
 private DataSource dataSource;
 
 public void setDataSource(DataSource dataSource){ // DataSource 타입 빈을 DI 받을 수 있게 준비해둔다.
  this.dataSource = dataSource; 
 }
 
 public void workWithStatementStrategy(StatementStrategy stmt) throws SQLException {
  Connection c = null;
  PreparedStatement ps = null;
  
  try {
    c = this.dataSource.getConnection();
    
    ps = stmt.makePreparedStatement(c);
    
    ps.executeUpdate();
  } catch (SQLException e) {
    throw e;
  } finally {
    if(ps != null ) { try { ps.close();} catch(SQLException) {} }
    if(c != null ) { try { c.close();} catch(SQLException) {} }
  }
  
 }
}
```

## 3.4.2 JdbcContext의 특별한 DI

JdbcContext 생성과 DI 작업을 수행하는 setDataSource()메소드
```java
public class UserDao {
  ...
  private JdbcContext jdbcContext;
  
  public void setDataSource(DataSource dataSource) { // 수정자 메소드이면서 jdbcContext에 대한 생성, DI 작업을 동시에 진행한다.
    this.jdbcContext = new JdbcContext(); // jdbcContext 생성(IoC)
    
    this.jdbcContext.setDataSource(dataSource); // 의존오브젝트 주입 (DI)
  }
  
}
```
