package study.querydsl.entity;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

	void findOne() {
		Member member1 = em.createQuery("select m from Member m where m.username = :username", Member.class)
			.setParameter("username", "멤버1")
			.getSingleResult();

		Member member2 = query.select(member)
			.from(member)
			.where(member.username.eq("멤버1"))
			.fetchOne();
	}

	void search() {
		Member foundMember = query.selectFrom(member)
			.where(
				member.username.eq("member1"),
				member.age.eq(10)
			)
			.fetchOne();

		List<Member> resultList = em.createQuery("select m from Member m where m.username = 'member1' and m.age = 10",
				Member.class)
			.getResultList();
	}

	void fetchResult() {
		QueryResults<Member> results = query.selectFrom(member)
			.fetchResults();

		em.createQuery("select count(m) from Member m", Long.class).getSingleResult();
		em.createQuery("select m from Member m", Member.class).getResultList();
	}

	void fetchCount() {
		long count = query.selectFrom(member)
			.fetchCount();

		em.createQuery("select count(m) from Member m", Long.class).getSingleResult();
	}

	void sort() {
		List<Member> res = query.selectFrom(member)
			.where(member.age.eq(100))
			.orderBy(member.age.desc(), member.username.asc().nullsLast())
			.fetch();

		em.createQuery("select m from Member m where m.age = 100 order by m.age desc, m.username asc nulls last",
				Member.class)
			.getResultList();
	}

	void paging() {
		List<Member> page = query.selectFrom(member)
			.orderBy(member.username.desc())
			.offset(1)
			.limit(2)
			.fetch();

		em.createQuery("select m from Member m order by m.username desc")
			.setFirstResult(1)
			.setMaxResults(2)
			.getResultList();
	}

	void statistics() {
		List<Tuple> res = query.select(
				member.count(),
				member.age.sum(),
				member.age.avg(),
				member.age.max(),
				member.age.min()
			)
			.from(member)
			.fetch();

		em.createQuery(
				"select count(m), sum(m.age), avg(m.age), max(m.age), min(m.age) from Member m")
			.getSingleResult();
	}

	void group() throws Exception {
		List<Tuple> res = query.select(team.name, member.age.avg())
			.from(member)
			.join(member.team, team)
			.groupBy(team.name)
			.having(member.age.avg().goe(20))
			.fetch();

		em.createQuery("select t.name, avg(m.age) from Member m join m.team t"
				+ " group by t.name"
				+ " having avg(m.age) >= 20")
			.getResultList();
	}

	void join() throws Exception {
		List<Member> members = query.selectFrom(member)
			.join(member.team, team)
			.leftJoin(member.team, team)
			.where(team.name.eq("team1"))
			.fetch();

		List<Member> res2 = em.createQuery("select m from Member m left join m.team t where t.name = :teamName",
				Member.class)
			.setParameter("teamName", "team1")
			.getResultList();

		List<Member> res = em.createQuery("select m from Member m left join m.team t on t.name = :teamName",
				Member.class)
			.setParameter("teamName", "team1")
			.getResultList();

		List<Member> res3 = em.createQuery("select m from Member m left join m.team t on t.name = :teamName"
				+ " and m.age=10", Member.class)
			.setParameter("teamName", "team1")
			.getResultList();
	}

	void thetaJoin() throws Exception {
		List<Member> members = query.select(member)
			.from(member, team)
			.where(member.username.eq(team.name))
			.fetch();

		em.createQuery("select m, t from Member m, Team t");
	}

	void onJoin() throws Exception {
		List<Tuple> res = query.select(member, team)
			.from(member)
			.leftJoin(member.team, team).on(team.name.eq("팀1"))
			.fetch();

		em.createQuery("select m, t from Member m left join m.team t on t.name='팀1'")
			.getResultList();
	}

	void noRelationThetaOuterJoin() throws Exception {
		List<Tuple> tuples = query.select(member, team)
			.from(member)
			.leftJoin(team).on(member.username.eq(team.name))
			.fetch();

		em.createQuery("select m, t from Member m left join Team t on m.username = t.name")
			.getResultList();
	}

	void noFetchJoin() throws Exception {
		Member foundMember = query.selectFrom(member)
			.where(member.username.eq("member1"))
			.fetchOne();
	}

	void fetchJoin() throws Exception {
		Member foundMember = query.selectFrom(member)
			.join(member.team, team).fetchJoin()
			.where(member.username.eq("member1"))
			.fetchOne();

		em.createQuery("select m from Member m join fetch m.team t where m.username='member1'")
			.getSingleResult();
	}

	void subQuery() throws Exception {
		QMember memberSub = new QMember("memberSub");
		Member maxAgeMember = query.selectFrom(member)
			.where(
				member.age.eq(
					JPAExpressions.select(memberSub.age.max())
						.from(memberSub)
				)
			).fetchOne();

		em.createQuery("select m from Member m where m.age = (select max(sm.age) from Member sm)", Member.class)
			.getSingleResult();
	}

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

		em.createQuery("select m from Member m where m.age >= (select avg(sm.age) from Member sm)", Member.class)
			.getResultList();
	}

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

		em.createQuery("select m from Member m where m.age in (select sm.age from Member sm)", Member.class)
			.getResultList();
	}

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

		// nativeQuery = select m.username as un, (select avg(sm.age) from member as sm) as age_average from member as m;

		em.createQuery("select m.username, (select avg(sm.age) from Member sm) from Member m")
			.getResultList();
	}

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
	}

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

	void constant() {
		Tuple a = query
			.select(member.username, Expressions.constant("A"))
			.from(member)
			.fetchFirst();
	}

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
	}

	void projectionOne() throws Exception {
		List<String> names = query
			.select(member.username)
			.from(member)
			.fetch();
	}

	void projectionTuple() {
		List<Tuple> uples = query
			.select(member.username, member.age)
			.from(member)
			.fetch();
	}

	void projectionDTOSetter() {
		List<MemberDto> memberDtos = query
			.select(Projections.bean(MemberDto.class,
				member.username,
				member.age))
			.from(member)
			.fetch();
	}

	void projectionDTOField() {
		List<MemberDto> memberDtos = query
			.select(Projections.fields(MemberDto.class,
				member.username,
				member.age))
			.from(member)
			.fetch();
	}

	void projectionDTOConstructor() {
		List<MemberDto> memberDtos = query
			.select(Projections.constructor(MemberDto.class,
				member.username,
				member.age))
			.from(member)
			.fetch();
	}

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

	void projectionDto() {
		List<MemberProjectionDto> dtos = query
			.select(new QMemberProjectionDto(member.username, member.age)).distinct()
			.from(member)
			.fetch();
	}

	void batch() throws Exception {
		List<Member> fetch = query.selectFrom(member)
			.fetch();

		long count = query
			.update(member)
			.set(member.username, "비회원")
			.where(member.age.lt(20))
			.execute();

		em.createQuery("update Member m set m.username='비회원' where m.age < 20").executeUpdate();

		List<Member> members = query.selectFrom(member)
			.fetch();
	}

	void batchAdd() throws Exception {
		long execute = query
			.update(member)
			.set(member.age, member.age.add(-1))
			.execute();
	}

	void batchMutliply() throws Exception {
		long execute = query
			.update(member)
			.set(member.age, member.age.multiply(10))
			.execute();
	}

	void batchDelete() throws Exception {
		long execute = query
			.delete(member)
			.where(member.age.gt(20))
			.execute();

		em.createQuery("delete from Member m where m.age <= 20").executeUpdate();
	}

	void sqlFunction1() throws Exception {
		List<String> names = query
			.select(
				Expressions.stringTemplate("function('replace', {0}, {1}, {2})", member.username, "member", "M")
			).from(member)
			.fetch();

		//Native Query = select replace(m.username, 'member', 'M') as rep from member as m

		List<String> resultList = em.createQuery("select function('replace', m.username, 'member', 'M') from Member m",
				String.class)
			.getResultList();
	}

	void sqlFunction2() throws Exception {
		List<String> names = query
			.select(member.username)
			.from(member)
//            .where(member.username.eq(
//                Expressions.stringTemplate("function('lower', {0})", member.username))
			.where(member.username.eq(member.username.lower())
			).fetch();
	}

}
