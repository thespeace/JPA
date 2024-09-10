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

