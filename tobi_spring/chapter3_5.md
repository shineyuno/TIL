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



try/catch/finally를 적용한 calcSum() 메소드
```java
public Integer calcSum(String filepath) throws IOException {
 BufferedReader br = null;
 
 try {
  br = new BufferedReader(new FileReader(filepath));;
  Integer sum = 0;
  String line = null;
  while((line = br.readLine()) != null) {
   sum += Integer.valueOf(line);
  }
  return sum;
 }
 catch(IOException e) {
  System.out.println(e.getMessage());
  throw e;
 }
 finally {
  if(br != null) { // BufferedReader 오브젝트가 생성된기 전에 예외가 발생할 수도 있으므로 반드시 null체크를 먼저 해야한다.
   try { br.close();}
   catch(IOException e) { System.out.println(e.getMessage()); }
  }
 }
}
```

#### 중복의 제거와 템플릿 / 콜백 설계
템플릿/ 콜백 패턴을 적용해 보자. 먼저 템플릿에 담을 반복되는 작업 흐름은 어떤 것인지 살펴보자.
템플릿이 콜백에게 전달해줄 내부의 정보는 무엇이고, 콜백이 템플릿에게 돌려줄 내용은 무엇인지도 생각해보자.
템플릿이 작업을 마친뒤 클라이언트에게 전달해줘야 할 것도 있을 것이다. 템플릿/콜백을 적용할 때는 템플릿과 콜백의
경계를 정하고 템플릿이 콜백에게, 콜백이 템플릿에게 각각 전달하는 내용이 무엇인지 파악하는게 가장 중요하다.
그에 따라 콜백의 인터페이스를 정의해야 하기 때문이다.


BufferedReader를 전달받는 콜백 인터페이스
```java
public interface BufferedReaderCallback {
 Integer doSomethingWithReader(BufferedReader br) throws IOException;
}
```

BufferedReaderCallback을 사용하는 템플릿 메소드
```java
public Integer fileReadTemplate(String filepath, BufferedReaderCallback callback) throws IOException {
 BufferedReader br = null;
 try {
  br = new BufferedReader(new FileReader(filepath));
  int ret = callback.doSomethingWithReader(br); //콜백 오브젝트 호출. 템플릿에서 만든 컨텍스트 정보인 BufferedReader를 전달해주고 콜백의 작업 결과를 받아둔다.
  return ret;
 }
 catch(IOException e) {
  System.out.println(e.getMessage());
  throw e;
 }
 finally {
  if(br != null) { 
   try { br.close();}
   catch(IOException e) { System.out.println(e.getMessage()); }
  }
 }
}
```

템플릿/콜백을 적용한 calcSum()메소드
```java
public Integer calcSum(String filepath) throws IOException {
 BufferedReaderCallback sumCallbakc = new BufferedReaderCallback() {
  public Integer doSomethingWithReader(BufferedReader br) throws IOException {
   Integer sum = 0;
  String line = null;
  while((line = br.readLine()) != null) {
   sum += Integer.valueOf(line);
  }
  return sum;
  }
 };
 return fileReadTemplate(filepath, sumCallback);
}
```


#### 템플릿/ 콜백의 재설계
템플릿과 콜백을 찾아낼 때는, 변하는 코드의 경계를 찾고 그 경계를 사이에 두고 주고받는 일정한 정보가 있는지 확인하면 된다고 했다.

라인별 작업을 정의한 콜백 인터페이스
```java
public interface LineCallback {
 Integer doSomethingWithLine(String line, Integer value);
}
```

LineCallback을 사용하는 템플릿
```java
public Integer lineReadTemplate(String filepath, LineCallback callback, int initVal) throws IOException {
// initVal 계산 결과를 저장할 변수의 초기값
 BufferedReader br = null;
 try {
  br = new BufferedReader(new FileReader(filepath));
  Integer res = initVal;
  String line = null;
 
  while((line = br.readLine()) != null) { // 파일의 각 라인을 루프를 돌면서 가져오는 것도 템플릿이 담당한다.
   res =  callback.doSomethingWithLine(line, res); //line 각 라인의 내용을 가지고 계산하는 작업만 콜백에게 맡긴다.
    // res 콜백이 계산한 값을 저장해뒀다가 다음 라인 계산에 다시 사용한다.
  }  
  return res;
 }
 catch(IOException e) {...}
 finally {...}
}
```

lineReadTemplate()을 사용하도록 수정한 calSum() 메소드
```java
public Integer calcSum(String filepath) throws IOException {
 LineCallback sumCallback = new LineCallback() {
  public Integer doSomethingWithLine(String line, Integer value) {
   return value + Integer.valueOf(line);
  }
 };
 return lineReadTemplate(filepath, sumCallback, 0);
}
```
여타 로우레벨의 파일 처리 코드가 템플릿으로 분리되고 순수한 계산 로직만 남아 있기 때문에 코드의 관심이 무엇인지 명확하게 보인다.
