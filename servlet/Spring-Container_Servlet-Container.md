# 서블릿 컨테이너와 스프링 컨테이너

## 서블릿 컨테이너
서블릿 컨테이너는 개발자가 웹서버와 통신하기 위하여 소켓을 생성하고, 특정 포트에 리스닝하고, 스트림을 생성하는 등의 복잡한 일들을 할 필요가 없게 해준다. 컨테이너는 servlet의 생성부터 소멸까지의 일련의 과정(Lifer Cycle)을 관리한다. 서블릿 컨테이너는 요청이 들어올 때마다 새로운 자바 스레드를 만든다. 우리가 알고 있는 대표적인 Servlet Container가 Tomcat 이다. 톰켓같은 was가 java파일을 컴파일해서 Class로 만들고 메모리에 올려 servlet객체를 만든다.

Servlet 동작과정



사용자가 URL을 클릭하면 HTTP Request를 Servlet Container에 보낸다.
Servlet Container는 HttpServletRequest, HttpServletResponse 두 객체를 생성한다.
사용자가 요청한 URL을 분석하여 어느 서블릿에 대한 요청인지 찾는다.
컨테이너는 서블릿 service() 메소드를 호출하며, POST, GET여부에 따라 doGet() 또는 doPost()가 호출된다.
doGet() or doPost() 메소드는 동적인 페이지를 생성한 후 HttpServletResponse객체에 응답을 보낸다.
응답이 완료되면 HttpServletRequest, HttpServletResponse 두 객체를 소멸시킨다.

## 서블릿 생명주기
init() - 서버가 켜질때 한번만 실행
service - 모든 유저들의 요청을들을 받는다.
destroy() - 서버가 꺼질대 한번만실행



## Spring Container
먼저 Spring Container를 이해하기위해서는 IOC와 DI를 이해해야한다.

이름	설명
IoC(Inversion of Control)	개발자는 JAVA코딩시 New연산자, 인터페이스 호출,팩토리 호출방식으로 객체를 생성하고 소멸시킨다. IOC란 인스턴스의 생성부터 소멸까지의 객체 생명주기 관리를 개발자가하는 대신 스프링(컨테이너)가 관리한다
DI(Dependency Injection)	IoC를 실제로 구현하는 방법으로서 의존성있는 컴포넌트들 간의 관계를 개발자가 직접 코드로 명시하지 않고 컨테이너인 Spring이 런타임에 찾아서 연결해주게 하는 것이다.
Spring Container는 Bean들의 생명주기를 관리한다. Spring Container는 어플리케이션을 구성하는 Bean들을 관리하기위해 IOC를 사용한다. Spring 컨테이너 종류에는 BeanFactory와 이를 상속한ApplicationContext가 존재한다. 이 두개의 컨테이너로 의존성 주입된 빈들을 제어하고 관리할 수 있다. 아래는 스프링 웹어플리케이션 동작원리이다.

## 웹어플리케이션 동작원리


웹 애플리케이션이 실행되면 Tomcat(WAS)에 의해 web.xml이 loading된다.
web.xml에 등록되어 있는 ContextLoaderListener(Java Class)가 생성된다. 
ContextLoaderListener 클래스는 ServletContextListener 인터페이스를 구현하고 있으며, ApplicationContext를 생성하는 역할을 수행한다.
생성된 ContextLoaderListener는 root-context.xml을 loading한다.
root-context.xml에 등록되어 있는 Spring Container가 구동된다. 
이 때 개발자가 작성한 비즈니스 로직에 대한 부분과 DAO, VO 객체들이 생성된다.
클라이언트로부터 웹 애플리케이션이 요청이 온다.
DispatcherServlet(Servlet)이 생성된다. DispatcherServlet은 FrontController의 역할을 수행한다. 
클라이언트로부터 요청 온 메시지를 분석하여 알맞은 PageController에게 전달하고 응답을 받아 요청에 따른 응답을 어떻게 할 지 결정만한다. 
실질적은 작업은 PageController에서 이루어지기 때문이다. 이러한 클래스들을 HandlerMapping, ViewResolver 클래스라고 한다.
DispatcherServlet은 servlet-context.xml을 loading 한다.
두번째 Spring Container가 구동되며 응답에 맞는 PageController 들이 동작한다. 
이 때 첫번째 Spring Container 가 구동되면서 생성된 DAO, VO, ServiceImpl 클래스들과 협업하여 알맞은 작업을 처리하게 된다.
