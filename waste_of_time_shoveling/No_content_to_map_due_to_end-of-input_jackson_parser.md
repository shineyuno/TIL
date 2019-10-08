# [오늘의 삽질] No content to map due to end-of-input jackson parser
오늘 개발을 하면서 위와 같은 No content to map due to end-of-input jackson parser 에러 메시지를 만났다.
스택오버 플로우에 비슷한 상황에 대한 질의 응답이 있어 정리해 보았다. 


서버에서 이런 응답을 내려주고 `{"status":"true","msg":"success"}`

Jackson 파서 라이브러리를 사용 하여 이 json 문자열을 파싱 하려고 했는데 매핑 예외에 직면했다.

```java
com.fasterxml.jackson.databind.JsonMappingException: No content to map due to end-of-input
 at [Source: java.io.StringReader@421ea4c0; line: 1, column: 1]
```


파싱은 다음과 같은 방법으로 하였다.
```java
StatusResponses loginValidator = null;

ObjectMapper objectMapper = new ObjectMapper();
objectMapper.configure(Feature.AUTO_CLOSE_SOURCE, true);

try {
    String res = result.getResponseAsString();//{"status":"true","msg":"success"}
    loginValidator = objectMapper.readValue(result.getResponseAsString(), StatusResponses.class);
} catch (Exception e) {
    e.printStackTrace();
}
```


해결법
```java
try {
    String res = result.getResponseAsString();//{"status":"true","msg":"success"}
    loginValidator = objectMapper.readValue(res, StatusResponses.class); // result.getResponseAsString() 또 호출하지 않는다.
} catch (Exception e) {
    e.printStackTrace();
}
```

## 원인
에러 문구를 보면 
+ `No content to map due to end-of-input` 라고 써있다.  input 이 끝나서 맵에 넣을 콘텐트가 없다고 나온다.
+ getResponseAsString()을 호출하면 응답에서 모든 바이트를 읽고 연결을 닫기 때문에 다시 호출되면 null이 반환돼 파싱에러가 난다. 

HttpMethod.html#getResponseBodyAsStream() 메소드 설명을 보면 아래와 같이 나와있다. 
+ HTTP 메소드의 응답 본문이있는 경우 InputStream으로 리턴합니다. 
+ 응답에 본문이 없거나 메소드가 아직 실행되지 않은 경우 null이 리턴됩니다. 
+ 또한 releaseConnection ()이 호출되었거나이 메서드가 이전에 호출되어 결과 스트림이 닫히면 null이 반환 될 수 있습니다.
+ https://hc.apache.org/httpclient-3.x/apidocs/org/apache/commons/httpclient/HttpMethod.html#getResponseBodyAsStream()

## 참고
https://stackoverflow.com/questions/26925058/no-content-to-map-due-to-end-of-input-jackson-parser
