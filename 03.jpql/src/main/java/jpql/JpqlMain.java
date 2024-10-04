package jpql;

import jakarta.persistence.*;
import jpql.dto.MemberDTO;
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



            //프로젝션 - 여러 값, Query 타입으로 조회
            List resultList1 = em.createQuery("select m.username, m.age from Member m")
                    .getResultList();
            Object o = resultList1.get(0);
            Object[] result1 = (Object[]) o;
            System.out.println("result1(username) = " + result1[0]);
            System.out.println("result1(age) = " + result1[1]);

            //프로젝션 - 여러 값, Query 타입으로 조회
            List<Object[]> resultList2 = em.createQuery("select m.username, m.age from Member m")
                    .getResultList();
            Object[] result2 = resultList2.get(0);
            System.out.println("result1(username) = " + result2[0]);
            System.out.println("result1(age) = " + result2[1]);

            //프로젝션 - 여러 값, new 명령어로 조회
            List<MemberDTO> resultList3 = em.createQuery("select new jpql.dto.MemberDTO(m.username, m.age) from Member m", MemberDTO.class)
                    .getResultList();
            MemberDTO memberDTO = resultList3.get(0);
            System.out.println("memberDTO.getUsername() = " + memberDTO.getUsername());
            System.out.println("memberDTO.getAge() = " + memberDTO.getAge());



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
