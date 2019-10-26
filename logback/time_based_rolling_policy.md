# TimeBasedRollingPolicy
TimeBasedRollingPolicy는 아마도 가장 널리 사용되는 롤링 정책 일 것입니다. 
시간을 기준으로 롤오버 정책을 정의합니다 (예 : 일별 또는 월별). 
TimeBasedRollingPolicy는 롤오버 및 롤오버 트리거를 담당합니다. 
실제로 TimeBasedTriggeringPolicy는 RollingPolicy 및 TriggeringPolicy 인터페이스를 모두 구현합니다.

TimeBasedRollingPolicy의 구성에는 하나의 필수 fileNamePattern 속성과 여러 선택적 속성이 필요합니다.

## fileNamePattern 
+ Property Name : fileNamePattern 
+ Type : String

### Description

필수 fileNamePattern 속성은 롤오버 (아카이브 된) 로그 파일의 이름을 정의합니다. 
그 값은 파일 이름과 적절하게 배치 된 % d 변환 지정자로 구성되어야합니다.
% d 변환 지정자에는 java.text.SimpleDateFormat 클래스에 지정된 날짜 및 시간 패턴이 포함될 수 있습니다. 
날짜 및 시간 패턴이 생략되면 기본 패턴 yyyy-MM-dd가 가정됩니다. 
**롤오버 기간은 fileNamePattern의 값에서 유추됩니다.**

RollingFileAppender (TimeBasedRollingPolicy의 부모)의 파일 속성을 설정하거나 생략 할 수 있습니다. 
포함하는 FileAppender의 파일 특성을 설정하여 활성 로그 파일의 위치와 아카이브 된 로그 파일의 위치를 분리 할 수 있습니다. 
현재 로그는 항상 파일 속성으로 지정된 파일을 대상으로합니다. 
현재 활성 로그 파일의 이름은 시간이 지나도 변경되지 않습니다. 
그러나 파일 특성을 생략하도록 선택하면 fileNamePattern의 값을 기반으로 각 기간마다 활성 파일이 새로 계산됩니다. 
아래 예제는이 점을 명확히해야합니다.

% d {}의 괄호안에서 발견되는 날짜 및 시간 패턴은 java.text.SimpleDateFormat 규칙을 따릅니다. 
fileNamePattern 속성 또는 날짜 및 시간 패턴 내에서 슬래시 '/'또는 백 슬래시 '\'문자는 디렉토리 구분 기호로 해석됩니다.

#### Multiple %d specifiers

여러 % d 지정자를 지정할 수 있지만 그 중 하나만 기본이 될 수 있습니다 (즉, 롤오버 기간을 유추하는 데 사용). 
다른 모든 토큰은 'aux'매개 변수를 전달하여 보조로 표시해야합니다 (아래 예 참조).

여러 % d 지정자를 사용하면 롤오버 기간과 다른 폴더 구조로 아카이브 파일을 구성 할 수 있습니다. 
예를 들어 아래에 표시된 파일 이름 패턴은 연도 및 월별로 로그 폴더를 구성하지만 매일 자정에 롤오버 로그 파일을 구성합니다.

/var/log/%d{yyyy/MM, aux}/myapplication.%d{yyyy-MM-dd}.log

#### 타임 존
특정 상황에서는 호스트와 다른 시간대의 시계에 따라 로그 파일을 롤오버 할 수 있습니다. 
% d 변환 지정자 내에서 날짜 및 시간 패턴 뒤에 시간대 인수를 전달할 수 있습니다. 
예를 들면 다음과 같습니다.

aFolder/test.%d{yyyy-MM-dd-HH, UTC}.log

지정된 시간대 식별자를 알 수 없거나 철자가 틀린 경우 GMT 시간대는 
TimeZone.getTimeZone (String) 메소드 스펙에 의해 지정된 것으로 간주됩니다.

## maxHistory

+ Property Name : maxHistory 
+ Type : int

### Description
선택적 maxHistory 특성은 보관할 최대 아카이브 파일 수를 제어하여 이전 파일을 비동기 적으로 삭제합니다. 
예를 들어, 월별 롤오버를 지정하고 maxHistory를 6으로 설정하면 6 개월 이상의 아카이브 파일이 6 개월보다 
오래된 파일이 삭제 된 상태로 유지됩니다. 
오래된 보관 로그 파일이 제거되므로 로그 파일 보관을 위해 생성 된 모든 폴더가 적절하게 제거됩니다.



## totalSizeCap

+ Property Name : totalSizeCap
+ Type : int

### Description
선택적 totalSizeCap 특성은 모든 아카이브 파일의 전체 크기를 제어합니다. 
총 크기 제한을 초과하면 가장 오래된 아카이브가 비동기 적으로 삭제됩니다. 
totalSizeCap 속성에는 maxHistory 속성도 설정해야합니다. 
또한 "max history"제한이 항상 먼저 적용되고 "total size cap"제한이 두 번째로 적용됩니다.

## cleanHistoryOnStart

+ Property Name : cleanHistoryOnStart
+ Type : boolean

### Description

true로 설정하면 어펜더 시작시 아카이브 제거가 실행됩니다. 
기본적으로이 속성은 false로 설정되어 있습니다.

아카이브 제거는 일반적으로 롤오버 중에 수행됩니다. 
그러나 일부 응용 프로그램은 롤오버가 트리거 될 정도로 오래 작동하지 않을 수 있습니다. 
따라서 수명이 짧은 응용 프로그램의 경우 아카이브 제거가 실행 기회를 얻지 못할 수 있습니다. 
cleanHistoryOnStart를 true로 설정하면 어펜더 시작시 아카이브 제거가 수행됩니다.


## fileNamePattern 값에 따른 그 효과에 대한 설명
+ fileNamePattern : /foo/%d{yyyy-MM,aux}/%d.log

### Rollover schedule
매일 롤오버. 연도와 월이 포함 된 폴더 아래에있는 아카이브

### Example
이 예에서 첫 번째 % d 토큰은 보조로 표시됩니다. 
그런 다음 시간 및 날짜 패턴이 생략 된 두 번째 % d 토큰이 기본으로 간주됩니다. 
따라서 롤오버는 매일 발생하며 (% d의 기본값) 폴더 이름은 연도 및 월에 따라 다릅니다. 
예를 들어 2006 년 11 월에는 보관 된 파일이 모두 /foo/2006-11/ 폴더 아래에 배치됩니다 

(예 : /foo/2006-11/2006-11-14.log )
