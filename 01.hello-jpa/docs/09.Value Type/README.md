# 값 타입

<br>

## 기본값 타입

### JPA의 데이터 타입 분류
* 엔티티 타입(최상위 레벨)
  * ```@Entity```로 정의하는 객체
  * 데이터가 변해도 식별자로 지속해서 추적 가능
  * 예) 회원 엔티티의 키나 나이 값을 변경해도 식별자로 인식 가능
* 값 타입
  * ```int```, ```Integer```, ```String```처럼 단순히 값으로 사용하는 자바 기본 타입이나 객체
  * 식별자가 없고 값만 있으므로 변경시 추적 불가
  * 예) 숫자 100을 200으로 변경하면 완전히 다른 값으로 대체

<br>

### 값 타입 분류
* 기본값 타입
  * 자바 기본 타입(```int```, ```double```)
  * 래퍼 클래스(```Integer```, ```Long```)
  * ```String```
* 임베디드 타입(embedded type, 복합 값 타입)
* 컬렉션 값 타입(collection value type)

<br>

### 기본값 타입
* 예): ```String name```, ```int age```
* 생명주기를 엔티티에게 의존한다.
  * 예) 회원을 삭제하면 이름, 나이 필드도 함께 삭제
* 값 타입은 절대 공유하면 안된다.
  * 예) 회원 이름 변경시 다른 회원의 이름도 함께 변경되면 안됨(이를 사이드 이펙트라 한다.)

<br>

### 참고: 자바의 기본 타입은 절대 공유 하지 않는다.
* ```int```, ```double``` 같은 기본 타입(primitive type)은 절대 공유하지 않는다.
  * ```java
    int a = 10;
    int b = a;
    
    b = 20;
    System.out.println("a = " + a); //출력: a = 10
    System.out.println("b = " + b); //출력: b = 20
    
    //기본 타입은 항상 값을 복사하기 때문에, b가 a를 참조한다고 해도 a의 값이 바뀌지 않는다.
    ```
* ```Integer``` 같은 래퍼 클래스나 ```String``` 같은 특수한 클래스는 공유 가능한 객체이지만 변경은 불가능하다.(사이드이펙트 불가)
  * ```java
    Integer a = new Integer(10);
    Integer b = a;
    
    a.setValue(20);
    System.out.println("a = " + a); //출력: a = 20
    System.out.println("b = " + b); //출력: b = 20
    
    //a와 b는 같은 레퍼런스를 공유하기 때문에
    ```

### 위의 내용은 되게 기초적인 것이어서 굳이 인지하며 개발하지 않았지만, JPA에서는 매우 중요한 개념이다.


<br>

## 임베디드 타입(복합 값 타입)

<br>

### 임베디드(내장) 타입
* 새로운 값 타입을 직접 정의할 수 있다.
* JPA는 임베디드 타입(embedded type)이라 한다.
* 주로 기본 값 타입을 모아서 만들어서 복합 값 타입이라고도 한다.
* ```int```,```String```과 같은 값 타입(추적 X, 변경 O)


* 예제)
  * 회원 엔티티는 이름, 근무 시작일, 근무 종료일, 주소 도시, 주소 번지, 주소 우편번호를 가진다.  
    ![Embedded type example](../../img/Embedded%20type%20example%201.PNG)
    * 공통적인 속성들을 추상화해서 클래스 타입 묶는다. 이렇게 묶어 낼 수 있는 것을 임베디드 타입이라 한다.
  * 회원 엔티티는 이름, 근무 기간, 집 주소를 가진다.  
    ![Embedded type example](../../img/Embedded%20type%20example%202.PNG)  
  * 최종적으로 멤버는 id, name, workPeriod, homeAddress의 4가지 속성을 가진다. 쉽게 이야기해서 기존 엔티티에서 속성 2개를 추출하여 클래스화한 것.  
    ![Embedded type example](../../img/Embedded%20type%20example%203.PNG)

<br>

### 임베디드 타입 사용법
* ```@Embeddable```: 값 타입을 정의하는 곳에 표시
* ```@Embedded```: 값 타입을 사용하는 곳에 표시
* 기본 생성자 필수

<br>

### 임베디드 타입의 장점
* 재사용성
* 높은 응집도
* ```Period.isWork()```처럼 해당 값 타입만 사용하는 의미 있는 메소드를 만들 수 있다.
* 임베디드 타입을 포함한 모든 값 타입은, 값 타입을 소유한 엔티티에 생명주기를 의존한다.

<br>

### 임베디드 타입과 테이블 매핑
![Embedded type](../../img/Embedded%20type%201.PNG)
* 임베디드 타입은 엔티티의 값일 뿐이다.
* 임베디드 타입을 사용하기 전과 후에 **_매핑하는 테이블은 같다_**.
* 객체와 테이블을 아주 세밀하게(find-grained) 매핑하는 것이 가능하다.(메소드 활용)
* 잘 설계한 ORM 애플리케이션은 매핑한 테이블의 수보다 클래스의 수가 더 많다.
* 예제) [EmbeddedMember.java](../../src/main/java/hellojpa/valueType/embedded/EmbeddedMember.java), [Address.java](../../src/main/java/hellojpa/valueType/embedded/Address.java), [Period.java](../../src/main/java/hellojpa/valueType/embedded/Period.java)

<br>

### 임베디드 타입과 연관관계
* JPA 표준 스펙에 나와 있는 내용으로 임베디드 타입이 엔티티 값 타입을 가질 수 있다.

![Embedded type](../../img/Embedded%20type%202.PNG)

<br>

### 한 엔티티에서 같은 값 타입을 사용하려면?
* 컬렴 명이 중복되는 문제가 발생.  
  ```@AttributeOverrides```, ```@AttributeOverride```를 사용해서 컬러 명 속성을 재정의해야 한다.
* 예제) [EmbeddedMember.java](../../src/main/java/hellojpa/valueType/embedded/EmbeddedMember.java)

<br>

### 임베디드 타입과 null
* 임베디드 타입의 값이 null이면 매핑한 컬럼 값은 모두 null이 된다.