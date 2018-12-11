# monitor
1.0.0부터 사용 가능합니다.

MONITOR 는 Redis 서버에서 처리 한 모든 명령을 다시 스트림하는 디버깅 명령입니다. 
데이터베이스에 어떤 일이 일어나고 있는지 이해하는 데 도움이 될 수 있습니다. 
이 명령은 모두 redis-cli 및 telnet 통해 사용할 수 있습니다.

서버로 처리되는 모든 요청을 볼 수있는 기능은 Redis를 데이터베이스로 사용하거나 
분산 캐싱 시스템으로 사용할 때 응용 프로그램에서 버그를 발견하는 데 유용합니다.

```sh
$ redis-cli monitor
1339518083.107412 [0 127.0.0.1:60866] "keys" "*"
1339518087.877697 [0 127.0.0.1:60866] "dbsize"
1339518090.420270 [0 127.0.0.1:60866] "set" "x" "6"
1339518096.506257 [0 127.0.0.1:60866] "get" "x"
1339518099.363765 [0 127.0.0.1:60866] "del" "x"
1339518100.544926 [0 127.0.0.1:60866] "get" "x"
```

SIGINT (Ctrl-C)를 사용하여 redis-cli 를 통해 실행중인 모니터 스트림을 중지하십시오.

```sh
$ telnet localhost 6379
Trying 127.0.0.1...
Connected to localhost.
Escape character is '^]'.
MONITOR
+OK
+1339518083.107412 [0 127.0.0.1:60866] "keys" "*"
+1339518087.877697 [0 127.0.0.1:60866] "dbsize"
+1339518090.420270 [0 127.0.0.1:60866] "set" "x" "6"
+1339518096.506257 [0 127.0.0.1:60866] "get" "x"
+1339518099.363765 [0 127.0.0.1:60866] "del" "x"
+1339518100.544926 [0 127.0.0.1:60866] "get" "x"
QUIT
+OK
Connection closed by foreign host.
```


### MONITOR에 의해 기록되지 않은 명령
보안 문제 때문에 CONFIG 와 같은 특정 관리 명령이 MONITOR 출력에 기록되지 않습니다.

### MONITOR 실행 비용
MONITOR는 모든 명령을 다시 스트림하기 때문에 비용이 많이 든다. 
다음 (완전히 비합리적인) 벤치 마크 수치는 MONITOR 실행 비용을 나타냅니다.

MONITOR가 실행 되지 않은 벤치 마크 결과 :
```sh
$ src/redis-benchmark -c 10 -n 100000 -q
PING_INLINE: 101936.80 requests per second
PING_BULK: 102880.66 requests per second
SET: 95419.85 requests per second
GET: 104275.29 requests per second
INCR: 93283.58 requests per second
```


MONITOR가 실행되는 벤치 마크 결과 ( redis-cli monitor > /dev/null ) :
```sh
$ src/redis-benchmark -c 10 -n 100000 -q
PING_INLINE: 58479.53 requests per second
PING_BULK: 59136.61 requests per second
SET: 41823.50 requests per second
GET: 45330.91 requests per second
INCR: 41771.09 requests per second
```

이 특별한 경우, 단일 MONITOR 클라이언트를 실행하면 처리량을 50 % 이상 줄일 수 있습니다. 
더 많은 MONITOR 클라이언트를 실행하면 처리량이 훨씬 줄어 듭니다.

### 반환 값
비표준 반환 값 은받은 명령을 무한 흐름으로 덤프합니다.
