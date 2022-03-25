package study.querydsl.entity;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "many")
public class Many {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "many_id")
	private Long id;

	@OneToMany(mappedBy = "many")
	private List<One> ones = new ArrayList<>();

	@OneToMany(mappedBy = "many")
	private List<OtherOne> otherOnes = new ArrayList<>();
}
