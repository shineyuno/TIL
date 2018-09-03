# 아파치 하이브
아파치 하이브(Apache Hive)는 하둡에서 동작하는 데이터 웨어하우스(Data Warehouse) 인프라 구조로서 데이터 요약, 질의 및 분석 기능을 제공한다. </br>
초기에는 페이스북에서 개발되었지만 넷플릭스등과 같은 회사에서 사용되고 있으며 개발되고 있다.</br>

아파치 하이브는 아파치 HDFS이나 아파치 HBase와 같은 데이터 저장 시스템에 저장되어 있는 대용량 데이터 집합들을 분석한다.</br> 
HiveQL 이라고 불리는 SQL같은 언어를 제공하며 맵리듀스의 모든 기능을 지원한다. </br>
쿼리를 빠르게 하기위해 비트맵 인덱스를 포함하여 인덱스 기능을 제공한다.</br>

기본적으로 하이브는 메타데이터를 내장된 아파치 더비(Derby) 데이터 베이스안에 저장한다. </br>
그렇지만 MySQL과 같은 다른 서버/클라이언트 데이터 베이스를 사용할 수 있는 선택권을 제공한다. </br>
현재 TEXTFILE, SEQUENCEFILE, ORC 그리고 RCFILE등 4개의 파일 포맷을 지원한다.

## 하이브Hive란? 
* 하둡 데이터(파일)를 SQL과 비슷한 쿼리를 이용해서 다룰 수 있게 해 주는 기술 
* DW 어플리케이션에 적합 
  * 하둡에 기반 
    * 대량의 데이터를 배치 연산 
    * 레코드 단위 갱신/삭제, 트랜잭션 지원 안 함 
  * SQL과 유사한 하이브 QL 지원 
    * 테이블 생성, 데이터 조회, 테이블 간 조인 지원
    
## 하이브 DB 
* 데이터베이스 
  * 테이블의 네임스페이스 
  * 테이블을 논리적인 묶음으로 구성하는데 사용 
* 관련 쿼리 
  * 생성 쿼리 
    * CREATE DATABASE dbname; 
    * CREATE DATABASE IF NOT EXISTS dbname; 
  * 조회 
    * SHOW DATABASES; 
  * 설명 
    * DESCRIBE DATABASE dbname;

## 데이터베이스와 파일시스템 
데이터베이스/테이블은 파일시스템 경로와 매칭 
```hive
hive> describe database madvirus; 
OK 
default Default Hive database hdfs://bt0: 9000/user/hive/warehouse/madvirus.db 

hive> describe database default; 
OK 
default Default Hive database hdfs://bt0: 9000/user/hive/warehous
```

## 하이브 테이블 
* 테이블 
  * RDBMS 테이블과 유사한 스키마 가짐 
  * 파일 시스템의 파일을 테이블 데이터로 사용 
* 테이블의 종류 
  * 관리(managed) 테이블 
    * 하이브가 데이터 파일을 관리 
    * 테이블 삭제시 메타 정보와 파일 함께 삭제 
  * 외부(external) 테이블 
    * 기존 파일을 테이블의 데이터로 사용 
    * 테이블 삭제시 메타 정보만 삭제

## 관리 테이블 생성 
생성 쿼리 
```
CREATE TABLE IF NOT EXISTS dbname.tablename ( 
  컬럼1이름 타입 COMMENT ‘설명’, 
  컬럼2이름 타입 COMMENT ‘설명2’, 
  … 
  컬럼n이름 타입 COMMENT ‘설명n’ 
) COMMENT ‘테이블 설명’ 
ROW FORMAT DELIMITED 
FIELDS TERMINATED BY ‘001’ 
LINES TERMINATED BY ‘n’ 
LOCATION ‘/user/hive/warehouse/dbname.db/tablename’ 
; 
```
dbname: [dbname.] 생략시 default db 사용 
LOCATION: 
* 테이블 데이터가 보관될 디렉토리 경로 
* 지정하지 않을 경우 데이터베이스 디렉토리의 하위 디렉토리에 생성 구분자: 001 = ^A, 002 = ^B, 003 = ^C

