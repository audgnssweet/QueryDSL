package study.querydsl.service;

import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import study.querydsl.entity.Many;
import study.querydsl.repository.many.ManyRepository;

@RequiredArgsConstructor
@Transactional
@Service
public class ManyService {

	private final ManyRepository manyRepository;

	public void doSomething(Long manyId) {
		Many many = manyRepository.fetchTwoCollectionsTogether(manyId);

		System.out.println();
	}

	public void doSomething2() {
		Page<Many> manies = manyRepository.fetchCollectionAndPaging(PageRequest.of(0, 10));

		System.out.println();
	}

}
