package study.querydsl.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javassist.expr.Expr;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.MemberProjectionDto;
import study.querydsl.dto.QMemberProjectionDto;
import study.querydsl.dto.UserDto;

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
                member.username.eq("member1"),
                QMember.member.age.eq(10)
            )
            .fetchOne();

        em.clear();

        List<Member> resultList = em.createQuery("select m from Member m where m.username = 'member1' and m.age = 10",
                Member.class)
            .getResultList();
    }

    @Disabled
    @Test
    void fetchResult() {
        QueryResults<Member> results = query.selectFrom(member)
            .fetchResults();

        em.clear();

        em.createQuery("select count(m) from Member m", Long.class).getSingleResult();
        em.createQuery("select m from Member m", Member.class).getResultList();

        System.out.println(results.getTotal());
        System.out.println(results.getOffset());
        System.out.println(results.getLimit());
    }

    @Disabled
    @Test
    void fetchCount() {
        long count = query.selectFrom(member)
            .fetchCount();

        em.createQuery("select count(m) from Member m", Long.class).getSingleResult();

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

        em.clear();

        em.createQuery("select m from Member m where m.age = 100 order by m.age desc, m.username asc nulls last",
                Member.class)
            .getResultList();

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

        em.createQuery("select m from Member m order by m.username desc")
            .setFirstResult(1)
            .setMaxResults(2)
            .getResultList();

//        QueryResults<Member> results = query.selectFrom(member)
//            .orderBy(member.username.desc())
//            .offset(1)
//            .limit(2)
//            .fetchResults();
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

        em.clear();

        Object singleResult = em.createQuery(
                "select count(m), sum(m.age), avg(m.age), max(m.age), min(m.age) from Member m")
            .getSingleResult();

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

        em.clear();

        em.createQuery("select t.name, avg(m.age) from Member m join m.team t"
                + " group by t.name"
                + " having avg(m.age) >= 20")
            .getResultList();

        for (int i = 0; i < res.size(); i++) {
            System.out.println(res.get(i).get(team.name) + ", " + res.get(i).get(member.age.avg()));
        }
    }

    /**
     * team1에 속한 member들을 조회해라
     */
    @Disabled
    @Test
    void join() throws Exception {
        List<Member> members = query.selectFrom(member)
                .join(member.team, team)
                .leftJoin(member.team, team)
                .where(team.name.eq("team1"))
                .fetch();

        em.clear();

        //아래 두 쿼리는 명확히 다른 것 on과 where의 차이 때문에
        List<Member> res2 = em.createQuery("select m from Member m left join m.team t where t.name = :teamName", Member.class)
            .setParameter("teamName", "team1")
            .getResultList();

        em.clear();

        List<Member> res = em.createQuery("select m from Member m left join m.team t on t.name = :teamName", Member.class)
                .setParameter("teamName", "team1")
                .getResultList();

        em.clear();

        List<Member> res3 = em.createQuery("select m from Member m left join m.team t on t.name = :teamName"
                + " and m.age=10", Member.class)
            .setParameter("teamName", "team1")
            .getResultList();

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
        //기본적으로 아래처럼 from에 여러개 지정해주면 알아서 cross join 해준다.
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

        em.createQuery("select m, t from Member m, Team t");

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
                .leftJoin(member.team, team).on(team.name.eq("팀1"))
                .fetch();

        em.clear();

        em.createQuery("select m, t from Member m left join m.team t on t.name='팀1'")
            .getResultList();

        for (Tuple r : res) {
            System.out.println(r);
        }
    }

    /**
     * 얘를 위의 thetaJoin과 비교해보라. outer join 하고싶으면 on 쓰라고 했던게 이거다.
     * 연관관계 없는 join시 cross join 말고 inner join, outer join 쓰는 경우
     */
    @Disabled
    @Test
    void noRelationThetaOuterJoin() throws Exception {
        //given
        em.persist(new Member("team1", 20, null));
        em.persist(new Member("team2", 20, null));

        em.flush();
        em.clear();

        List<Tuple> tuples = query.select(member, team)
            .from(member)
            .leftJoin(team).on(member.username.eq(team.name))
            .fetch();

        em.flush();
        em.clear();

        em.createQuery("select m, t from Member m left join Team t on m.username = t.name")
            .getResultList();

        System.out.println();
    }

    @PersistenceUnit
    private EntityManagerFactory emf;

    @Disabled
    @Test
    void noFetchJoin() throws Exception {
        Member foundMember = query.selectFrom(member)
            .where(member.username.eq("member1"))
            .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(foundMember.getTeam());
        assertThat(loaded).as("패치 조인 적용 여부").isFalse();
    }

    @Disabled
    @Test
    void fetchJoin() throws Exception {
        Member foundMember = query.selectFrom(member)
            .join(member.team, team).fetchJoin()
            .where(member.username.eq("member1"))
            .fetchOne();

        em.createQuery("select m from Member m join fetch m.team t where m.username='member1'")
            .getSingleResult();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(foundMember.getTeam());
        assertThat(loaded).as("패치조인 적용 여부").isTrue();
    }

    /**
     * JPA SubQuery는 select, where 에서만 허용한다. from은 허용하지 않는다. (JPA의 한계)
     * from절에서 subQuery 써야할 때 해결법
     * 1. join으로 변경 (높은 확률로 가능)
     * 2. 쿼리 분리
     * 3. nativeQuery 사용
     */

    //서브쿼리. 나이가 가장 많은 회원 조회
    @Disabled
    @Test
    void subQuery() throws Exception {
        QMember memberSub = new QMember("memberSub");
        Member maxAgeMember = query.selectFrom(QMember.member)
            .where(
                QMember.member.age.eq(
                    JPAExpressions.select(memberSub.age.max())
                        .from(memberSub)
                )
            ).fetchOne();

        em.clear();

        em.createQuery("select m from Member m where m.age = (select max(sm.age) from Member sm)", Member.class)
            .getSingleResult();

        assertThat(maxAgeMember).extracting("age").isEqualTo(50);
    }

    // 서브쿼리. 나이가 평균 이상인 회원들 조회
    @Disabled
    @Test
    void subQueryTwo() throws Exception {
        QMember subMember = new QMember("SubMember");

        List<Member> members = query.selectFrom(member)
            .where(
                member.age.goe(
                    JPAExpressions
                        .select(subMember.age.avg())
                        .from(subMember)
                )
            ).fetch();

        em.clear();

        em.createQuery("select m from Member m where m.age >= (select avg(sm.age) from Member sm)", Member.class)
            .getResultList();

        assertThat(members).size().isEqualTo(3);
        assertThat(members).extracting("age").containsExactly(30, 40, 50);
    }

    //서브쿼리. in 절
    @Disabled
    @Test
    void subQueryThree() throws Exception {
        QMember subMember = new QMember("subMember");

        List<Member> members = query.selectFrom(member)
            .where(
                member.age.in(
                    JPAExpressions
                        .select(subMember.age)
                        .from(subMember)
                        .where(subMember.age.goe(30))
                )
            ).fetch();

        em.clear();

        em.createQuery("select m from Member m where m.age in (select sm.age from Member sm)", Member.class)
            .getResultList();

        assertThat(members).size().isEqualTo(3);
        assertThat(members).extracting("age").containsExactly(30, 40, 50);
    }

    @Disabled
    @Test
    void subQueryFour() throws Exception {
        QMember subMember = new QMember("subMember");

        List<Tuple> fetch = query
            .select(
                member.username,
                JPAExpressions
                    .select(subMember.age.avg())
                    .from(subMember)
            ).from(member)
            .fetch();

        em.clear();

        // nativeQuery = select m.username as un, (select avg(sm.age) from member as sm) as age_average from member as m;

        em.createQuery("select m.username, (select avg(sm.age) from Member sm) from Member m")
            .getResultList();
    }

    /**
     * 보통 이런경우는 application에서 logic을 처리하는게 맞다. (presentation같은곳에서)
     */
    @Disabled
    @Test
    void basicCase() throws Exception {
        List<String> res = query
            .select(
                member.age
                    .when(10).then("열살")
                    .when(20).then("스무살")
                    .otherwise("기타")
            ).from(member)
            .fetch();

        //native query = select case when m.age=10 then '열살' else '기타' end as agecase from member as m;

        em.createQuery("select case when m.age=10 then '열살' when m.age=20 then '스무살' else '기타' end from Member m")
            .getResultList();

        res.forEach(System.out::println);
    }

    @Disabled
    @Test
    void caseComplex() throws Exception {
        List<String> res = query
            .select(new CaseBuilder()
                .when(member.age.between(0, 20)).then("0~20살")
                .when(member.age.between(21, 30)).then("20대")
                .otherwise("기타")
            ).from(member)
            .fetch();

        em.createQuery("select case"
            + " when m.age between 0 and 20 then '미성년자'"
            + " when m.age >= 20 then '성인'"
            + " else '기타' end"
            + " from Member m").getResultList();
    }

    //상수 더하기
    @Disabled
    @Test
    void constant() {
        Tuple a = query
            .select(member.username, Expressions.constant("A"))
            .from(member)
            .fetchFirst();
    }

    //종종 사용할 일이 있다.
    //StringValue 같은 경우에 ENUM에 쓸일이 많다.
    @Disabled
    @Test
    void stringConcat() throws Exception {
        List<String> fetch = query
            .select(member.username.concat("_").concat(member.age.stringValue()))
            .from(member)
            .fetch();

        // native query
        //    select
        //        ((member0_.username||'_')||cast(member0_.age as char)) as col_0_0_
        //    from
        //        member member0_

        List<String> resultList = em.createQuery("select concat(concat(m.username, '_'), m.age) from Member m",
                String.class)
            .getResultList();

        fetch.forEach(System.out::println);
        System.out.println("-----------");
        resultList.forEach(System.out::println);
    }

    /**
     * 프로젝션 하나면 타입지정, 둘 이상이면 DTO나 튜플인데 DTO를 많이 사용한다.
     */
    @Disabled
    @Test
    void projectionOne() throws Exception {
        List<String> names = query
            .select(member.username)
            .from(member)
            .fetch();
    }

    /**
     * 계층간 분리문제 때문에 repository에서만 쓰고 그 외에는 dto로 변환하여 사용하는 것을 추천
     */
    @Disabled
    @Test
    void projectionTuple() {
        List<Tuple> uples = query
            .select(member.username, member.age)
            .from(member)
            .fetch();

        for (Tuple tuple : uples) {
            tuple.get(member.username);
            tuple.get(member.age);
        }
    }

    /**
     * projection의 setter 방식.
     * 먼저 기본 생성자를 호출한 뒤에 setter로 값 세팅
     */
    @Disabled
    @Test
    void projectionDTOSetter() {
        List<MemberDto> memberDtos = query
            .select(Projections.bean(MemberDto.class,
                member.username,
                member.age))
            .from(member)
            .fetch();
    }

    /**
     * field 주입 방식 직접 field에 떄려박음
     */
    @Disabled
    @Test
    void projectionDTOField() {
        List<MemberDto> memberDtos = query
            .select(Projections.fields(MemberDto.class,
                member.username,
                member.age))
            .from(member)
            .fetch();
    }

    /**
     * 생성자 호출 방식. 일반적인 JPQL로는 이방식밖에 안된다.
     * 하지만 필드가 더 들어가도, 런타임에 에러를 잡아낸다.
     */
    @Disabled
    @Test
    void projectionDTOConstructor() {
        List<MemberDto> memberDtos = query
            .select(Projections.constructor(MemberDto.class,
                member.username,
                member.age))
            .from(member)
            .fetch();
    }

    /**
     * 필드 이름이 다를 때, subQuery를 사용할 때
     */
    @Disabled
    @Test
    void projectionDTONameDifferent() throws Exception {
        QMember subMember = new QMember("subMember");

        query
            .select(Projections.fields(UserDto.class,
                member.username.as("name"),
                ExpressionUtils.as(
                    JPAExpressions
                        .select(subMember.age.max())
                        .from(subMember)
                , "age")
            ))
            .from(member)
            .fetch();
    }

    /**
     * @QueryProjection 어노테이션을 붙여야하고
     * dto자체가 querydsl에 대한 의존성이 생긴다.
     * 하지만 컴파일타임에 에러를 찾아낼 수 있다는게 장점
     */
    @Disabled
    @Test
    void projectionDto() {
        List<MemberProjectionDto> dtos = query
            .select(new QMemberProjectionDto(member.username, member.age)).distinct()
            .from(member)
            .fetch();
    }

    /**
     * 동적 쿼리
     * 1. BooleanBuilder
     * 2. Where 다중 파라미터
     * parameter의 값이 null이냐 아니냐에 따라서 동적으로 query가 변해야한다.
     * null이면 조건이 빠져야한다.
     */
    @Test
    void dynamicQueryBooleanBuilder() {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> res = searchMember(usernameParam, ageParam);
        System.out.println();
    }

    private List<Member> searchMember(String usernameParam, Integer ageParam) {
        BooleanBuilder builder = new BooleanBuilder(); //여기에 초기값 가능
        if (usernameParam != null) {
            builder.and(member.username.eq(usernameParam));
        }
        if (ageParam != null) {
            builder.and(member.age.eq(ageParam));
        }

        return query
            .selectFrom(member)
            .where(builder)
            .fetch();
    }

}
