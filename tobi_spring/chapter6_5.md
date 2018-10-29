# 6.5 스프링 AOP

## 6.5.1 자동 프록시 생성
프록시 팩토리 빈 방식의 접근 방법의 한계라고 생각했던 두가지 문제
- 부가기능이 타깃 오브젝트마다 새로 만들어지는 문제는 스프링 ProxyFactoryBean의 어드바이스를 통해 해결
- 남은것은 부가기능의 적용이 필요한 타깃 오브젝트마다 거의 비슷한 내용의 ProxyFactoryBean 빈 설정정보를 추가해주는 부분

### 중복 문제의 접근 방법
지금 까지 다뤄봤던 반복적이고 기계적인 코드에 대한 해결책
- JDBC API를 사용하는 DAO 코드 -> 메소드마다 JDBC try/catch/finally 블록으로 구성된 비슷한 코드가 반복 
-- 이 코드는 바뀌지 않는 부분과 바뀌는 부분을 구분해서 분리하고 . 템플릿과 콜백, 클라이언트로 나누는 방법을 통해 해결 ->전락패턴 과 DI 적용한덕분

- 좀 다른 방법으로 반복되는 코드의 문제 -> 반복적인 위임코드가 필요한 프록시 클래스 코드
-- 변하지 않는 타깃으로의 위임과 부가기능 적용 여부 판단이라는 부분은 코드 생성기법을 이용하는 다이내믹 프록시 기술에 맡기곡,
-- 변하는 부가기능 코드는 별도로 만들어서 다이내믹 프록시 생성 팩토리에 DI로 제공하는 방법을 사용
-- 의미있는 부가기능 로직인 트랜잭션 경계설정은 코드로 만들게 하고, 기계적인 코드인 타깃 인터페이스 구현과 위임, 부가기능 연동 부분은 자동생성하게


반복적인 ProxyFactoryBean 설정 문제는 설정 자동등록 기법으로 해결 할수 없을까?
일정한 타깃빈의 목록을 제공하면 자동으로 각 타깃 빈에 대한 프록시를 만들어주는 방법이 있다면 ProxyFactoryBean 타입 빈 설정을 
매번추가해서 프록시를 만들어내는 수고를 덜수 있을거 같다.

### 빈 후처리기를 이용한 자동 프록시 생성기
스프링은 컨테이너로서 제공하는 기능 중에서 변하지 않는 핵심적인 부분외에는 대부분 확장할 수 있도록 확장 포인트를 제공해준다.
그중에서 관심을 가질만한 확장포인트는 바로 BeanPostProcessor 인터페이스를 구현해서 만드는 빈 후처리기다.
빈 후처리기는 이름 그대로 스프링 빈 오브젝트로 만들어지고 난 후에, 빈 오브젝트를 다시 가공할 수 있게 해준다.

DefaultAdvisorAutoProxyCreator는 어드바이저를 이용한 자동 프록시 생성기인 빈 후처리기 이다.
스프링은 빈 후처리기가 빈으로 등록되어 있으면 빈 오브젝트가 생성될 때마다 빈후처리기에 보내서 후처리 작업을 요청한다.
빈후처리기는 빈 오브젝트의 프로퍼티를 강제로 수정할수도 있고 별도의 초기화 작업을 수행할 수도 있다.
심지어는 만들어진 빈 오브젝트를 자체를 바꿔치기할 수도 있다.
따라서 스프링이 설정을 참고해서 만든 오브젝트가 아닌 다른 오브젝트를 빈으로 등록시키는 것이 가능하다.
이를 잘 이용하면 스프링이 생성하는 빈 오브젝트의 일부를 프록시로 포장하고, 프록시를 빈으로 대신 등록할 수도 있다.
바로 이것이 자동 프록시 생성 빈 후처리기다.

빈으로 등록된 모든 어드바이저 내의 포인트컷을 이용해 전달받은 빈이 프록시 적용 대상인지 확인한다.
프록시 적용대상이면 그때는 내장된 프록시 생성기에게 현재 빈에 대한 프록시를 만들게 하고, 만들어진 프록시에 어드바이저를 연결해준다.
빈 후처리기는 프록시가 생성되면 원래 컨테이너가 전달해준 빈 오브젝트 대신 프록시 오브젝트를 컨테이너에게 돌려준다.
컨테이너는 최종적으로 빈 후처리기가 돌려준 오브젝트를 빈으로 등록하고 사용한다.

적용할 빈을 선정하는 로직이 추가된 포인트컷이 담긴 어드바이저를 등록하고 빈 후처리기를 사용하면 일일이 ProxyFactoryBean 빈을 등록하지
않아도 타깃 오브젝트에 자동으로 프록시가 적용되게 할수 있다. 


### 확장된 포인트컷
두 가지 기능을 정의한 Pointcut 인터페이스
```java
public interface Pointcut {
    ClassFilter getClassFilter(); //프록시를 적용할 클래스인지 확인해준다.
    MethodMatcher getMethodMatcher();   //어드바이스를 적용할 메소드인지 확인해준다.
}
```
기존에 사용한 NameMatchMethodPointcut은 메소드 선별 기능만 가진 특별한 포인트 컷이다.
메소드만 션별한다는건 클래스 필터는 모든 클래스를 다 받아주도록 만들어져 있다는 뜻이다.
ProxyFactoryBean에서 포인트컷을 사용할때는 이미 타깃이 정해져 있기 때문에 포인트컷은 메소드 선별만 해주면 그만이다.
만약 Pointcut 선정 기능을 모두 적용한다면 먼저 프록시를 적용할 클래스인지 판단하고 나서, 적용 대상 클래스인 경우에는
어드바이스를 적용할 메소드인지 확인하는 식으로 동작한다.

모든 빈에 대해 프록시 자동 적용 대상을 선별해야 하는 빈 후처리기인 DefaultAdvisorAutoProxyCreator는 클래스와 메소드
선정 알고리즘을 모두 갖고 있는 포인트컷이 필요하다.

확장 포인트컷 테스트
```java
@Test
public void classNamePointcutAdvisor(){
    //포인트컷 준비
    NameMatchMethodPointcut classMethodPointcut = new NameMatchMethodPointcut(){
        public ClassFilter getClassFilter() {   //익명 내부 클래스 방식으로 클래스를 정의한다.
            return new ClassFilter(){
                public boolean match(Class<?> clazz){
                    return clazz.getSimpleName().startsWith("HelloT");  // 클래스 이름이 HelloT로 시작하는것만 선정한다.
                }
            };
        }
    };
    
    classMethodPointcut.setMappedName("sayH*"); //sayH로 시작하는 메소드 이름을 가진 메소드만 선정한다.
    
    //테스트
    checkAdviced(new HelloTarget(), classMethodPointcut,true);  //적용 클래스다
    
    class HelloWorld extends HelloTarget {};
    checkAdviced(new HelloWorld(), classMethodPointcut, false); // 적용클래스가 아니다.
}

private void checkAdviced(Object target, Pointcut pointcut, boolean adviced) { [3]  //adviced ->적용대상인가?
    ProxyFactoryBean pfBean = new ProxyFactoryBean();
    pfBean.setTarget(target);
    pfBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdivce()));
    Hello proxiedHello = (Hello) pfBean.getObject();
    
    if(adviced){
        assertThat(proxiedHello.sayHello("Toby"), is("HELLO TOBY"));    // 메소드 선정 방식을 통해 어드바이스 적용
    } else {
        assertThat(proxiedHello.sayHello("Toby"), is("Hello Toby"));    //어드바이스 적용 대상 후보에서 아예터ㅏㄹ럭 
    }
        
}
```
포인트컷은 NameMatchMethodPointcut을 내부 익명 클래스 방식으로 확장해서 만들었다.
원래 모든 클래스를 다 받아주는 클래스 필터를 리턴하던 getClassFilter()를 오버라이드해서
이름이 HelloT로 시작하는 클래스만을 선정해주는 필터로 만들었다.

포인트컷이 클래스 필터까지 동작해서 클래스를 걸러버리면 아무리 프록시를 적용했다고 해도 부가기능은 전혀
제공되지 않는다는 점에 주의해야 한다.

## 6.5.2 DefaultAdvisorAutoProxyCreator의 적용


### 클래스 필터를 적용한 포인트컷 작성
클래스 필터가 포함된 포인트컷
```java
public class NameMatchClassMethodPointcut extends NameMatchMethodPointcut {
    public void setMappedClassName(String mappedClassName){
        this.setClassFilter(new SimpleClassFilter(mappedClassName)); //모든 클래스를 다 허용하던 디폴트 클래스 필터를 프로퍼티로
                                                                    //받은 클래스 이름을 이용해서 필터를 만들어 덮어씌운다.
    }

    static class SimpleClassFilter implements ClassFilter {
        String mappedName;

        private SimpleClassFilter(String mappedName) {
            this.mappedName = mappedName;
        }

        public boolean matches(Class<?> clazz){
            return PatternMatchUtils.simpleMatch(mappedName, clazz.getSimpleName());
            //simpleMatch() 와일드카드(*)가 들어간 문자열 비교를 지원하는 스프링의 유틸리티 메소드다. *.name, name.*,*name* 세가지 방식을 모두 지원한다.
        }
    }
}

```

### 어드바이저를 이용하는 자동 프록시 생성기 등록
적용할 자동프록시 생성기인 DefaultAdvisorAutoProxyCreator는 등록된 빈중에서 Advisor 인터페이스를 구현한 것을 모두 찾는다.
그리고 생성되는 모든 빈에 대해 어드바이저의 포인트컷을 적용해보면서 프록시 적용 대상을 선정한다.

DefaultAdvisorAutoProxyCreator 등록은 다음 한 줄이면 충분한다.
```xml
<bean class="org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator" />
```
이 빈의 정의에는 특이하게도 id 애트리뷰트가 없고 class뿐이다. 다른 빈에서 참조되거나 코드에서 빈 이름으로
조회될 필요가 없는 빈이라면 아이디를 등록하지 않아도 무방한다.

### 포인트컷 등록

포인트컷 빈
```xml
<bean id="transactionPointcut" class="springbook.service.NameMatchClassMethodPointcut" >
    <property name="mappedClassName" value="*ServiceImpl" /> ##클래스 이름 패턴
    <property name="mappedName" value="upgrade" />  ## 메소드 이름패턴
</bean>
```

### 어드바이스와 어드바이저
자동생성된 프록시에 다이내믹하게 DI 돼서 동작하는 어드바이저가 된다.

### ProxyFactoryBean 제거와 서비스 빈의 원상복구

프록시 팩토리빈을 제거한 후의 빈 설정
```xml
<bean id="userService" class="springbook.service.UserServiceImpl" >
    <property name="userDao" ref="userDao" />
    <property name="mailSender" ref="mailSender" />
</bean>
```

### 자동 프록시 생성기를 사용하는 테스트
수정한 테스트용 UserService 구현 클래스

```java
static class TestUserServiceImpl extends UserServiceImpl {  //포인트컷의 클래스 필터에 선정되도록 이름 변경
    private String id = "madnite1"; // 테스트 픽스처의 user(3) id값을 고정시켜버렸다.
    
    protected void upgradeLevel(User user){
        if(user.getId().equals(this.id)) throw new TestUserServiceException();
        super.upgradeLevel(user);
    }
}
```