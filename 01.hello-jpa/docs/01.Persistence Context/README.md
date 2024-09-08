# 영속성 관리
JPA를 이해하려면 영속성 컨텍스트(Persistence Context)라는 것을 이해해야 한다.  

<br>

### JPA에서 가장 중요한 2가지
* 객체와 관계형 데이터베이스 매핑하기(Object Relational Mapping)
  * 정적 관점
* **영속성 컨텍스트(Persistence Context)**
  * 동적 관점(내부 동작)

영속성 컨텍스트에 대해 알아보자.

<br>

### 엔티티 매니저 팩토리와 엔티티 매니저
![Entity Manager Factory and Entity Manager](../../img/Entity%20Manager%20Factory%20and%20Entity%20Manager.PNG)

<br>

### 영속성 컨텍스트
* JPA를 이해하는데 가장 중요한 용어
* "엔티티를 영구 저장하는 환경(문맥)"이라는 뜻
* ```EntityManager.persist(entity);```
  * 객체를 DB에 저장한다는 개념 이전에 영속성 컨텍스트를 통해 엔티티를 영속화한다는 개념이 있다
* 영속성 컨텍스트는 논리적인 개념으로 눈에 보이지 않는다
* 엔티티 매니저를 통해서 영속성 컨텍스트에 접근
  * #### J2SE 환경
    * 엔티티 매니저와 영속성 컨텍스트가 1:1  
    ![](../../img/J2SE.PNG)
  * #### J2EE, 스프링 프에미워크 같은 컨테이너 환경
    * 엔티티 매니저와 영속성 컨텍스트가 N:1  
    ![](../../img/J2EE.PNG)

<br>

### 엔티티의 생명주기
![Entity life cycle](../../img/Entity%20life%20cycle.PNG)

* **비영속 (new/transient)**
  * 영속성 컨텍스트와 전혀 관계가 없는 새로운 상태  
  ![](../../img/new,transient.PNG)
    ```java
    //객체를 생성한 상태(비영속)
    Member member = new Member();
    member.setId("member1");
    member.setUsername("회원1");
    ```
* **영속 (managed)**
  * 영속성 컨텍스트에 관리되는 상태  
  ![managed](../../img/managed.PNG)
    ```java
    //객체를 생성한 상태(비영속)
    Member member = new Member();
    member.setId("member1");
    member.setUsername("회원1");
  
    EntityManager em = emf.createdEntityManager();
    em.getTransaction().begin();
  
    //객체를 저장한 상태(영속)
    em.persist(member);
    ```
* **준영속 (detached)**
  * 영속성 컨텍스트에 저장되었다가 분리된 상태
    ```java
    //회원 엔티티를 영속성 컨텍스트에서 분리, 준영속 상태
    em.detach(member);
    ```
* **삭제 (removed)**
  * 삭제된 상태
    ```java
    //객체를 삭제한 상태(삭제)
    em.remove(member);
    ```
    
<br>

## 영속성 컨텍스트의 이점
* ### 1차 캐시
  ![Entity lookup, primary cache](../../img/Entity%20lookup,%20primary%20cache.PNG)
    ```java
    //엔티티를 생성한 상태(비영속)
    Member member = new Member();
    member.setId("member1");
    member.setUsername("회원1");
  
    //엔티티를 영속
    em.persist(member);
    ```
    * 1차 캐시에서 조회
      ```java
      Member member = new Member();
      member.setId("member1");
      member.setUsername("회원1");
      
      //1차 캐시에 저장됨
      em.persist(member);
      
      //1차 캐시에서 조회
      Member findMember = em.find(Member.class, "member1");
      ```
      ![Lookup from primary cache](../../img/Lookup%20from%20primary%20cache.PNG)
    * 데이터베이스에서 조회
      ```java
      Member findMember2 = em.find(Member.class, "member2");
      ```
      ![Lookup in database](../../img/Lookup%20in%20database.PNG)
  * 사실 1차 캐시를 사용함으로써 얻는 성능적 이점으로는 비즈니스 로직이 매우 복잡할 때 약간 이점이 있을뿐 크게 도움은 되지 않는다.  
    하지만 성능적인 이점보다는 이 컨셉이 주는 이점으로 인해 조금 더 객체 지향적으로 코드를 작성하는데 도움이 된다.

<br>

* ### 동일성(identity) 보장
  ```java
  Member a = em.find(Member.class, "member1");
  Member b = em.find(Member.class, "member1");
  
  System.out.println(a == b); //동일성 비교 true
  ```
  * Java Collection과 마찬가지로 영속 엔티티의 동일성을 보장해준다.
  * 쉽게 말해, ```==``` 비교가 가능하다는 뜻으로, 영속성 컨텍스트 내에서 동일한 엔티티 ID를 가진 엔티티는 한 번 조회된 이후에는 1차 캐시에 저장되고, 이후 동일한 엔티티를 조회할 때 데이터베이스에서 다시 읽지 않고 1차 캐시에서 반환되기 때문에 동일한 인스턴스를 반환한다.
  * 이를 조금 더 어렵게 설명하면, 1차 캐시로 반복 가능한 읽기(REPEATABLE READ) 등급의 트랜잭션 격리 수준을 데이터베이스가 아닌 애플리케이션 차원에서 제공한다라고 생각하면 된다.

<br>

* ### 트랜잭션을 지원하는 쓰기 지연(transactional write-behind)
  ```java
  EntityManager em = emf.createEntityManager();
  EntityTransaction transaction = em.getTransaction();
  //엔티티 매니저는 데이터 변경시 트랜잭션을 시작해야 한다.
  transaction.begin(); // [트랜잭션] 시작
  
  em.persist(memberA);
  em.persist(memberB);
  //여기까지 INSERT SQL을 데이터베이스에 보내지 않는다.
  
  //커밋하는 순간 데이터베이스에 INSERT SQL을 보낸다.
  transaction.commit(); // [트랜잭션] 커밋
  ```
  * ```em.persist(memberA);```      
    ![transactional write-behind](../../img/transactional%20write-behind1.PNG)
  * ```em.persist(memberB);```  
    영속성 컨텍스트 안에서 1차 캐시에 저장함과 동시에 ```JPA가 해당 엔티티를 분석하고 쿼리를 생성``` 후, ```쓰기 지연 SQL 저장소```에 쌓아둔다.  
    ![transactional write-behind](../../img/transactional%20write-behind2.PNG)
  * ```transaction.commit();```  
    ```트랜잭션을 커밋```하는 시점에 ```쓰기 지연 SQL 저장소```에 있던 쿼리들이 DB에 flush가 된 후, 실제 DB 트랜잭션이 커밋된다.  
    ![transactional write-behind](../../img/transactional%20write-behind3.PNG)
  * 이러한 점 때문에 버퍼링, JDBC Batch라는 기능을 사용 할 수 있다. 이러한 기능을 잘 활용한다면 기본적인 옵션 하나로 최적화하여 성능을 향상 시킬 수 있다.

<br>

* ### 변경 감지(Dirty Checking)
  ```java
  EntityManager em = emf.createEntityManager();
  EntityTransaction transaction = em.getTransaction();
  transaction.begin(); // [트랜잭션] 시작
  
  // 영속 엔티티 조회
  Member memberA = em.find(Member.class, "memberA");
  
  // 영속 엔티티 데이터 수정
  memberA.setUsername("hi");
  memberA.setAge(10);
  
  //em.update(member) 이런 코드가 있어야 하지 않을까?
  
  transaction.commit(); // [트랜잭션] 커밋
  ```
  ![Dirty Checking](../../img/Dirty%20Checking.PNG)
  * JPA의 목적은 Java 컬렉션을 다루듯이 객체를 다루는 것이기 때문에, 변경할 값을 꺼내고 변경 후 다시 집어넣지 않아도 된다.
  * 어떻게 이런게 가능할까? 내부 매커니즘을 간단하게 알아보면 JPA는 DB 트랜잭션을 커밋하는 시점에 내부적으로 ```flush()```라는게 호출된다. 그러면서 영속성 컨텍스트에 저장되어 있던 스냅샷(값을 읽어온 시점의 상태)과 엔티티를 비교한다.
    비교 후 변경된 엔티티를 감지하면 Update쿼리를 생성 후 쓰기 지연 SQL 저장소에 쌓는다. 이를 변경 감지라 한다. 
  * ```em.remove(memberA); //엔티티 삭제``` 삭제 메커니즘도 이와 같다.

<br>

* ### 지연 로딩(Lazy Loading)
  * 데이터베이스에서 연관된 데이터를 **필요할 때만 조회**해서 성능을 최적화할 수 있다.
    ```java
    //Order와 OrderItem이라는 두 엔티티가 있다.
    //Order 엔티티를 조회할 때마다 모든 OrderItem을 즉시 가져온다면, 주문에 포함된 항목이 많을 경우 성능이 저하될 수 있다.
    //이럴 때 지연 로딩을 사용하면, Order만 먼저 조회하고, 실제로 OrderItem이 필요할 때 그때서야 데이터베이스에서 조회하여 최적화가 가능해진다.
    Order order = em.find(Order.class, 1L);  // Order만 조회
    List<OrderItem> items = order.getItems();  // OrderItem은 이때 조회 (필요할 때)
    ```
    * 처음에는 기본 엔티티만 조회하고, 나중에 연관된 엔티티가 필요할 때만 데이터베이스에서 조회하므로 불필요한 쿼리를 줄일 수 있다.
    * 처음부터 모든 데이터를 메모리에 로드하지 않고, 필요한 시점에만 로드하기 때문에 메모리 사용을 줄일 수 있다.
    * 사용자가 특정 시점에서만 연관 데이터를 조회하도록 할 수 있어서 유연한 데이터 처리가 가능해진다.