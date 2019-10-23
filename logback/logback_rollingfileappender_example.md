# Logback RollingFileAppender Example
이 예제에서는 Logback RollingFileAppender를 설명하고 간단한 예제에서 RollingFileAppender를 실행하는 방법으로 넘어갑니다. 
Logback은 현대적이고 빠르고 유연한 로깅 프레임 워크입니다. 
Logback의 RollingFileAppender는 엔터프라이즈 세계에서 가장 많이 사용되는 Appender 중 하나입니다. 
로그 파일을 롤오버하는 기능으로 FileAppender를 확장합니다. 

## 1. 롤링 FileAppender
FileAppender를 사용하면 이벤트 메시지가 로그 파일 끝에 지속적으로 추가됩니다. 
로그 파일의 크기가 점차 증가합니다.
RollingFileAppender를 적용하면 일별, 주별, 월별과 같은 특정 일정에 따라 일반 로그 파일이 여러 개로 분할됩니다. 
매일 구성한다고 가정하면 로그 파일 목록은 다음과 같습니다.
```
example.log.2015-04-30
example.log.2015-05-01
example.log.2015-05-02
example.log.2015-05-03
…
example.log
```
로그 파일은 매일 롤아웃되며 이름에 날짜가없는 파일이 현재 로그 파일입니다. 
하루가 끝나면 현재 로그 파일 (example.log)이 이름에 날짜 정보가있는 파일로 백업됩니다. 
예를 들어“example.log.2015-05-03”입니다. 
그리고“example.log”는 새로운 날의 로깅 파일이됩니다.

이 유용하고 효과적인 기술로 로그 파일의 크기가 더 작습니다.
이전에 생성 된 로그 파일을 삭제하거나 다른 디스크 공간으로 이동할 수 있습니다. 
문제가 발생하면 관련 로그 파일 만 검사하여 신속하게 식별 할 수 있습니다.

### 1.1 RollingFileAppender의 구성
RollingFileAppender는 FileAppender를 확장합니다. 
따라서“file”,“encoder”,“append”및“prudent”속성은 FileAppender에 상속됩니다. 
RollingFileAppender와 관련된 다른 속성은 rollingPolicy 및 triggeringPolicy입니다. 
RollingPolicy는 롤오버에 필요한 작업을 수행 할 책임이 있습니다. 
TriggeringPolicy는 언제 롤오버가 발생하는지 정확하게 결정합니다. 
따라서 RollingPolicy는 무엇을 담당하고 TriggeringPolicy는 언제를 책임집니다.

TimeBasedRollingPolicy는 아마도 가장 널리 사용되는 롤링 정책 일 것입니다. 
시간을 기준으로 롤오버 정책을 정의합니다. 다음과 같은 속성이 필요합니다.

| Property Name       | Type    | Mandatory? | Description                                                                                                                                                                                                                                                                                                                                                                                                                        |
|---------------------|---------|------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| fileNamePattern     | String  | Yes        | 롤오버 된 (아카이브 된) 로그 파일의 이름을 정의합니다. 값은 파일 이름과 날짜 및 시간 패턴을 포함 할 수있는 적절하게 배치 된 % d 변환 지정자로 구성되어야합니다. % d {}의 찬사에서 발견되는 날짜 및 시간 패턴은 java.text.SimpleDateFormat 규칙을 따릅니다. fileNamePattern 속성 또는 날짜 및 시간 패턴 내에서 슬래시‘/’또는 백 슬래시‘\’문자는 디렉토리 구분 기호로 해석됩니다. 롤오버 기간은 fileNamePattern의 값에서 유추됩니다. |
| maxHistory          | int     | No         | 보관할 최대 보관 파일 수를 제어하여 오래된 파일을 삭제합니다. 예를 들어, 월별 롤오버를 지정하고 maxHistory를 8로 설정하면 8 개월 이상의 아카이브 파일이 8 개월보다 오래된 파일이 삭제 된 상태로 유지됩니다.                                                                                                                                                                                                                        |
| cleanHistoryOnStart | boolean | No         | true로 설정하면 어 펜더 시작시 아카이브가 제거됩니다. 기본적으로이 속성은 false로 설정되어 있습니다.                                                                                                                                                                                                                                                                                                                               |                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |

다음은 설명이 포함 된 fileNamePattern 값입니다.

| fileNamePattern                     | Rollover Schedule | Description                                                                                                                                                                                    |
|-------------------------------------|-------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| /logs/example.%d                    | Daily rollover    | % d 토큰 지정자의 시간 및 날짜 패턴이 생략되었습니다. 따라서 기본 패턴은 "yyyy-MM-dd"이며 이는 매일 롤오버에 해당합니다.                                                                       |
| /logs/example.%d{yyyy-MM}.log       | Monthly rollover  | 롤오버 된 파일은 다음과 같습니다. example.2015-03.log, example.2015-04.log                                                                                                                     |
| /logs/example.%d{yyyy-MM-dd_HH}.log | Hourly rollover   | 롤오버 된 파일은 다음과 같습니다. example.2015-05-17_09.log, example.2015-05-17_10.log                                                                                                         |
| /logs/%d{yyyy/MM}/example.log       | Monthly rollover  | 날짜 및 시간 패턴은 디렉토리 이름에 있습니다. 현재 로그 파일 경로는 "logs / example.log"입니다. 롤오버 된 파일의 디렉토리는“logs / 2015 / 04 / example.log”,“logs / 2015 / 03 / example.log”,… |
| /logs/example.%d{yyyy-ww}.zip       | Weekly rollover   | 파일 패턴은 "zip"으로 끝납니다. 따라서 롤오버 된 파일이 압축됩니다. fileNamePattern 옵션의 값이 .gz 또는 .zip으로 끝나는 경우 로그 백은 자동 파일 압축을 적용합니다.                           |

## 참고
https://examples.javacodegeeks.com/enterprise-java/logback/logback-rollingfileappender-example/
