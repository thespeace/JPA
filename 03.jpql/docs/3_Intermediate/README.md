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

