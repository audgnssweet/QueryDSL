package study.querydsl.repository;

import static java.util.stream.Collectors.toList;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.paging.QuerySort;

/**
 * 네이밍규칙은 jparepository+Impl 같은 MemberRepository여도, MemberRepository, MemberQueryRepository면 Impl도 두개가 할 수 있겠다. Spring
 * bean으로 등록 안해도 된다.
 */
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final EntityManager em;
    private final JPAQueryFactory query;

    private final Querydsl querydsl;

    public MemberRepositoryImpl(EntityManager em, JPAQueryFactory query) {
        this.em = em;
        this.query = query;
        this.querydsl = new Querydsl(em, new PathBuilderFactory().create(Member.class));
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return query
            .select(new QMemberTeamDto(
                member.id.as("memberId"),
                member.username,
                member.age,
                team.id.as("teamId"),
                team.name.as("teamName")
            ))
            .from(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe())
            )
            .fetch();
    }

    private BooleanExpression usernameEq(String username) {
        return StringUtils.hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return StringUtils.hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }


    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        QueryResults<MemberTeamDto> results = query
            .select(new QMemberTeamDto(
                member.id.as("memberId"),
                member.username,
                member.age,
                team.id.as("teamId"),
                team.name.as("teamName")
            ))
            .from(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetchResults();// JPA에서 자동으로 count용 쿼리까지 2번날림

        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> content = getContents(condition, pageable);

        //count쿼리를 원래쿼리와 다르게 쉽게 만들 수 있는 경우
        //count쿼리를 먼저 날려서 0이면 실제쿼리를 안날리게 한다던가.
        //이건 데이터가 매우많을때 얘기임
        long total = getTotalCount(condition);

        return new PageImpl<>(content, pageable, total);
        //아래처럼 size보다 전체 데이터가 적거나, 마지막 페이지일때 count쿼리를 생략하도록 할 수 있다.
//        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchCount());
    }

    private List<MemberTeamDto> getContents(MemberSearchCondition condition, Pageable pageable) {
        return query
            .select(new QMemberTeamDto(
                member.id.as("memberId"),
                member.username,
                member.age,
                team.id.as("teamId"),
                team.name.as("teamName")
            ))
            .from(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    private long getTotalCount(MemberSearchCondition condition) {
        return query
            .select(member)
            .from(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe())
            )
            .fetchCount();
    }

    /**
     * spring data의 paging and sorting 과 함께 사용하는 방법. 이게 아니면? 그냥 parameter로 받아서 orderby로 사용해야지
     */
    @Override
    public Page<Member> searchPageQueryDSL(MemberSearchCondition condition, Pageable pageable, List<QuerySort> sorts) {

        JPAQuery<Member> query = this.query
            .select(member)
            .from(member)
            .join(member.team, team).fetchJoin()
            .where(
                usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe())
            )
            .orderBy(getSortCondition(sorts));

        List<Member> content = querydsl.applyPagination(pageable, query).fetch();
        return PageableExecutionUtils.getPage(content, pageable, query::fetchCount);
    }

    public OrderSpecifier[] getSortCondition(List<QuerySort> sorts) {
        if (sorts != null) {
            return sorts.stream().map(QuerySort::getSpecifier).toArray(OrderSpecifier[]::new);
        }
        return new OrderSpecifier[]{member.id.desc()};
    }

    @Override
    public Page<MemberTeamDto> searchPageQueryDSLCount(MemberSearchCondition condition, Pageable pageable) {
        JPAQuery<MemberTeamDto> query = this.query
            .select(new QMemberTeamDto(
                member.id.as("memberId"),
                member.username,
                member.age,
                team.id.as("teamId"),
                team.name.as("teamName")
            ))
            .from(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe())
            );

        JPAQuery<Member> countQuery = query
            .select(member)
            .from(member)
            .leftJoin(member.team, team)
            .where(
                usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe())
            );

        List<MemberTeamDto> content = querydsl.applyPagination(pageable, query).fetch();
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }
}
