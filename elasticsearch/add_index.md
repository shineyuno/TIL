# Add an Index
Elasticsearch에 데이터를 추가하려면 관련 데이터를 저장할 수있는 인덱스가 필요합니다. 
실제로, 인덱스는 하나 이상의 물리적 샤드를 가리키는 논리적 네임 스페이스입니다.

샤드는 인덱스의 모든 데이터 조각 만 보유하는 저수준 작업자 단위입니다. 
샤드가 Lucene의 단일 인스턴스이며 자체적으로 완전한 검색 엔진이라는 것을 알면 충분합니다. 
문서는 샤드로 저장 및 색인되지만 응용 프로그램은 직접 대화하지 않습니다.
대신, 그들은 색인과 대화합니다.


샤드는 Elasticsearch가 클러스터에 데이터를 분산시키는 방법입니다. 
샤드를 데이터의 컨테이너로 생각하십시오. 
문서는 샤드에 저장되고 샤드는 클러스터의 노드에 할당됩니다. 
클러스터가 커지거나 줄어들면 Elasticsearch는 노드간에 샤드를 자동으로 마이그레이션하여 클러스터의 균형을 유지합니다.

샤드는 기본 샤드 또는 복제 샤드 일 수 있습니다. 
색인의 각 문서는 단일 기본 샤드에 속하므로 보유하는 기본 샤드 수에 따라 색인에 보유 할 수있는 최대 데이터 양이 결정됩니다.

> + 각 Elasticsearch 샤드는 Lucene 색인입니다. 
> + 단일 Lucene 색인이 포함할 수 있는 문서 수의 최대 한도가 있습니다. 
> + LUCENE-5843에 따르면 2,147,483,519`개(= Integer.MAX_VALUE - 128)입니다.


복제 샤드는 단지 기본 샤드의 사본입니다. 
복제본은 하드웨어 장애로부터 보호하고 문서 검색 또는 검색과 같은 읽기 요청을 처리하기 위해 중복 데이터 사본을 제공하는 데 사용됩니다.

인덱스의 기본 샤드 수는 인덱스 생성시 고정되어 있지만 언제든지 복제 샤드 수를 변경할 수 있습니다.

## 참고 
https://www.elastic.co/guide/en/elasticsearch/guide/2.x/_add_an_index.html
