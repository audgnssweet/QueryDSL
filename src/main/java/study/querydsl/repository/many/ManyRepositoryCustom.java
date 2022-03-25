package study.querydsl.repository.many;

import study.querydsl.entity.Many;

public interface ManyRepositoryCustom {
	Many fetchOnesAndOtherOnes(Long manyId);
}
