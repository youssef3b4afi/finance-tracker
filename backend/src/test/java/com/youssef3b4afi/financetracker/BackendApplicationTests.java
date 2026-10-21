package com.youssef3b4afi.financetracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfig.class)
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
