package com.pairlearning.expensetracker.unittests;

import com.pairlearning.expensetracker.Constants;
import com.pairlearning.expensetracker.filters.AuthFilter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    private static final String API_SECRET_KEY = "your_secret_key";

    @Test
    public void testValidToken() throws Exception {
        // Create a valid JWT token with userId
        String token = createValidToken(123); // Assume 123 is a valid userId
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        // Mock JWT verification
        Claims claims = Jwts.parser()
                .setSigningKey(Constants.API_SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        // Mock user id claim
        claims.put("userId", 123);

        // Invoke filter
        AuthFilter filter = new AuthFilter();
        filter.doFilter(request, response, chain);

        // Verify that userId attribute is set and chain.doFilter() is called, which means that no error is sent
        verify(request).setAttribute("userId", 123);
        verify(chain).doFilter(request, response);
    }

    @Test
    public void testExpiredToken() throws Exception {
        // Create an expired JWT token
        String expiredToken = createExpiredToken(); // Assume 123 is a valid userId
        when(request.getHeader("Authorization")).thenReturn("Bearer " + expiredToken);

        // Invoke filter
        AuthFilter filter = new AuthFilter();
        filter.doFilter(request, response, chain);

        // Verify that HTTP 403 Forbidden error is sent
        verify(response).sendError(HttpStatus.FORBIDDEN.value(), "invalid/expired token");

        // Verify that chain.doFilter() is NOT called
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    public void failWhenTokenIsNotBearer() throws ServletException, IOException
    {
        // Create a valid JWT token with userId
        String token = createValidToken(123); // Assume 123 is a valid userId
        when(request.getHeader("Authorization")).thenReturn( "Basic " +token);

        // Invoke filter
        AuthFilter filter = new AuthFilter();
        filter.doFilter(request, response, chain);

        // Verify that HTTP 403 Forbidden error is sent
        verify(response).sendError(HttpStatus.FORBIDDEN.value(), "Authorization token must be Bearer [token]");

        // Verify that chain.doFilter() is NOT called
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    public void failWhenTokenIsNotProvided() throws ServletException, IOException
    {
        // Create a valid JWT token with userId
        String token = createValidToken(123); // Assume 123 is a valid userId
        when(request.getHeader("Authorization")).thenReturn(null);

        // Invoke filter
        AuthFilter filter = new AuthFilter();
        filter.doFilter(request, response, chain);

        // Verify that HTTP 403 Forbidden error is sent
        verify(response).sendError(HttpStatus.FORBIDDEN.value(), "Authorization token must be provided");

        // Verify that chain.doFilter() is NOT called
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    public void idMaxIntegerShouldSucceed() throws ServletException, IOException
    {
        String token = createValidToken(Integer.MAX_VALUE); // Assume 123 is a valid userId
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        // Mock JWT verification
        Claims claims = Jwts.parser()
                .setSigningKey(Constants.API_SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        // Mock user id claim
        claims.put("userId", 123);

        // Invoke filter
        AuthFilter filter = new AuthFilter();
        filter.doFilter(request, response, chain);

        // Verify that userId attribute is set and chain.doFilter() is called, which means that no error is sent
        verify(request).setAttribute("userId", Integer.MAX_VALUE);
        verify(chain).doFilter(request, response);
    }

    private String createValidToken(int userId) {
        return Jwts.builder()
                .setSubject("testuser")
                .claim("userId", userId)
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour validity
                .signWith(SignatureAlgorithm.HS256, Constants.API_SECRET_KEY)
                .compact();
    }

    private String createExpiredToken() {
        return Jwts.builder()
                .setSubject("testuser")
                .claim("userId", 123)
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // Expired one second ago
                .signWith(SignatureAlgorithm.HS256, Constants.API_SECRET_KEY)
                .compact();
    }
}
