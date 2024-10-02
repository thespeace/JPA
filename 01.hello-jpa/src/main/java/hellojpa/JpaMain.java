package hellojpa;

import hellojpa.advancedMapping.joinStrategy.Movie;
import hellojpa.permanenceTransition.Child;
import hellojpa.permanenceTransition.Parent;
import hellojpa.valueType.collection.AddressEntity;
import hellojpa.valueType.collection.CollectionMember;
import hellojpa.valueType.embedded.Address;
import hellojpa.valueType.embedded.EmbeddedMember;
import hellojpa.valueType.embedded.Period;
import jakarta.persistence.*;

import java.util.List;
import java.util.Set;

public class JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager(); //get Database Connection.

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            /**
             * JPA 정석 코드
             */
            //추가
            Member member = new Member();
            member.setId(1L);
            member.setName("HelloA");

            em.persist(member);

            //수정
            Member findMember = em.find(Member.class, 1L);
            System.out.println("findMember.getId() = " + findMember.getId());
            System.out.println("findMember.getName() = " + findMember.getName());
            findMember.setName("HelloB");

            //따로 저장 X : JPA를 통해서 가져온 엔티티는 JPA가 관리, tx를 커밋하는 시점에서 체크하여 변경된 부분을 캐치하여 Update쿼리 실행.(== JAVA Collection 설계)
//            em.persist(findMember);

            //삭제
            em.remove(findMember);



            /**
             * JPQL: 전체 회원 검색
             */
            List<Member> result = em.createQuery("select m from Member as m", Member.class)
                    .getResultList();

            for (Member m : result) {
                System.out.println("m.getName() = " + m.getName());
            }



            /**
             * 상속 관계 매핑 예시
             */
            Movie movie = new Movie();
            movie.setDirector("봉준호");
            movie.setActor("송강호");
            movie.setName("기생충");
            movie.setPrice(18000);

            em.persist(movie);

            //영속성 컨텍스트 초기화.
            em.flush();
            em.clear();

            Movie findMovie = em.find(Movie.class, movie.getId());
            System.out.println("findMovie = " + findMovie);



            /**
             * 영속성 전이(CASCADE) 예시
             */
            Child child1 = new Child();
            Child child2 = new Child();

            Parent parent = new Parent();
            parent.addChild(child1);
            parent.addChild(child2);

            em.persist(parent);
//            em.persist(child1); //CASCADE 설정으로 인해 생략가능.
//            em.persist(child2);

            em.flush();
            em.clear();

            //orphanRemoval = true로 인해 해당 컬렉션에서 빠진 것은 삭제가 된다.
            Parent findParent = em.find(Parent.class, parent.getId());
            findParent.getChildList().remove(0);



            /**
             * 임베디드 타입 사용 예시
             */
            EmbeddedMember embeMember = new EmbeddedMember();
            embeMember.setUsername("hello");
            embeMember.setHomeAddress(new Address("city", "street", "10000"));
            embeMember.setWorkPeriod(new Period());

            em.persist(embeMember);



            /**
             * 값 타입 컬렉션 사용 예시
             */
            CollectionMember cMember = new CollectionMember();
            cMember.setUsername("member1");
            cMember.setHomeAddress(new Address("homeCity", "steet", "10000"));

            cMember.getFavoriteFoods().add("치킨");
            cMember.getFavoriteFoods().add("족발");
            cMember.getFavoriteFoods().add("피자");

            cMember.getAddressHistory().add(new Address("old1", "steet", "10000"));
            cMember.getAddressHistory().add(new Address("old2", "steet", "10000"));

            em.persist(cMember); //저장(영속성 전이 + 고아 객체 기능 필수)

            em.flush();
            em.clear();

            System.out.println("=========== 조회 START ===========");
            CollectionMember findCMember = em.find(CollectionMember.class, cMember.getId());

            List<Address> addressHistory = findCMember.getAddressHistory();
            for (Address address : addressHistory) {
                System.out.println("address.getCity() = " + address.getCity()); //지연 로딩
            }

            Set<String> favoriteFoods = findCMember.getFavoriteFoods();
            for (String favoriteFood : favoriteFoods) {
                System.out.println("favoriteFood = " + favoriteFood); //지연 로딩
            }

            //수정: homeCity -> newCity
//            findCMember.getHomeAddress().setCity("newCity"); //옳지 않은 방법, 사이드 이펙트 발생 가능.

            //값 타입은 통째로 갈아 끼워야 한다.
            Address old = findCMember.getHomeAddress();
            findCMember.setHomeAddress(new Address("newCity", old.getStreet(), old.getZipcode()));

            //치킨 -> 한식
            findCMember.getFavoriteFoods().remove("치킨");
            findCMember.getFavoriteFoods().add("한식");

            //주소
            System.out.println("=========== 주소 변경 ===========");
            findCMember.getAddressHistory().remove(new Address("old1", "steet", "10000")); //equals()를 사용.
            findCMember.getAddressHistory().add(new Address("newCity1", "steet", "10000"));
            //의도한대로 수정이 되었지만, 쿼리가 old1 데이터를 삭제하고, newCity1 데이터를 삽입하는게 아니라-
            //테이블 전체를 삭제한 후에 남아있어야할 데이터만 삽입하는 식으로 동작한다.(값 타입 컬렉션의 제약사항)

            //값 타입 -> 엔티티 승급 사용(실무에서 많이 사용)
            cMember.getNewAddressHistory().add(new AddressEntity("newCity2", "steet", "10000"));
            cMember.getNewAddressHistory().add(new AddressEntity("newCity3", "steet", "10000"));

            em.persist(cMember);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            //EntityManager가 내부적으로 DB Connection을 가지고 동작하기 때문에 꼭 닫아주어야 함
            em.close();
        }

        emf.close();
    }
}
