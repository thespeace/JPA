# 플러시(Flush)
영속성 컨텍스트와 데이터베이스 간의 상호작용을 관리하는 핵심 메커니즘 중 하나로 **영속성 컨텍스트의 변경 내용을 데이터베이스에 반영(동기화)** 하는 과정을 뜻한다.

<br>

### 플러시 발생
1. 변경 감지(Dirty Checking)
2. 수정된 엔티티 쓰기 지연 SQL 저장소에 등록
3. 쓰기 지연 SQL 저장소의 쿼리를 데이터베이스에 전송(등록, 수정, 삭제 쿼리)
* 중요한 점은, 플러시가 발생한다고 해서 데이터베이스 트랜잭션이 커밋되는 건 아니다. 반대로 데이터베이스 트랜잭션이 커밋되면 플러시가 자동으로 발생한다.
  따라서 **플러시는 트랜잭션의 커밋(Commit)과는 다르다.**

<br>

### 영속성 컨텍스트를 플러시하는 방법
* ```em.flush()```: 직접 호출
  ```java
  EntityManager em = ...;
  em.getTransaction().begin();

  Order order = new Order();
  order.setId(1L);
  em.persist(order);  // 여기서는 데이터베이스에 반영되지 않음

  em.flush();  // 여기서 실제로 데이터베이스에 반영됨

  em.getTransaction().commit();  // 트랜잭션이 커밋되며 최종적으로 완료
  ```
  * Q.```flush()```를 하게 되면 1차 캐시가 다 지워질까?  
    1차 캐시는 그대로 다 유지가 되고 영속성 컨텍스트에 있는 쓰기 지연 SQL 저장소에 있는 쿼리를 DB에 반영하는 과정으로 보면 된다.
* 트랜잭션 커밋: 플러시 자동 호출
* JPQL 쿼리 실행: 플러시 자동 호출

<br>

### JPQL 쿼리 실행시 플러시가 자동으로 호출되는 이유
```java
em.persist(memberA);
em.persist(memberB);
em.persist(memberC);

//중간에 JPQL 실행
query = em.createQuery("select m from Member m", Member.class);
List<Member> members= query.getResultList();
```
* 멤버 A,B,C를 DB에 반영한 다음 바로 조회를 하고자 한다. 위 코드는 조회가 될까?  
  JPQL은 실행 시 플러시가 자동으로 발생해서 조회가 가능하다.  
  그 이유는 영속성 컨텍스트와 데이터베이스 간의 상태 불일치를 방지하고자, 데이터의 일관성을 보장하고자 자동으로 플러시를 실행하고 쿼리를 실행해준다.

<br>

### 플러시 모드 옵션
```java
em.setFlushMode(FlushModeType.COMMIT) //사실 쓸 일이 거의 없다
```
* ```FlushModeType.AUTO```: 커밋이나 쿼리를 실행할 때 플러시(기본값)
* ```FlushModeType.COMMIT```: 커밋할 때만 플러시, JPQL은 플러시 X

<br>

### 플러시 정리
* 영속성 컨텍스트를 비우지 않음
* 영속성 컨텍스트의 변경내용을 데이터베이스에 동기화
* 트랜잭션이라는 작업 단위가 중요 -> 커밋 직전에만 동기화하면 된다.