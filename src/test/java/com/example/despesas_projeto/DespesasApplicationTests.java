package com.example.despesas_projeto;

import com.example.despesas_projeto.config.BaseIT;
import com.example.despesas_projeto.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestConfig.class)
class DespesasApplicationTests extends BaseIT {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
		assertThat(mockMvc).isNotNull();
	}
}



