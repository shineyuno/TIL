# 5장 서비스 추상화
사용 방법과 형식은 다르지만 기능과 목적이 유사한 기술이 존재한다.
스프링이 어떻게 성격이 비슷한 여러종류의 기술을 추상화하고 이를 일관된 방법으로 사용할 수 있도록
지원하는지를 살펴볼 것이다.

## 5.1 사용자 레벨 관리 기능 추가

### 5.1.1 필드추가
#### Level 이늄
정수형 상수 값으로 정의한 사용자 레벨
```java
class User {
  private static final int BASIC = 1;
  private static final int SILVER = 2;
  private static final int GOLD = 3;
  
  int level;
  
  public void setLevel(int level){
    this.level = level
  }
}
```
level의 타입이 int 이기 때문에 다른종류의 정보를 넣는 실수를 해도 컴파일러가 체크해주지 못한다.

사용자 레벨용 이늄
```java
public enum Level {
  BASIC(1), SILVER(2), GOLD(3); // 세 개의 이늄 오브젝트 정의
  
  private final int value;
  
  Level(int value) { // DB에 저장할 값을 넣어줄 생성자를 만들어 둔다.
    this.value = value; 
  }
  
  public int intValue() { // 값을 가져오는 메소드
    return value;
  }
  
  public static Level valueOf(int value) {  // 값으로부터 Level 타입 오브젝트를 가져오도록 만든 스태틱 메소드
    switch(value) {
      case 1: return BASIC;
      case 2: return SILVER;
      case 3: return GOLD;
      default: throw new AssertionError("Unknown value: " + value);
    }
  }
}
```
이렇게 만들어진 Level 이늄은 내부에는 DB에 저장할 int 타입의 값을 갖고 있지만, 겉으로는 Level 타입의 오브젝트이기 때문에
안전하게 사용할 수 있다. user1.setLevel(1000)과 같은 코드는 컴파일러가 타입이 일치하지 않는다는 에러를 내면서 걸러줄 것이다.

#### UserDaoJdbc 수정 코드
차가된 필드를 위한 UserDaoJdbc의 수정코드
```java
public class UserDaoJdbc implements UserDao {
  ...
  private RowMapper<User> userMapper =
    new RowMapper<User>() {
            public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                User user = new User();
                user.setId(rs.getString("id"));
                user.setName(rs.getString("name"));
                user.setPassword(rs.getString("password"));
                user.setLevel(Level.valueOf(rs.getInt("level")));
                user.setLogin(rs.getInt("login"));
                user.setRecommend(rs.getInt("recommend"));
                return user;
            }
   };
   
   public void add(User user) {
      this.jdbcTemplate.update(
        "insert into users(id, name, password, level, login, recommend) " +
        "values(?,?,?,?,?,?)", user.getId(), user.getName(),
        user.getPassword(), user.getLevel().intValue(),
        user.getLogin(), user.getRecommend()
      ); 
   }
  ...
}
```
여기서 눈여겨볼 것은 Level 타입의 level 필드를 사용하는 부분이다. Level 이늄은 오브젝트이므로 
DB에 저장될수 있는 SQL 타입이 아니다. 따라서 DB에 저장 가능한 정수형 값으로 변환해줘야 한다. 각 Level 이늄의 DB 저장용
값을 얻기 위해서는 Level에 미리 만들어둔 intValue() 메소드를 사용한다.

반대로 조회를 했을 경우, ResultSet에서는 DB의 타입인 int로 level 정보를 가져온다. 이값을 User의 setLevel() 메소드에 
전달하면 타입이 일치하지 않는다는 에러가 발생할 것이다. 이때는 Level의 스태틱 메소드인 valueOf()를 이용해 int 타입의 값을
Level 타입의 이늄 오브젝트로 만들어서 setLevel()메소드에 넣어줘야 한다.
