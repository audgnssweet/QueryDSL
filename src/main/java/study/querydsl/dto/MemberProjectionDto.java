package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MemberProjectionDto {

    private String username;
    private int age;

    @QueryProjection
    public MemberProjectionDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
