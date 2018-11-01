# Bundler
Bundler는 정확히 필요한 gem과 그 gem의 버전을 설치하고, 추적하는 것으로 일관성 있는 Ruby 프로젝트를 제공하는 도구다. 
그 기본적인 사용법에 대해 알아보겠다.


## 번들러의 목적과 근거
먼저, 이 의존성을 애플리케이션의 루트에 **Gemfile** 이란 파일로 정의했습니다. 내용은 이렇습니다.
```sh
source 'https://rubygems.org'

gem 'rails', '4.1.0.rc2'
gem 'rack-cache'
gem 'nokogiri', '~> 1.6.1'
```
이 Gemfile은 몇 가지 이야기를 합니다. 
먼저 번들러는 Gemfile 안에 선언된 gem을 기본 값으로 설정된 https://rubygems.org에서 찾아야 합니다. 
사설 gem 서버에서 가져와야 하는 gem이 있다면, 이 기본 소스는 그 gem에서만 덮어 쓸 수 있습니다.

다음으로, 몇 가지 의존성을 선언했습니다.
- rails의 4.1.0.rc2 버전
- rack-cache의 아무 버전
- nokogiri의 >= 1.6.1지만 < 1.7.0인 버전
첫 번째 의존성을 선언한 후에 번들러에게 받도록 해야합니다.
```sh
$ bundle install    # 'bundle'은 'bundle install'의 단축 명령입니다.
```
번들러는 rubygems.org(또는 선언한 다른 소스)에 연결하고 기술한 요구사항에 따라 요구된 모든 gem의 목록을 찾을 것입니다. 
Gemfile에 있는 모든 gem은 각각의 의존성을 가지고 (또 그 의존성이 다른 의존성을 가질 수) 있기 때문입니다. 
bundle install을 실행하면 위의 Gemfile은 꽤 적은 gem을 설치합니다

필요한 gem이 이미 설치되어 있다면, 번들러는 그것을 사용할 것입니다. 
필요한 gem을 시스템에 설치 후, 번들러는 설치된 모든 gem과 버전 스냅숏을 Gemfile.lock에 적습니다.


## 간단한 번들러 작업 흐름

레일스 애플리케이션을 처음 만들 때, 이미 Gemfile이 들어있습니다. 시나트라 같은 다른 애플리케이션은 다음을 실행하세요.
```sh
$ bundle init
```

bundle init 명령은 수정할 수 있는 간단한 Gemfile을 만듭니다.

그런 다음, 애플리케이션이 의존할 gem을 추가하세요. 필요한 특정 gem의 버전을 신경써야 한다면, 적절한 버전 제약을 하세요.
```sh
source 'https://rubygems.org'

gem 'sinatra', '~> 1.3.6'
gem 'rack-cache'
gem 'rack-bug'
```

아직 시스템에 설치된 gem이 없다면, 다음을 실행하세요.
```sh
$ bundle install
```

gem의 버전 요구사항을 업데이트하려면, 먼저 Gemfile을 수정하세요.
```sh
source 'https://rubygems.org'

gem 'sinatra', '~> 1.4.5'
gem 'rack-cache'
gem 'rack-bug'
```

그리고 다음을 실행하세요.
```sh
$ bundle install
```

bundle install이 Gemfile과 Gemfile.lock이 충돌한다는 보고를 하면 다음을 실행하세요.
```sh
$ bundle update sinatra
```
이는 시나트라 gem과 그 의존성만 업데이트합니다.

Gemfile에 있는 모든 gem을 가능한 최신 버전으로 업데이트 하려면, 다음을 실행하세요.
```sh
$ bundle update
```
Gemfile.lock이 변경될 때마다 버전 관리 시스템에 넣으세요. 
이는 애플리케이션을 성공적으로 실행하는데 필요한 모든 서드 파티 코드의 정확한 버전의 이력을 남기게 합니다.


스테이징이나 프로덕션 서버에 코드를 배포할 때, 먼저 테스트를 실행하거나 지역 개발 서버에서 기동해 보고, 
버전 관리 시스템에 Gemfile.lock이 들어있는지 확인하세요. 원격 서버에서 다음을 실행 하세요.
```sh
$ bundle install --deployment
```
