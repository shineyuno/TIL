# 4장 구조적 프로그래밍

## 증명
goto 문장이 모듈을 더 작은 단위로 재귀적으로 분해하는 과정에 방해가 되는 경우가 있다는 사실을 발견했다.
만약 모듈을 분해할 수 없다면, 합리적으로 증명할 때 필수적인 기법인 분할 정복 접근법을 사용할 수 없게 된다.

데이크스트라는 이런 goto문의 좋은 사용방식은  if/then/else 와 do/while과 같은 분기와 반복이라는 단순한 제어 구조에 해당한다는
사실을 발견했다. 모듈이 이러한 종류의 제어 구조만을 사용한다면 증명 가능한 단위로까지 모듈을 재귀적으로 세분화하는것이 가능해 보였다.

모든 프로그램을 순차, 분기, 반복 이라는 세가지 구조만으로 표현할 수 있다는 사실을 증명했다.

이 발견은 실로 놀라웠다. 즉, 모듈을 증명 가능하게 하는 바로 그 제어 구조가 모든 프로그램을 만들 수 있는 
제어 구조의 최소 집합과 동일하다는 사실이었다. 구조적 프로그래밍은 이렇게 탄생했다.

## 해로운 성명서
현재의 우리 모두는 구조적 프로그래머이며, 여기에는 선택의 여지가 없다.
제어흐름을 제약없이 직접 전환할 수 있는 선택권 자체를 언어에서 제공하지 않기 때문이다.

## 기능적 분해
구조적 프로그래밍을 통해 모듈을 증명 가능한 더 작은 단위로 재귀적으로 분해 할수 있게 되었고,
이는 결국 모듈을 기능적으로 분해할수 있음을 뜻했다.
즉, 거대한 문제 기술서를 받더라도 문제를 고수준의 기능들로 분해할 수있다.
그리고 이들 각 기능은 다시 저수준의 함수들로 분해할 수 있고, 이러한 분해 과정을 끝없이 반복할수있다.
게다가 이렇게 분해한 기능들은 구조적 프로그래밍의 제한된 제어 구조를 이용하여 표현할 수 있다.

## 과학이 구출하다.
과학은 서술된 내용이 사실임을 증명하는 방식이 아니라 서술이 틀렸음을 증명하는 방식으로 동작한다.
각고의 노력으로도 반례를 들 수 없는 서술이 있다면 목표에 부합할 만큼은 참이라고 본다.

수학은 증명 가능한 서술이 참임을 입증하는 원리라고 볼 수 있다.
반면에 과학은 증명 가능한 서술이 거짓임을 입증하는 원리라고 볼 수 있다.

## 테스트
데이크스트라는 "테스트는 버그가 있음을 보여줄 뿐, 버그가 없음을 보여줄수는 없다"고 말한 적이 있다.

소프트웨어 개발이 수학적인 구조를 다루는 듯 보이더라도, 소프트웨어 개발은 수학적인 시도가 아니라는 사실이다.
오히려 소프트웨어는 과학과 같다. 
최선을 다하더라도 올바르지 않음을 증명하는 데 실패함으로써 올바름을 보여주기 때문이다.

이러한 부정확함에 대한 증명은 입증 가능한 프로그램에만 적용할 수 있다.

예를 들어 제약 없는 goto문을 사용하는 등의 이유로 입증이 불가능한 프로그램은 테스트를 아무리 많이 수행하더라도 절대로 올바르다고 볼수없다.

## 결론
구조적 프로그래밍이 오늘날까지 가치 있는 이유는 프로그래밍에서 반증 가능한 단위를 만들어 낼 수 있는 바로 이능력 때문이다.
아키텍처 관점에서는 기능적 분해를 최고의 실천법 중 하나로 여기는 이유이기도 하다.

가장 작은 기능에서부터 가장 큰 컴포넌트에 이르기까지 모든 수준에서 소프트웨어는 과학과 같고,
따라서 반증 가능성에 의해 주도된다. 
소프트웨어 아키텍트는 모듈, 컴포넌트, 서비스가 쉽게 반증 가능하도록(테스트하기 쉽도록) 만들기 위해 분주히 노력해야 한다.
이를 위해 구조적 프로그래밍과 유사한 제한적인 규칙들을 받아들여 활용해야 한다.

