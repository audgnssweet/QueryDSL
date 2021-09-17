package study.querydsl.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.paging.QuerySort;
import study.querydsl.repository.MemberJpaQueryRepository;
import study.querydsl.service.MemberService;

@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberJpaQueryRepository memberJpaQueryRepository;
    private final MemberService memberService;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMember(MemberSearchCondition condition) {
        return memberJpaQueryRepository.search(condition);
    }

    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchMemberV2(
        MemberSearchCondition condition,
        @RequestParam(name = "sorts", required = false) List<QuerySort> sorts,
        @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
        @RequestParam(name = "size", required = false, defaultValue = "3") int size
    ) {
        return memberService.getMembersWithTeam(condition, PageRequest.of(offset, size), sorts);
    }

}
