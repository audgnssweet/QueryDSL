package study.querydsl.service;

import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import study.querydsl.entity.Many;
import study.querydsl.repository.many.ManyRepository;

@RequiredArgsConstructor
@Transactional
@Service
public class ManyService {

	private final ManyRepository manyRepository;

	public void doSomething(Long manyId) {
		Many many = manyRepository.fetchOnesAndOtherOnes(manyId);

		System.out.println();
	}

}
