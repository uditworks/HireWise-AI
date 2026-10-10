package com.hirewise.hirewise_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "hirewise.jwt.secret=TestSecretKeyForHireWiseAppTestingMustBeAtLeast32BytesLong!"
})
class HirewiseBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
