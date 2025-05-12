package com.capstone.meerkatai;

import com.capstone.meerkatai.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
class MeerkataiApplicationTests {

	@Test
	void contextLoads() {
		// 테스트 컨텍스트가 로드되는지 확인
	}
}
