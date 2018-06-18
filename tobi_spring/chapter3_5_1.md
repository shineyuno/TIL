# 3.5 템플릿과 콜백

#### 템플릿
템플릿(template)은 어떤 목적을 위해 미리 만들어둔 모양이 있는 틀을 가리킨다.
JSP는 HTML이라는 고정된 부분에 EL과 스크립릿이라는 변하는 부분을 넣은 일종의 템플릿 파일이다. 
템플릿 메소드패턴은 고정된 틀의 로직을 가진 템플릿 메소드를 슈퍼클래스에 두고, 바뀌는 부분을 서브 클래스의 메소드에 두는 구조로 이뤄진다.

#### 콜백
콜백(callback)은 실행되는 것을 목적으로 다른 오브젝트의 메소드에 전달되는 오브젝트를 말한다.
파라미터로 전달되지만 값을 참조하기 위한 것이 아니라 특정 로직을 담은 메소드를 실행 시키기 위해 사용한다.
자바에선 메소드 자체를 파라미터로 전달할 방법은 없기 때문에 메소드가 담긴 오브젝트를 전달해야 한다. 그래서 펑셔널 오브젝트(functional object)라고도 한다.

## 3.5.2 편리한 콜백의 재활용

### 콜백의 분리와 재활용

익명 내부 클래스를 사용한 클라이언트 코드 
```java
public void deleteAll() throws SQLException {
 this.jdbcContext.workWithStatementStrategy (
  new StatementStrategy() {   //변하지 않는 콜백 클래스 정의와 오브젝트 생성
    public PreparedStatement makePreparedStatement(Connection c) throws SQLException {
      return c.prepareStatement("delete from users");
    }
  }
 );
}
```

변하지 않는 부분을 분리시킨 deleteAll 메소드
```java
public void deleteAll() throws SQLException {
 executeSql("delete from users"); // 변한는 sql 문장
}

//  ------------------------------------------------------------분리

private void executeSql(final String query) throws SQLException {
  this.jdbcContext.workWithStatementStrategy (
  new StatementStrategy() {   //변하지 않는 콜백 클래스 정의와 오브젝트 생성
    public PreparedStatement makePreparedStatement(Connection c) throws SQLException {
      return c.prepareStatement(query);
    }
  }
 );
}
```
변하는 것과 변하지 않는 것을 분리하고 변하지 않는건 유연하게 재활용할 수 있게 만든다는 간단한 원리를 계속 적용했을때
이렇게 단순하면서도 안전하게 작성 가능한 JDBC 활용 코드가 완성된다. 바로 이런 게 객체지향 언어와 설계를 사용하는 매력이 아닐까.


### 콜백과 템플릿의 결합
재사용 가능한 콜백을 담고 있는 메소드라면 DAO가 공유할수 있는 템플릿 클래스 안으로 옮겨도 된다.

jdbcContext로 옮긴 executeSql() 메소드
```java
public class JdbcContext {
  ...
  public void executeSql(final String query) throws SQLException {
  workWithStatementStrategy (
  new StatementStrategy() {   
    public PreparedStatement makePreparedStatement(Connection c) throws SQLException {
      return c.prepareStatement(query);
    }
  }
 );
}
  ...
}
```

JdbcContext로 옮긴 executeSql()을 사용하는 deleteAll()메소드
```java
public void deleteAll() throws SQLException {
 this.jdbcContext.executeSql("delete from users");
}
```

## 3.5.3 템플릿/ 콜백의 응용
 고정된 작업 흐름을 갖고 있으면서 여기저기서 자주 반복되는 코드가 있다면, 중복되는 코드를 분리할 방법을 생각해보는 습관을 기르자.
 중복된 코드는 먼저 메소드로 분리하는 간단한 시도를 해본다. 그중 일부 작업을 필요에 따라 바꾸어 사용해야 한다면 인터페이스를 사이에 두고
 분리해서 전략 패턴을 적용하고 DI로 의존관계를 관리하도록 만든다.
 그런데 바뀌는 부분이 한 애플리케이션 안에서 동시에 여러 종류가 만들어 질수 있다면 이번엔 템플릿/콜백 패턴을 적용하는 것을 고려해볼 수 있다.


