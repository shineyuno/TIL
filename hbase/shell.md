# Hbase  shell 명령어 몇가지

### list
+ 테이블 리스트 조회 
```sh
hbase(main):001:0> list
TABLE
...
name_change_histories
...
16 row(s) in 0.5920 seconds
```

### get
+ 테이블 row키로 조회 
+ 사용법 : get 'TABLE_NAME', 'ROWKEY'
```sh
hbase(main):002:0> get 'name_change_histories', '0000000032_1440688034872'
COLUMN                                   CELL
 f:req                                   timestamp=1440688034895, value={"old_locale":"ko","new_last_name":"\xEC\xA2\x85\xED\x98\xB8","old_first_name":"\xEA\
                                         xB9\x80","new_locale":"ko","old_last_name":"\xEC\xA2\x85\xED\x98\xB8","new_first_name":"\xEA\xB9\x80"}
 f:updated                               timestamp=1440688034895, value={"old_locale":"ko","new_last_name":"\xEC\xA2\x85\xED\x98\xB8","old_first_name":"\xEA\
                                         xB9\x80","new_locale":"ko","old_last_name":"\xEC\xA2\x85\xED\x98\xB8","new_first_name":"\xEA\xB9\x80"}
```

### scan
+ 테이블 조회
+ 사용법 : scan 'TABLE_NAME', 옵션 
+ LIMIT 옵션 : 리밋숫자만큼 rowkey 반환 

```sh
hbase(main):005:0> scan 'name_change_histories', { LIMIT => 1}
ROW                                                COLUMN+CELL
 0000000017_1426867222964                          column=f:req, timestamp=1426867222981, value={"new_locale":"ko","new_last_name":"\xEC\xA3\xBC\xEC\x97\xB0","old_last_name":"\xEC\xA3\xBC\xEC\x97\
                                                   xB0 Janice","old_locale":"ko","old_first_name":"\xEC\x86\xA1","new_first_name":"\xEC\x86\xA1"}
 0000000017_1426867222964                          column=f:updated, timestamp=1426867222982, value={"new_locale":"ko","new_last_name":"\xEC\xA3\xBC\xEC\x97\xB0","old_last_name":"\xEC\xA3\xBC\xEC\
                                                   x97\xB0 Janice","old_locale":"ko","old_first_name":"\xEC\x86\xA1","new_first_name":"\xEC\x86\xA1"}
```


#### ColumnCountGetFilter
+ 컬럼갯수제한 필터
+ 이 필터는 로우당 지정한 최대 개수만큼의 컬럼만 반환받는 용도로 사용
```
hbase(main):007:0> scan 'name_change_histories', { LIMIT => 1,FILTER =>"ColumnCountGetFilter(1)" }
ROW                                                COLUMN+CELL
 0000000017_1426867222964                          column=f:req, timestamp=1426867222981, value={"new_locale":"ko","new_last_name":"\xEC\xA3\xBC\xEC\x97\xB0","old_last_name":"\xEC\xA3\xBC\xEC\x97\
                                                   xB0 Janice","old_locale":"ko","old_first_name":"\xEC\x86\xA1","new_first_name":"\xEC\x86\xA1"}
```
