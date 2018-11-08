# 시작 Redis

## 가장간단한 redis 서버 실행하는법
Redis 서버를 시작하는 가장 간단한 방법은 인수없이 Redis 서버 바이너리를 실행하는 것입니다.
```sh
$ ./redis-server
[28550] 01 Aug 19:29:28 # Warning: no config file specified, using the default config. In order to specify a config file use 'redis-server /path/to/redis.conf'
[28550] 01 Aug 19:29:28 * Server started, Redis version 2.2.12
[28550] 01 Aug 19:29:28 * The server is now ready to accept connections on port 6379
... more logs ...
```
위의 예에서 Redis는 명시 적 구성 파일없이 시작되었으므로 **모든 매개 변수가 내부 기본값을 사용** 합니다. 

## redis redis.conf 구성파일로 시작하기 
Redis를 시작하거나 개발을 위해 Redis를 시작하는 경우에는 문제가 없지만 프로덕션 환경에서는 구성 파일을 사용해야합니다.
구성 파일로 Redis를 시작하려면 구성 파일의 전체 경로를 첫 번째 인수로 사용하십시오  

참조하려는 구성파일위치가 /etc/redis.conf 인경우
```sh
$ ./redis-server /etc/redis.conf  
```

참조하려는 구성파일위치가 /Users/leo/Downloads/redis-4.0.11/redis.conf 인경우
```sh
$ ./redis-server /Users/leo/Downloads/redis-4.0.11/redis.conf
```

redis-server 실행파일 위치인 /Users/leo/Downloads/redis-4.0.11/src/ 에서 실행하고 redis.conf 파일은 상위디렉토리에 있다면 아래처럼 실행가능 
```sh
$ ./redis-server ../redis.conf
```
Redis 소스 코드 배포의 루트 디렉토리에 포함 된 redis.conf 파일을 템플릿으로 사용하여 구성 파일을 작성해야합니다.

### Redis conf 구성
Redis는 내장 된 기본 구성을 사용하여 구성 파일없이 시작할 수 있지만이 설정은 테스트 및 개발 목적으로 만 권장됩니다.
Redis를 구성하는 적절한 방법은 보통 redis.conf 라고하는 Redis 구성 파일을 제공하는 redis.conf 입니다.

redis.conf 파일에는 매우 간단한 형식의 많은 지시문이 있습니다.
```
 keyword argument1 argument2 ... argumentN 
```
다음은 구성 지시문의 예입니다.
```
 slaveof 127.0.0.1 6380 
```

다음 예제와 같이 따옴표를 사용하여 공백을 포함하는 문자열을 인수로 제공 할 수 있습니다.
 보안을 위해 접속시 password 요청 구성지시문
```
requirepass "hello world"
```

##  command line 통해 인수 전달
Redis 2.6부터 명령 줄을 사용하여 Redis 구성 매개 변수를 직접 전달할 수도 있습니다. 
이는 테스트 목적으로 매우 유용합니다. 
다음은 포트 6380을 127.0.0.1 포트 6379에서 실행중인 인스턴스의 슬레이브로 사용하여 새 Redis 인스턴스를 시작하는 예제입니다.
```sh
 $ ./redis-server --port 6380 --slaveof 127.0.0.1 6379 
```
#### 포트 6380, 보안설정으로 접속시 password 설정
비밀번호를 "hello world" 로 설정한 결우
```sh
$./redis-server --port 6380 --requirepass "hello world"
```

명령 줄을 통해 전달 된 인수의 형식은 redis.conf 파일에서 사용 된 형식과 동일하지만 키워드 앞에 -- 가 붙는 경우는 예외입니다.

내부적으로 이는 내부 메모리 임시 설정 파일을 생성합니다 (가능하다면 사용자가 전달한 설정 파일을 연결하여). 
여기서 인수는 redis.conf의 형식으로 변환됩니다
