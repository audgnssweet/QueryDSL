package study.querydsl.repository.many;

import static study.querydsl.entity.QMany.many;
import static study.querydsl.entity.QOne.one;
import static study.querydsl.entity.QOtherOne.otherOne;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.querydsl.entity.Many;

@RequiredArgsConstructor
@Repository
public class ManyRepositoryImpl implements ManyRepositoryCustom{

	private final JPAQueryFactory query;

	@Override
	public Many fetchOnesAndOtherOnes(Long manyId) {
		List<Many> fetch = query.selectFrom(many)
			.join(many.ones, one).fetchJoin()
//			.join(many.otherOnes, otherOne).fetchJoin()
			.where(many.id.eq(manyId))
			.groupBy(many)
			.fetch();

		return null;
	}
}
