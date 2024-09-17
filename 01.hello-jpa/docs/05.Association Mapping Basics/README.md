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

<br>

---

<br>

## 단방향 연관관계

* ### 객체 지향 모델링(객체 연관관계 사용)
  ![One-way modeling](../../img/One-way%20modeling.PNG)
  
<br>

* ### 객체의 참조와 테이블의 외래 키를 매핑.
  ```java
  @Entity
  public class Member {
  
      @Id @GeneratedValue
      private Long id;
  
      @Column(name = "USERNAME")
      private String name;
      private int age;
  
      // @Column(name = "TEAM_ID")
      // private Long teamId;
  
      @ManyToOne
      @JoinColumn(name = "TEAM_ID")
      private Team team;
  
      ...
  ```
  
<br>

* ### ORM 매핑  
  ![One-way ORM mapping](../../img/One-way%20ORM%20mapping.PNG)

<br>

* ### 연관관계 저장
  ```java
  //팀 저장
  Team team = new Team();
  team.setName("TeamA");
  em.persist(team);

  //회원 저장
  Member member = new Member();
  member.setName("member1");
  member.setTeam(team); //단방향 연관관계 설정, 참조 저장(jpa가 team에서 pk값을 꺼내 사용)
  em.persist(member);
  ```
  
<br>

* ### 참조로 연관관계 조회(객체 그래프 탐색)
  ```java
  //조회
  Member findMember = em.find(Member.class, member.getId());

  //참조를 사용해서 연관관계 조회
  Team findTeam = findMember.getTeam();
  ```
  
<br>

* ### 연관관계 수정
  ```java
  // 새로운 팀B
  Team teamB = new Team();
  teamB.setName("TeamB");
  em.persist(teamB);
  
  // 회원1에 새로운 팀B 설정(외래키 변경)
  member.setTeam(teamB);
  ```
  
<br>

---

<br>

## 양방향 매핑    
아래 그림을 살펴보면 양방향으로 객체를 참조하게 연관관계를 만드려고 하는데, 테이블 연관관계는 단방향 연관관계와 똑같다.  
이유는 DB에는 ```join```이 있기때문에 테이블의 연관관계에서는 외래키로 양방향 연관관계가 가능하다. **_사실상 테이블의 연관관계에는 방향이라는 개념 자체가 없다_**.(쉽게 말해 외래키가 지정되어 있으면 서로에 대해 다 알 수 있다.)

하지만 객체는 이와 다르게 ```Member```에서 ```Team```을 갖고 있기때문에 ```Team```에 대해 알 수 있는 것이었다. ```Team```에서도 ```Member```를 알기 위해서는 ```members```라는 객체를 작성해줘야 서로에 대해 양방향으로 연관관계를 맺을 수가 있다.

이러한 차이가 **_객체 참조와 테이블의 외래키의 가장 큰 차이_** 이다.  
![Bidirectional mapping](../../img/Bidirectional%20mapping.PNG)

<br>

* ### 양방향 매핑(```Team``` 엔티티는 컬렉션 추가, ```Member``` 엔티티는 단방향과 동일)
  ```java
  @Entity
  public class Team {
  
      @Id @GeneratedValue
      private Long id;
  
      private String name;
  
      @OneToMany(mappedBy = "team") //매핑되어 있는 객체의 변수명 작성.
      List<Member> members = new ArrayList<Member>(); //초기화는 관례, add의 nullpointException 방지.
      ...
  ```
  
<br>

* ### 반대 방향으로 객체 그래프 탐색
  ```java
  //조회
  Team findTeam = em.find(Team.class, team.getId());
  
  int memberSize = findTeam.getMembers().size(); //역방향 조회
  ```
  
<br>

## 연관관계의 주인과 ```mappedBy```
* mappedBy = JPA의 멘탈붕괴 난이도
* mappedBy는 처음에는 이해하기 어렵다.
* 객체와 테이블간에 연관관계를 맺는 차이를 이해해야 한다.

<br>

### 객체와 테이블이 관계를 맺는 차이
* 객체 연관관계 = 2개
  * 회원 -> 팀 : 연관관계 1개(단방향)
  * 팀 -> 회원 : 연관관계 1개(단방향)
* 테이블 연관관계 = 1개
  * 회원 <-> 팀의 연관관계 1개(양방향, 사실은 방향이 없는 것)

<br>

### 객체의 양방향 관계
* **_객체의 양방향 관계는 사실 양방향 관계가 아니라 서로 다른 단방향 관계 2개_** 다.
* 따라서 객체를 양방향으로 참조하려면 **_단방향 연관관계를 2개_** 만들어야 한다.
  ```java
  class A {
      B b;
  }
  class B {
      A a;
  }
  // A -> B : a.getB()
  // B -> A : b.getA()
  ```
  
<br>

### 테이블의 양방향 연관관계
* 테이블은 **_외래 키 하나_** 로 두 테이블의 연관관계를 관리
* MEMBER.TEAM_ID 외래 키 하나로 양방향 연관관계를 가진다.(양쪽으로 조인할 수 있다.)
  ```sql
  SELECT *
  FROM MEMBER M
  JOIN TEAM T ON M.TEAM_ID = T.TEAM_ID
  
  SELECT *
  FROM TEAM T
  JOIN MEMBER M ON T.TEAM_ID = M.TEAM_ID
  ```
  
<br>

### 둘 중 하나로 외래 키를 관리해야 한다.
연관 관계를 관리하는 포인트는 외래 키인데, 양방향 관계를 맺으면서 객체 서로가 외래 키를 가질 수 있게 되었다.  
따라서 두 객체 중 하나의 객체가 외래키를 관리해야 하는데, **_외래 키를 관리하는 객체를 연관관계의 주인_** 이라 한다.  
우리의 예시를 빗대어 설명하자면, 멤버에 있는 팀으로 외래키를 관리할지 아니면 팀에 있는 members로 외래키를 관리할지 둘 중에 하나를 주인으로 정해야 한다. 이것이 바로 연관관계의 주인이다.  
![Foreign key management dilemma](../../img/Foreign%20key%20management%20dilemma.PNG)

<br>

### 연관관계의 주인(Owner)
연관관계의 주인이라는 개념은 사실 양방향 맵핑에서 나오는 것이다.
* 객체의 두 관계중 하나를 연관관계의 주인으로 지정해야 한다.
* **_연관관계의 주인만이 외래 키를 관리(등록, 수정, 삭제)할 수 있다._**
* **_주인이 아닌쪽은 읽기만 가능하다._**
* 주인은 ```mappedBy``` 속성을 사용할 수 없다.
* 주인이 아니면 mappedBy 속성으로 주인을 지정해야 한다.

<br>

### 누구를 주인으로 지정해야 할까?
* 외래 키가 있는 곳을 주인으로 정해라!(가이드)
  * 외래 키를 가진 테이블과 매핑되는 엔티티가 외래 키를 관리하는 것이 다방면으로 효율적이다.
* 우리의 예시에서는 ```Member.team```이 연관관계의 주인  
  ![Master of association](../../img/Master%20of%20association.PNG)

<br>

### 양방향 매핑시 가장 많이 하는 실수
연관관계의 주인에 값을 입력하지 않음
  ```java
  Team team = new Team();
  team.setName("TeamA");
  em.persist(team);
  
  Member member = new Member();
  member.setName("member1");
  
  //역방향(주인이 아닌 방향)만 연관관계 설정
  team.getMembers().add(member);
  
  em.persist(member);
  ```

<br>

### 양방향 매핑시 연관관계의 주인에 값을 입력해야 한다.
순수한 객체 관계를 고려하면 항상 양쪽다 값을 입력해야 한다.

  ```java
  Team team = new Team();
  team.setName("TeamA");
  em.persist(team);
  
  Member member = new Member();
  member.setName("member1");
  
  team.getMembers().add(member);
  //연관관계의 주인에 값 설정
  member.setTeam(team);
  
  em.persist(member);
  ```

<br>

### 양방향 연관관계 주의 사항
* **_"순수 객체 상태를 고려해서 항상 양쪽에 값을 설정하자"_**
* 연관관계 편의 메소드를 생성하자
  ```java
  /** 
   *  Member.java || Team.java
   *  연관관계 객체 중 하나에 해당 메서드(연관관계 편의 메서드)를 원자적으로 사용해서
   *  연관관계가 걸려있는 양쪽의 값을 설정하는 메서드를 작성하여 사용할 수도 있다.
   *  사용한다면 둘 중에 하나에만 작성하자. 무한루프와 여러 오류를 발생시킬 수도 있음.
   *  getter setter 관례로 인해 메서드 이름을 변경. (setTeam -> changeTeam)
   **/
  public void changeTeam(Team team) {
      this.team = team;
      team.getMembers().add(this);
  }
  ```
* 양방향 매핑시에 무한 루프를 조심하자
  * ex) toString(), lombok
  * ex) JSON 생성 라이브러리
    * 1.무한루프
    * 2.API 스펙 변경
    * 해당 문제를 해결하기 위해서는 controller에서 값 반환시 Entity 자체를 반환하지 말고 DTO(단순하게 값만 있는)로 변환해서 반환하면 해당 문제는 해결된다.

<br>

### 양방향 매핑 정리
* **_단방향 매핑만으로도 이미 연관관계 매핑은 완료가 된 것._**
* 양방향 매핑은 반대 방향으로 조회(객체 그래프 탐색) 기능이 추가 된 것 뿐이다.
* 설계 관점에서보면 객체 입장에서 양방향 매핑은 장점이 하나도 없다. 이미 단방향 매핑만으로도 이미 설계가 끝이 난 것인데, 그렇다면 양방향 매핑은 왜 필요한 것이냐면 막상 실무에서 JPA를 사용하다보면 역방향으로 탐색할 일이 많아 진다.(+JPQL) 그렇기에 필요한 것이다.
* 단방향 매핑을 잘 해놓으면 양방향은 필요할 때 추가해도 된다.(테이블에 영향을 주지 않음)

<br>

### 연관관계의 주인을 정하는 기준
* 비즈니스 로직을 기준으로 연관관계의 주인을 선택하면 안된다.
* **_연관관계의 주인은 외래 키의 위치를 기준으로 정해야 한다._**