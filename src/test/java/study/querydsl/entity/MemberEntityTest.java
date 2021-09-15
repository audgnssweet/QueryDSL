package study.querydsl.entity;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
public class MemberEntityTest {

    @PersistenceContext
    private EntityManager em;

    private JPAQueryFactory query;

    @BeforeEach
    void init() {
        query = new JPAQueryFactory(em);
    }

    @Disabled
    @Test
    void save() {
        Team team1 = new Team("팀1");
        Team team2 = new Team("팀2");

        em.persist(team1);
        em.persist(team2);

        Member member1 = new Member("멤버1", 10, team1);
        Member member2 = new Member("멤버2", 20, team1);
        Member member3 = new Member("멤버3", 30, team2);
        Member member4 = new Member("멤버4", 40, team2);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();

        List<Member> members = em.createQuery("select m from Member m", Member.class)
            .getResultList();

        for (Member member : members) {
            System.out.println(member.getUsername() + ", " + member.getTeam().getName());
        }
    }

    @Disabled
    @Test
    void findOne() {
        Member member1 = em.createQuery("select m from Member m where m.username = :username", Member.class)
            .setParameter("username", "멤버1")
            .getSingleResult();

        System.out.println(member1.getUsername());

        em.clear();

        Member member2 = query.select(member)
            .from(member)
            .where(member.username.eq("멤버1"))
            .fetchOne();

        System.out.println(member2.getUsername());
    }

    @Disabled
    @Test
    void search() {
        Member foundMember = query.selectFrom(QMember.member)
            .where(
                member.username.eq("멤버1"),
                QMember.member.age.eq(10)
            )
            .fetchOne();

        System.out.println(foundMember.getUsername());
    }

    @Disabled
    @Test
    void fetchResult() {
        QueryResults<Member> results = query.selectFrom(member)
            .fetchResults();

        System.out.println(results.getTotal());
        System.out.println(results.getOffset());
        System.out.println(results.getLimit());
    }

    @Disabled
    @Test
    void fetchCount() {
        long count = query.selectFrom(member)
            .fetchCount();
        System.out.println(count);
    }

    @Disabled
    @Test
    void sort() {
        em.persist(new Member(null, 100, null));
        em.persist(new Member("멤버5", 100, null));
        em.persist(new Member("멤버6", 100, null));

        em.flush();
        em.clear();

        List<Member> res = query.selectFrom(member)
            .where(member.age.eq(100))
            .orderBy(member.age.desc(), member.username.asc().nullsLast())
            .fetch();

        for (Member member : res) {
            if (member.getUsername() == null) {
                System.out.println("null");
                continue;
            }
            System.out.println(member.getUsername());
        }
    }

    @Disabled
    @Test
    void paging() {
        List<Member> page = query.selectFrom(member)
            .orderBy(member.username.desc())
            .offset(1)
            .limit(2)
            .fetch();

        em.clear();

        QueryResults<Member> results = query.selectFrom(member)
            .orderBy(member.username.desc())
            .offset(1)
            .limit(2)
            .fetchResults();
    }

    @Disabled
    @Test
    void statistics() { //실무에서는 DTO로 뽑아오는 방식을 사용
        List<Tuple> res = query.select(
                member.count(),
                member.age.sum(),
                member.age.avg(),
                member.age.max(),
                member.age.min()
            )
            .from(member)
            .fetch();

        Tuple tuple = res.get(0);
        tuple.get(member.count());
        tuple.get(member.age.sum());
        tuple.get(member.age.avg());
        tuple.get(member.age.max());
        tuple.get(member.age.min());
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Disabled
    @Test
    void group() throws Exception {
        List<Tuple> res = query.select(team.name, member.age.avg())
            .from(member)
            .join(member.team, team)
            .groupBy(team.name)
            .having(member.age.avg().goe(20))
            .fetch();

        for (int i = 0; i < res.size(); i++) {
            System.out.println(res.get(i).get(team.name) + ", " + res.get(i).get(member.age.avg()));
        }
    }

    /**
     * team1에 속한 member들을 조회해라
     */
//    @Disabled
    @Test
    void join() throws Exception {
//        List<Member> members = query.selectFrom(member)
//                .join(member.team, team)
//                .leftJoin(member.team, team)
//                .where(team.name.eq("team1"))
//                .fetch();

        List<Member> res = em.createQuery("select m from Member m left join m.team t on t.name = :teamName", Member.class)
                .setParameter("teamName", "team1")
                .getResultList();

        for (Member re : res) {
            System.out.println(re.getUsername());
        }

//        for (Member member : members) {
//            System.out.println(member.getUsername());
//        }
    }

    /**
     * team과 이름이 같은 회원을 조회해라.
     */
    @Disabled
    @Test
    void thetaJoin() throws Exception {
        //세타조인 = 연관관계 없는 조인
        //먼저 member랑 team이랑 합친 다음에 where로 필터링
        //일단 다가져와서 다합치고(카타시안곱) 필터링하는데, DB가 알아서 최적화를 한다.
        //세타조인은 join on 을 이용해서만 외부조인 가능
        //세타조인은 명시적으로 join을 호출하지 않는다.

        //given
        em.persist(new Member("team1", 20, null));
        em.persist(new Member("team2", 20, null));

        em.flush();
        em.clear();

        //when & then
        List<Member> members = query.select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        for (Member member : members) {
            System.out.println(member.getUsername());
        }
    }

    /**
     * on - 조인 대상 필터링, 세타조인시에 외부조인 위해 사용
     */

    /**
     * 팀 이름이 team1인 팀의 팀과 회원 전부를 가져오기
     * JPQL: select m, t from Member m left join m.team t on t.name = 'team1'
     */
    @Disabled
    @Test
    void onJoin() throws Exception {
        List<Tuple> res = query.select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("team1"))
                .fetch();
        for (Tuple r : res) {
            System.out.println(r);
        }
    }

}
