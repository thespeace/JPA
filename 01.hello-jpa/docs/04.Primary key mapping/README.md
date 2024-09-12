# 기본 키 매핑

<br>

### 어노테이션
* ```@Id```
* ```@GeneratedValue```
  ```java
  @Id @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  ```

<br>

## 기본 키 매핑 방법
* 직접 할당: ```@Id```만 사용
* 자동 생성(```@GeneratedValue```)
  1. IDENTITY: 데이터베이스에 위임, MYSQL
  2. SEQUENCE: 데이터베이스 시퀀스 오브젝트 사용, ORACLE
    * ```@SequenceGenerator``` 필요
  3. TABLE: 키 생성용 테이블 사용, 모든 DB에서 사용
    * ```@TableGenerator``` 필요
  4. AUTO: RDBS 방언에 따라 자동 지정, 기본값

<br>

### IDENTITY 전략 - 특징
* ```java
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  ```
* 기본 키 생성을 데이터베이스에 위임
* 주로 MySQL, PostgreSQL, SQL Server, DB2에서 사용  
  (예: MySQL의 AUTO_INCREMENT)
* JPA는 보통 트랜잭션 커밋 시점에 INSERT SQL 실행
* AUTO_INCREMENT는 데이터베이스에 INSERT SQL을 실행한 이후에 ID 값을 알 수 있다.  
  따라서 IDENTITY 전략은 ```em.persist()``` 시점에 **즉시 INSERT SQL 실행**하고 DB에서 식별자를 조회한다.
  ```java
  Member member = new Member();
  member.setUsername("C");
  
  em.persist(member);
  System.out.println("member.id = " + member.getId()); //조회 가능
  
  tx.commit();
  ```
  때문에 Batch를 활용할 수 없다.

<br>

### SEQUENCE 전략 - 특징
* ```java
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private Long id;
  ```
* 데이터베이스 시퀀스는 유일한 값을 순서대로 생성하는 특별한 데이터베이스 오브젝트(예: 오라클 시퀀스)
* 오라클, PostgreSQL, DB2, H2 데이터베이스에서 사용

<br>

### SEQUENCE - ```@SequenceGenerator```
* 테이블마다 시퀀스명을 따로 관리해야 하기 때문에 시퀀스명을 지정할 수 있다.
* ```java
  @Entity
  @SequenceGenerator(
          name = "MEMBER_SEQ_GENERATOR",
          sequenceName = "MEMBER_SEQ", //매핑할 데이터베이스 시퀀스 이름
          initialValue = 1, allocationSize = 1)
  public class Member {
  
      @Id
      @GeneratedValue(strategy = GenerationType.SEQUENCE,
              generator = "MEMBER_SEQ_GENERATOR")
      private Long id;
  ```
* 속성 설명
  * name: 식별자 생성기 이름, 기본값 필수
  * sequenceName: 데이터베이스에 등록되어 있는 시퀀스 이름, 기본값은 hibernate_sequence
  * initialValue: DDL 생성 시에만 사용됨, 시퀀스 DDL을 생성할 때 처음 1 시작하는 수를 지정한다. 기본값은 1
  * allocationSize: 시퀀스 한 번 호출에 증가하는 수(성능 최적화에 사용됨. 데이터베이스 시퀀스 값이 하나씩 증가하도록 설정되어 있으면 이 값을 반드시 1로 설정해야 한다.) 기본값은 **50**
  * catalog, schema: 데이터베이스 catalog, schema 이름.
* 주의: allocationSize 기본값 = 50, 50~100 설정이 적절하다.

<br>

### SEQUENCE 전략과 최적화
* 시퀀스 최적화의 핵심은 데이터베이스에 대한 연속적인 네트워크 요청을 줄여 성능을 향상시키기 위해 사용된다.  
  즉, 데이터베이스 시퀀스의 값은 일정량(예: 50개)을 한번에 메모리로 가져와서 필요할 때마다 메모리에서 하나씩 사용된다.  
  이 방식은 매번 데이터베이스에 시퀀스의 값을 요청하지 않아도 되므로 트랜잭션 처리 시간을 단축할 수 있다.

<br>

### TABLE 전략
* 키 생성 전용 테이블을 하나 만들어서 데이터베이스 시퀀스를 흉내내는 전략
* 장점: 모든 데이터베이스에 적용 가능
* 단점: 성능 이슈 및 최적화, 때문에 잘 사용하지 않는다.
* ```sql
  create table MY_SEQUENCES (
      sequence_name varchar(255) not null,
      next_val bigint,
      primary key ( sequence_name )
  )
  ```
  
<br>

### @TableGenerator
* ```java
  @Entity
  @TableGenerator(
          name = "MEMBER_SEQ_GENERATOR",
          table = "MY_SEQUENCES",
          pkColumnValue = "MEMBER_SEQ", allocationSize = 1)
  public class Member {
  
      @Id
      @GeneratedValue(strategy = GenerationType.TABLE,
                     generator = "MEMBER_SEQ_GENERATOR")
      private Long id;
  ```
* 속성 설명
  * name: 식별자 생성기 이름, 기본값 필수
  * table: 키생성 테이블명, 기본값은 hibernate_sequences
  * pkColumnName: 시퀀스 컬럼명, 기본값은 sequence_name
  * valueColumnName: 시퀀스 값 컬럼명, 기본값은 next_val
  * pkColumnValue: 키로 사용할 값 이름, 기본값은 엔티티 이름
  * initialValue: 초기 값, 마지막으로 생성된 값이 기준이다. 기본값은 0
  * allocationSize: 시퀀스 한 번 호출에 증가하는 수(성능 최적화에 사용됨). 기본값은 **50**
  * catalog, schema: 데이터베이스 catalog, schema 이름
  * uniqueConstraints(DDL): 유니크 제약 조건을 지정할 수 있다. 

<br>

### 권장하는 식별자 전략
* 기본 키 제약 조건: null 아님, 유일, **변하면 안된다.**
* 미래까지 이 조건을 만족하는 자연키는 찾기 어렵다. 대리키(대체키)를 사용하자.
* 예를 들어 주민등록번호도 기본 키로 적절하기 않다.
* **권장: Long형 + 대체키 + 키 생성전략 사용**
