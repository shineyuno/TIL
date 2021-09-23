# 22장 클린아키텍처
시스템 아키텍처와 관련된 여러가지 아이디어
+ 육각형 아키텍처 : 포트와 어댑터
+ DCI (Data, Context and Interaction)
+ BCE (Boundary-Control-Entity)

목표는 관심사의 분리 separation of concerns
소프트웨어를 계층으로 분리함으로써 관심사의 분리라는 목표를 달성
각 아키텍처는 최소한 업무규칙을 위한 계층하나와, 사용자와 시스템 인터페이스를 위한 또 다른 계층 하나를 반드시 포함한다.

이들 아키텍처는 모두 시스템이 다음과 같은 특징을 지니도록 만든다.
+ 프레임워크 독립성. 아키텍처는 다양한 기능의 라이브러리를 제공하는 소프트웨어, 즉 프레임워크의 존재 여부에 의존하지 않는다.
이를 통해 이러한 프레임워크를 도구로 사용할수 있으며, 프레임워크가 지닌 제약사항안으로 시스템을 욱여 넣도록 강제하지 않는다.
+ 테스트 용이성. 업무규칙은 UI, 데이터베이스, 웹 서버, 또는 여타 외부 요소가 없이도 테스트할 수 있다.
+ UI독립성.
+ 데이터베이스 독립성. 업무규칙은 데이터베이스에 결합되지 않는다.
+ 모든 외부 에이전시에 대한 독립성. 실제로 업무규칙은 외부 세계와의 인터페이스에 대해 젼혀 알지 못한다.


#### 클린아키텍처
장치,웹,UI, DB, 외부 인터페이스 > 컨트롤러,게이트웨이, 프레젠터 > 유스케이스 > 엔티티

## 의존성규칙
아키텍처가 동작하도록 하는 가장 중요한 규칙은 의존성 규칙이다.
소스코드 의존성은 반드시 안쪽으로, 고수준의 정책을 향해야 한다.

## 엔티티
엔티티는 전사적인 핵심업무 규칙을 캡슐화 한다.

## 유스케이스
유스케이스 계층의 소프트웨어는 애플리케이션에 특화된 업무규칙을 포함한다.

## 인터페이스 어댑터
인터페이스 어댑터(Interface Adapter)계층은 일련의 어댑터들로 구성된다.
어댑터는 데이터를 유스케이스와 엔티티에게 가장 편리한 형식에서 데이터베이스나 웹 같은 외부 에이전시에게 가장 편리한 형식으로 변환한다.
이 계층은, 예를 들어 GUI의 MVC 아키텍처를 모두 포괄한다.
프레젠터, 뷰, 컨트롤러는 모두 인터페이스 어댑터 계층에 속한다.
모델은 그저 데이터 구조 정도에 지나지 않으며, 컨트롤러에서 유스케이스로 전달되고, 다시 유스케이스에서 프레젠터와 뷰로 되돌아 간다.

## 프레임워크와 드라이버
프레임워크와 드라이버 계층은 모두 세부사항이 위치하는 곳이다. 
웹은 세부사항이다.
데이터베이스는 세부사항이다.
우리는 이러한 것들을 모두 외부에 위치시켜서 피해를 최소화한다.

## 경계 횡단하기
제어흐름과 의존성의 방향이 명백히 반대여야 하는 경우, 대체로 의존성 역전 원칙을 사용하여 해결한다.
예를 들어 자바 같은 언에에서는 인터페이스와 상속관계를 적절하게 배치함으로써, 제어흐름이 경계를 가로지르는 바로 그 지점에서 소스 코드
의존성을 제어흐름과는 반대가 되게 만들수 있다.
예를 들어 유스케이스에서 프레젠터를 호출해야 한다고 가정해 보자.
이때 직접 호출해서는 안되는데, 직접 호출해 버리면 의존성 규칙(내부의 원에서는 외부 원에 있는 어떤 이름도 언급해서는 안 된다.)을 위배하기 때문이다.
따라서 우리는 유스케이스가 내부 원의 인터페이스를 호출하도록 하고, 외부 원의 프레젠터가 그 인터페이스를 구현하도록 만든다.

## 경계를 횡단하는 데이터는 어떤 모습인가
경계를 가로질러 데이터를 전달할 때, 데이터는 항상 내부의 원에서 사용하기에 가장 편리한 형태를 가져야만 한다.

## 결론 
소프트웨어를 계층으로 분리하고 의존성 규칙을 준수한다면 본질적으로 테스트하기 쉬운 시스템을 만들게 될 것이며,
그에 따른 이점을 누릴 수 있다. 데이터베이스나 웹 프레임워크와 같은 시스템의 외부요소가 구식이 되더라도, 이들 요소를 야단스럽지 않게 교체할 수 있다.