package study.querydsl.paging;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

import com.querydsl.core.types.OrderSpecifier;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuerySort {
    MEMBER_ID_ASC(member.id.asc()),
    MEMBER_ID_DESC(member.id.desc()),
    MEMBER_USERNAME_ASC(member.username.asc()),
    MEMBER_USERNAME_DESC(member.username.desc()),
    TEAM_ID_ASC(team.id.asc()),
    TEAM_ID_DESC(team.id.desc()),
    TEAM_NAME_ASC(team.name.asc()),
    TEAM_NAME_DESC(team.name.desc());

    private final OrderSpecifier specifier;
}
