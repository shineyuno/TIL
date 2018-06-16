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

#### DI 적용을 위한 클라이언트/ 컨텍스트 분리

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
