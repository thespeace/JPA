# JPQL - 경로 표현식

<br>

### 경로 표현식
* .(점)을 찍어 객체 그래프를 탐색하는 것
  ```sql
  select m.username #상태 필드(엔티티의 값)
  from Member m
  join m.team t #단일 값 연관 필드
  join m.orders o #컬렉션 값 연관 필드
  where t.name = '팀A'
  ```
* 경로표현식에 따라 내부동작 방식이 달라진다.
  
<br>

### 경로 표현식 용어 정리
* **상태 필드**(state field): 단순히 값을 저장하기 위한 필드(ex) ```m.username```)
* **연관 필드**(association field): 연관관계를 위한 필드
  * **단일 값 연관 필드**: @ManyToOne, @OneToOne, 대상이 엔티티(ex) ```m.team```)
  * **컬렉션 값 연관 필드**: @OneToMany, @ManyToMany, 대상이 컬렉션(ex) ```m.orders```)

<br>

### 경로 표현식 특징
* **상태 필드**(state field): 경로 탐색의 끝, 더이상 탐색이 안된다.
* **단일 값 연관 경로**: **_묵시적 내부 조인_**(inner join) 발생, 더 탐색이 가능하다.
  * 묵시적 내부 조인으로 인해 성능 튜닝이 어렵다.
* **컬렉션 값 연관 경로**: 묵시적 내부 조인 발생, 더이상 탐색이 안된다.
  * FROM 절에서 명시적 조인을 통해 별칭을 얻으면 별칭을 통해 탐색 가능  
    ```select m.username From Team t join t.members m```
> 묵시적 조인을 지양하고 명시적 조인을 사용하라!

<br>

### 상태 필드 경로 탐색
* JPQL이 SQL과 같다.
  * JPQL: ```select m.username, m.age from Member m```
  * SQL: ```select m.username, m.age from Member m```

<br>

### 단일 값 연관 경로 탐색
* 묵시적 조인이 발생한다.
  * JPQL: ```select o.member from Order o```
  * SQL:  
    ```sql
    select m.*
    from Orders o
    inner join Member m on o.member_id = m.id
    ```
    
<br>

### 명시적 조인, 묵시적 조인
* 명시적 조인: join 키워드 직접 사용
  * ```select m from Member m join m.team t```
* 묵시적 조인: 경로 표현식에 의해 묵시적으로 SQL 조인 발생(내부 조인만 가능)
  * ```select m.team from Member m```

<br>

### 경로 표현식 - 예제
* ```sql
  select o.member.team from Order o #성공
  ```
* ```sql
  select t.members from Team #성공
  ```
* ```sql
  select t.members.username from Team t #실패
  ```
* ```sql
  select m.username from Team t join t.members m #성공(묵시적 조인을 명시적 조인으로 변경)
  ```
  
<br>

### 경로 탐색을 사용한 묵시적 조인 시 주의사항
* 항상 내부 조인
* 컬렉션은 경로 탐색의 끝, 명시적 조인을 통해 별칭을 얻어야 한다.
* 경로 탐색은 주로 ```SELECT```, ```WHERE``` 절에서 사용하지만 묵시적 조인으로 인해 SQL의 ```FROM``` (```JOIN```) 절에 영향을 준다.

<br>

### 실무 조언
* **_가급적 묵시적 조인 대신에 명시적 조인 사용_**
* 조인은 SQL 튜닝에 중요 포인트
* 묵시적 조인은 조인이 일어나는 상황을 한눈에 파악하기 어렵다.

<br>

## JPQL - 페치 조인(fetch join)
### **_실무에서 정말정말 중요하다._**

<br>

### 페치 조인(fetch join)
* SQL 조인 종류가 아니고, JPQL에서 **_성능 최적화_** 를 위해 제공하는 기능이다.
* 연관된 엔티티나 컬렉션을 **_SQL 한 번에 함께 조회_** 하는 기능(한방 쿼리)
* ```join fetch``` 명령어 사용
* 페치 조인 ::= ```[ LEFT [OUTER] | INNER ] JOIN FETCH 조인경로```
* 지연 로딩으로 설정을 해도 페치 조인이 항상 우선이다.

<br>

### 엔티티 페치 조인
* 회원을 조회하면서 연관된 팀도 함께 조회(SQL 한 번에)
* JPQL:
  ```sql
  select m from Member m join fetch m.team
  ```
* SQL:
  ```sql
  SELECT M.*, T.* FROM MEMBER M 
  INNER JOIN TEAM T ON M.TEAM_ID=T.ID
  ```  
  SQL을 보면 회원 뿐만 아니라 팀(```T.*```)도 함께 ```SELECT```

<br>

### 예시
![Fetch join example](../../img/Fetch%20join%20example.PNG)

<br>

* 페치 조인 사용 전
  ```java
  String query = "select m From Member m";
  List<Member> resultList = em.createQuery(query, Member.class).getResultList();
  for (Member member : resultList) {
      System.out.println("member = " + member.getUsername() + ", " + member.getTeam().getName());
  }
  // 회원1, 팀A(SQL)
  // 회원2, 팀A(1차캐시)
  // 회원3, 팀B(SQL)
  
  // 회원 100명 -> N + 1
  // 쿼리가 N+1 수행, 성능 저하 발생
  ```
  
<br>
  
* 패치 조인 사용 코드
  ```java
  String query = "select m From Member m join fetch m.team";
  List<Member> resultList = em.createQuery(query, Member.class).getResultList();
  for (Member member : resultList) {
      //페치 조인으로 회원과 팀을 함께 조회해서 지연 로딩 X
      System.out.println("member = " + member.getUsername() + ", " + member.getTeam().getName());
  }
  ```

<br>

### 컬렉션 페치 조인
* 일대다 관계, 컬렉션 페치 조인
* JPQL:  
  ```sql
  select t
  from Team t join fetch t.members
  where t.name = '팀A'
  ```
* SQL:
  ```sql
  SELECT T.*, M.*
  FROM TEAM T
  INNER JOIN MEMBER M ON T.ID=M.TEAM_ID
  WHERE T.NAME = '팀A'
  ```  
   
  ![Fetch join example](../../img/Fetch%20join%20example2.PNG)

<br>

### 페치 조인과 DISTINCT
* SQL의 DISTINCT는 중복된 결과를 제거하는 명령
* JPQL의 DISTINCT 2가지 기능 제공
  1. SQL에 DISTINCT를 추가
  2. 애플리케이션에서 엔티티 중복 제거
* 예시  
  ```sql
  select distinct t
  from Team t join fetch t.members
  where t.name = ‘팀A’
  ```
  * SQL에 ```DISTINCT```를 추가하지만 데이터가 다르므로 SQL 결과에서 중복제거 실패  
    ![Fetch join example](../../img/Fetch%20join%20example3.PNG)
  * DISTINCT가 추가로 애플리케이션에서 중복 제거시도
  * 같은 식별자를 가진 Team 엔티티 제거  
    ![Fetch join example](../../img/Fetch%20join%20example4.PNG)
  * DISTINCT 추가시 결과
    ```
    teamname = 팀A, team = Team@0x100
    -> username = 회원1, member = Member@0x200
    -> username = 회원2, member = Member@0x300
    ```

<br>

### 하이버네이트6 변경 사항
* DISTINCT가 추가로 애플리케이션에서 중복 제거시도
  * -> 하이버네이트6 부터는 DISTINCT 명령어를 사용하지 않아도 애플리케이션에서 중복 제거가 자동으로 적용됩니다.
* 참고 링크
  * https://github.com/hibernate/hibernate-orm/blob/6.0/migration-guide.adoc#distinct

<br>

### 페치 조인과 일반 조인의 차이
* 일반 조인 실행시 연관된 엔티티를 함께 조회하지 않는다.
* JPQL:  
  ```sql
  select t
  from Team t join t.members m
  where t.name = ‘팀A'
  ```
* SQL:
  ```sql
  SELECT T.*
  FROM TEAM T
  INNER JOIN MEMBER M ON T.ID=M.TEAM_ID
  WHERE T.NAME = '팀A'
  ```

* JPQL은 결과를 반환할 때 연관관계를 고려하지 않는다.
* 단지 SELECT 절에 지정한 엔티티만 조회할 뿐
* 위 쿼리에서는 TEAM 엔티티만 조회하고, MEMBER 엔티티는 조회하지 않는다.


* 반대로 페치 조인을 사용할 때만 연관된 엔티티도 함께 조회한다.(즉시 로딩)
* 페치 조인은 객체 그래프를 SQL 한번에 조회하는 개념이다.

<br>

### 페치 조인의 특징과 한계
* **페치 조인 대상에는 별칭을 줄 수 없다.**
  * 하이버네이트는 가능, 가급적 사용하지 말자.
* **둘 이상의 컬렉션은 페치 조인 할 수 없다.**(데이터 정합성을 위반할 수 있다)
* **컬렉션을 페치 조인하면 페이징 API(```setFirstResult()```, ```setMaxResults()```)를 사용할 수 없다.**
  * 일대일, 다대일 같은 단일 값 연관 필드들은 페치 조인해도 페이징 가능
  * 하이버네이트는 경고 로그를 남기고 메모리에서 페이징(매우 위험)


* 연관된 엔티티들을 SQL 한 번으로 조회 - 성능 최적화 가능
* 엔티티에 직접 적용하는 글로벌 로딩 전략보다 우선순위가 높다.
  * ```@OneToMany(fetch = FetchType.LAZY) //글로벌 로딩 전략```
* 실무에서 글로벌 로딩 전략은 모두 지연 로딩으로 설정
* 최적화가 필요한 곳은 페치 조인 적용한다.
* 위와 같이 적용하면 대부분의 성능 문제가 해결된다.(JPA의 문제 70~80%는 N+1 문제)

<br>

### 페치 조인 - 정리
* 모든 것을 페치 조인으로 해결할 수는 없다.
* 페치 조인은 객체 그래프를 유지할 때 사용하면 효과적이다.
* 여러 테이블을 조인해서 엔티티가 가진 모양이 아닌 전혀 다른 결과를 내야 하면, 페치 조인보다는 일반 조인을 사용하고 필요한 데이터들만 조회해서 DTO로 반환하는 것이 효과적이다.
* **예전에는 쿼리를 고치면서 튜닝을 했지만 이제는 페치 조인을 잘 알아두면 실제 운영할 때 성능상의 큰 이점을 발휘할 수 있다!**

<br>

## JPQL - 다형성 쿼리

<br>

### 예시

![polymorphic query](../../img/polymorphic%20query.PNG)

* 다형적으로 설계, JPA가 특수한 기능을 제공한다.


* ### TYPE
  * 조회 대상을 특정 자식으로 한정
  * 예) Item 중에 Book, Movie를 조회해라.
  * JPQL
    ```sql
    select i from Item i
    where type(i) IN (Book, Movie)
    ```
  * SQL
    ```sql
    select i from i
    where i.DTYPE in (‘B’, ‘M’)
    ```


* ### TREAT(JPA 2.1)
  * 자바의 타입 캐스팅과 유사, 상속 구조에서 부모 타입을 특정 자식 타입으로 다룰 때 사용한다.(다운 캐스팅처럼-)
  * ```FROM```, ```WHERE```, ```SELECT```(하이버네이트 지원) 사용
  * 예) 부모인 Item과 자식 Book이 있다.
  * JPQL
    ```sql
    select i from Item i
    where treat(i as Book).author = ‘kim’
    ```
  * SQL
    ```sql
    #싱글 테이블 전략시 해당 쿼리
    select i.* from Item i
    where i.DTYPE = ‘B’ and i.author = ‘kim’
    ```
    
<br>

## JPQL - 엔티티 직접 사용

<br>

### 엔티티 직접 사용 - 기본 키 값
* JPQL에서 엔티티를 직접 사용하면 SQL에서 해당 엔티티의 기본 키 값을 사용
* JPQL
  ```sql
  select count(m.id) from Member m #엔티티의 아이디를 사용
  select count(m) from Member m #엔티티를 직접 사용
  ```
* SQL
  ```sql
  #JPQL 둘다 같은 다음 SQL 실행
  select count(m.id) as cnt from Member m
  ```
  
<br>

### 엔티티 직접 사용 - 기본 키 값
* 엔티티를 파라미터로 전달
  ```java
  String jpql = “select m from Member m where m = :member”;
  List resultList = em.createQuery(jpql).setParameter("member", member).getResultList();
  ```
* 식별자를 직접 전달
  ```java
  String jpql = “select m from Member m where m.id = :memberId”;
  List resultList = em.createQuery(jpql).setParameter("memberId", memberId).getResultList();
  ```
* 실행된 SQL
  ```sql
  #엔티티를 파라미터로 전달하거나 식별자를 직접 전달해도 같은 SQL 실행
  select m.* from Member m where m.id=?
  ```

<br>

### 엔티티 직접 사용 - 외래 키 값
* ```java
  Team team = em.find(Team.class, 1L);
  String qlString = “select m from Member m where m.team = :team”; //외래키
  List resultList = em.createQuery(qlString).setParameter("team", team).getResultList();
  ```
* ```java
  String qlString = “select m from Member m where m.team.id = :teamId”; //외래키
  List resultList = em.createQuery(qlString).setParameter("teamId", teamId).getResultList();
  ```
* 실행된 SQL
  ```sql
  select m.* from Member m where m.team_id=?
  ```
  
<br>

## JPQL - Named 쿼리

<br>

### Named 쿼리 - 정적 쿼리
* 미리 정의해서 이름을 부여해두고 사용하는 JPQL
* 정적 쿼리만 가능
* 어노테이션, XML에 정의
* 애플리케이션 로딩 시점에 초기화 후 재사용
* **_애플리케이션 로딩 시점에 쿼리를 검증(컴파일 시점에 검증!)_**

<br>

### Named 쿼리 - 어노테이션
```java
@Entity
@NamedQuery(
        name = "Member.findByUsername",
        query="select m from Member m where m.username = :username")
public class Member {
 // ...
}
```
```java
List<Member> resultList = em.createNamedQuery("Member.findByUsername", Member.class)
        .setParameter("username", "회원1")
        .getResultList();
```

<br>

### Named 쿼리 - XML에 정의
* ```META-INF/persistence.xml```
  ```xml
  <persistence-unit name="jpabook" >
      <mapping-file>META-INF/ormMember.xml</mapping-file>
  ```
* ```META-INF/ormMember.xml```
  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <entity-mappings xmlns="http://xmlns.jcp.org/xml/ns/persistence/orm" version="2.1">
      <named-query name="Member.findByUsername">
          <query><![CDATA[
              select m
              from Member m
              where m.username = :username
          ]]></query>
      </named-query>
  
      <named-query name="Member.count">
          <query>select count(m) from Member m</query>
      </named-query>
  </entity-mappings>
  ```

<br>

### Named 쿼리 환경에 따른 설정
* Annotation보다 XML이 항상 우선권을 가진다.
* 애플리케이션 운영 환경에 따라 다른 XML을 배포할 수 있다.
* 사실 실무에서는 위 소개한 Named 쿼리말고 Spring Data JPA의 정적 쿼리 기능을 쓰는게 좋다.

<br>

## JPQL - 벌크 연산

<br>

### 벌크 연산
* 쉽게 이야기 하자면 우리가 일반적으로 아는 SQL의 ```UPDATE```, ```DELETE```문이라고 보면 된다.
* PK를 콕 찝어서 실행하는 ```UPDATE```와 ```DELETE```문 여러 개를 한번에 실행하는 것을 벌크 연산이라 한다.


* 재고가 10개 미만인 모든 상품의 가격을 10% 상승하려면?
  * JPA 변경 감지 기능으로 실행하려면 너무 많은 SQL 실행해야 한다.
    1. 재고가 10개 미만인 상품을 리스트로 조회한다.
    2. 상품 엔티티의 가격을 10% 증가한다.
    3. 트랜잭션 커밋 시점에 변경감지가 동작한다.
  * 변경된 데이터가 100건이라면 100번의 UPDATE SQL 실행..


* 벌크 연산 쿼리 한 번으로 여러 테이블 로우 변경(엔티티)할 수 있다.
* ```executeUpdate()```는 영향받은 엔티티 수 반환
* ```UPDATE```, ```DELETE``` 지원
* ```INSERT```도 가능, 표준 스펙엔 없지만 하이버네이트가 지원(```insert into .. select```)
  ```java
  String qlString = "update Product p " +
                    "set p.price = p.price * 1.1 " +
                    "where p.stockAmount < :stockAmount";
  
  int resultCount = em.createQuery(qlString).setParameter("stockAmount", 10)
                      .executeUpdate(); 
  ```
  
<br>

### 벌크 연산 주의
* 벌크 연산은 영속성 컨텍스트를 무시하고 데이터베이스에 직접 쿼리 실행
* 데이터 정합성에 위배!  
  해결책 두 가지
  * 벌크 연산을 먼저 실행
  * **_벌크 연산 수행 후 영속성 컨텍스트 초기화_**