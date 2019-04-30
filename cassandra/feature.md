# Cassnadra 의 기본 특징 정리
NoSQL 데이터베이스의 한 종류인 Cassnadra 의 기본 특징에 대하여 알아봅시다.



## 데이터모델
+ Key space
+ Table
+ Row
+ column name : column value
+  SET, LIST, MAP 도 칼럼에 저장 가능
### (참고)
+ **Cassandra : Key-space > Table > Row > Column name : Column value**
+ Mongodb : db > collection > document > key:value
+ RDBMS : DB > Table > row > column
+ elasticsearch : index > type> document > key : value


## Java 기반


## Column-family(Wide-Column) key-value store
+ 하나의 row는 여러개의 column을 가질 수 있다.
+ 각각의row가 같은 수의 column을 가질 필요는 없다. (Sparse Multidimensional Hash Table)
+ 각 row는 unique key를 가진다. partitioning에 사용됨.
+ No relational
+ Map< RowKey, SortedMap<ColumnKey, ColumnValue> >와 같은 형태로 저장됨.



## Schemaless (Schema-free)
+ 스키마란?
  + 데이터베이스를 구성하는 개체(Entity), 속성(Attribute), 관계(Relationship) 및 데이터 조작 시에 데이터 값들이 갖는 제약조건 등에 관해 전반적으로 정의하는 것이다.
  + Schema가 존재한다는 것은 그 구조가 미리 정의되어 있어야 한다는 의미. 이는 데이터의 급격한 변화에 대응하기 힘듬.
+ Cassnadra 는 데이터 구조가 어떤 형태를 가질지, 어떤 fields를 가질지 미리 정의할 필요 없다.
+ 데이터베이스에 저장된 Document는 각기 다른, 다양한 필드를 가질 수 있다.
+ 각 필드는 서로 다른 데이터타입을 가질 수 있다.
+ It actually mean dynamically typed schema.
  + 계속해서 필드의 추가 변경이 이루어지는 경우 유용함.
  + Agile development methodology 에 유용함
+ Unstructured data 를 쉽게 저장할 수 있음
+ 데이터 모델링을 한 다음 복잡한 join 문을 사용해서 쿼리하는 대신, 카산드라는 원하는 쿼리를 모델링한 다음 데이터를 제공하도록 함.

## 참고
https://nicewoong.github.io/development/2018/02/11/cassandra-feature/
