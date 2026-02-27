package com.journeo.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.journeo.BackendApplication;

@SpringBootTest(classes = BackendApplication.class)
@ActiveProfiles("test")
public class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
