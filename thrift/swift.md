# Swift

Swift는 Thrift 직렬화 가능 유형 및 서비스를 작성하기위한 사용하기 쉬운 annotation-based Java 라이브러리입니다.

# Swift Codec
Swift Codec 은 Java 오브젝트가 Thrift로 변환되는 방식과 Thrift에서 변환되는 방식을 지정하는 간단한 라이브러리입니다. 
이 라이브러리는 JaxB (XML) 및 Jackson (JSON)과 유사하지만 Thrift 용입니다. 
Swift 코덱은 필드, 메소드, 생성자 및 빌더 주입을 지원합니다. 예 :

```java
@ThriftStruct
public class LogEntry
{
    private final String category;
    private final String message;

    @ThriftConstructor
    public LogEntry(String category, String message)
    {
        this.category = category;
        this.message = message;
    }

    @ThriftField(1)
    public String getCategory()
    {
        return category;
    }

    @ThriftField(2)
    public String getMessage()
    {
        return message;
    }
}
```

# Swift Service
Swift Service 는 Thrift와 함께 내보내는 서비스에 annotating을 달아주는 간단한 라이브러리입니다. 예 :

```java
@ThriftService("scribe")
public class InMemoryScribe
{
    private final List<LogEntry> messages = new ArrayList<>();

    public List<LogEntry> getMessages()
    {
        return messages;
    }

    @ThriftMethod("Log")
    public ResultCode log(List<LogEntry> messages)
    {
        this.messages.addAll(messages);
        return ResultCode.OK;
    }
}
```
# Swift Generator
Swift Generator 는 Thrift IDL 파일의 Swift 코덱에서 사용할 수있는 Java 코드를 생성하는 라이브러리이며 그 반대의 경우도 마찬가지입니다.

# Swift Maven plugin
Swift Maven 플러그인을 사용하면 Maven 빌드의 코드 생성기를 사용하여 즉시 소스 코드를 생성 할 수 있습니다.

# 참고
https://github.com/facebookarchive/swift
