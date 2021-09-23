# 24장 부분분 경계
아키텍처 경계를 완벽하게 만드는 데는 비용이 많이든다.
쌍방향의 다형적 Boundary 인터페이스, Input과 Output을 위한 데이터 구조를 만들어야 할 뿐만 아니라, 
두영역을 독립적으로 컴파일하고 배포할 수 있는 컴포넌트로 격리하는데 필요한 모든 의존성을 관리해야 한다.

애자일 커뮤니티 
- 선행적인 설계 탐탁치 않음
- YAGNI You Aren't Going to Need it 원칙 위배 - 필요한 작업만 해라

하지만 어쩌면 필요할지도
- 만약 그렇다면 부분적 경계 partial boundary

## 마지막 단계를 건너뛰기
부분적 경계를 생성하는 방법 하나는 독립적으로 컴파일하고 배포할수 있는 컴포넌트를 만들기 위한 작업은 모두 수행한후,
단일 컴포넌트에 그대로 모아만 두는 것이다.
쌍방향 인터페이스도 그 컴포넌트에 있고, 입력.출력 데이터 구조도 거기에 있으며, 모든것이 완전히 준비되어 있다.
하지만 이 모두를 단일 컴포넌트로 컴파일해서 배포한다.

다수의 컴포넌트를 관리하는 작업은 하지 않아도 된다.
추적을 위한 버전 번호도 없으며, 배포 관리 부담도 없다.

## 일차원 경계
전략(Strategy) 패턴을 사용한 사례

## 퍼사드
훨씬 더 단순한 경계는 퍼사드(Facace)패턴
의존성 역전까지도 희생.
경계는 Facade 클래스로만 간단히 정의된다.
Facade 클래스에는 모든 서비스 클래스를 메서드 형태로 정의하고, 서비스 호출이 발생하면 해당 서비스 클래스로 호출을 전달한다.
클라이언트는 이들 서비스 클래스에 직접 접근할 수 없다.

## 결론
각 접근법은 해당 경계가 실제로 구체화되지 않으면 가치가 떨어질 수 있다.
아키텍처 경계가 언제, 어디에 존재해야 할지, 그리고 그 경계를 완벽하게 구현할지 아니면 부분적으로 구현할지를 결정하는 일 또한 아키텍트의 역할이다.