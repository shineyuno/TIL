# ConcurrentModificationException thrown by sublist 

java8 구현체에서 ArrayList의 subList()를 사용할 때에도 ConcurrentModificationException이 발생

```java
    List<String> list = new ArrayList<String>();
    String a = "a";
    String b = "b";
    String c = "c";
    String d = "d";

    list.add(a);
    list.add(b);
    list.add(c);

    List<String> backedList = list.subList(0, 2);
    list.add(0, d); 
    System.out.println("2b: " + backedList);
```
list.add (0, d)에 의해 ConcurrentModificationException 예외가 발생합니다. 

## 해결
sublist를 만들때 새로운 리스트로 생성 
```java
List<String> backedList = new ArrayList<>(list.subList(0, 2));
```

## 이유
subList는 원본 목록의 간단한보기입니다 ([참조](https://docs.oracle.com/javase/6/docs/api/java/util/List.html#subList%28int,%20int%29)). 
당신은 그 안의 요소를 변경할 수는 있지만 목록의 구조를 변경해서는 안됩니다.

설명서에 따르면 구조적으로 변경하려고하면 하위 목록 동작이 정의되지 않습니다. 
이 특정 구현에서 ConcurrentModificationException은 정의되지 않은 동작으로 결정되었습니다.

백킹리스트 (즉,이 리스트)가 리턴 된리스트를 통하지 않고 다른 방식으로 구조적으로 수정되면 이 메소드에 의해 리턴된 리스트의 의미는 정의되지 않습니다. 
(구조적 수정은이 목록의 크기를 변경하거나 진행중인 반복이 잘못된 결과를 생성 할 수있는 방식으로 방해합니다.)


# java.util.ConcurrentModificationException Streams

다음 코드 Java 8 SE를 시도했지만  아래에 언급 된 예외가 발생
```java

List<String> test = new ArrayList<>();
test.add("A");
test.add("B");
test.add("c");
test = test.subList(0, 2);
Stream<String> s = test.stream();
test.add("d");
s.forEach(System.out::println);
```

```java
Exception in thread "main" java.util.ConcurrentModificationException
    at java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1388)
    at java.util.stream.ReferencePipeline$Head.forEach(ReferencePipeline.java:580)
```

## 이유
### Java-8 [Bug]
The code above executed with Java-8 throws a CME. According to the [javadoc of ArrayList](https://docs.oracle.com/javase/8/docs/api/)

이 클래스의 이터레이터와 listIterator 메소드에 의해 리턴 된 이터레이터는 빠르다 : 
이터레이터가 작성된 후 언제라도리스트가 구조적으로 수정되면, 
이터레이터 자체의 remove 또는 add 메소드를 제외하고 어떤 식 으로든 이터레이터는 ConcurrentModificationException을 발생시킨다.

따라서, 동시 수정에 직면하여, 반복기는 미래에 결정되지 않은 시간에 임의의 비 결정적 행동을 위험에 빠뜨리기보다는 신속하고 깨끗하게 실패합니다.

# Why is a ConcurrentModificationException thrown and how to debug it
Collection (JPA에서 간접적으로 사용하는 HashMap)을 사용하고 있지만 분명히 무작위로 코드에서 ConcurrentModificationException이 발생합니다. 
무엇이 원인이며이 문제를 어떻게 해결합니까?

## 해결책
This is not a synchronization problem. This will occur if the underlying collection that is being iterated over is modified by anything other than the Iterator itself.
```java
Iterator it = map.entrySet().iterator();
while (it.hasNext())
{
   Entry item = it.next();
   map.remove(item.getKey());
}
```
This will throw a ConcurrentModificationException when the it.hasNext() is called the second time.

The correct approach would be
```java
   Iterator it = map.entrySet().iterator();
   while (it.hasNext())
   {
      Entry item = it.next();
      it.remove();
   }
```   
Assuming this iterator supports the remove() operation.



# 참조
https://stackoverflow.com/questions/8817608/concurrentmodificationexception-thrown-by-sublist

https://stackoverflow.com/questions/602636/why-is-a-concurrentmodificationexception-thrown-and-how-to-debug-it

https://github.com/shineyuno/TIL/new/master/waste_of_time_shoveling

https://knight76.tistory.com/entry/Java8%EC%9D%98-ArrayListsubList%ED%95%9C%ED%9B%84-Iterate%EC%8B%9C-ConcurrentModificationException-%EB%B0%9C%EC%83%9D
