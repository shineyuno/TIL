# NGINX 설정
NGINX의 설정은 NGINX가 어떻게 동작해야 하는가를 지정하는 기능으로 파일에 설정 값을 기술한다. 설정 파일은 conf 디렉토리 아래에 위치하고, 설치 방법에 따라서 설정 파일의 위치가 다를 수 있다. 컴파일을 통해서 설치한 경우라면 기본적으로 /usr/local/nginx/conf 디렉토리에 위치하고, apt-get을 이용해서 우분투에 설치한 경우는 /etc/nginx에 위치한다. 설치 방법에 따라서 환경설정 파일의 위치는 다를 수 있기 때문에 아래의 명령을 이용하면 설정 파일의 위치를 쉽게 찾을 수 있다. 

```
sudo find / -name nginx.conf
```
## 설정 파일의 역할
* nginx.conf : 메인 설정 파일. 
* fcgi.conf : FastCGI 환경설정 파일
* sites-enabled : 활성화된 사이트들의 설정 파일들이 위치. 아파치에서는 Virtual host의 설정에 해당한다. 기본적으로 존재하지 않을수도 있다. 이 디렉토리를 직접 만들어서 사용하는 방법은 가상 호스팅편에서 알아본다.
* sites-available : 비활성화된 사이트들의 설정 파일들이 위치

## 기본설정의 구분
NGINX의 환경설정을 이해하기 위해서는 환경설정의 구조를 파악해야 한다. 아래는 nginx 메인 환경설정 파일인 nginx.conf의 내용이다. 

```
worker_processes  1;
events {
    worker_connections  1024;
}
http { 
    include       mime.types;
    server {
        listen       80;
        location / {
            root   html;
            index  index.html index.htm;
        }
    }
}
```
위의 예제를 바탕으로 각각의 부분을 파악해보자.

### Core 모듈 설정
위의 예의 work_processes와 같은 지시자 설정 파일 최상단에 위치하면서 nginx의 기본적인 동작 방식을 정의한다. 여기서 사용되는 지시어들은 다른 곳에서 사용되지 않는다. 코어모듈 지시어 사전을 참고하자. 

### http 블록
http 블록은 이후에 소개할 server, location의 루트 블록이라고 할 수 있고, 여기서 설정된 값을 하위 블록들은 상속한다. http 블록은 여러개를 사용할 수 있지만 관리상의 이슈로 한번만 사용하는 것을 권장한다. 

http, server, location 블록은 계층구조를 가지고 있다. 많은 지시어가 각각의 블록에서 동시에 사용할 수 있는데, http의 내용은 server의 기본값이 되고, server의 지시어는 location의 기본값이 된다. 그리고 하위의 블록에서 선언된 지시어는 상위의 선언을 무시하고 적용된다. 

### server 블록
server 블록은 하나의 웹사이트를 선언하는데 사용된다. 가상 호스팅(Virtual Host)의 개념이다. 예를들어 하나의 서버로 http://opentutorials.org 과 http://egoing.net 을 동시에 운영하고 싶은 경우 사용할 수 있는 방법이다. 가상 호스팅에 대한 자세한 내용은 가상 호스팅 수업을 참고하자. 

### location 블록
location 블록은 server 블록 안에 등장하면서 특정 URL을 처리하는 방법을 정의한다. 이를테면 http://opentutorials.org/course/1 과 http://opentutorials.org/module/1 로 접근하는 요청을 다르게 처리하고 싶을 때 사용한다. 

### events 블록
이벤트 블록은 주로 네트워크의 동작방법과 관련된 설정값을 가진다. 이벤트 블록의 지시어들은 이벤트 블록에서만 사용할 수 있고, http, server, location와는 상속관계를 갖지 않는다. 이벤트 모듈 지시어에 대한 설명은 이벤트 모듈 지시어 사전을 참고한다.

### 설정 파일의 반영
설정 파일의 내용을 변경한 후에는 이를 NGINX에 반영해야 하는데 아래와 같이 reload 명령을 이용한다. restart를 이용해도 되지만 권장되지 않는다. 
```
sudo service nginx reload;
```
