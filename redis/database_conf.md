# Redis databases parameter
레디스 설정 파일인 redis.conf 에 있는 databases 파라미터에 대한 설명입니다.   


### 설명
레디스 서버는 여러개의 데이터베이스를 가질 수 있다.   기본 값은 16이다.   DB 번호 0부터 15까지 16개의 데이터베이스를 갖는다. 
레디스에서 데이터베이스는 서로 다른 키 공간이다.   즉 키 AAA는 데이터베이스 내에서는 유일하지만 다른 데이터베이스에는 또 존재할 수 있다.   
접속하면 기본으로 0번 데이터베이스로 접속된다.   
데이터베이스 선택은 select 문으로 한다.   
select 5 는 5번 데이터베이스에 접속한다.
클러서터 모드에서는 데이터베이스가 하나만 존재한다.

### 사용 방법
redis.conf 설정
```
databases 10
```

redis-cli 에서 select로 db변경 
```sh 
127.0.0.1:6379> select 10
OK
127.0.0.1:6379[10]>
```

-n <dbnum> option: 접속시 db 선택 
```
$ redis-cli -n 10
127.0.0.1:6379[10]>
```
