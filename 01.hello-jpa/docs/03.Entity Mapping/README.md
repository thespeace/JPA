# 객체와 테이블 매핑
JPA에서 가장 중요한 것 중 하나는 엔티티와 테이블을 정확하게 매핑하는 것이다.  
따라서 매핑 어노테이션을 숙지해야 하는데, 아래의 4가지로 분류할 수 있다.

* 객체와 테이블 매핑: ```@Entity```, ```@Table```
* 필드와 컬럼 매핑: ```@Column```
* 기본 키 매핑: ```@Id```
* 연관관계 매핑: ```@ManyToOne```, ```@JoinColumn```

<br>

### ```@Entity```
* ```@Entity```가 붙은 클래스는 JPA가 관리, 엔티티라 한다.
* JPA를 사용해서 테이블과 매핑할 클래스는 ```@Entity```가 필수다.
* 속성: name
  * ex) ```@Entity(name = "Member")``` 
  * JPA에서 사용할 엔티티 이름을 지정한다.
  * 기본값: 클래스 이름을 그대로 사용(예: Member)
  * 같은 클래스 이름이 없으면 가급적 기본값을 사용한다.
* **주의**
  * 기본 생성자 필수(파라미터가 없는 public 또는 protected 생성자)
  * final 클래스, enum, interface, inner 클래스에는 사용 불가
  * 저장할 필드에 final 사용 불가

<br>

### ```@Table```
* ```@Table```은 엔티티와 매핑할 테이블 지정
  * 속성
    * name: 매핑할 테이블 이름, 지정하지 않으면 엔티티 이름을 사용. ex) ```@Table(name = "tbl_member")```
    * catalog: 데이터베이스 catalog 매핑
    * schema: 데이터베이스 schema 매핑
    * uniqueConstraints(DDL): DDL 생성 시에 유니크 제약 조건 생성

<br>

---
## 데이터베이스 스키마 자동 생성
* DDL을 애플리케이션 실행 시점에 자동 생성해준다.
* 테이블 중심 -> 객체 중심
* 데이터베이스 방언을 활용해서 데이터베이스에 맞는 적절한 DDL 생성!
* 이렇게 **생성된 DDL은 개발 장비에서만 사용**
* 생성된 DDL은 운영서버에서는 사용하지 않거나, 적절히 다듬은 후 사용하는 것을 권장

<br>

### 속성
* ```create```: 기존테이블 삭제 후 다시 생성 (DROP + CREATE)
  ```java
  //persistence.xml
  <property name="hibernate.hbm2ddl.auto" value="create" />
  ```
* ```create-drop```: create와 같으나 종료시점에 테이블 DROP
  ```java
  <property name="hibernate.hbm2ddl.auto" value="create-drop" />
  ```
* ```update```: 변경분만 반영(```alter table```, 운영DB에는 사용하면 안된다)
  ```java
  <property name="hibernate.hbm2ddl.auto" value="update" />
  ```
* ```validate```: 엔티티와 테이블이 정상 매핑되었는지만 확인
  ```java
  <property name="hibernate.hbm2ddl.auto" value="validate" />
  ```
* ```none```: 사용하지 않음, none이라는 속성은 실제 존재하지 않지만 관례상 표기할 때 사용
  ```java
  <property name="hibernate.hbm2ddl.auto" value="none" />
  ```
  
<br>

### 주의점
* ***운영 장비에는 절대 create, create-drop, update 사용하면 안된다.***
* 개발 초기 단계는 create 또는 update
* 테스트 서버는 update 또는 validate
* 스테이징과 운영 서버는 validate 또는 none

<br>

### DDL 생성 기능 추가 설명
* DDL 생성 기능은 DDL을 자동 생성할 때만 사용되고 JPA의 실행 로직에는 영향을 주지 않는다.
* 예시
  * 제약조건 추가: 회원 이름은 필수, 10자 초과X
    * ```@Column(nullable = false, length = 10)```
  * 유니크 제약조건 추가
    * ```@Table(uniqueConstraints = {@UniqueConstraint( name = "NAME_AGE_UNIQUE",columnNames = {"NAME", "AGE"} )})```

<br>

---
## 필드와 컬럼 매핑
아래와 같은 요구사항을 코드로 추가해보고 하나하나 살펴보자.

> 요구사항  
> 1. 회원은 일반 회원과 관리자로 구분해야 한다.
> 2. 회원 가입일과 수정일이 있어야 한다.
> 3. 회원을 설명할 수 있는 필드가 있어야 한다. 이 필드는 길이 제한이 없다.    
> <br>
>   코드: [Member.java](../../src/main/java/hellojpa/Member.java)

<br>

### 매핑 어노테이션 정리
hibernate.hbm2ddl.auto
* ```@Column```: 컬럼 매핑
* ```@Temporal```: 날짜 타입 매핑
* ```@Enumerated```: enum 타입 매핑
* ```@Lob```: BLOB, CLOB 매핑
* ```@Transient```: 특정 필드를 컬럼에 매핑하지 않음(매핑 무시, 메모리에서만 사용)

<br>

### ```@Column``` 속성
* name: 필드와 매핑할 테이블의 컬럼 이름, 기본값은 객체의 필드 이름. ```@Column(name = "name")```
* insertable, updatable: 등록, 변경 가능 여부, 기본값은 TRUE. ```@Column(name = "name", updatable = false)```
* nullable(DDL): null 값의 허용 여부를 설정한다. false로 설정하면 DDL 생성 시에 not null 제약조건이 붙는다.
* unique(DDL): @Table의 uniqueConstraints와 같지만 한 컬럼에 간단히 유니크 제약조건을 걸 때 사용한다. unique보다는 uniqueConstraints를 더 많이 사용한다.
* columnDefinition(DDL): 데이터베이스 컬럼 정보를 직접 줄 수 있다. 기본값은 필드의 자바 타입과 방언 정보를 사용. ex) varchar(100) default 'EMPTY'
* length(DDL): 문자 길이 제약조건, String 타입에만 사용한다. 기본값은 255.
* precision, scale(DDL): BigDecimal 타입에서 사용한다.(BigInteger도 사용할 수 있다.) precision은 소수점을 포함한 전체 자릿 수를, scale은 소수의 자릿수다.
  참고로 double, float 타입에는 적용되지 않는다. 아주 큰 숫자나 정밀한 소수를 다루어야 할 때만 사용한다. 기본값은 precision = 19, scale = 2

<br>

### ```@Enumerated``` 속성
자바 enum 타입을 매핑할 때 사용
* value
  * EnumType.ORIDINAL: enum 순서를 데이터베이스에 저장  
  * EnumType.STRING은 enum 이름을 데이터베이스에 저장  
  * 기본값은 EnumType.ORDINAL이지만 주의! ORDINAL보다는 STRING을 주로 사용하자. 그 이유는 순서에 의한 문제를 예방하기 위함이다. 

<br>

### ```@Temporal``` 속성
날짜 타입(java.util.Date, java.util.Calendar)을 매핑할 때 사용  
참고: LocalDate, LocalDateTime을 사용할 때는 생략 가능(최신 하이버네이트 지원)
* value
  * TemporalType.DATE: 날짜, 데이터베이스 date 타입과 매핑. ex) 2013–10–11
  * TemporalType.TIME: 시간, 데이터베이스 time 타입과 매핑. ex) 11:11:11
  * TemporalType.TIMESTAMP: 날짜와 시간, 데이터베이스 timestamp 타입과 매핑. ex) 2013–10–11 11:11:11

<br>

### ```@Lob``` 속성
데이터베이스 BLOB, CLOB 타입과 매핑
* @Lob에는 지정할 수 있는 속성이 없다.
* 매핑하는 필드 타입이 문자면 CLOB 매핑, 나머지는 BLOB 매핑
  * CLOB: String, char[], java.sql.CLOB
  * BLOB: byte[], java.sql. BLOB

<br>

### ```@Transient``` 속성
* 필드 매핑X
* 데이터베이스에 저장X, 조회X
* 주로 메모리상에서만 임시로 어떤 값을 보관하고 싶을 때 사용
* ```
  @Transient  
  private Integer temp;
  ```