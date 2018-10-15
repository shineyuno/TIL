## HyperLogLog
### 소개 
HyperLogLog는 집합의 원소의 개수를 추정하는 방법으로 Redis 버전 2.8.9에 새로 추가되었습니다.
HyperLogLog는 매우 적은 메모리로 집합의 원소 개수를 추정할 수 있는 방법입니다. 
집합의 원소 개수를 정확하게 계산하기 위해 아주 많은 메모리가 필요할 때나 하나의 메모리에 모두 담을 수 없을 정도로 원소의 개수가 많을 때, 
정확하지 않지만 최대한 정확한 값을 상대적으로 적은 메모리만 사용해 얻고 싶을 때 사용할 수 있는 방법입니다.


#### 용도
용도: 주로 매우 큰 데이터의 오차가 1% 이하의 근사치를 구할 때 사용합니다. 
예를 들어, 어떤 검색 엔진의 하루 검색어 수를 계산할 때 사용할 수 있습니다. 메모리는 매우 적게 사용하고 오차는 적습니다.

#### 메모리 사용량
가장 큰 장점은 매모리를 매우 적게 사용합니다.   
Java HashSet이나 Redis Set을 사용할 경우 원소의 수에 따라 메모리를 사용합니다. 
예를 들어, Redis Set에 1백만개의 숫자를 저장하면 4,848kb, 1천만개의 숫자를 저장하면 46,387kb를 사용하지만, 
Redis HyperLogLog를 사용하면 원소 개수와 상관없이 고정으로 12kb만 사용합니다. 
이는 1백만개 숫자의 Set과 비교하면 단지 0.25% 만 사용하는 것입니다.
Redis HyperLogLog에서는 16384개 레지스터를 사용하고 레지스터 당 6bits를 사용하므로 사용하는 메모리 량은 12kb입니다.   
계산식: 16,384 * 6bits = 98,304bits / 8 = 12,288bytes

#### 원소 개수 추정 오차
HyperLogLog 오차는 1.04/SQRT(m)입니다. 
m은 레지스터(register) 개수로 Redis에서는 16384개를 사용하므로 오차는 0.81%입니다.

#### 저장(add) 속도
HyperLogLog와 Set의 저장 속도를 비교하기 위해 1백만개의 데이터를 PFADD와 SADD로 넣어습니다. 
PFADD는 5.23초, SADD는 8.64초로 PFADD가 약 1.6배 빨랐습니다.

#### 조회 속도 
PFCOUNT와 SCARD 모두 O(1)입니다.

HyperLogLog 데이터는 별도 data structure를 사용하지 않고 String을 사용합니다. String 내부는 HyperLogLog data structure입니다.


## 명령어
### PFADD
원소(element)를 추가
사용법은 pfadd key ele1 ele2 입니다. 
원소를 추가했을 경우 원소의 개수와 상관없이 1을 리턴, 원소가 이미 집합에 있을 경우는 0을 리턴합니다.
```redis
redis> PFADD hll a b c d e f g
(integer) 1
redis> PFCOUNT hll
(integer) 7
redis> 
```
논리적 처리 소요시간은 O(1)이다.


### PFCOUNT
원소(element)의 개수를 조회
사용법은 pfcount key 입니다. 
추정 오류는 1% 이하입니다.
```redis
redis> PFADD hll foo bar zap
(integer) 1
redis> PFADD hll zap zap zap
(integer) 0
redis> PFADD hll foo bar
(integer) 0
redis> PFCOUNT hll
(integer) 3
redis> PFADD some-other-hll 1 2 3
(integer) 1
redis> PFCOUNT hll some-other-hll
(integer) 6
redis> 
```
논리적 처리 소요시간은 O(1)이다


### PFMERGE
2개 이상의 집합을 합한다.
사용법은 pfmerge destkey sourcekey1 [sourcekey2 ...] 입니다. 
2개 이상의 집합을 합해서 하나의 집합으로 만듭니다.

```redis
redis> PFADD hll1 foo bar zap a
(integer) 1
redis> PFADD hll2 a b c foo
(integer) 1
redis> PFMERGE hll3 hll1 hll2
"OK"
redis> PFCOUNT hll3
(integer) 6
redis> 
```
논리적 처리 소요시간은 O(N)이다.

## 참조
[확률적 자료구조를 이용한 추정 - 유일한 원소 개수(Cardinality) 추정과 HyperLogLog](https://d2.naver.com/helloworld/711301) <br>
[https://redis.io/commands/pfadd](https://redis.io/commands/pfadd) <br>
[http://redisgate.kr/redis/command/hyperloglog.php](http://redisgate.kr/redis/command/hyperloglog.php) <br>
