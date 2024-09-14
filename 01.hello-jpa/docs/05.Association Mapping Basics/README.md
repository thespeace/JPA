# 연관관계 매핑 기초

<br>

## 연관관계 매핑이란?

**_객체의 참조와 테이블의 외래 키를 매핑하는 것_** 을 의미한다.  
RDBMS에 초점을 맞춰서 설계하는 방식이 아니라 객체에 맞춰 객체지향스럽게 설계하는데에 초점을 맞춰 설계를 해야한다.

그렇게 하기위해서는 우선 객체가 지향하는 패러다임과 RDBMS가 지향하는 패러다임이 다르기 때문에 이 둘 간에서의 차이에서 오는 간극에 대해 이해하고 넘어가야한다.

1. **_우선 객체와 테이블 연관관계의 차이를 이해를 해야 한다._**  
   객체는 레퍼런스, 즉 Member.getTeam() 이런 식으로 찾아 갈 수 있는데, DB의 테이블은 특정 컬럼과 연관된 컬럼을 특정하기 위해 외래키를 사용한다.
2. 이와 같은 차이점을 매핑한다.  
   **_객체의 참조와 테이블의 외래 키를 매핑_** 해야 하는데, 그 방법을 알아보자.
   

* 용어 이해
  * **_방향_**(Direction): 단방향, 양방향
  * **_다중성_**(Multiplicity): 다대일(N:1), 일대다(1:N), 일대일(1:1), 다대다(N:M) 이해
  * **_연관관계의 주인_**(Owner): 객체 양방향 연관관계는 관리 주인이 필요하다.

<br>

## 연관관계가 필요한 이유

**_"객체지향 설계의 목표는 자율적인 객체들의 협력 공동체를 만드는 것이다."_** -조영호(객체지향의 사실과 오해)

예제를 통해 연관관계가 필요한 이유에 대해 살펴보자.

* ### 시나리오
  * 회원과 팀이 있다.
  * 회원은 하나의 팀에만 소속될 수 있다.
  * 회원과 팀은 다대일 관계다.(N:1)

<br>

* ### 객체를 테이블에 맞추어 모델링(연관관계가 없는 객체)  
  여러 명의 회원이 하나의 팀에 소속될 수 있고, 반대로 하나의 팀에 여러 명의 회원이 소속될 수 있다.  
  ![Model objects to fit tables](../../img/Model%20objects%20to%20fit%20tables.PNG)

<br>

* ### 참조 대신 외래 키를 그대로 사용
  ```java
  @Entity
  public class Member {
      @Id @GeneratedValue
      private Long id;
  
      @Column(name = "USERNAME")
      private String name;
  
      @Column(name = "TEAM_ID")
      private Long teamId;
      ...
  }
  
  @Entity
  public class Team {
  
      @Id @GeneratedValue
      private Long id;
      private String name;
      ...
  }
  ```

<br>  

* ### 외래 키 식별자를 직접 다룬다.(객체 지향스럽지 않다)
  ```java
  //팀 저장
  Team team = new Team();
  team.setName("TeamA");
  em.persist(team);
  
  //회원 저장
  Member member = new Member();
  member.setName("member1");
  member.setTeamId(team.getId());
  em.persist(member);
  ```
  
<br>

* ### 식별자로 다시 조회(객체 지향적인 방법은 아니다)
  ```java
  //조회
  Member findMember = em.find(Member.class, member.getId());
  
  //연관관계가 없음
  Team findTeam = em.find(Team.class, team.getId());
  ```
  
<br>

* ### 객체를 테이블에 맞추어 데이터 중심으로 모델링하면, 협력 관계를 만들 수 없다.
  * **_테이블은 외래 키로 조인_** 을 사용해서 연관된 테이블을 찾는다.
  * **_객체는 참조_** 를 사용해서 연관된 객체를 찾는다.
  * 테이블과 객체 사이에는 이런 큰 간격이 있다.