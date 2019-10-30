# _routing fieldedit

문서는 다음 공식을 사용하여 색인의 특정 샤드로 라우팅됩니다.
```java
shard_num = hash(_routing) % num_primary_shards
```
_routing에 사용되는 기본값은 문서의 _id입니다.

문서 당 사용자 정의 라우팅 값을 지정하여 사용자 정의 라우팅 패턴을 구현할 수 있습니다. 예를 들어 :

```json
PUT my_index/_doc/1?routing=user1&refresh=true  (1)
{
  "title": "This is a document"
}

GET my_index/_doc/1?routing=user1  (2)
```
+ (1) 이 문서는 ID 대신 user1을 라우팅 값으로 사용합니다.
+ (2) 문서를 가져 오거나 삭제하거나 업데이트 할 때 동일한 라우팅 값을 제공해야합니다.


_routing 필드의 값은 쿼리에서 액세스 할 수 있습니다.
```json
GET my_index/_search
{
  "query": {
    "terms": {
      "_routing": [ "user1" ]  (1)
    }
  }
}
```
+ (1) Querying on the _routing field (also see the ids query)	


## Searching with custom routing 

Custom routing은 검색의 영향을 줄일 수 있습니다.
검색 요청을 인덱스의 모든 샤드로 팬 아웃하는 대신 요청을 특정 라우팅 값 (또는 값)과 일치하는 샤드로만 보낼 수 있습니다.

```json
GET my_index/_search?routing=user1,user2  (1)
{
  "query": {
    "match": {
      "title": "document"
    }
  }
}
```
+ (1) 이 검색 요청은 user1 및 user2 라우팅 값과 연관된 샤드에서만 실행됩니다.

## 필요한 라우팅 값 만들기
사용자 지정 라우팅(custom routing)을 사용하는 경우 문서를 인덱싱, 가져 오기, 삭제 또는 업데이트 할 때마다 라우팅 값을 제공해야합니다.

라우팅 값을 잊어 버리면 문서가 둘 이상의 샤드에서 색인화 될 수 있습니다. 
보호 수단으로 _routing 필드는 모든 CRUD 작업에 필요한 사용자 지정 라우팅 값을 만들도록 구성 할 수 있습니다.

```json
PUT my_index2
{
  "mappings": {
    "_routing": {
      "required": true  (1)
    }
  }
}

PUT my_index2/_doc/1  (2)
{
  "text": "No routing value provided"
}
```
+ (1) _doc 문서에는 라우팅이 필요합니다.
+ (2) 이 인덱스 요청은 routing_missing_exception을 발생시킵니다.

## Unique IDs with custom routing
사용자 정의 _routing을 지정하여 문서를 색인 할 때 색인의 모든 샤드에서 _id의 고유성이 보장되지는 않습니다. 
실제로 동일한 _id를 가진 문서는 다른 _routing 값으로 인덱싱 된 경우 다른 샤드로 끝날 수 있습니다.

ID가 인덱스에서 고유한지 확인하는 것은 사용자의 책임입니다.

## Routing to an index partition
사용자 지정 라우팅 값이 단일 샤드가 아닌 샤드의 하위 집합으로 이동하도록 인덱스를 구성 할 수 있습니다. 
이렇게하면 검색의 영향을 줄이면서 불균형 클러스터로 끝나는 위험을 줄일 수 있습니다.

인덱스 생성시 인덱스 수준 설정 index.routing_partition_size를 제공하면됩니다. 
파티션 크기가 증가함에 따라 요청에 따라 더 많은 샤드를 검색해야하는 대신 데이터가 더 고르게 분산됩니다.

이 설정이 있으면 샤드 계산 공식은 다음과 같습니다.
```java
shard_num = (hash(_routing) + hash(_id) % routing_partition_size) % num_primary_shards
```
즉, _routing 필드는 색인 내 샤드 set를 계산하는 데 사용되며 _id는 해당 set 내 샤드를 선택하는 데 사용됩니다.

이 기능을 사용하려면 index.routing_partition_size의 값이 1보다 크고 index.number_of_shards보다 작아야합니다.

활성화되면 분할 된 인덱스에는 다음과 같은 제한이 있습니다.
+ 조인 필드 관계가있는 매핑은 그 안에 만들 수 없습니다.
+ 인덱스 내의 모든 매핑에는 _routing 필드가 필요한 것으로 표시되어 있어야합니다.
