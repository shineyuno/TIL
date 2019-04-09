# JSON_CONTAINS(target, candidate[, path])

지정된 candidate JSON 문서가  target JSON 문서에 포함되어 있거나, 또는 path 인수가 제공된 경우 
타겟 내의 특정 경로에 후보가 있는지를 1 또는 0으로 반환하여 표시 합니다. 
NULL인수가있는 경우 NULL또는 경로 인수가 대상 문서의 섹션을 식별하지 않는 경우를 반환 합니다. 

target또는 candidate가  유효한 JSON 문서가 아니거나, path 인수가 유효한 경로 표현식이 아니거나 
a* 또는 ** 와일드 카드를 포함하는 경우 오류가 발생합니다.

경로에 데이터가 있는지 여부 만 확인하려면, 대신에 JSON_CONTAINS_PATH()를 사용하십시오.

+ candidate 스칼라는 target 스칼라에 포함됩니다. 대상 스칼라가 비교 가능하고 동등한 경우에만 해당됩니다.
두 개의 스칼라 값은 JSON_TYPE () 유형이 같은 경우 비교할 수 있습니다.
단, INTEGER 및 DECIMAL 유형의 값도 서로 비교할 수 있습니다.
+ candidate 배열인경우 candidate 모든 요소가 target의 일부 요소에 포함되어있는 경우에만 target의 배열에 포함됩니다.
+ candidate 비배열인경우 후보가 대상의 일부 요소에 포함되어있는 경우에만 target의 배열에 포함됩니다.
+ candidate의 각 키에 대해 target에 동일한 이름을 가진 키가 있고 candidate 키와 관련된 값이 target 키와 관련된 값에 포함되는 경우에만 target 객체에 candidate 객체가 포함됩니다.

위와같지 않으면 candidate의 값이 target 문서에 포함되지 않습니다.

```sql
mysql> SET @j = '{"a": 1, "b": 2, "c": {"d": 4}}';
mysql> SET @j2 = '1';
mysql> SELECT JSON_CONTAINS(@j, @j2, '$.a');
+-------------------------------+
| JSON_CONTAINS(@j, @j2, '$.a') |
+-------------------------------+
|                             1 |
+-------------------------------+
mysql> SELECT JSON_CONTAINS(@j, @j2, '$.b');
+-------------------------------+
| JSON_CONTAINS(@j, @j2, '$.b') |
+-------------------------------+
|                             0 |
+-------------------------------+

mysql> SET @j2 = '{"d": 4}';
mysql> SELECT JSON_CONTAINS(@j, @j2, '$.a');
+-------------------------------+
| JSON_CONTAINS(@j, @j2, '$.a') |
+-------------------------------+
|                             0 |
+-------------------------------+
mysql> SELECT JSON_CONTAINS(@j, @j2, '$.c');
+-------------------------------+
| JSON_CONTAINS(@j, @j2, '$.c') |
+-------------------------------+
|                             1 |
+-------------------------------+
```



참고
+ https://dev.mysql.com/doc/refman/5.7/en/json-search-functions.html#function_json-contains
+ https://www.slideshare.net/LeeIGoo/mysql-57-nf-json-datatype
+ https://stackoverflow.com/questions/35198140/mysql-index-json-arrays-of-variable-length
