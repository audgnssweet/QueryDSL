package study.querydsl.repository.many;

import org.springframework.data.jpa.repository.JpaRepository;
import study.querydsl.entity.Many;

public interface ManyRepository extends JpaRepository<Many, Long>, ManyRepositoryCustom {

}
