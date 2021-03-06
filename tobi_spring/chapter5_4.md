## 5.4 메일 서비스 추상화


#### 테스트와 서비스 추상화
일반적으로 서비스 추상화라고 하면 트랜잭션과 같이 기능은 유사하나 사용 방법이 다른 로우레벨의 다양한 기술에 대해
추상 인터페이스와 일관성있는 접근방법을 제공해주는 것을 말한다.
반면에 JavaMail의 경우처럼 테스트를 어렵게 만드는 건전하지 않은 방식으로 설계된 API를 사용할 때도 유용하게 쓰일 수 있다.

서비스 추상화란 이렇게 원활한 테스트만을 위해서도 충분히 가치가 있다. 기술이나 환경이 바뀔 가능성이 있음에도,
JavaMail처럼 확장이 불가능하게 설계해놓은 API를 사용해야 하는 경우라면 추상화 계층의 도입을 적극 고려해볼 필요가 있다.
특별히 외부의 리소스와 연동하는 대부분 작업은 추상화의 대상이 될 수 있다.

### 5.4.4 테스트 대역
테스트 대상이 사용하는 의존 오브젝트를 대체할 수 있도록 만든 오브젝트를 테스트 대역(test double)이라고 한다.

테스트 대역은 테스트 대상 오브젝트가 원활하게 동작할수 있도록 도우면서 테스트를 위해 간접적인 정보를 제공해주기도 한다.

테스트 대역중에서 테스트 대상으로부터 전달받은 정보를 검증할 수 있도록 설계된 것을 목 오브젝트라고 한다.
