package study.querydsl.repository.member;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.paging.QuerySort;

public interface MemberRepositoryCustom {

    List<MemberTeamDto> search(MemberSearchCondition condition);

    /**
     * paging 자체는 지원하는데, sort는 Spring data의 것을 사용하면 join시 지원이 잘 안될 뿐 아니라 복잡해지면 차라리 parameter로 받아서 처리하는게 좋다.
     */
    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);

    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);

    Page<Member> searchPageQueryDSL(MemberSearchCondition condition, Pageable pageable, List<QuerySort> sorts);

    Page<MemberTeamDto> searchPageQueryDSLCount(MemberSearchCondition condition, Pageable pageable);

    Member findMemberByIdFetchTeamAndTeamDetail(Long id);
}
