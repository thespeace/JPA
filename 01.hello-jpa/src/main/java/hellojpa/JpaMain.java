package hellojpa;

import hellojpa.advancedMapping.joinStrategy.Movie;
import hellojpa.permanenceTransition.Child;
import hellojpa.permanenceTransition.Parent;
import jakarta.persistence.*;

import java.util.List;

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
