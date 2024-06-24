package com.pairlearning.expensetracker.unittests;

import com.pairlearning.expensetracker.domain.User;
import com.pairlearning.expensetracker.exceptions.EtAuthException;
import com.pairlearning.expensetracker.repositories.UserRepository;
import com.pairlearning.expensetracker.services.UserServiceImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {

    private UserRepository userRepository;

    private UserServiceImpl userService;

    @Before
    public void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl();
        userService.setUserRepository(userRepository);
    }

    @Test
    public void validateUserShouldReturnUserWhenCredentialsAreValid() throws EtAuthException {
        String email = "test@example.com";
        String password = "password";
        User user = new User(1, "Test", "User", email, "hashedPassword");

        when(userRepository.findByEmailAndPassword(email, password)).thenReturn(user);

        User result = userService.validateUser(email, password);

        assertEquals(user, result);
    }

    @Test
    public void validateUserShouldThrowEtAuthExceptionWhenCredentialsAreInvalid() {
        String email = "test@example.com";
        String password = "password";

        when(userRepository.findByEmailAndPassword(email, password)).thenThrow(new EtAuthException("Invalid email/password"));

        assertThrows(EtAuthException.class, () -> userService.validateUser(email, password));
    }

    @Test
    public void registerUserShouldReturnUserWhenDetailsAreValid() throws EtAuthException {
        String firstName = "Test";
        String lastName = "User";
        String email = "test@example.com";
        String password = "password";
        User user = new User(1, firstName, lastName, email, "hashedPassword");

        when(userRepository.getCountByEmail(email)).thenReturn(0);
        when(userRepository.create(firstName, lastName, email, password)).thenReturn(1);
        when(userRepository.findById(1)).thenReturn(user);

        User result = userService.registerUser(firstName, lastName, email, password);

        assertEquals(user, result);
    }

    @Test
    public void registerUserShouldThrowEtAuthExceptionWhenEmailIsInvalid() {
        String firstName = "Test";
        String lastName = "User";
        String email = "invalidEmail";
        String password = "password";

        assertThrows(EtAuthException.class, () -> userService.registerUser(firstName, lastName, email, password));
    }

    @Test
    public void registerUserShouldThrowEtAuthExceptionWhenEmailIsAlreadyInUse() {
        String firstName = "Test";
        String lastName = "User";
        String email = "test@example.com";
        String password = "password";

        when(userRepository.getCountByEmail(email)).thenReturn(1);

        assertThrows(EtAuthException.class, () -> userService.registerUser(firstName, lastName, email, password));
    }
}
