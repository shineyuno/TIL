# 쿼리때만 임시로 잠시쓸 가상 테이블 만들기
하고 싶었던것 : 매우 작은 데이터셋이 있고 이값이 다른테이블에 있는지 확인하고 싶었다.

회사 규칙상 db관리자를 통하지 않으면 실db에 테이블 생성을 할수없고
그리고 된다고 해도 일회성 확인 작업을 위해 테이블을 생성 하고 싶지 않았다.
이런경우 쿼리때만 임시로 쓸 가상 테이블 만들고 싶었고 아래 방법을 사용하였다.

select로 column을 만들고 union으로 row를 만듦
간단한예
```mysql
select * from
(
	select 1 as trackId union
	select 2 union
	select 3 union
	select 4 union
	select 5 union
	select 6 union
	select 7 union
	select 8 union
	select 9 union
	select 10 
) as b 
```
이렇게 한경우 컬럼명이 trackId이고 1-10까지 값이 있는 테이블 생성됨
