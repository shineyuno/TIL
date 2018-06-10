# 1.8 XML을 이용한 설정

## 1.8.1 XML 설정
@Bean 메소드를 통해 얻을수 있는 빈의 DI 정보
* 빈의 이름 : @Bean 메소드 이름이 빈의 이름이다. 이 이름은 getBean()에서 사용된다.
* 빈의 클래스 : 빈 오브젝트를 어떤 클래스를 이용해서 만들지를 정의한다.
* 빈의 의존 오브젝트 : 빈의 생성자나 수정자 메소드를 통해 의존 오브젝트를 넣어준다. 의존오브젝트도 하나의 빈이므로
이름이 있을 것이고, 그 이름에 해당하는 메소드를 호출해서 의존 오브젝트를 가져온다. 의존 오브젝트는 하나 이상일 수도 있다.

#### 클래스 설정과 XML 설정의 대응항목

|   | 자바 코드 설정 정보 | XML 설정정보         |
| :------------ | :----------- | :-- |
| 빈 설정파일     | @Configuration          | <beans> |
| 빈의 이름      | @Bean methodName()      | <bean id="methodName"|
| 빈의 클래스    | return new BeanClass()  | class="a.b.c.. BeanClass">  |

<bean> 태그의 class 애트리뷰트에 지정하는 것은 자바 메소드에서 오브젝트를 만들 때 사용하는 클래스 이름이라는 점에 주의하자
메소드의 리턴 타입을 class 애트리뷰트에 사용하지 않도록 하자. XML에서는 리턴하는 타입을 지정하지 않아도 된다.


#### XML의 의존관계 주입 정보
<property> 태그의 name과 ref는 그 의미가 다르므로 이름이 같더라도 어떤차이가 있는지 구별할수 있어야 한다.
name 애트리뷰트는 DI에 사용할 수정자 메소드의 프로퍼티 이름이며, ref 애트리뷰트는 주입할 오브젝트를 정의한 빈의 ID다.

#### DTD와 스키마
XML 문서는 미리 정해진 구조를 따라서 작성됐는지 검사할 수 있다. XML 문서의 구조를 정의하는 방법에는
DTD와 스키마(schema)가 있다. 스프링의 XML 설정파일은 이 두 가지 방식을 모두 지원한다.

## 1.8.4 프로퍼티 값의 주입
텍스트나 단순 오브젝트 등을 수정자 메소드에 넣어주는 것을 스프링에서는 '값을 주입한다'고 말한다.
이것도 성격은 다르지만 일종의DI라고 볼수 있다.


#### 코드를 통한 DB연결정보 주입
```java
dataSource.setDriverClass(com.mysql.jdbc.Driver.class);
dataSource.setUrl("jdbc:mysql://localhost/springbook");
dataSource.setUsername("spring");
dataSource.setPassword("book");
```

#### XML을 이용한 DB연결정보 설정
```xml
<property name="driverClass" vlaue="com.mysql.jdbc.Driver" />
<property name="url" value="jdbc:mysql://localhost/springbook" />
<property name="username" value="spring" />
<property name="password" value="book" />
```

스프링은 value에 지정한 텍스트값을 적절한 자바 타입으로 변환해준다.
