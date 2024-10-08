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
            String query9 = "select m.username, 'HELLO', true from Member m " +
                            "where m.type = :userType";
            List<Object[]> resultList9 = em.createQuery(query9).setParameter("userType", MemberType.USER)
                    .getResultList();
            for (Object[] objects : resultList9) {
                System.out.println("objects[0] = " + objects[0]);
                System.out.println("objects[1] = " + objects[1]);
                System.out.println("objects[2] = " + objects[2]);
            }



            //조건식 - CASE 식
            String query10 =
                    "select " +
                            "case when m.age <= 10 then '학생요금' " +
                            "     when m.age >= 60 then '경로요금' " +
                            "     else '일반요금' " +
                            "end " +
                    "from Member m";
            List<String> resultList10 = em.createQuery(query10, String.class)
                    .getResultList();
            for (String s : resultList10) {
                System.out.println("s = " + s);
            }



            //JPQL 함수 - 사용자 정의 함수(FunctionContributer 구현체 사용)
            String query11 = "select function('group_concat', m.username) From Member m";
            List<String> resultList11 = em.createQuery(query11, String.class)
                    .getResultList();
            for (String s : resultList11) {
                System.out.println("s = " + s);
            }



            //페치 조인
            Team teamA = new Team();
            teamA.setName("팀A");
            em.persist(teamA);

            Team teamB = new Team();
            teamB.setName("팀B");
            em.persist(teamB);

            Member member1 = new Member();
            member1.setUsername("회원1");
            member1.setTeam(teamA);
            em.persist(member1);

            Member member2 = new Member();
            member2.setUsername("회원2");
            member2.setTeam(teamA);
            em.persist(member2);

            Member member3 = new Member();
            member3.setUsername("회원3");
            member3.setTeam(teamB);
            em.persist(member3);

            em.flush();
            em.clear();

            String query12 = "select m From Member m join fetch m.team";
            List<Member> resultList12 = em.createQuery(query12, Member.class)
                    .getResultList();
            for (Member member : resultList12) {
                System.out.println("member = " + member.getUsername() + ", " + member.getTeam().getName());
            }

            //컬렉션 페치 조인
            String query13= "select t From Team t join fetch t.members";
            List<Team> resultList13 = em.createQuery(query13, Team.class)
                    .getResultList();
            for (Team teams : resultList13) {
                System.out.println("teams = " + teams.getName() + "| members = " + teams.getMembers().size());
                for (Member member : teams.getMembers()) {
                    System.out.println("-> member = " + member);
                }
            }

            //페치 조인 페이징 사용
            String query14= "select t From Team t";
            List<Team> resultList14 = em.createQuery(query14, Team.class)
                    .setFirstResult(0)
                    .setMaxResults(2)
                    .getResultList();
            System.out.println("resultList14.size() = " + resultList14.size());

            for (Team team1 : resultList14) {
                System.out.println("team1.getName() = " + team1.getName() + "| members = " + team1.getMembers());
                for (Member member : team1.getMembers()) {
                    System.out.println("-> member = " + member);
                }
            }



            //엔티티 직접 사용 - 기본 키 값
            String query15 = "select m From Member m where m.id = :memberId";
            Member findMember1 = em.createQuery(query15, Member.class)
                    .setParameter("memberId", member3.getId())
                    .getSingleResult();
            System.out.println("findMember = " + findMember1);

            //엔티티 직접 사용 - 외래 키 값
            String query16 = "select m From Member m where m.team = :team";
            List<Member> findMember2 = em.createQuery(query16, Member.class)
                    .setParameter("team", teamA)
                    .getResultList();
            for (Member member : findMember2) {
                System.out.println("member = " + member);
            }



            //Named 쿼리 - 정적 쿼리
            List<Member> resultList15 = em.createNamedQuery("Member.findByUsername", Member.class)
                    .setParameter("username", "회원1")
                    .getResultList();
            for (Member member : resultList15) {
                System.out.println("member = " + member);
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
