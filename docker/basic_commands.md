# 도커 기본 명령어

## 컨테이너 목록 확인하기 (ps)
컨테이너 목록을 확인하는 명령어는 다음과 같습니다.
```sh
docker ps [OPTIONS]
```
일단 기본옵션과 -a, --all 옵션만 살펴봅니다.

```sh
docker ps
```
output:
```sh
CONTAINER ID        IMAGE                           COMMAND                  CREATED              STATUS              PORTS                                                    NAMES
6a1d027b604f        teamlab/pydata-tensorflow:0.1   "/opt/start"             About a minute ago   Up About a minute   0.0.0.0:6006->6006/tcp, 22/tcp, 0.0.0.0:8888->8888/tcp   desperate_keller
52a516f87ceb        wordpress                       "docker-entrypoint.sh"   8 minutes ago        Up 8 minutes        0.0.0.0:8080->80/tcp                                     happy_curran
2e2c569115b9        mysql:5.7                       "docker-entrypoint.sh"   9 minutes ago        Up 9 minutes        0.0.0.0:3306->3306/tcp                                   mysql
56341072b515        redis                           "docker-entrypoint.sh"   16 minutes ago       Up 9 minutes        0.0.0.0:1234->6379/tcp                                   furious_tesla
```
ps 명령어는 실행중인 컨테이너 목록을 보여줍니다. detached mode로 실행중인 컨테이너들이 보입니다. 
어떤 이미지를 기반으로 만들었는지 어떤 포트와 연결이 되어있는지 등 간단한 내용을 보여줍니다.

이번에는 -a 옵션을 추가로 실행해보겠습니다.
```sh
docker ps -a
```
output:
```sh
CONTAINER ID        IMAGE                           COMMAND                  CREATED             STATUS                      PORTS                                                    NAMES
6a1d027b604f        teamlab/pydata-tensorflow:0.1   "/opt/start"             2 minutes ago       Up 2 minutes                0.0.0.0:6006->6006/tcp, 22/tcp, 0.0.0.0:8888->8888/tcp   desperate_keller
52a516f87ceb        wordpress                       "docker-entrypoint.sh"   9 minutes ago       Up 9 minutes                0.0.0.0:8080->80/tcp                                     happy_curran
2e2c569115b9        mysql:5.7                       "docker-entrypoint.sh"   10 minutes ago      Up 10 minutes               0.0.0.0:3306->3306/tcp                                   mysql
56341072b515        redis                           "docker-entrypoint.sh"   18 minutes ago      Up 10 minutes               0.0.0.0:1234->6379/tcp                                   furious_tesla
e1a00c5934a7        ubuntu:16.04                    "/bin/bash"              32 minutes ago      Exited (0) 32 minutes ago                                                            berserk_visvesvaraya
```
맨 처음 실행했다가 종료된 컨테이너(Exited (0))가 추가로 보입니다. 컨테이너는 종료되어도 삭제되지 않고 남아있습니다. 
종료된 건 다시 시작할 수 있고 컨테이너의 읽기/쓰기 레이어는 그대로 존재합니다. 
명시적으로 삭제를 하면 깔끔하게 컨테이너가 제거됩니다.



## 이미지 목록 확인하기 (images)
도커가 다운로드한 이미지 목록을 보는 명령어는 다음과 같습니다.
```sh
docker images [OPTIONS] [REPOSITORY[:TAG]]
```
간단하게 도커 이미지 목록을 확인해보겠습니다.
```sh
docker images
```
output:
```sh
REPOSITORY                  TAG                 IMAGE ID            CREATED             SIZE
wordpress                   latest              b1fe82b15de9        43 hours ago        400.2 MB
redis                       latest              45c3ea2cecac        44 hours ago        182.9 MB
mysql                       5.7                 f3694c67abdb        46 hours ago        400.1 MB
ubuntu                      16.04               104bec311bcd        4 weeks ago         129 MB
teamlab/pydata-tensorflow   0.1                 7bdf5d7e0191        6 months ago        3.081 GB
```
이미지 주소와 태그, ID, 생성시점, 용량이 보입니다. 
이미지가 너무 많이 쌓이면 용량을 차지하기 때문에 사용하지 않는 이미지는 지우는 것이 좋습니다.

### 이미지 다운로드하기 (pull)
이미지를 다운로드하는 명령어는 다음과 같습니다.
```sh
docker pull [OPTIONS] NAME[:TAG|@DIGEST]
```
ubuntu:14.04를 다운받아보겠습니다.
```sh
docker pull ubuntu:14.04
```
run명령어를 입력하면 이미지가 없을 때 자동으로 다운받으니 pull명령어를 언제 쓰는지 궁금할 수 있는데 pull은 최신버전으로 다시 다운 받습니다. 
같은 태그지만 이미지가 업데이트 된 경우는 pull명령어를 통해 새로 다운받을 수 있습니다.


## 컨테이너 실행하기

도커를 실행하는 명령어는 다음과 같습니다.
```sh
docker run [OPTIONS] IMAGE[:TAG|@DIGEST] [COMMAND] [ARG...]
```
다음은 자주 사용하는 옵션들입니다.

```
옵션	설명
-d	detached mode 흔히 말하는 백그라운드 모드
-p	호스트와 컨테이너의 포트를 연결 (포워딩)
-v	호스트와 컨테이너의 디렉토리를 연결 (마운트)
-e	컨테이너 내에서 사용할 환경변수 설정
–name	컨테이너 이름 설정
–rm	프로세스 종료시 컨테이너 자동 제거
-it	-i와 -t를 동시에 사용한 것으로 터미널 입력을 위한 옵션
–link	컨테이너 연결 [컨테이너명:별칭]
```
엄청나게 직관적인 옵션으로 몇번 실행해보면 자연스럽게 익숙해집니다.

### ubuntu 16.04 container
시작은 가볍게 ubuntu 16.04 컨테이너를 생성하고 컨테이너 내부에 들어가 봅니다.
```sh
docker run ubuntu:16.04
➜  ~ docker run ubuntu:16.04                                                    
Unable to find image 'ubuntu:16.04' locally                                     
16.04: Pulling from library/ubuntu                                              
                                                                                
b3e1c725a85f: Downloading  12.7 MB/50.22 MB                                     
4daad8bdde31: Download complete                                                 
63fe8c0068a8: Download complete                                                 
4a70713c436f: Download complete                                                 
bd842a2105a8: Download complete                                                 
```    
run명령어를 사용하면 사용할 이미지가 저장되어 있는지 확인하고 없다면 다운로드(pull)를 한 후 컨테이너를 생성(create)하고 시작(start) 합니다.

위 예제는 ubuntu:16.04 이미지를 다운받은 적이 없기 때문에 이미지를 다운로드 한 후 컨테이너가 실행되었습니다. 
컨테이너는 정상적으로 실행됐지만 뭘 하라고 명령어를 전달하지 않았기 때문에 컨테이너는 생성되자마자 종료됩니다. 
컨테이너는 프로세스이기 때문에 실행중인 프로세스가 없으면 컨테이너는 종료됩니다.
                                                                                
## 참고
https://subicura.com/2017/01/19/docker-guide-for-beginners-2.html
                                                                                
                                                                                
                                                                                
                                                                                
                                                                                
                                                                                
                                                                                
                                                                                
                                                                                
                                                                                
                                                                                
                                                                                
                                                                                
00:00
run ubuntu 16.04 container
run명령어를 사용하면 사용할 이미지가 저장되어 있는지 확인하고 없다면 다운로드(pull)를 한 후 컨테이너를 생성(create)하고 시작(start) 합니다.
