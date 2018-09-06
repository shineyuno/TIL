# hive 쿼리 결과 파일로 뽑기

hive 쿼리 들어있는 파일 준비
```sql
select distinct(b.id) from tmp a, sr.pro b where 1=1 and a.updated >= '2018-09-03 00:00:00' and a.id = b.id  and b.date='2018-09-04' ;
```

위에서 만든 파일명이 tmp_invitations_0902.hql 였다면
서버 커맨드 창에 아래와 같이 hive 명령어 실행 
```bash
$ hive -f tmp_invitations_0902.hql > file_tmp_invitations_0903.csv
```
