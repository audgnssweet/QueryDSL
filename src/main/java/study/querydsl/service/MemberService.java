package study.querydsl.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.paging.QuerySort;
import study.querydsl.repository.MemberRepository;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public Page<MemberTeamDto> getMembersWithTeam(MemberSearchCondition condition, Pageable pageable, List<QuerySort> sorts) {
        return memberRepository.searchPageQueryDSL(condition, pageable, sorts)
            .map(MemberTeamDto::new);
    }

}
