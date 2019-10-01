# analyzer
analyzed문자열 필드 의 값은 분석기 를 통해 문자열을 토큰 또는 용어 스트림으로 변환합니다. </br>
예를 들어, 문자열 `"The quick Brown Foxes."`, 분석기를 사용에 따라 수, 토큰에 분석 : `quick, brown, fox.` </br>
이들은 필드에 대해 색인화 된 실제 용어이므로 큰 텍스트 덩어리 내에서 개별 단어를 효율적으로 검색 할 수 있습니다 . </br>

이 분석 프로세스는 인덱스 시간뿐만 아니라 쿼리 시간에도 발생해야합니다. </br>
쿼리 문자열은 동일한 (또는 유사한) 분석기를 통과하여 찾으려는 용어가 동일한 형식을 갖도록해야합니다. 색인에 존재합니다. </br>

Elasticsearch 는 추가 구성없이 사용할 수있는 사전 정의 된 여러 분석기 와 함께 제공됩니다. </br>
또한 여러 문자 필터, 토큰 화기 및 토큰 필터 가 함께 제공되어 인덱스 당 사용자 정의 분석기를 구성 할 수 있습니다. </br>

분석기는 쿼리, 필드 또는 인덱스별로 지정할 수 있습니다. 인덱스 타임에 Elasticsearch는 다음 순서로 분석기를 찾습니다. 

- `analyzer`필드 매핑에 정의.
- `default`인덱스 설정에 이름이 지정된 분석기 .
- `standard`분석기.

쿼리시 몇 가지 레이어가 더 있습니다.
- `analyzer`의 정의 된 전체 텍스트 쿼리 .
- `search_analyzer`필드 매핑에 정의.
- `analyzer`필드 매핑에 정의.
- `default_search`인덱스 설정에 이름이 지정된 분석기 .
- `default`인덱스 설정에 이름이 지정된 분석기 .
- `standard`분석기.

특정 필드에 대한 분석기를 지정하는 가장 쉬운 방법은 다음과 같이 필드 맵핑에서 분석기를 정의하는 것입니다.

```http
PUT /my_index
{
  "mappings": {
    "properties": {
      "text": { (1)
        "type": "text",
        "fields": {
          "english": {  (2)
            "type":     "text",
            "analyzer": "english"
          }
        }
      }
    }
  }
}

GET my_index/_analyze (3)
{
  "field": "text",
  "text": "The quick Brown Foxes."
}

GET my_index/_analyze  (4)
{
  "field": "text.english",
  "text": "The quick Brown Foxes."
}

```
	
(1) 이 text필드는 기본 standard분석기를 사용합니다 .

(2) text.english 다중 필드 용도 english단어를 중지 제거 및 형태소 적용 분석기.

(3) 이 토큰을 반환 : [ `the, quick, brown, foxes` ].

(4) 이 토큰을 반환 : [ `quick, brown, fox` ].

## 참조
https://www.elastic.co/guide/en/elasticsearch/reference/current/analyzer.html
