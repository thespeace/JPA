package jpql;

import jakarta.persistence.*;
import jpql.entity.Member;

import java.util.List;

public class JpqlMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpql");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Member member = new Member();
            member.setUsername("member1");
            member.setAge(10);
            em.persist(member);

            em.flush();
            em.clear();

            //TypeQuery, Query
            TypedQuery<Member> query1 = em.createQuery("select m from Member m", Member.class);
            TypedQuery<String> query2 = em.createQuery("select m.username from Member m where m.id = 1", String.class);
            Query query3 = em.createQuery("select m.username, m.age from Member m");

            //결과 조회 API - 리스트
            List<Member> resultList = query1.getResultList();
            for (Member member1 : resultList) {
                System.out.println("member1 = " + member1);
            }

            //결과 조회 API - 단일
            String singleResult1 = query2.getSingleResult();
            System.out.println("singleResult1 = " + singleResult1);

            //파라미터 바인딩 - 이름 기준
            Member singleResult2 = em.createQuery("select m from Member m where m.username = :username", Member.class)
                    .setParameter("username", "member1")
                    .getSingleResult();
            System.out.println("singleResult2 = " + singleResult2.getUsername());

            //파라미터 바인딩 - 위치 기준(사용 하지 않는 것을 권장)
            Member singleResult3 = em.createQuery("select m from Member m where m.username = ?1", Member.class)
                    .setParameter(1, "member1")
                    .getSingleResult();
            System.out.println("singleResult3 = " + singleResult3.getUsername());



            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }

        emf.close();
    }
}
