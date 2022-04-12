package study.querydsl.repository.many;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsl.entity.Many;

public interface ManyRepositoryCustom {
	Many fetchTwoCollectionsTogether(Long manyId);
	Page<Many> fetchCollectionAndPaging(Pageable pageable);
}
