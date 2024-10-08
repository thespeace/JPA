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