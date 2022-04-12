package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.service.ManyService;

@RestController
@RequiredArgsConstructor
public class ManyController {

	private final ManyService manyService;

	@GetMapping("/test")
	public void test(@RequestParam("many-id") Long manyId) {
		manyService.doSomething(manyId);
	}

	@GetMapping("/test2")
	public void test() {
		manyService.doSomething2();
	}


}
