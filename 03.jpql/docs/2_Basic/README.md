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