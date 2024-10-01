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

<br>

## 값 타입과 불변 객체
**_"값 타입은 복잡한 객체 세상을 조금이라도 단순화하려고 만든 개념이다.  
따라서 값 타입은 단순하고 안전하게 다룰 수 있어야 한다"_**

<br>

### 값 타입 공유 참조
* 임베디드 타입 같은 값 타입을 여러 엔티티에서 공유하면 위험하다.
* 부작용(side effect) 발생
  ![Embedded type](../../img/Embedded%20type%203.PNG)
  ```java
  Address address = new Address("city", "street", "10000");
  
  Member member1 = new Member();
  member1.setUsername("member1");
  member1.setHomeAddress(address);
  em.persist(member1);
  
  Member member2 = new Member();
  member2.setUsername("member2");
  member2.setHomeAddress(address);
  em.persist(member2);
  
  //member1의 city값만 변경되길 원했지만, member2의 city 값도 변경(사이드 이펙트 발생)
  member.getHomeAddress().setCity("newCity");
  ```

<br>

### 값 타입 복사
* 값 타입의 실제 인스턴스인 값을 공유하는 것은 위험하다.
* 대신 값(인스턴스)를 복사해서 사용해야 한다.
  ![Embedded type](../../img/Embedded%20type%204.PNG)
  ```java
  Address address = new Address("city", "street", "10000");
  
  Member member1 = new Member();
  member1.setUsername("member1");
  member1.setHomeAddress(address);
  em.persist(member1);
  
  Address copyAddress = new Address(address.getCity(), address.getStreet(), address.getZipcode());
  
  Member member2 = new Member();
  member2.setUsername("member2");
  member2.setHomeAddress(copyAddress);
  em.persist(member2);
  
  //값 타입을 공유하지 않고 복사해서 사용하니 의도대로 member1의 값만 변경되었다.
  member.getHomeAddress().setCity("newCity");
  ```
  
<br>

### 객체 타입의 한계
* 항상 값을 복사해서 사용하면 공유 참조로 인해 발생하는 부작용을 피할 수 있다.
* 문제는 임베디드 타입처럼 **_직접 정의한 값 타입은 자바의 기본 타입이 아니라 객체 타입_** 이다.
* 자바 기본 타입에 값을 대입하면 값을 복사한다.
* **_객체 타입은 참조 값을 직접 대입하는 것을 막을 방법이 없다._**
* **_객체의 공유 참조는 피할 수 없다._**
  * 기본 타입(primitive type)
    ```java
    int a = 10;
    int b = a; //기본 타입은 값을 복사
    b = 4;
    System.out.println(a); //10 
    ```
  * 객체 타입
    ```java
    Address a = new Address("Old");
    Address b = a; //객체 타입은 참조를 전달
    b.setCity("New");
    System.out.println(b); //New
    ```
    
<br>

### 불변 객체
* 객체 타입을 수정할 수 없게 만들면 **_부작용을 원천 차단_** 할 수 있다.
* **_값 타입은 불변 객체(immutable object)로 설계해야 한다._**
* **_불변 객체: 생성 시점 이후 절대 값을 변경할 수 없는 객체_**
* 가장 쉬운 방법으로는 생성자로만 값을 설정하고 수정자(Setter)를 만들지 않으면 된다.
* 만약 객체의 특정 값을 변경하고 싶다면, 객체를 통으로 다시 생성해서 바꾸면 된다.  
  Value Object라는 것 자체가 값을 하나를 바꾸는 것보단 통을 바꿔끼는 게 맞는 개념이다.
* 참고: ```Integer```, ```String```은 자바가 제공하는 대표적인 불변 객체

### **_"불변이라는 작은 제약으로 부작용이라는 큰 재앙을 막을 수 있다"_**
