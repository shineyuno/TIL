# 2.2 UserDaoTest 개선
 개발 과정에서, 또는 유지보수를 하면서 기존 애플리케이션 코드에 수정을 할때 마음의 평안을 얻고,
자신이 만지는 코드에 대해 항상 자신감을 가질 수 있으며, 새로 도입한 기술의 적용에 문제가 없는지 확인할 수 있는 
가장 좋은 방법은 빠르게 실행 가능하고 스스로 테스트 수행과 기대하는 결과에 대한 확인까지 해주는 코드로 된 자동화된 
테스트를 만들어 두는 것이다.

# 2.3 개발자를 위한 테스틍 프레임워크 JUnit
### 동일한 결과를 보장하는 테스트
 단위 테스트는 항상 일관성 있는 결과가 보장돼야 한다는 점을 잊어선 안 된다.
DB에 남아 있는 데이터와 같은 외부 환경에 영향을 받지 말아야 하는 것은 물론이고, 
테스트를 실행하는 순서를 바꿔도 동일한 결과가 보장되도록 만들어야 한다.

### 예외조건에 대한 테스트
JUnit은 예외 조건 테스트를 위한 특별한 방법을 제공한다.

**메소드의 예외상황에 대한 테스트**
```java
@Test(expected=EmptyResultDataAccessException.class) // 테스트중에 발생할 것으로 기대하는 예외 클래스를 지정해준다.
public void getUserFailure() throws SQLException {
  ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");
  
  UserDao dao = context.getBean("userDao", UserDao.class);
  dao.deleteAll();
  assertThat(dao.getCount(), is(0));
  
  dao.get("unknown_id"); // 이 메소드 실행 중에 예외가 발생해야 한다. 예외가 발생하지 않으면 테스트가 실패한다.
}
```
이 테스트에서 중요한것은@Test  애노테이션의 expected앨리먼트다. 
expected는 테스트 메소드 실행 중에 발생하리라 기대하는 예외 클래스를 넣어주면된다.
@Test에 expected를 추가해놓으면 보통의 테스트와는 반대로, 정상적으로 테스트 메소드를 마치면 테스트가 실패하고,
expected에서 지정한 예외가 던져지면 테스트가 성공한다. 
