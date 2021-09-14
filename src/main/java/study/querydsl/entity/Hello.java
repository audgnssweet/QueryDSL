package study.querydsl.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Hello {

    @Id
    @GeneratedValue
    @Column(name = "hello_id")
    private Long id;

}
