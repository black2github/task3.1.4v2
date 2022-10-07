package ru.kata.spring.boot_security.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SpringBootSecurityDemoApplicationTests {
	private static final Logger log = LoggerFactory.getLogger(SpringBootSecurityDemoApplicationTests.class);

	@Autowired
	MockMvc mvc;

	// @BeforeEach
	// void setup(WebApplicationContext wac) {
	// 	this.mvc = MockMvcBuilders.webAppContextSetup(wac).build();
	// }

	// @Test
	// void contextLoads() {
	// }

	//
	// Test Anonymous Users
	//
	@Test
	@WithAnonymousUser
	public void whenAnonymousAccessLogin_thenOk() throws Exception {
		mvc.perform(get("/login"))
				.andExpect(status().isOk());
	}

	@Test
	@WithAnonymousUser
	public void whenAnonymousAccessRoot_thenOk() throws Exception {
		mvc.perform(get("/"))
				.andExpect(status().isOk());
	}

	@Test
	@WithAnonymousUser
	public void whenAnonymousAccessRestrictedEndpoint_thenIsUnauthorized() throws Exception {
		mvc.perform(get("/user"))
				.andExpect(status().is3xxRedirection());
	}

	//
	// Test Admin Role
	//
	@Test
	@WithUserDetails(value = "user@a.b")
	public void whenUserAccessUserSecuredEndpoint_thenOk() throws Exception {
		mvc.perform(get("/user"))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(value = "user@a.b")
	public void whenUserAccessAdminSecuredEndpoint_thenIsForbidden() throws Exception {
		mvc.perform(get("/admin"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails(value = "admin@a.b")
	public void whenAdminAccessAdminSecuredEndpoint_thenIsForbidden() throws Exception {
		mvc.perform(get("/admin"))
				.andExpect(status().isOk());
	}
}
