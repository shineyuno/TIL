# ?refresh
인덱스, 업데이트, 삭제 및 대량 API는이 요청에 의한 변경 사항이 검색에 표시되는시기를 제어하기 위해 새로 고침 설정을 지원합니다. 
허용되는 값은 다음과 같습니다.

### Empty string or `true`
작업이 발생한 직후 관련 기본 및 복제 샤드 (전체 인덱스가 아님)를 새로 고쳐 업데이트 된 문서가 검색 결과에 즉시 표시되도록합니다. 
인덱싱 및 검색 관점 모두에서 성능이 저하되지 않는다는 신중한 사고와 검증 후에 만 수행해야합니다.

### `wait_for`
응답하기 전에 새로 고침으로 요청의 변경 사항이 표시 될 때까지 기다립니다. 
이것은 즉시 새로 고침을 강요하는 것이 아니라 새로 고침이 발생할 때까지 기다립니다. 
Elasticsearch는 모든 변경된 샤드를 자동으로 새로 고칩니다. 
index.refresh_interval 기본값은 1 초입니다. 
그 설정은 동적입니다. 
Refresh API를 호출하거나 이를 지원하는 모든 API에서 refresh를 `true`로 설정하면 
새로 고침이 발생하여 `refresh = wait_for`로 이미 실행중인 요청이 리턴됩니다.

### `false` (기본값)
Take no refresh related actions
이 요청에 의한 변경 사항은 요청이 반환 된 후 어느 시점에 표시됩니다.

## 사용할 설정 선택하기
변경 사항이 표시 될 때까지 기다릴 이유가없는 한 항상 `refresh = false`를 사용하십시오. 
기본값이므로 refresh 매개 변수를 URL에서 빼 두십시오. 
가장 간단하고 빠른 선택입니다.

요청과 동기식으로 요청에 대한 변경 사항을 표시해야하는 경우 
Elasticsearch에 더 많은 부하를 두는 것 (`true`)과 응답을 기다리는 시간 (`wait_for`) 중 하나를 선택해야합니다. 
다음은 그 결정에 도움이되는 몇 가지 사항입니다.

- 인덱스에 대한 변경이 많을수록 `true`에 비해 `wait_for`가 더 많은  작업이 절약됩니다.
In the case that the index is only changed once every `index.refresh_interval` then it saves no work.

- `true`는 덜 효율적인 인덱스 구성 (작은 세그먼트)을 생성하여 나중에보다 효율적인 인덱스 구성 (큰 세그먼트)으로 병합해야합니다. 
작은 세그먼트를 생성하기 위해 인덱스 시간에, 작은 세그먼트를 검색하기 위해 검색 시간에, 
더 큰 세그먼트를 만들기 위해 병합 시간에 `true`의 비용이 지불됨을 의미합니다.

- 한 번에 여러 개의 `refresh = wait_for` 요청을 시작하지 마십시오. 
대신 refresh = wait_for를 사용하여 단일_대량_요청(single bulk request)으로 일괄 처리하면 Elasticsearch는 이들을 
모두 병렬로 시작하고 모두 완료된 경우에만 반환합니다.

- 새로 고침 간격이 `-1`로 설정되어 자동 새로 고침을 비활성화하면 `refresh = wait_for` 요청은 일부 작업이 새로 고침을 일으킬 때까지 무기한 대기합니다. 
반대로 `index.refresh_interval`을 기본값 인 200ms보다 짧은 값으로 설정하면 `refresh = wait_for`가 더 빨리 돌아 오지만 
여전히 비효율적 인 세그먼트가 생성됩니다.

- `refresh = wait_for`는 요청이있는 경우에만 영향을 미치지 만, 즉시 새로 고침을 실행하면 `refresh = true`가 진행중인 다른 요청에 영향을줍니다. 
일반적으로 실행중인 시스템이있는 경우 방해하지 않으려면 `refresh = wait_for`가 약간 수정됩니다.


## refresh = wait_for 새로 고침을 강제 할 수 있습니다
이미 `index.max_refresh_listeners` (기본값은 1000) 요청이 해당 샤드에서 새로 고치기를 기다리는 경우 
`refresh = wait_for` 요청이 들어 오면 해당 요청은 새로 고침이 true로 설정된 것처럼 대신 새로 고침을 수행합니다. 
이렇게하면 `refresh = wait_for` 요청이 리턴 될 때 변경 사항을 검색에 표시하고 차단 된 요청에 대해 확인되지 않은 자원 사용을 방지 할 수 있습니다. 
요청에 리스너 슬롯이 부족하여 새로 고침을 강제 실행 한 경우 응답에 `"forced_refresh": true`가 포함됩니다.

대량 요청은 샤드를 수정 한 횟수에 관계없이 각 샤드에서 하나의 슬롯 만 차지합니다.

## 예
문서가 생성되고 인덱스가 즉시 새로 고쳐 표시됩니다.
```http
PUT /test/_doc/1?refresh
{"test": "test"}
PUT /test/_doc/2?refresh=true
{"test": "test"}
```
 
검색 할 수 있도록 문서를 만들지 않아도됩니다.
```http
PUT /test/_doc/3
{"test": "test"}
PUT /test/_doc/4?refresh=false
{"test": "test"}
```
 
문서가 만들어지고 검색을 위해 표시 될 때까지 기다립니다.
```http
PUT /test/_doc/4?refresh=wait_for
{"test": "test"}
```
