#  Capistrano : Ruby, Rake 및 SSH를 기반으로하는 배포 자동화 도구.
Capistrano는 자동 배포 스크립트를 작성하기위한 프레임 워크입니다. </br>
Capistrano 자체는 Ruby로 작성되었지만 Rails, Java 또는 PHP와 같은 언어 또는 프레임 워크의 프로젝트를 배포하는 데 쉽게 사용할 수 있습니다. </br>

Capistrano를 설치하면 커맨드 라인의 편리함에서 배포를 수행 할 수있는 cap 툴을 제공합니다.
```
 $ cd my-capistrano-enabled-project $ cap production deploy 
```
cap 을 실행하면 Capistrano는 SSH를 통해 서버에 충실하게 연결하고 프로젝트를 배포하는 데 필요한 단계를 실행합니다. </br>
Rake 태스크를 작성하거나 Capistrano 커뮤니티에서 제공하는 사전 빌드 된 태스크 라이브러리를 사용하여 이러한 단계를 직접 정의 할 수 있습니다. </br>

작업은 간단합니다. 다음은 그 예입니다.

```ruby
task :restart_sidekiq do
  on roles(:worker) do
    execute :service, "sidekiq restart"
  end
end
after "deploy:published", "restart_sidekiq"
```

*참고 :이 문서는 현재 버전의 Capistrano (3.x) 용입니다.*

---

## Contents

* [Features](#features)
* [Gotchas](#gotchas)
* [Quick start](#quick-start)
* [Finding help and documentation](#finding-help-and-documentation)
* [How to contribute](#how-to-contribute)
* [License](#license)

## Features
간단한 rsync bash 스크립트에서 복잡한 컨테이너 도구 체인에 이르기까지 여러 가지 방법으로 배포를 자동화 할 수 있습니다. Capistrano는 중간에 위치합니다. SSH를 사용하여 수동으로 수행하는 방법을 이미 알고 있지만 반복 가능하고 확장 가능한 방식으로 자동화합니다. 여기에 마법은 없습니다!

카피스트라노가 훌륭한 이유는 다음과 같습니다.

#### Strong conventions 강력한 규칙
Capistrano는 기본적으로 모든 Capistrano 지원 프로젝트가 따르는 표준 배포 프로세스를 정의합니다. 스크립트를 구성하는 방법, 배포 된 파일을 서버에 배치하는 방법 또는 일반적인 작업을 수행하는 방법을 결정할 필요가 없습니다. Capistrano가 이 작업을 수행했습니다.

#### Multiple stages 
배포를 한 번 정의한 다음 `qa` , `staging` 및 `production` 과 같은 여러 단계 (환경)에 맞게 쉽게 매개 변수화 할 수 있습니다. 복사하여 붙여 넣기는 필요하지 않습니다. IP 주소와 같이 각 단계마다 다른 내용 만 지정하면됩니다.

#### Parallel execution 병렬 실행
앱 서버에 배포 하시겠습니까? Capistrano는 각 서버에서 각 배포 작업을 동시에 실행할 수 있으며 속도를 위해 연결 풀링을 사용합니다.

#### Server roles 서버 역할
응용 프로그램에는 데이터베이스 서버, 응용 프로그램 서버, 두 개의 웹 서버 및 작업 대기열 작업 서버와 같이 다양한 유형의 서버가 필요할 수 있습니다. Capistrano를 사용하면 하나 이상의 역할이있는 각 서버에 태그를 지정하여 실행할 작업을 제어 할 수 있습니다.

#### Community driven 
Capistrano는 rubygems 패키지 관리자를 사용하여 쉽게 확장 할 수 있습니다. Rails 애플리케이션을 배포 하시겠습니까?  Wordpress? Laravel? 누군가 당신의 선택 틀을 위해 이미 카피 스트라 노 작업을 작성하고 그것을 gem으로 배포했을 가능성이 있습니다. 많은 Ruby 프로젝트에는 Capistrano 태스크가 내장되어 있습니다.

#### It's just SSH
Capistrano의 모든 것은 원격 서버에서 SSH 명령을 실행하는 것으로 귀결됩니다. 한편으로는 카피스트라노가 단순해진다. 다른 한편으로는, 당신이 리눅스 박스에 SSH-ing하고 명령 행에 물건을 넣는 것을 편안하게하지 않는다면, 카피스트라노는 아마 당신을위한 것이 아닙니다.

## Gotchas
Capistrano는 모든 유형의 배포에 공통적으로 적용되는 강력한 규칙을 제공하지만 프로젝트의 특성을 이해하는 데 도움이 필요하며 Capistrano는 적합하지 않은 몇 가지 사항이 있습니다.

#### Project specifics 프로젝트 세부 사항
상자 밖에서 Capistrano는 서버에 코드를 배포 할 수 있지만 코드 실행 방법을 알지 못합니다. `foreman` 이 달릴 필요가 있습니까? 아파치를 다시 시작해야합니까? 카피스트라노에게 이 배포 단계를 직접 작성하거나 카피스트라노 커뮤니티에서 gem을 찾는 방법으로 이 부분을 수행하는 방법을 설명해야합니다.

#### Key-based SSH 키 기반 SSH
Capistrano는 키 기반 (즉, 암호가없는) 인증을 사용하여 SSH로 서버에 연결하는 방법에 따라 다릅니다. Capistrano를 사용하려면 먼저이 기능이 필요합니다.

#### Provisioning
마찬가지로 배포를 수행하려면 서버에 지원 소프트웨어가 설치되어 있어야합니다. Capistrano 자체는 SSH 이외의 다른 요구 사항은 없지만 데이터베이스 소프트웨어, Apache 또는 Nginx와 같은 웹 서버, Java, Ruby 또는 PHP와 같은 언어 런타임이 필요할 것입니다. 이러한 서버 프로비저닝 단계는 Capistrano가 수행하지 않습니다.

#### `sudo`, etc.
Capistrano는 대화 형 이 아닌 SSH 세션을 사용하여 권한이없는 단일 SSH 사용자를 사용하여 배포하도록 설계되었습니다. 배포시 sudo , 대화 형 프롬프트가 필요하지만 한 사용자로 인증하지만 다른 명령으로 명령을 실행하는 경우 Capistrano를 사용하여 이를 수행 할 수는 있지만 어려울 수 있습니다. 이러한 요구 사항을 피할 수 있으면 자동화 된 배포가 훨씬 원활 해집니다.

#### Shells
Capistrano 3는 Bash 나 Sh와 같은 POSIX 쉘을 기대합니다. tcsh, csh와 같은 쉘은 작동하지만 아마 그렇지 않을 것입니다.

## 참고
 [https://github.com/capistrano/capistrano]
