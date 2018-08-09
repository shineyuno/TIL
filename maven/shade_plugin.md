# Apache Maven Shade Plugin
의존성을 포함한 실행 가능한 single jar(이하 uber-jar) 파일을 생성해 주는 플러그인

shade 플러그인의 강력함은, 배포시에 필요한 라이브러리들을 'exclude/include' 시킬 수있고, </br>
라이브러리 수준뿐만 아니라 class 파일 수준으로 jar 파일을 minimize 함으로서 보다 가벼운 jar 파일을 생성할 수 있다는 것이다.


## 기본 사용법

아래는 기본적인 사용법이다.
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>2.4.3</version>
            <configuration>
                <!-- put your configurations here -->                         
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
메이븐 골(goal)로 'shade:shade' 를 입력하여 직접 구동 시킬 수 있지만, </br> 
<executions> 설정으로 package phase에 shade 골을 바인딩 하는 설정을 하면, 'mvn package' 으로 구동 시킬 수 있다.


## Resource Transformer

shade 플러그인을 적용 할때  'Resource Transformer' 라는 개념을 이해할 필요가 있다 

Resource Transformer 설정을 하면 서로 다른 artifacts 들로부터 uber-jar 를 생성할때, </br> 
classes 및 resources 파일들을 '중복없이' 패키징 할 수 있게 해준다.


이들 설정 가운데 흔히 쓰이는 몇가지 설정만 살펴보자.

### (1) ManifestResourcesTransformer - 실행 가능 jar 만들기

여기서 주로 쓰이는 것은 ManifestResourcesTransformer 인데, 설명에 나와있는대로 자바 'MANIFEST' 파일의 entries 를 세팅해 준다.

아래와 같이 <configuration> 설정에 추가한다
```xml
<configuration>
    <transformers>
        <transformer
            implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
            <mainClass>com.asuraiv.project.aaa.MainClass</mainClass>
        </transformer>
    </transformers>
</configuration>
```
실행 가능한 jar 파일을 생성할시에 자바 어플리케이션을 구동할 MainClass를 지정해야하는데, </br>
이것은 'MANIFEST' 파일의 entry 중 하나이다. 
위 예제처럼 <mainClass> 설정으로 해당 어플리케이션의 메인클래스를 입력한다.


### (2) AppendingTransformer

만약 스프링 batch 프로젝트를 shade 플러그인을 통해 'Executable JAR' 파일로 패키징 한다고 하자. 그럴 경우 Main 클래스는 스프링 batch job을 커맨드 라인에서 실행 할 수 있게 해주는 'org.springframework.batch.core.launch.support.CommandLineJobRunner' 가 된다.

그러면 ManifestResourceTransformer의 <mainClass> 설정만 해당 클래스로 설정해주면 될까?
그것만 해서는 안된다.

스프링으로 구성된 어플리케이션은 스프링 컨텍스트 xml의 namespace를 핸들링 해주는 Handler 클래스들이 정의되어 있는 spring.handlers 파일과, 스프링 컨텍스트 xml 설정 파일의 스키마(xsd 파일 등)가 정의되어 있는 spring.schemas 파일이 필요하다.

바로 이때, AppendingTransformer 설정을 사용하여 uber-jar에 포함 시킬 수 있다.
```xml
<configuration>
    <transformers>
        <transformer
            implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
            <mainClass>org.springframework.batch.core.launch.support.CommandLineJobRunner</mainClass>                                   
        </transformer>
        <transformer
            implementation="org.apache.maven.plugins.shade.resource.AppendingTransformer">
            <resource>META-INF/spring.handlers</resource>
        </transformer>
        <transformer
            implementation="org.apache.maven.plugins.shade.resource.AppendingTransformer">
            <resource>META-INF/spring.schemas</resource>
        </transformer>
</configuration>
```
기본적으로 스프링 라이브러리 jar 파일(spring-context, spring-aop, spring-beans 등등...)을 까보면 </br> 
각각 META-INF 밑에 spring.handlers, spring.schemas 파일이 존재한다. </br>

앞서 설명했듯이 shade 플러그인이 이 모든 의존관계 라이브러리들을 한데 묶어서 uber-jar 를 생성할때, </br>
위 2개의 파일들이 각각 스프링 라이브러리에 동일한 이름으로 존재(하지만 그 내용은 또 각각 다르다)하기 때문에 중복의 문제가 존재한다. </br>

따라서 AppendingTransformer 설정으로 해당 파일들을 포함시키면, </br>
마치 'merge' 를 하는것과 같이 각 라이브러리의 핸들러, 스키마 정보들이 각각 하나의 </br> 
spring.handlers, spring.shcemas 파일로 생성되는 것이다. 

 
 
 ## 참고
 [Apache Maven Shade Plugin](http://maven.apache.org/plugins/maven-shade-plugin/index.html) </br>
 [Executable JAR](http://maven.apache.org/plugins/maven-shade-plugin/examples/executable-jar.html) </br>
[http://asuraiv.blogspot.com/2016/01/maven-shade-plugin-1-resource.html](http://asuraiv.blogspot.com/2016/01/maven-shade-plugin-1-resource.html)
