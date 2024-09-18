# 실전 예제 - 1. 요구사항 분석과 기본 매핑

<br>

## 요구사항 분석
* 회원은 상품을 주문할 수 있다.
* 주문 시 여러 종류의 상품을 선택할 수 있다.

<br>

## 기능 목록
* 회원 기능
  * 회원 등록
  * 회원 조회
* 상품 기능
  * 상품 등록
  * 상품 수정
  * 상품 조회
* 주문 기능
  * 상품 주문
  * 주문 내역 조회
  * 주문 취소

  ![Feature list](./img/Feature%20list.PNG)

<br>

## 도메인 모델 분석
* **회원과 주문의 관계**: 회원은 여러 번 주문할 수 있다. (일대다)
* **주문과 상품의 관계**: 주문할 때 여러 상품을 선택할 수 있다. 반대로 같은 상품도 여러 번 주문될 수 있다.  
                       주문상품 이라는 모델을 만들어서 다대다 관계를 일다대, 다대일 관계로 풀어냄  

  ![Domain model analysis](./img/Domain%20model%20analysis.PNG)

<br>

## 테이블 설계
![Table design](./img/Table%20design.PNG)

<br>

## 엔티티 설계와 매핑
![Entity Design and Mapping](./img/Entity%20Design%20and%20Mapping.PNG)

<br>

## 데이터 중심 설계의 문제점
* 현재 방식은 객체 설계를 테이블 설계에 맞춘 방식
* 테이블의 외래키를 객체에 그대로 가져온다. 그래서 객체 그래프 탐색이 불가능
* 참조가 없으므로 UML도 잘못됐다.
* 따라서 다음에는 ***연관 관계 매핑***을 적용해서 이러한 문제점을 해소해보자.

<br>

---

# 실전 예제 - 2. 연관관계 매핑 시작

<br>

## 테이블 구조
* 테이블 구조는 이전과 같다.

<br>

## 객체 구조
* 참조를 사용하도록 변경.  
  ![Start association-object structure](./img/Start%20association-object%20structure.PNG)