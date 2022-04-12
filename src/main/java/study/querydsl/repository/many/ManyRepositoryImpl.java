package study.querydsl.repository.many;

import static study.querydsl.entity.QMany.many;
import static study.querydsl.entity.QOne.one;
import static study.querydsl.entity.QOtherOne.otherOne;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import study.querydsl.entity.Many;

@RequiredArgsConstructor
@Repository
public class ManyRepositoryImpl implements ManyRepositoryCustom {

	private final JPAQueryFactory query;

	/**
	 * 에러 발생 - 두 개의 collection 을 fetchjoin 하기 때문에
	 */
	@Override
	public Many fetchTwoCollectionsTogether(Long manyId) {
		return query.selectFrom(many)
			.join(many.ones, one).fetchJoin()
			.join(many.otherOnes, otherOne).fetchJoin()
			.where(many.id.eq(manyId))
			.fetchOne();
	}

	@Override
	public Page<Many> fetchCollectionAndPaging(Pageable pageable) {
		QueryResults<Many> result = query.selectFrom(many)
			.join(many.ones, one).fetchJoin()
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetchResults();

		return new PageImpl<>(result.getResults(), pageable, result.getTotal());
	}
}
