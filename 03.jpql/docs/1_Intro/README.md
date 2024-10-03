# 객체지향 쿼리 언어

<br>

## 객체지향 쿼리 언어 소개

<br>

### JPA는 다양한 쿼리 방법을 지원
* **_JPQL_**
* JPA Criteria
* **_QueryDSL_**
* 네이티브 SQL
* JDBC API 직접 사용, MyBatis, SpringJdbcTemplate 함께 사용

<br>

### JPQL 소개
* 가장 단순한 조회 방법
  * ```EntityManager.find()```
  * 객체 그래프 탐색(```a.getB().getC()```)
* **_나이가 18살 이상인 회원을 모두 검색하고 싶다면?_**

<br>

### JPQL
* JPA를 사용하면 엔티티 객체를 중심으로 개발
* 문제는 검색 쿼리
* 검색을 할 때도 **_테이블이 아닌 엔티티 객체를 대상으로 검색_**
* 모든 DB 데이터를 객체로 변환해서 검색하는 것은 불가능
* 결국에는 애플리케이션이 필요한 데이터만 DB에서 불러오려면 결국 검색 조건이 포함된 SQL이 필요

<br>

* **_JPA는 SQL을 추상화한 JPQL_** 이라는 객체 지향 쿼리 언어 제공
* SQL과 문법 유사, ```SELECT```, ```FROM```, ```WHERE```, ```GROUP BY```, ```HAVING```, ```JOIN``` 지원
* JPQL은 엔티티 객체를 대상으로 쿼리 <-> SQL은 데이터베이스 테이블을 대상으로 쿼리
* JPQL을 사용하면 결국 SQL로 번역되어 실행되는 것

<br>

* 테이블이 아닌 객체를 대상으로 검색하는 객체 지향 쿼리
* SQL을 추상화해서 특정 데이터베이스 SQL에 의존하지 않는다는 장점(데이터베이스 방언에 자유롭다)
* JPQL을 한마디로 정의하면 **_객체 지향 SQL_**

<br>

### JPQL과 실행된 SQL
```java
//검색
 String jpql = "select m from Member m where m.age > 18";
 List<Member> result = em.createQuery(jpql, Member.class).getResultList();
```
```sql
#실행된 SQL
select
    m.id as id,
    m.age as age,
    m.USERNAME as USERNAME,
    m.TEAM_ID as TEAM_ID
from
    Member m
where
    m.age>18
```

<br>

### Criteria 소개
```java
//Criteria 사용 준비
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<Member> query = cb.createQuery(Member.class);

//루트 클래스 (조회를 시작할 클래스)
Root<Member> m = query.from(Member.class);

//쿼리 생성 CriteriaQuery<Member> cq =
query.select(m).where(cb.equal(m.get("username"), "kim"));
List<Member> resultList = em.createQuery(cq).getResultList();
```
* 문자가 아닌 자바코드로 JPQL을 작성할 수 있다.(동적 쿼리 작성에 유리)
* JPQL 빌더 역할
* JPA 공식 기능
* **_단점: 너무 복잡하고 실용성이 없다._**
* Criteria 대신에 **_QueryDSL 사용 권장_**

<br>

### QueryDSL 소개
```java
//JPQL
//select m from Member m where m.age > 18
JPAFactoryQuery query = new JPAQueryFactory(em);
QMember m = QMember.member;

List<Member> list = 
        query.selectFrom(m)
             .where(m.age.gt(18))
             .orderBy(m.name.desc())
             .fetch();
```
* 문자가 아닌 자바코드로 JPQL을 작성할 수 있다.
* JPQL 빌더 역할
* 컴파일 시점에 문법 오류를 찾을 수 있다.
* 동적쿼리 작성 편리하다.
* **_단순하고 쉽다._**
* **_실무 사용 권장_**
* http://querydsl.com

<br>

### 네이티브 SQL 소개
```java
String sql = "SELECT ID, AGE, TEAM_ID, NAME FROM MEMBER WHERE NAME = 'kim'";
List<Member> resultList = em.createNativeQuery(sql, Member.class).getResultList();
```
* JPA가 제공하는 SQL을 직접 사용하는 기능
* JPQL로 해결할 수 없는 특정 데이터베이스에 의존적인 기능
* 예) 오라클 CONNECT BY, 특정 DB만 사용하는 SQL 힌트

<br>

### JDBC 직접 사용, SpringJdbcTemplate 등
* JPA를 사용하면서 JDBC 커넥션을 직접 사용하거나, 스프링 JdbcTemplate, 마이바티스등을 함께 사용 할 수 있다.
* 단 영속성 컨텍스트를 적절한 시점에 강제로 플러시 필요(```em.flush();```)
* 예) JPA를 우회해서 SQL을 실행하기 직전에 영속성 컨텍스트 수동 플러시

<br>

### 정리
실무에서는 약 95%는 ```JPQL + QueryDSL```로 작성하고, 엄청나게 복잡한 통계성 쿼리는 SpringJdbcTemplate나 네이티브 SQL로 처리.
그런데 그 마저도 웬만하면 다 ```JPQL + QueryDSL```로 해결가능하다. 