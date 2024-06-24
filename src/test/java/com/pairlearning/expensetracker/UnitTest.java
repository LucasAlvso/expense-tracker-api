package com.pairlearning.expensetracker;

import com.pairlearning.expensetracker.unittests.AuthFilterTest;
import com.pairlearning.expensetracker.unittests.UserRepositoryImplTest;
import com.pairlearning.expensetracker.unittests.UserResourceTest;
import com.pairlearning.expensetracker.unittests.UserServiceImplTest;
import org.junit.jupiter.api.Test;
import org.junit.runners.Suite;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Suite.SuiteClasses({
		AuthFilterTest.class,
		UserRepositoryImplTest.class,
		UserServiceImplTest.class,
		UserResourceTest.class
})
class UnitTest
{

	@Test
	void contextLoads() {
	}

}
