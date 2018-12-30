# Resque
Resque는 background jobsd을 생성하고 여러 대기열에 배치 한 다음 나중에 처리하기위한 Redis 지원 루비 라이브러리입니다. 
http://resque.github.io/


백그라운드 작업은 perform 을  응답하는 모든 Ruby 클래스 또는 모듈 일 수 있습니다. 
기존 클래스를 백그라운드 작업으로 쉽게 변환하거나 작업을 수행 할 수 있도록 새 클래스를 만들 수 있습니다. 또는 둘 다 할 수 있습니다.

Resque는 DelayedJob의 영향을 크게받으며 세 부분으로 구성됩니다.

* 작업 생성, 질의 및 처리를위한 Ruby 라이브러리
* 작업을 처리하는 작업자를 시작하기위한 레이크 작업
* 대기열, 작업 및 작업자를 모니터링하는 Sinatra 앱.

Resque 작업자는 여러 머신간에 분산되고, 우선 순위를 지원하고,
메모리가 부풀어 오르는 / "누출"에 강하며, REE에 최적화되어 있지만 (MRI 및 JRuby에서 작동), 수행중인 작업을 알려주고 오류를 예상합니다.

Resque 대기열은 지속적입니다. 일정 시간, 원 푸시 및 팝 지원 (Redis 덕분); 내용에 대한 가시성을 제공한다. 간단한 JSON 패키지로 작업을 저장할 수 있습니다.

Resque 프론트 엔드는 작업자가 수행중인 작업, 작업자가 수행하지 않는 작업, 사용중인 대기열, 대기열에있는 작업, 일반적인 사용 통계를 제공하고 오류를 추적하는 데 도움이됩니다.

Resque는 이제 Ruby 2.3.0 이상을 지원합니다. 우리는 또한 Redis 3.0 이상을 지원

# jobs
백그라운드에서 무엇을 실행해야합니까? 시간이 걸리는 모든 것. 느린 INSERT 문, 디스크 조작, 데이터 처리 등

GitHub에서는 Resque를 사용하여 다음 유형의 작업을 처리합니다.

* Warming caches
* Counting disk usage
* Building tarballs
* Building Rubygems
* Firing off web hooks
* Creating events in the db and pre-caching them
* Building graphs
* Deleting users
* Updating our search index
글을 쓰는 현재 우리는 약 35가지의 다른 배경 직업을 가지고 있다.

Resque를 사용하기 위해서는 웹 애플리케이션이 필요 없다

