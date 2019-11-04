# builder pattern

```java
class Something {

    private Something(int number, String name, double size) {
        //Something 클래스 초기화
    }

    public static class Builder {
        int number=0;
        String name=null;
        double size=0d;

        public Builder() {
            //Builder 초기화
        }

        public Builder setNumber(int number) {
            this.number = number;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setSize(double size) {
            this.size = size;
            return this;
        }

        public Something build() {
            return new Something(number, name, size);
        }
    }
}
```

(Java 기준, 대상 클래스와 빌더 클래스)
```java
public void createSomething() {
    Something something = new Something.Builder().setNumber(number).setName(name).setSize(size).build();
}
```

(Java 기준, 빌더 클래스의 사용)

빌더 클래스는 인스턴스를 생성자를 통해 직접 생성하지 않고, 빌더라는 내부 클래스를 통해 간접적으로 생성하게 하는 패턴이다.

사용 목적에는 크게 두 가지로 나뉜다.

## 클래스와 사용 대상의 결합도를 낮추기 위해
어떤 클래스의 사양 변경으로 인해, 생성자에 인수로 전달해야 하는 부분의 규격이 변경되었다면 어떻게 수정해야 될까?
일반적인 패턴으로는 일단 해당 클래스를 수정한 후, 해당 클래스를 생성하는 모든 부분의 코드를 일일히 다 수정해야 할 것이다.(그렇지 않으면 컴파일 오류가 난다.)
혼자 만드는 건 어찌어찌 Ctrl+F로 코드 찾아가면서 해당 클래스의 생성자를 전부 찾아가면서 변경을 하겠지만, 
해당 부분이 다른 사람에게 배포하여 사용하는 Library같은 물건이라면?
Builder는 해당 문제점을 해결하기 위해 고안된 패턴이다.
대상 클래스의 생성자는 private 등의 접근 제한자로 제한하여 외부에서 임의로 접근하는 것을 막아 클래스와 사용대상의 결합도를 떨어뜨리고, 
대신 Builder라는 내부 클래스를 통해 해당 클래스를 간접적으로 생성한다.
Builder는 설정되지 않은 인수에 대해서는 적절한 값으로 초기화를 하여 해당 인수가 할당되지 않더라도 일단 컴파일 자체는 가능하며, 
사용자의 요청에 따라 상세한 값을 설정하는 것도 가능하다.
예를 들어, 위 Something 클래스에서 double weight라는 인수를 추가로 할당하려고 하면, 
전통적인 패턴에서는 위에 언급한대로 모든 생성자마다 double weight라는 단서를 추가로 달아야겠지만, 
Builder 패턴에서는 대상 클래스의 private 생성자에 weight를 추가하고, 
Builder에 setWeight(double weight) 하나만 추가하면 끝. 
기본값은 -1(설정되지 않음)으로 하면 수많은 코드들을 일일히 찾아다니지 않아도 기능 추가가 가능하다.

## 생성자에 전달하는 인수에 의미를 부여하기 위해
예를 들어서, 위에 제시된 예시에서 빌더 패턴이 없다고 가정하고 인스턴스를 생성하려면 
Something something = new Something(number, name, size); 이렇게 코드를 작성하여야 한다.
위의 예시에서는 인수가 세 개니까 그냥 저렇게 써도 큰 문제는 없지만, 
생성자에 전달하는 인수의 가짓수가 열 종류 가까이 되는 클래스의 경우에는 고전적인 생성자 패턴으로는 인수를 전달하는 것이 상당히 비직관적이 된다.
(인수의 종류를 외워서 써넣어야되는 것뿐만 아니라, 인수의 순서까지 고려해야 한다!)
그래서 빌더 패턴을 통해 setXXX 형식으로 인수를 전달하면 한 눈에 보기에도 이것이 무슨 인수인지를 파악하기가 쉽다.

빌더와 팩토리 패턴은 유사점이 많아 그냥 팩토리로 퉁쳐서 칭하기도 한다. 특히 자바 이외의 언어에서.

## 참고
https://namu.wiki/w/%EB%94%94%EC%9E%90%EC%9D%B8%20%ED%8C%A8%ED%84%B4
