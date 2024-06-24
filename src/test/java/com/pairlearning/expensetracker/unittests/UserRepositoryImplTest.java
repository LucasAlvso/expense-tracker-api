package com.pairlearning.expensetracker.unittests;

import com.pairlearning.expensetracker.domain.User;
import com.pairlearning.expensetracker.exceptions.EtAuthException;
import com.pairlearning.expensetracker.repositories.UserRepositoryImpl;
import org.junit.Before;
import org.junit.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class UserRepositoryImplTest {

    private JdbcTemplate jdbcTemplate;

    private UserRepositoryImpl userRepository;

    @Before
    public void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        userRepository = new UserRepositoryImpl();
        userRepository.setJdbcTemplate(jdbcTemplate);
    }

    @Test
    public void createShouldThrowEtAuthExceptionWhenAnyExceptionIsThrown() {
        String firstName = "Test";
        String lastName = "User";
        String email = "test@example.com";
        String password = "password";

        doThrow(new RuntimeException("Database error")).when(jdbcTemplate).update(any(), any(KeyHolder.class));

        assertThrows(EtAuthException.class, () -> userRepository.create(firstName, lastName, email, password));
    }

    @Test
    public void findByEmailAndPasswordShouldReturnUserWhenCredentialsAreValid() throws EtAuthException {
        String email = "test@example.com";
        String password = "password";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User(1, "Test", "User", email, hashedPassword);

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(RowMapper.class))).thenReturn(user);

        User result = userRepository.findByEmailAndPassword(email, password);

        assertEquals(user, result);
    }

    @Test
    public void findByEmailAndPasswordShouldThrowEtAuthExceptionWhenCredentialsAreInvalid() {
        String email = "test@example.com";
        String password = "password";

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(RowMapper.class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThrows(EtAuthException.class, () -> userRepository.findByEmailAndPassword(email, password));
    }

    @Test
    public void getCountByEmailShouldReturnCount() {
        String email = "test@example.com";

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), eq(Integer.class))).thenReturn(1);

        Integer count = userRepository.getCountByEmail(email);

        assertEquals(1, (int) count);
    }

    @Test
    public void findByIdShouldReturnUserWhenUserIdIsValid() {
        Integer userId = 1;
        User user = new User(userId, "Test", "User", "test@example.com", "hashedPassword");

        when(jdbcTemplate.queryForObject(anyString(), any(Object[].class), any(RowMapper.class))).thenReturn(user);

        User result = userRepository.findById(userId);

        assertEquals(user, result);
    }
}
