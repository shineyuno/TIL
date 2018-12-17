# INFO [섹션]
1.0.0부터 사용 가능합니다.

INFO 명령은 컴퓨터로 구문 분석하기 쉽고 사람이 쉽게 읽을 수있는 형식으로 서버에 대한 정보와 통계를 반환합니다.

선택적 매개 변수를 사용하여 특정 정보 섹션을 선택할 수 있습니다.

* server : Redis 서버에 대한 일반 정보
* clients : 클라이언트 연결 섹션
* memory : 메모리 소비 관련 정보
* persistence : RDB 및 AOF 관련 정보
* stats : 일반 통계
* replication : 마스터 / 복제본 복제 정보
* cpu : CPU 소비 통계
* commandstats : Redis 명령 통계
* cluster : Redis 클러스터 섹션
* keyspace : 데이터베이스 관련 통계

또한 다음 값을 사용할 수 있습니다.
* all : 모든 섹션 반환
* default : 섹션의 기본 세트 만 반환합니다.
매개 변수가 제공되지 않으면 default 옵션으로 간주됩니다.

반환 값
대량 문자열 응답 : 텍스트 줄의 모음입니다.

행은 섹션 이름 (# 문자로 시작) 또는 특성을 포함 할 수 있습니다. 
모든 속성은 \r\n 의해 종료 된 field:value 의 field:value 형식입니다.


## 통계 섹션의 필드
* instantaneous_ops_per_sec : 초당 처리 된 명령의 수
