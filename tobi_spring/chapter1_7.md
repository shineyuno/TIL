# 1.7 의존관계 주입(DI)
## 1.7.1 제어의 역전과 의존관계 주입
 스프링이 제공하는 IoC 방식을 핵심을 짚어주는 의존관계 주입(Dependency Injection)이라는,
좀 더 의도가 명확히 드러나는 이름을 사용하기 시작했다.
 엄밀히 말해서 오브젝트는 다른 오브젝트에 주입할수 있는게 아니다. 오브젝트의 레퍼런스가 전달될 뿐이다.
DI는 오브젝트 레퍼런스를 외부로부터 제공(주입)받고 이를 통해 여타 오브젝트와 다이내믹하게 의존관계가 만들어지는 것이 핵심이다.

## 1.7.2 런타임 의존관계 설정
### 의존관계
두 개의 클래스 또는 모듈이 의존관계에 있다고 말할때는 항상 방향성을 부여해줘야 한다.
즉 누가 누구에게 의존하는 관계에 있다는 식이어야 한다.
대표적인 예는 A가 B를 **사용하는 경우** , 예를 들어 A에서 B에 정의된 메소드를 호출해서 사용하는 경우다.
 이럴 땐 '사용에 대한 의존관계'가 있다고 말할수 있다.
의존관계 주입은 구체적인 의존 오브젝트와 그것을 사용할 주체, 보통 클라이언트라고 부르는 오브젝트를 런타임 시에 연결해주는 작업을 말한다.
 의존관계 주입의 핵심은 설계 시점에는 알지 못했던 두 오브젝트의 관계를 맺도록 도와주는 제3의 존재가 있다는 것이다.
스프링의 애플리케이션 컨텍스트, 빈 팩토리, IoC 컨테이너 등이 모두 외부에서 오브젝트 사이의 런타임 관계를 맺어주는
책임을 지는 제3의 존재라고 볼수 있다.

## 1.7.3 의존관계 검색과 주입
 런타임 시에 의존관계를 결정한다는 점에서 의존관계 주입과 비슷하지만, 의존관계를 맺는 방법이 외부로부터의 주입이
 아니라 스스로 검색을 이용하기 때문에 의존관계 검색(dependency lookup)이라고 불리는 것도 있다.
### 의존관계 검색과 의존관계 주입 적용 차이점
의존관계 검색 방식에서는 검색하는 오브젝트는 자신이 스프링의 빈일 필요가 없다.
 DI를 원하는 오브젝트는 먼저 자기 자신이 컨테이너가 관리하는 빈이 돼야 한다.

## 1.7.5 메소드를 이용한 의존관계 주입
 * 수정자 메소드를 이용한 주입
 * 일반 메소드를 이용한 주입

 수정자 메소드 DI를 사요하는 팩토리 메소드
```java
    @Bean //오브젝트 생성을 담당하는 IoC용 메소드라는 표시
    public UserDao userDao(){
        UserDao userDao = new UserDao();
        userDao.setConnectionMaker(connectionMaker());
        return userDao;
    }
```