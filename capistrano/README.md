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

## 참고
 [https://github.com/capistrano/capistrano]
