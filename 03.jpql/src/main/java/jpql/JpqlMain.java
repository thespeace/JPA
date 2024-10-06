package jpql;

import jakarta.persistence.*;
import jpql.dto.MemberDTO;
import jpql.entity.Member;
import jpql.entity.MemberType;
import jpql.entity.Team;

import java.util.List;

public class JpqlMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpql");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            Team team = new Team();
            team.setName("teamA");
            em.persist(team);

            for (int i = 0; i < 20; i++) {
                Member member = new Member();
                member.setUsername("member" + i);
                member.setAge(i);
                member.setTeam(team);
                member.setType(MemberType.USER);

                em.persist(member);
            }

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



            //페이징 API
            List<Member> result3 = em.createQuery("select m from Member m order by m.age desc", Member.class)
                    .setFirstResult(0)
                    .setMaxResults(10)
                    .getResultList();
            System.out.println("result1.size() = " + result3.size());
            for (Member member1 : result3) {
                System.out.println("member1 = " + member1);
            }



            //조인 - 내부 조인
            String query4 = "select m from Member m join m.team t";
            List<Member> resultList4 = em.createQuery(query4, Member.class)
                    .getResultList();
            System.out.println("resultList4.size() = " + resultList4.size());

            //조인 - 외부 조인
            String query5 = "select m from Member m left join m.team t";
            List<Member> resultList5 = em.createQuery(query5, Member.class)
                    .getResultList();
            System.out.println("resultList5.size() = " + resultList5.size());

            //조인 - 세타 조인
            String query6 = "select m from Member m, Team t where m.username = t.name";
            List<Member> resultList6 = em.createQuery(query6, Member.class)
                    .getResultList();
            System.out.println("resultList6.size() = " + resultList6.size());

            //조인(ON절을 이용) - 조인 대상 필터링
            String query7 = "select m from Member m left join m.team t on t.name = 'teamA'";
            List<Member> resultList7 = em.createQuery(query7, Member.class)
                    .getResultList();
            System.out.println("resultList7.size() = " + resultList7.size());

            //조인(ON절을 이용) - 연관관계 없는 엔티티 외부 조인
            String query8 = "select m from Member m left join Team t on m.username = t.name";
            List<Member> resultList8 = em.createQuery(query8, Member.class)
                    .getResultList();
            System.out.println("resultList8.size() = " + resultList8.size());



            //JPQL 타입 표현 - ENUM
            String query9 = "select m.username, 'HELLO', true from Member m" +
                            "where m.type = :userType";
            List<Object[]> resultList9 = em.createQuery(query9)
                    .setParameter("userType", MemberType.USER)
                    .getResultList();
            for (Object[] objects : resultList9) {
                System.out.println("objects[0] = " + objects[0]);
                System.out.println("objects[1] = " + objects[1]);
                System.out.println("objects[2] = " + objects[2]);
            }



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
