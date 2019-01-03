# 7.4 인터페이스 상속을 통한 안전한 기능확장

## 7.4.1 DI와 기능의 확장

### DI를 의식하는 설계
SqlService의 내부기능을 적절한 책임과 역할에 따라 분리하고, 인터페이스를 정의해 느슨하게 연결해주고,
DI를 통해 유연하게 의존관계를 지정하도록 설계해뒀기 때문에 그 뒤의 작업은 매우 쉬워졌다.

오브젝트들이 서로의 세부적인 구현에 얽매이지 않고 유연한 방식으로 의존관계를 맺으며 독립적으로 발전할 수 있게 해주는 DI 덕분이다.
결국 유연하고 확장 가능한 좋은 오브젝트 설계와 DI 프로그래밍 모델은 서로 상승작용을 한다.

객체지향 설계를 잘하는 방법은 다양하겠지만, 그중에서 추천하고 싶은 한가지가 있다면 바로 DI를 의식하면서 설계하는 방식이다.
DI를 적용하려면 커다란 오브젝트 하나만 존재해서는 안 된다. 
최소한 두개 이상의, 의존관계를 가지고 서로 협력해서 일하는 오브젝트가 필요하다. 그래서 적절한 책임에 따라 오브젝트를 분리해줘야 한다.

DI를 잘 활용할 수 있는 방법을 생각하면서 오브젝트를 설계한다면 객체지향 기술이 약속하는 유연한 확장과 재사용이 가능한 설계를 만드는 데
많은 도움이 될것이다.
확장은 항상 미래에 일어나는 일이다.
DI란 결국 미래를 프로그래밍하는 것이다.

### DI와 인터페이스 프로그래밍
DI를 DI답게 만들려면 두 개의 오브젝트가 인터페이스를 통해 느슨하게 연결돼야 한다.
인터페이스를 사용하는 첫 번째 이유는 다형성을 얻기 위해서다.
하나의 인터페이스를 통해 여러 개의 구현을 바꿔가면서 사용할 수 있게 하는 것이 DI가 추구하는 첫 번째 목적이다.

두번째 이유는 인터페이스 분리 원칙을 통해 클라이언트와 의존 오브젝트 사이의 관계를 명확하게 해줄 수 있기 때문이다.
인터페이스는 하나의 오브젝트가 여러 개를 구현할 수 있으므로, 하나의 오브젝트를 바라보는 창이 여러 가지일 수 있다는 뜻이다.
각기 다른 관심과 목적을 가지고 어떤 오브젝트에 의존하고 있을 수 있다는 의미다.

오브젝트가 그 자체로 충분히 응집도가 높은 작은 단위로 설계됐더라도, 목적과 관심이 각기 다른 클라이언트가 있다면 인터페이스를 통해
이를 적절하게 분리해줄 필요가 있고, 이를 객체지향 설계 원칙에서는 인터페이스 분리 원칙(interface segregation principle)이라고 부른다.
인터페이스를 사용하지 않고 클래스를 직접 참조하는 방식으로 DI를 했다면, 인터페이스 분리 원칙과 같은 클라이언트에 특화된 의존관계를 만들어낼
방법 자체가 없는 것이다.

다형성은 물론이고 클라이언트별 다중 인터페이스 구현과 같은 유연하고 확장성 높은 설계가 가능함에도 인터페이스를 피할 이유는 없다.

## 7.4.2 인터페이스 상속
 하나의 오브젝트가 구현하는 인터페이스를 여러 개 만들어서 구분하는 이유 중의 하나는 오브젝트의 기능이 발전하는 과정에서 다른 종류의 클라이언트가
 등장하기 때문이다.
 때로는 인터페이스를 여러 개 만드는 대신 기존 인터페이스를 상속을 통해 확장하는 방법도 사용된다.
  인터페이스 분리 원칙이 주는 장점은 모든 클라이언트가 자신의 관심에 따른 접근 방식을 불필요한 간섭 없이 유지할 수 있다는 점이다.
  
리스트 7-61 SqlRegistry 인터페이스
```java
public interface SqlRegistry {
  void registerSql(String key, String sql);
  
  String findSql(String key) throws SqlNotFoundException;
}
```

리스트 7-62 SQL 수정 기능을 가진 확장 인터페이스
```java
public interface UpdatableSqlRegistry extends SqlRegistry {
  public void updateSql(String key, String sql) throws SqlUpdateFailureException;
  
  public void updateSql(Map<String, String> sqlmap) throws SqlUpdateFailureException;
}
```

SQL 변경 요청을 담당하는 SQL 관리용 오브젝트가 있다고 하고, 클래스 이름은 SqlAdminService라고 하자.
SQL 업데이트 기능까지 구현한 SQL 레지스트리 클래스를 MyUpdatableSqlRegistry라고 하자 

리스트 7-63 MyUpdatableSqlRegistry의 의존관계
```xml
<bean id="sqlService" class="springbook.user.sqlservice.BaseSqlService">
...
  <property name="sqlRegistry" ref="sqlRegistry" />
</bean>

<bean id="sqlRegistry" class="springbook.user.sqlservice.MyUpdatableSqlRegistry" />

<bean id="sqlAdminService" class="springbook.user.sqlservice.SqlAdminService" >
  <property name="updatableSqlRegistry" ref="sqlRegistry" />
  ...
</bean>  
```

BaseSqlService와 SqlAdminService는 동일한 오브젝트에 의존하고 있지만 각자의 관심과 필요에 따라서 다른 인터페이스를 통해 접근한다.
