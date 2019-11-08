# 19장 정책과 수준
소프트웨어 시스템이란 정책을 기술한 것이다.
실제로 컴퓨터 프로그램의 핵심부는 이게 전부다.
컴퓨터 프로그램은 각 입력을 출력으로 변환하는 정책을 상세하게 기술한 설명서다.

소프트웨어 아키텍처를 개발하는 기술에는 이러한 정책을 신중하게 분리하고, 정책이 변경되는 양상에 따라 정책을 재편성하는 일도 포함된다.
동일한 이유로 동일한 시점에 변경되는 정책은 동일한 수준에 위치하며, 동일한 컴포넌트에 속해야 한다.
서로 다른 이유로, 혹은 다른 시점에 변경되는 정책은 다른 수준에 위치하며, 반드시 다른 컴포넌트로 분리해야 한다.

## 수준
`수준(level)`을 엄밀하게 정의하자면 `입력과 출력까지의 거리`다. 
시스템의 입력과 출력 모두로부터 멀리 위치할수록 정책의 수준은 높아진다.
입력과 출력을 다루는 정책이라면 시스템에서 최하위 수준에 위치한다.

소스 코드 의존성은 그 수준에 따라 결합되어야 하며, 데이터 흐름을 기준으로 결합되어서는 안 된다.

```javascript
function encrypt(){
  while(true)
    writeChar(translate(readChar()));
}    
```
이는 잘못된 아키텍처다.
고수준인 encrypt 함수가 저수준이 readChar와 writeChar함수에 의존하기때문

## 결론
정책에 대한 논의는 단일 책임 원칙, 개방 폐쇄 원칙, 공통 폐쇄 원칙, 의존성 역전 원칙, 안정된 의존성 원칙, 안정된 추상화 원칙을 모두 포함한다.