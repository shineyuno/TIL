# 

# 4. Project Reactor - the Foundation for Reactive Apps

- 4장 리액터 프로젝트 리액티브 앱의 기초

## 1. A brief history of Project Reactor

- 리액터 프로젝트의 간략한 역사
- 리액터 프로젝트는 리액티브 스트림 스펙 중 가장 인기 있는 구현체

### 1-1 . Project Reactor version 1.x

### 1-2. Project Reactor version 2.x

## 2. Project Reactor essentials

- 리액터 프로젝트 필수 요소

### 2-1. Adding Reactor to the project

### 2-2. Reactive types Flux and Mono

- 리액터 프로젝트에는 Publisher<T>의 구현체로 Flux<T>, Mono<T> 가 있음
1. Flux
2. Mono
3. Reactive types of RxJava 2
    1. Observable
    2. Flowable
    3. Single
    4. Maybe
    5. Completable

### 2-3. Creating Flux and Mono sequences

### 2-4. Subscribing to Reactive Streams

1. Implementing custom subscribers

### 2-5. Transforming reactive sequences with operators

1. Mapping elements of reactive sequences&#xA0;
2. Filtering reactive sequences
3. Collecting reactive sequences
4. Reducing stream elements
5. Combining Reactive Streams
6. Batching stream elements
7. The flatMap, concatMap, and&#xA0;flatMapSequential operators
8. Sampling elements&#xA0;
9. Transforming reactive sequences into blocking structures
10. Peeking elements while sequence processing
11. Materializing and dematerializing signals
12. Finding an appropriate operator

### 2-6. Creating streams programmatically

- 코드를 통해 스트림 만들기
- 리액터를 이용해 스트림을 프로그래밍 방식으로 생성하는 방법
1. Factory methods push and create

    **push**

    ```java
    Flux.push(emitter -> IntStream                                     // (1)
            .range(2000, 3000)                                         // (1.1)
            .forEach(emitter::next))                                   // (1.2)
        .delayElements(Duration.ofMillis(1))                           // (2)
        .subscribe(e -> log.info("onNext: {}", e));                    // (3)
    ```

    - 단일 스레드 생성자를 적용해 Flux 인스턴스를 생성
    - 배압과 cancel에 대한 걱정없이 **비동기**, **단일 스레드**, 다중 값을 가지는 API를 적용하는데 유용
    - 구독자가 부하를 처리할 수 없는 경우 배압과 취소는 모두 큐를 이용해 처리됨

    **create**

    - FluxSink 인스턴스를 추가로 직렬화하므로 다른 스레드에서 이벤트를 보낼 수 있게 된다.

    ```java
    Flux.create(emitter -> { 
       emitter.onDispose(() -> log.info("Disposed")); 
       // push events to emitter 
     }) 
     .subscribe(e -> log.info("onNext: {}", e));
    ```

2. Factory method generate
    - generate : 메서드를 호출하는 오브젝트의 내부 전달 상태를 기반으로 복잡한 시퀀스를 만들 수 있도록 설계됨

    ```java
    Flux.generate(                                                     // (1)
        () -> Tuples.of(0L, 1L),                                       // (1.1)      
        (state, sink) -> {                                             //
            log.info("generated value: {}", state.getT2());            //
            sink.next(state.getT2());                                  // (1.2)
            long newValue = state.getT1() + state.getT2();             //
            return Tuples.of(state.getT2(), newValue);                 // (1.3) 
        })
        .delayElements(Duration.ofMillis(1))                           // (2)
        .take(7)                                                       // (3)
        .subscribe(e -> log.info("onNext: {}", e));                    // (4)
    ```

    output

    ```java
    generated value: 1
    onNext: 1
    generated value: 1
    onNext: 1
    generated value: 2
    onNext: 2
    generated value: 3
    onNext: 3
    generated value: 5
    onNext: 5
    generated value: 8
    onNext: 8
    generated value: 13
    onNext: 13
    ```

    - 추가로 볼거
        - Flux.generate() 함수는 동기 방식으로 한 번에 1개의 데이터를 생성할 때 사용한다.
        - Flux.generate() 메서드 중 하나는 다음과 같다.
            - Flux<T> generate(Consumer<SynchronousSink<T>> generator)
        - generator는 Subscriber로부터 요청이 왔을 때 신호를 생성한다.
        - generator는 전달받은 SynchronousSink를 사용해서 next, complete, error 신호를 발생한다. 한 번에 1개의 next() 신호만 발생할 수 있다.

        [https://javacan.tistory.com/entry/Reactor-Start-2-RS-just-generate](https://javacan.tistory.com/entry/Reactor-Start-2-RS-just-generate)

3. Wrapping disposable resources into Reactive Streams
    - 일회용 리소스를 Reactive Streams로 래핑

    **using** 팩토리 메소드

    - 일회성 리소스에 의존하는 스트림 생성
    - 리액티브 프로그래밍에서 사용하는 try-with-resources 방식의 접근법
    - using 팩토리 메서드를 사용하면 Connection 인스턴스 라이프 사이클을 스트림의 라이프 사이클에 래핑할수 있다.
    - Callable 인스턴스를 호출해 관리자원을 동기적으로 검색

    ```java
    public class Connection implements AutoCloseable {                 // (1)
       private final Random rnd = new Random();

       public Iterable<String> getData() {                             // (2)
          if (rnd.nextInt(10) < 3) {                                   // (2.1)
             throw new RuntimeException("Communication error");
          }
          return Arrays.asList("Some", "data");                        // (2.2)
       }

       public void close() {                                           // (3)
          log.info("IO Connection closed");
       }

       public static Connection newConnection() {                      // (4)       
          log.info("IO Connection created");
          return new Connection();
       }
    }
    ```

    ```java
    Flux<String> ioRequestResults = Flux.using(                        // (1)
        Connection::newConnection,                                     // (1.1)
        connection -> Flux.fromIterable(connection.getData()),         // (1.2)
        Connection::close                                              // (1.3)
    );

    ioRequestResults.subscribe(                                        // (2)
            data -> log.info("Received data: {}", data),                    //
            e -> log.info("Error: {}", e.getMessage()),                     //
            () -> log.info("Stream finished"));                             //

    public static <T, D> Flux<T> using(
    		Callable<? extends D> resourceSupplier, 
    		Function<? super D, ? extends Publisher<? extends T>> sourceSupplier,
    	  Consumer<? super D> resourceCleanup) {
    	return using(resourceSupplier, sourceSupplier, resourceCleanup, true);
    }
    //Params:
    //resourceSupplier – a Callable that is called on subscribe to generate the resource
    //sourceSupplier – a factory to derive a Publisher from the supplied resource
    //resourceCleanup – a resource cleanup callback invoked on completion
    ```

    (1) resourceSupplier – 리소스를 생성하기 위해 구독 시 호출되는 Callable 
    (2) sourceSupplier – 제공된 리소스에서 Publisher를 파생시키는 팩토리
    (3) resourceCleanup – 완료 시 호출되는 리소스 정리 콜백

    2. - 실제 처리를 시작하려면 onNext, onError, onComplete 시그널에 대한 핸들러를 사용해 구독을 생성해야 한다.

4. Wrapping reactive transactions with the usingWhen factory
    - usingWhen 팩토리를 사용한 리액티브 트랜잭션 래핑
    - Publisher의 인스턴스에 가입해 관리되는 리소스를 리액티브 타입으로 검색
    - 메인 스트림의 성공 및 실패에 대해 각각 다른 핸들러를 사용할 수 있음
        - usingWhen 연산자만으로 완전한 논블로킹 리액티브 트랜잭션을 구현할 수 있게 됨

```java
Flux.usingWhen(
    Transaction.beginTransaction(),                                  // (1)
    transaction -> transaction.insertRows(Flux.just("A", "B", "C")), // (2)
    Transaction::commit,                                             // (3)
    Transaction::rollback                                            // (4)
).subscribe(
    d -> log.info("onNext: {}", d),
    e -> log.info("onError: {}", e.getMessage()),
    () -> log.info("onComplete")
);

public static <T, D> Flux<T> usingWhen(Publisher<D> resourceSupplier,
			Function<? super D, ? extends Publisher<? extends T>> resourceClosure,
			Function<? super D, ? extends Publisher<?>> asyncComplete,
			Function<? super D, ? extends Publisher<?>> asyncError) {
		//null asyncCancel translates to using the `asyncComplete` function in the operator
		return onAssembly(new FluxUsingWhen<>(resourceSupplier, resourceClosure,
				asyncComplete, asyncError, null));
	}

//Params:
//resourceSupplier – a Publisher that "generates" the resource, subscribed for each subscription to the main sequence
//resourceClosure – a factory to derive a Publisher from the supplied resource
//asyncComplete – an asynchronous resource cleanup invoked if the resource closure terminates with onComplete or is cancelled
//asyncError – an asynchronous resource cleanup invoked if the resource closure terminates with onError
```

(1) resourceSupplier – 리소스를 "생성"하는 Publisher, 주 시퀀스에 대한 각 구독에 대해 구독
(2) resourceClosure – 제공된 리소스에서 Publisher 를 파생시키는 팩토리
(3) asyncComplete – 리소스 클로저가 onComplete로 종료되거나 취소된 경우 호출되는 비동기 리소스 정리
(4) asyncError – 리소스 클로저가 onError로 종료되는 경우 호출되는 비동기 리소스 정리

### 2-7. Handling errors

- 에러 처리하기
- onError 시그널은 리액티브 스트림 스펙의 필수 요소라서 예외를 처리 할수 있는 경로로 전파할수 있다.
- 최종 구독자가 onError 시그널에 대한 핸들러를 정의하지 않으면 UnsupportedOperationException을 발생시킨다.
- 리액티브 스트림은 onError가 스트림이 종료됐다고 정의하기 있기 때문에 시그널을 받으면 시퀀스가 실행을 중지한다.

```java
public Flux<String> recommendedBooks(String userId) {
    return Flux.defer(() -> {                                        // (1)
        if (random.nextInt(10) < 7) {
            return Flux.<String>error(new RuntimeException("Err"))   // (2)
                .delaySequence(Duration.ofMillis(100));
        } else {
            return Flux.just("Blue Mars", "The Expanse")             // (3)
                .delayElements(Duration.ofMillis(50));
        }
    }).doOnSubscribe(s -> log.info("Request for {}", userId));       // (4)
}
```

```java
Flux.just("user-1")                                                // (1)
    .flatMap(user ->                                               // (2)
        recommendedBooks(user)                                     // (2.1)
        .retryBackoff(5, Duration.ofMillis(100))                   // (2.2)
        .timeout(Duration.ofSeconds(3))                            // (2.3)
        .onErrorResume(e -> Flux.just("The Martian")))             // (2.4)
    .subscribe(                                                    // (3)
        b -> log.info("onNext: {}", b),
        e -> log.warn("onError: {}", e.getMessage()),
        () -> log.info("onComplete")
    );
```

(2.2) 오류가 발생할 경우 다시 실행을 시도하는 리액티브 워크플로를 정의할수 있다 ex) retryBackoff 연산자는 지수적인 백오프로 재시도 (100밀리초의 지연시간 시작 5회 이상 재시도하지않음)

(2.3) 재시도 전략이 3초 후에도 아무런 결과를 가져오지 않으면 오류 시그널 발생

(2.4) onErrorResume 연산자를 적용해 예외를 catch하고 대체 워크플로를 실행할수 있다.

### 2-8. Backpressure handling

- 배압 다루기
- onBackPressureBuffer : 제한되지 않은 요구를 요청하고 결과를 다운스트림으로 푸시
- onBackPressureDrop : 제한되지 않은 요구를 요청하고 데이터를 하위로 푸시
- onBackPressureLast : onBackPressureDrop과 유사. 가장 최근에 수신된 원소를 기억하고, 요청이 발생하면 다운스트림으로 푸시
- onBackPressureError : 데이터를 다운스트림으로 푸시하는 동안 크기를 제한하지 않고 요청

### 2-9. Hot and cold streams

- Hot 스트림과 cold 스트림
- reactive publishers, hot and cold 두가지 타입으로 분류할수 있다.

**Cold**

- Cold publishers는 구독자가 나타날 때마다 해당 구독자에 대해 모든 시퀀스 데이터가 생성되는 방식으로 동작한다.
- 구독자 없이는 데이터가 생성되지 않는다.

```java
Flux<String> coldPublisher = Flux.defer(() -> {
    log.info("Generating new items");
    return Flux.just(UUID.randomUUID().toString());
});

log.info("No data was generated so far");
coldPublisher.subscribe(e -> log.info("onNext: {}", e));
coldPublisher.subscribe(e -> log.info("onNext: {}", e));
log.info("Data was generated twice for two subscribers");
```

output

```java
No data was generated so far
Generating new items
onNext: 63c8d67e-86e2-48fc-80a8-a9c039b3909c
Generating new items
onNext: 52232746-9b19-4b5e-b6b9-b0a2fa76079a
Data was generated twice for two subscribers
```

- 구독자가 나타날때마다 새로운 시퀀스가 생성됨
- HTTP 요청이 이런식으로 동작

**Hot**

- Hot 퍼블리셔의 데이터 생성은 구독자의 존재 여부에 의존하지 않는다. 따라서 핫 퍼블리셔는 첫번째 구독자가 구독을 시작하기 전에 원소를 만들어내기 시작할 수 있다.
- 구독자가 나타나면 핫 퍼블리셔는 이전에 생성된 값을 보내지 않고 새로운 값만 보낼 수도 있다.
- 팩토리 메서드 just는 게시자가 빌드될 때 값이 한번만 계산되고 새 구독자가 도착하면 다시 계산되지 않는 형태의 핫퍼블리셔를 생성
    - just는 defer 로 래핑해 콜드 퍼블리셔로 전환할수도 있다.

→ add test check

추가 내용

- Publisher 생성하기

Spring Webflux Cold / Hot 이해하기- [https://hyunsoori.tistory.com/5](https://hyunsoori.tistory.com/5)

Spring Webflux 1(Reactor 살펴보기)- [https://tries1.github.io/spring/2020/01/28/spring_webflux_1.html](https://tries1.github.io/spring/2020/01/28/spring_webflux_1.html)

Reactor Hot Publisher vs Cold Publisher- [https://www.vinsguru.com/reactor-hot-publisher-vs-cold-publisher/](https://www.vinsguru.com/reactor-hot-publisher-vs-cold-publisher/)

1. Multicasting elements of a stream
    - 콜드 퍼블리셔를 리액티브변환을 통해 핫 퍼블리셔로 전환할 수 있다.
    - ConnectableFlux를 이용하면 가장 수요가 많은 데이터를 생성하고, 다른 모든 subscribers가 자신의 속도로 데이터를 처리할 수 있도록 캐시됩니다.

    ```java
    Flux<Integer> source = Flux.range(0, 3)
        .doOnSubscribe(s ->
            log.info("new subscription for the cold publisher"));

    ConnectableFlux<Integer> conn = source.publish();

    conn.subscribe(e -> log.info("[Subscriber 1] onNext: {}", e));
    conn.subscribe(e -> log.info("[Subscriber 2] onNext: {}", e));

    log.info("all subscribers are ready, connecting");
    conn.connect();
    ```

    output

    ```java
    all subscribers are ready, connecting
    new subscription for the cold publisher
    [Subscriber 1] onNext: 0
    [Subscriber 2] onNext: 0
    [Subscriber 1] onNext: 1
    [Subscriber 2] onNext: 1
    [Subscriber 1] onNext: 2
    [Subscriber 2] onNext: 2
    ```

    - cold publisher는 subscription을 받았고 결과적으로 항목을 한 번만 생성.
    - 그러나 두 가입자 모두 완전한 이벤트 세트를 받았습니다.

    → add test check

2. Caching elements of a stream
    - ConnectableFlux를 이용하면 다양한 캐싱 전략을 쉽게 구성할 수 있다.
    - 리액터에는 이벤트 캐싱을 위한 연산자로 cache 연산자가 이미 존재
        - 내부적으로 cache 연산자는 ConnectableFlux 사용
    - 캐시가 보유할 수 있는 데이터의 양과 캐시된 각 항목의 만료시간을 조정할 수 있다.

    ```java
    Flux<Integer> source = Flux.range(0, 2)                              // (1)
        .doOnSubscribe(s ->
            log.info("new subscription for the cold publisher"));

    Flux<Integer> cachedSource = source.cache(Duration.ofSeconds(1));    // (2)

    cachedSource.subscribe(e -> log.info("[S 1] onNext: {}", e));        // (3)
    cachedSource.subscribe(e -> log.info("[S 2] onNext: {}", e));        // (4)

    Thread.sleep(1200);                                                  // (5)

    cachedSource.subscribe(e -> log.info("[S 3] onNext: {}", e));        // (6)
    ```

    (1) 콜드 퍼블리셔 생성

    (2) 1초동안 cache 연산자와 함께 콜드 퍼블리셔 캐시함

    (5) 캐시된 데이터가 만료될 때까지 잠시 기다림

    output

    ```java
    new subscription for the cold publisher
    [S 1] onNext: 0
    [S 1] onNext: 1
    [S 2] onNext: 0
    [S 2] onNext: 1
    new subscription for the cold publisher
    [S 3] onNext: 0
    [S 3] onNext: 1
    ```

    - 처음 두구독자가 첫번째 구독의 캐시된 데이터를 공유
    - 지연시간이 지난 후 세번째 구독자가 캐시된 데이터를 검색할 수 없어서 콜드 퍼블리셔에 대한 새로 구독이 발생. 캐시로부터 데이터를 얻은 것은 아니지만 원하는 데이터를 수신

3. Sharing elements of a stream
    - ConnectableFlux를 사용해 여러개의 구독자에 대한 이벤트를 멀티캐스트 할 수 있다.
    - 구독자가 나타나고 나서야 처리가 시작
    - share연산자로 콜드 퍼블리셔를 핫 퍼블리셔로 변환할 수 있으며, 구독자가 각 신규 구독자에게 이벤트를 전파하는 방식으로 작동된다.

    ```java
    Flux<Integer> source = Flux.range(0, 5)
        .delayElements(Duration.ofMillis(100))
        .doOnSubscribe(s ->
            log.info("new subscription for the cold publisher"));

    Flux<Integer> cachedSource = source.share();

    cachedSource.subscribe(e -> log.info("[S 1] onNext: {}", e));
    Thread.sleep(400);
    cachedSource.subscribe(e -> log.info("[S 2] onNext: {}", e));
    ```

    output

    ```java
    new subscription for the cold publisher
    [S 1] onNext: 0
    [S 1] onNext: 1
    [S 1] onNext: 2
    [S 1] onNext: 3
    [S 2] onNext: 3
    [S 1] onNext: 4
    [S 2] onNext: 4
    ```

    - 두번째 구독자는 자신이 생성되기 전에 발생한 이벤트는 수신하지 못했습니다.

### 2-10. Dealing with time

- 시간 다루기
- 리액티브 프로그래밍은 비동기적이므로 본질적으로 시간의 축이 있다고 가정
- interval, delayElements, delaySequence, timestamp, timeout 시간 관련 이벤트를 처리할수있다
- elapsed로 이전 이벤트와의 시간 간격을 측정

```java
Flux.range(0, 5)
  .delayElements(Duration.ofMillis(100))
  .elapsed()
  .subscribe(e -> log.info("Elapsed {} ms: {}", e.getT1(), e.getT2()));
```

output

```java
Elapsed 151 ms: 0
Elapsed 105 ms: 1
Elapsed 105 ms: 2
Elapsed 103 ms: 3
Elapsed 102 ms: 4
```

- 리액터가 예정된 이벤트에 대해 자바의 ScheduledExecutorService를 사용하기때문에 정확하게 도착하지 않음
- 리액터 라이브러리에서 너무 정확한 시간(실시간) 간격을 요구하지 않도록 주의

### 2-11. Composing and transforming Reactive Streams

- 리액티브 스트림을 조합하고 변환하기
- 이전에는 스트림내에서 이벤트를 변환

**transform**

- transform 연산자를 사용하면 스트림 구조 자체를 보강(변경)할 수 있다.
    - 공통부분을 별도의 객체로 추출해 필요할 때마다 재사용
- transform 연산자는 스트림 라이프 사이클의 결합 단계에서 스트림 동작을 한번만 변경합니다.

```java
Function<Flux<String>, Flux<String>> logUserInfo =                 // (1)
    stream -> stream                                               //
        .index()                                                   // (1.1)
        .doOnNext(tp ->                                            // (1.2)
            log.info("[{}] User: {}", tp.getT1(), tp.getT2()))     //
        .map(Tuple2::getT2);                                       // (1.3)

Flux.range(1000, 3)                                                // (2)
    .map(i -> "user-" + i)                                         //
    .transform(logUserInfo)                                        // (3)
    .subscribe(e -> log.info("onNext: {}", e));
```

(3) transform 연산자를 적용해 logUserInfo 함수로 정의된 변환을 적용

**composer**

- 리액터에서 똑같은일 하는 연산자
- 구독자가 도착할때마다 동일한 스트림 변환 작업을 수행

```java
Function<Flux<String>, Flux<String>> logUserInfo = (stream) -> {     // (1)
    if (random.nextBoolean()) {
        return stream
            .doOnNext(e -> log.info("[path A] User: {}", e));
    } else {
        return stream
            .doOnNext(e -> log.info("[path B] User: {}", e));
    }
};

Flux<String> publisher = Flux.just("1", "2")                         // (2)
    .compose(logUserInfo);                                           // (3)

publisher.subscribe();                                               // (4)
publisher.subscribe();
```

(3) compose 연산자를 사용해 logUserInfo 함수를 실행 워크 플로에 포함합니다. 

output

```java
[path B] User: 1
[path B] User: 2
[path A] User: 1
[path A] User: 2
```

→add test check

### 2-12. Processors

- 리액티브 스트림 스펙은 Processor 인터페이스를 정의하고 있음
- Publisher이면서 동시에 Subscriber
- 구독이 가능하며, 시그널(onNext, onError, onComplete)를 수동으로 보낼 수 있다.
- 하지만 Processor 사용을 권장하지 않으며, 대부분 연산자 조합으로 대체가능
- 팩토리 메서드(push, create, generate) 사용을 권장
- 리액터 프로세서 종류
    - direct : Process 입력부를 직접 구현해 데이터를 푸시
    - Synchronous : 업스트림 Publisher 를 구독하거나 수동으로 데이터를 푸시
    - Asynchronous : 여러개의 업스트림 publisher에서 입력을 받아 다운스트림으로 푸시

### 2-13. Testing and debugging Project Reactor

- 리액터 프로젝트 테스트 및 디버깅하기
- 테스트 9장 리액티트 응용 프로그램 테스트하기에서 자세히 다룸
- 콜백 기반 프레임워크와 마찬가지로 리액터 프로젝의 스택트레이스는 유익하지 않음
    - 스택트레이스를 통해 예외 상황이 발생한 정확한 위치를 알기 어렵
- 리액터 라이브러리는 조립 단계에서 적용 가능한 디버깅 기능을 제공
    - 스트림 수명주기의 조립단계에 대한  자세한 내용은 **리액터 프로젝트 심화학습 절**에서...
    - 이 기능은 다음코드를 사용해 활성화

    ```java
    // 이 기능을 사용하면 조립할 모든 스트림에 대해 stacktrace 수집 
    Hooks.onOperatorDebug();
    ```

    - 스택 트레이스를 만든것은 비용이 많이 듦
    - 최후 수단으로 제한된 구역에서만 활성화 해야함

### 2-14. Reactor Addons

- 리액터 추가 기능

## 3. Advanced Project Reactor

- 리액터 프로젝트 심화학습

### 3-1. Reactive Streams life cycle

- 리액티브 스트림의 수명주기
1. Assembly-time
2. Subscription-time
3. Runtime

### 3-2. The thread scheduling model in Reactor

- 리액터에서 스레드 스케줄링 모델
1. The publishOn operator
    1. Parallelization with the publishOn operator
2. The subscribeOn operator
3. The parallel operator
4. Scheduler
5. Rector Context

### 3-3. Internals of Project Reactor

- 프로젝트 리액터의 내부 구조
1. Macro-fusion
2. Micro-fusion

## Summary
