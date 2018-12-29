# 7.3 서비스 추상화 적용
## 7.3.1 OXM 서비스 추상화
XML과 자바 오브젝트를 매핑해서 상호 변환해주는 기술을 간단히 OXM Object-XML-Mapping이라고도 한다.

### OXM 서비스 인터페이스
스프링이 제공하는 OXM 추상화 서비스 인터페이스에는 자바오브젝트를 XML로 변환하는 Marshaller와, 
반대로 XML을 자바오브젝트로 변환하는 Unmarshaller가 있다.

### JAXB 구현 테스트
JAXB를 이용하도록 만들어진 Unmarshaller 구현클래스는 Jaxb2Marshaller다.

리스트 7-46 JAXB용 Unmarshaller 빈설정
```xml
<bean id="unmarshaller" class="org.springframework.oxm.jaxb.Jaxb2Marshaller" >
  <property name="contextPath" value="springbook.user.sqlservice.jaxb" />
</bean>  
``` 
Jaxb2Marshaller 클래스를 빈으로 등록하고 바인딩 클래스의 패키지 이름을 지정하는 프로퍼티인 contextPath만 넣어주면 된다.

unmarshaller 빈은 Unmarshaller 타입이다.
따라서 스프링 컨텍스트 테스트의 @Autowired를 이용해 Unmarshaller 타입의 인스턴스 변수를 선언해주면 빈을 가져올수 있다.
