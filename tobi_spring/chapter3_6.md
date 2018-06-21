# 3.6 스프링의 Jdbc Template

#### query() 템플릿을 이용하는 getAll() 구현
query()의 리턴 타입은 List<T>다. query()는 제네릭 메소드로 타입은 파라미터로 넘기는 RowMapper<T> 콜백 오브젝트에서 결정된다.

```java
public List<User> getAll() {
 return this.jdbcTemplate.query("select * from users order by id",
    new RowMapper<User>() {
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getString("id"));
            user.setName(rs.getString("name"));
            user.setPassword(rs.getString("password"));

            return user;
        }
    });
}
```
query() 템플릿은 SQL을 실행해서 얻은 ResultSet의 모든로우를 열람하면서 로우마다 RowMapper 콜백을 호출한다.

#### 중복제거
RowMapper 콜백을 메소드에서 분리해 중복을 없애고 재사용되게 만들어야 한다.
먼저 매번 RowMapper 오브젝트를 새로 만들어야 할지 생각해보자. RowMapper 콜백 오브젝트에는 상태정보가 없다.
따라서 하나의 콜백 오브젝트를 멀티쓰레드에서 동시에 사용해도 문제가 되지 않는다. RowMapper 콜백은 하나만 만들어서 공유하자.
익명 내부 클래스는 클래스 안에서라면 어디서든 만들수 있다.

재사용 가능하도록 독립시킨 RowMapper
```java
public class UserDao {
    private RowMapper<User> userMapper =
    new RowMapper<User>() {
            public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setName(rs.getString("name"));
                user.setPassword(rs.getString("password"));

                return user;
            }
}
```

스프링에는 JdbcTemplate 외에도 십여 가지의 템플릿/콜백 패턴을 적용한 API가 존재한다.
클래스 이름이 Template으로 끝나거나 인터페이스 이름이 Callback으로 끝난다면 템플릿/콜백이 적용된 것이라고 보면 된다.


# 3.7
일정한 작업 흐름이 반복되면서 그중 일부 기능만 바뀌는 코드가 존재한다면 전략 패턴을 적용한다.
바뀌지 않는 부분은 컨텍스트로, 바뀌는 부분은 전략으로 만들고 인터페이스를 통해 유연하게 전략을 변경할 수 있도록 구성한다.

단일 전략 메소드를 갖는 전략 패턴이면서 익명 내부 클래스를 사용해서 매번 전략을 새로 만들어 사용하고, 컨텍스트 호출과 동시에
전략 DI를 수행하는 방식을 템플릿/콜백 패턴이라고 한다.

콜백의 코드에도 일정한 패턴이 반복된다면 콜백을 템플릿에 넣고 재활용하는 것이 편리하다.

템플릿은 한 번에 하나 이상의 콜백을 사용할 수도 있고, 하나의 콜백을 여러 번 호출할 수도 있다.

템플릿/콜백을 설계할 때는 템플릿과 콜백 사이에 주고받는 정보에 관심을 둬야 한다.