# JPQL(Java Persistence Query Language)

<br>

## JPQL - 기본 문법과 기능

<br>

### JPQL 소개
* JPQL은 객체지향 쿼리 언어다. 따라서 테이블을 대상으로 쿼리하는 것이 아니라 **_엔티티 객체를 대상으로 쿼리_** 한다.
* JPQL은 SQL을 추상화해서 특정데이터베이스 SQL에 의존하지 않는다.
* JPQL은 결국 SQL로 변환된다.(맵핑 정보 + DB 방언 = SQL)

![JPQL](../../img/JPQL01.PNG)

<br>

### JPQL 문법
```
select_문 :: =
    select_절
    from_절
    [where_절]
    [groupby_절]
    [having_절]
    [orderby_절]
    
update_문 :: = update_절 [where_절]
delete_문 :: = delete_절 [where_절]
```

* ```select m from Member as m where m.age > 18```
* 엔티티와 속성은 대소문자 구분한다. (```Member```, ```age```)
* JPQL 키워드는 대소문자 구분하지 않는다. (```SELECT```, ```FROM```, ```where```)
* 엔티티 이름 사용, 테이블 이름이 아니다.(```Member```)
* **_별칭은 필수(```m```)_** (```as```는 생략가능)

<br>

### 집합과 정렬
```
select
    COUNT(m), //회원수
    SUM(m.age), //나이 합
    AVG(m.age), //평균 나이
    MAX(m.age), //최대 나이
    MIN(m.age) //최소 나이
from Member m
```
* ```GROUP BY```, ```HAVING```
* ```ORDER BY```

<br>

### TypeQuery, Query
* TypeQuery: 반환 타입이 명확할 때 사용
* Query: 반환 타입이 명확하지 않을 때 사용
```java
TypedQuery<Member> query = em.createQuery("SELECT m FROM Member m", Member.class);
```
```java
Query query = em.createQuery("SELECT m.username, m.age from Member m"); 
```

<br>

### 결과 조회 API
* ```query.getResultList()```: 결과가 하나 이상일 때, 리스트 반환
  * 결과가 없으면 빈 리스트 반환
* ```query.getSingleResult()```: 결과가 정확히 하나, 단일 객체 반환
  * 결과가 없으면: ```javax.persistence.NoResultException```
  * 둘 이상이면: ```javax.persistence.NonUniqueResultException```

<br>

### 파라미터 바인딩 - 이름 기준, 위치 기준
```
SELECT m FROM Member m where m.username=:username
        
query.setParameter("username", usernameParam);
```
```
SELECT m FROM Member m where m.username=?1

query.setParameter(1, usernameParam);
```
* 위치 기반 파라미터 바인딩은 웬만하면 안쓰는 게 좋다. 작은 실수가 장애로 이어질 수 있다.

<br>

---

## 프로젝션
* ```SELECT``` 절에 조회할 대상을 지정하는 것
* 프로젝션 대상: 엔티티, 임베디드 타입, 스칼라 타입(숫자, 문자등 기본 데이터 타입)
* ```SELECT m FROM Member m``` -> 엔티티 프로젝션(영속성 컨텍스트에서 다 관리된다)
* ```SELECT m.team FROM Member m``` -> 엔티티 프로젝션
* ```SELECT o.address FROM Order o``` -> 임베디드 타입 프로젝션
* ```SELECT m.username, m.age FROM Member m``` -> 스칼라 타입 프로젝션
* ```DISTINCT```로 중복 제거

<br>

### 프로젝션 - 여러 값 조회
```SELECT m.username, m.age FROM Member m```
1. Query 타입으로 조회
2. Object[] 타입으로 조회
3. new 명령어로 조회
   * 단순 값을 DTO로 바로 조회  
     ```SELECT new jpabook.jpql.UserDTO(m.username, m.age) FROM Member m```
   * 패키지 명을 포함한 전체 클래스 명 입력
   * 순서와 타입이 일치하는 생성자 필요

<br>

## 페이징 API
* JPA는 페이징을 다음 두 API로 추상화
  * ```setFirstResult(int startPosition)``` : 조회 시작 위치(0부터 시작)
  * ```setMaxResults(int maxResult)``` : 조회할 데이터 수
* 예시)
  * ```java
    //페이징 쿼리
    String jpql = "select m from Member m order by m.name desc";
    List<Member> resultList = em.createQuery(jpql, Member.class)
            .setFirstResult(10)
            .setMaxResults(20)
            .getResultList();
    ```
* 인간의 머리로 추상적인 것들만 입력하면 구체적인 레벨을 프레임워크에서 해결해준다.
* 위 예시 코드와 Direct 정보를 합쳐서 쿼리가 실행된다.  
  예시)
  * ```MYSQL``` 방언
    * ```sql
      SELECT
        M.ID AS ID,
        M.AGE AS AGE,
        M.TEAM_ID AS TEAM_ID,
        M.NAME AS NAME
      FROM
        MEMBER M
      ORDER BY
        M.NAME DESC LIMIT ?, ?
      ```
  * ```Oracle``` 방언
    * ```sql
      SELECT * FROM
        ( SELECT ROW_.*, ROWNUM ROWNUM_
        FROM
            ( SELECT
                M.ID AS ID,
                M.AGE AS AGE,
                M.TEAM_ID AS TEAM_ID,
                M.NAME AS NAME
            FROM MEMBER M
            ORDER BY M.NAME
            ) ROW_
        WHERE ROWNUM <= ?
        )
      WHERE ROWNUM_ > ?
      ```
      
<br>

## 조인
* 내부 조인:  
  ```SELECT m FROM Member m [INNER] JOIN m.team t```
* 외부 조인:  
  ```SELECT m FROM Member m LEFT [OUTER] JOIN m.team t```
* 세타 조인:  
  ```select count(m) from Member m, Team t where m.username = t.name```

<br>

### 조인 - ON 절
* ON절을 활용한 조인(JPA 2.1부터 지원)
  1. 조인 대상 필터링
  2. 연관관계 없는 엔티티 외부 조인(하이버네이트 5.1부터)

<br>

### 1. 조인 대상 필터링
예) 회원과 팀을 조인하면서, 팀 이름이 A인 팀만 조인
* JPQL
  * ```SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'A'``` 
* SQL:
  * ```SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and t.name='A'```

<br>

### 2. 연관관계 없는 엔티티 외부 조인
예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
* JPQL
    * ```SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name```
* SQL:
    * ```SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name```

<br>

## 서브 쿼리
* 일반적인 SQL에서 지원하는 서브 쿼리 사용이 가능하다.
  * 나이가 평균보다 많은 회원  
    ```select m from Member m where m.age > (select avg(m2.age) from Member m2)```
  * 한 건이라도 주문한 고객  
    ```select m from Member m where (select count(o) from Order o where m = o.member) > 0```

<br>

### 서브 쿼리 지원 함수
* [NOT] ```EXISTS``` (subquery): 서브쿼리에 결과가 존재하면 참
  * {```ALL``` | ```ANY``` | ```SOME```} (subquery)
  * ```ALL``` 모두 만족하면 참
  * ```ANY```, ```SOME```: 같은 의미, 조건을 하나라도 만족하면 참
* [NOT] ```IN``` (subquery): 서브쿼리의 결과 중 하나라도 같은 것이 있으면 참

<br>

### 서브 쿼리 - 예제
* 팀A 소속인 회원(```exists```)  
  ```select m from Member m where exists (select t from m.team t where t.name = ‘팀A')```
* 전체 상품 각각의 재고보다 주문량이 많은 주문들(```ALL```)  
  ```select o from Order o where o.orderAmount > ALL (select p.stockAmount from Product p)```
* 어떤 팀이든 팀에 소속된 회원(```ANY```)  
  ```select m from Member m where m.team = ANY (select t from Team t)```

<br>

### JPA 서브 쿼리 한계
* JPA는 ```WHERE```, ```HAVING``` 절에서만 서브 쿼리 사용 가능
* ```SELECT``` 절도 가능(하이버네이트에서 지원)
* **_```FROM``` 절의 서브 쿼리는 현재 JPQL에서 불가능_**
  * **_조인으로 풀 수 있으면 풀어서 해결_**
  * 하이버네이트6 부터는 **_```FROM``` 절의 서브쿼리를 지원!_**
    * 참고 링크: https://in.relation.to/2022/06/24/hibernate-orm-61-features/

<br>

## JPQL 타입 표현
* 문자: ‘HELLO’, ‘She’’s’
* 숫자: 10L(Long), 10D(Double), 10F(Float)
* Boolean: TRUE, FALSE
* ENUM: jpabook.MemberType.Admin (패키지명 포함)
* 엔티티 타입: TYPE(m) = Member (상속 관계에서 사용)

<br>

### JPQL 기타
* SQL과 문법이 같다.
* EXISTS, IN
* AND, OR, NOT
* =, >, >=, <, <=, <>
* BETWEEN, LIKE, **_IS NULL_**

<br>

## 조건식 - CASE 식
* 기본 CASE 식
  ```sql
  select
    case when m.age <= 10 then '학생요금'
         when m.age >= 60 then '경로요금'
         else '일반요금'
    end
  from Member m
  ```
* 단순 CASE 식
  ```sql
  select
    case t.name
         when '팀A' then '인센티브110%'
         when '팀B' then '인센티브120%'
         else '인센티브105%'
    end
  from Team t
  ```
* ```COALESCE```: 하나씩 조회해서 null이 아니면 반환
* ```NULLIF```: 두 값이 같으면 null 반환, 다르면 첫번째 값 반환
  * 사용자 이름이 없으면 이름 없는 회원을 반환  
    ```select coalesce(m.username,'이름 없는 회원') from Member m```
  * 사용자 이름이 ‘관리자’면 null을 반환하고 나머지는 본인의 이름을 반환  
    ```select NULLIF(m.username, '관리자') from Member m```