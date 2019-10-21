# 원격으로 호스트목록에 명령어 실행하는 쉘 작성

원격으로 명령어를 날려야 호스트 목록이 들어있는 production.yml  파일이 아래와 같을때

```yml
hosts:
  - webapp001.server.io
  - webapp002.server.io
  - webapp003.server.io
  - webapp004.server.io
  - webapp005.server.io
```


파일에 있는 host 목록을 읽어와 외부에서 입력받은 명령어를 실행하고 결과를 화면에 출력하는 쉘 프로그램 작성
```bash
#!/bin/sh

BASEDIR=$(dirname $0)

command="$@"
servers=`cat ${BASEDIR}/../projects/파일위치.../hosts/production.yml | egrep '^ *-' | awk '{ print $2 }'`
for server in $servers
do

    echo "$server ______________________________________"
    result=`ssh -o StrictHostKeyChecking=no deploy@$server "$command"`
    echo "$result"
    echo
done
```

위 쉘 파일명이 app.sh 일때 이를 이용해 명령어 날리는 예
```bash
## 호스트 목록 디스크 용량 확인
$ ./app.sh df -h

## /request_201901~02 디렉토리안에 로그 파일 확인 ''따옴표로 여러 파일위치를 줄수있다.
$./app.sh ls '/파일위치/logs/request_201901*' '/파일위치/logs/request_201902*'

## /request_201901~02 디렉토리안에 로그 파일 삭제
$./app.sh rm -rf '/파일위치/logs/request_201901*' '/파일위치/logs/request_201902*'
```
