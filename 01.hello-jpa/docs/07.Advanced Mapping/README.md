# 고급 매핑

<br>

# 상속관계 매핑
* 관계형 데이터베이스는 객체의 상속 관계와 같은 상속 관계가 없다. 그나마 비슷한 모델이 있는데, 그건 슈퍼타입-서브타입 관계라는 모델링 기법이 객체 상속과 유사하다.
* 상속관계 매핑이란? 
  * 객체의 상속과 구조와 DB의 슈퍼타입-서브타입 관계를 매핑하는 것.  
    ![Inheritance relationship mapping example](../../img/Inheritance%20relationship%20mapping%20example.PNG)

<br>

## 슈퍼타입-서브타입 논리 모델을 실제 물리 모델로 구현하는 방법

### 1. 조인 전략(각각 테이블로 변환)
  ![join strategy](../../img/join%20strategy.PNG)
   * ```@Inheritance(strategy=InheritanceType.JOINED)```  + (```@DiscriminatorColumn(name=“DTYPE”)```, ```@DiscriminatorValue(“value”)```)
   * **_기본적으로 해당 전략을 정석으로 보고 주로 사용한다._**(객체와 잘 맞고, 정규화도 가능하고 설계도 깔끔하게 된다)
> * 장점
>   * 테이블 정규화
>   * 외래 키 참조 무결성 제약조건 활용 가능하다
>   * 저장공간 호율화
> * 단점
>   * 조회시 조인을 많이 사용, 성능 저하
>   * 조회 쿼리가 복잡하다
>   * 데이터 저장시 INSERT SQL 2번 호출

<br>

### 2. 단일 테이블 전략(통합 테이블로 변환)
  ![single table strategy](../../img/single%20table%20strategy.PNG)
   * ```@Inheritance(strategy=InheritanceType.SINGLE_TABLE)``` + ```@DiscriminatorColumn(name=“DTYPE”)```
   * ```DTYPE``` 컬럼이 없으면 데이터 구분이 안되기 때문에 필수로 생성된다.(```@DiscriminatorColumn``` 생략 가능)
   * **_기본적으로는 조인 전략을 사용하지만, 단일 테이블 전략의 장단점과 비교하여 애플리케이션에 맞는 방식을 선택하는 것이 좋다._**
     * ex) 비즈니스적으로 중요하고 복잡하다면 조인 전략 사용 <-> 확장 가능성이 없는 단순한 데이터를 다룰때 주로 단일 테이블 전략을 사용
> * 장점
>   * 조인이 필요 없으므로 일반적으로 조회 성능이 빠르다
>     * 조회 쿼리가 단순함
> * 단점
>   * 자식 엔티티가 매핑한 컬럼은 모두 null 허용
>   * 단일 테이블에 모든 것을 저장하므로 테이블이 커질 수 있다. 상황에 따라서 조회 성능이 오히려 느려질 수 있다.(임계점을 넘는다면-)

<br>

### 3. 구현 클래스마다 테이블 전략(서브타입 테이블로 변환)
  ![Table strategy for each implementation class](../../img/Table%20strategy%20for%20each%20implementation%20class.PNG)
  * ```@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)```
  * **_이 전략은 데이터베이스 설계자와 ORM 전문가 둘 다 추천하지 않는다._**
>  * 장점
>    * 서브 타입을 명확하게 구분해서 처리할 때 효과적이다
>    * not null 제약조건 사용 가능하다
>  * 단점
>    * 여러 자식 테이블을 함께 조회할 때 성능이 느리다(UNION SQL 필요)
>    * 자식 테이블을 통합해서 쿼리하기 어렵다
>    * 시스템 변경의 관점에서 상당히 비용이 많이 든다

