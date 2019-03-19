# 로컬 환경에서 버전이 다른 CDH에 접속하기
0.9 버전대 hbase에서 cdh4에 작동하지만 cdh5에서는 정상 작동하지 않는다.
반대로 1.0 버전대 hbase에서는 cdh5에  작동하지만 cdh4에서 정상 작동하지 않는다.

로컬에서 각 버전별로 hbase shell 접속 하는 방법에 대해 알아보자

로컬에  hbase가 두가지 버전이 설치되어있다는 가정하에 진행
HBASE_CONF_DIR 환경변수 설정한후 접속한다.

HBASE_CONF_DIR 내용은 HBase Cloudera Manager CDH4, HBase Cloudera Manager CDH5 접속후 다운 받아 이용한다. 


Cloudera Manage에서 다운 받은 config 폴더안
hbase-site.xml 파일의 configuration 항목에는 접속할 HBase 서버주소(정확히는 zookeeper 주소)가 들어있다.

### CDH4 접속
hbase 0.9버전대가 깔린 폴더로 이동후 접속 
```sh
## HBASE 접속 hbase 0.9버전대가 깔린 폴더로 이동 
cd ~/.story-spec/opt/hbase/bin
 
 
## story-cdh4 HBASE_CONF_DIR 설정후 hbase shell 실행
❯ HBASE_CONF_DIR=/Users/leeyunho/Downloads/hbase-conf ./hbase shell
```


### CDH5 접속

##HBASE 접속 hbase 1.2버전대가 깔린 폴더로 이동후 접속
```sh
cd ~/.story-spec/opt/hbase12/bin
 
 
## story-counter HBASE_CONF_DIR 설정후 hbase shell 실행  
❯ HBASE_CONF_DIR=/Users/leeyunho/Downloads/hbase-conf53 ./hbase shell
```
