package study.querydsl.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "one")
public class One {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "one_id")
	private Long id;

	@JoinColumn(name = "many_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private Many many;

}
