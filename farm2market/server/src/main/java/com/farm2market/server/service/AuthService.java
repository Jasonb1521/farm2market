package com.farm2market.server.service;

import com.farm2market.core.dto.LoginResponse;
import com.farm2market.core.enums.Role;
import com.farm2market.core.exception.AuthenticationException;
import com.farm2market.core.exception.InvalidInputException;
import com.farm2market.core.model.*;
import com.farm2market.core.service.ValidationService;
import com.farm2market.server.SessionManager;
import com.farm2market.server.db.JdbcUserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class AuthService {

    private final JdbcUserRepository userRepository;
    private final SessionManager sessionManager;
    private final ValidationService validationService;

    public AuthService() {
        this.userRepository = new JdbcUserRepository();
        this.sessionManager = SessionManager.getInstance();
        this.validationService = new ValidationService();
    }

    public LoginResponse login(String email, String password) throws AuthenticationException {
        String passwordHash = userRepository.getPasswordHashByEmail(email.toLowerCase());

        if (passwordHash == null) {
            throw new AuthenticationException(AuthenticationException.Reason.INVALID_CREDENTIALS);
        }

        if (!BCrypt.checkpw(password, passwordHash)) {
            throw new AuthenticationException(AuthenticationException.Reason.INVALID_CREDENTIALS);
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new AuthenticationException(AuthenticationException.Reason.INVALID_CREDENTIALS);
        }

        User user = userOpt.get();

        if (!user.canLogin()) {
            if (user.getStatus().name().equals("SUSPENDED")) {
                throw new AuthenticationException(AuthenticationException.Reason.ACCOUNT_SUSPENDED);
            } else {
                throw new AuthenticationException(AuthenticationException.Reason.ACCOUNT_DELETED);
            }
        }

        String token = sessionManager.createSession(user.getUserId());

        LoginResponse response = new LoginResponse(
                user.getUserId(),
                token,
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
        response.setProfileImageUrl(user.getProfileImageUrl());
        response.setExpiresAt(sessionManager.getExpiresAt(token));

        return response;
    }

    public LoginResponse register(String name, String email, String mobile, String password, Role role)
            throws AuthenticationException, InvalidInputException {

        // Validate inputs
        validationService.validateName(name);
        validationService.validateEmail(email);
        validationService.validatePassword(password);
        if (mobile != null && !mobile.isEmpty()) {
            validationService.validateMobile(mobile);
        }

        // Check for existing email
        if (userRepository.existsByEmail(email)) {
            throw new AuthenticationException(AuthenticationException.Reason.EMAIL_ALREADY_EXISTS);
        }

        // Check for existing mobile
        if (mobile != null && !mobile.isEmpty() && userRepository.existsByMobile(mobile)) {
            throw new AuthenticationException(AuthenticationException.Reason.MOBILE_ALREADY_EXISTS);
        }

        // Prevent admin self-registration
        if (role == Role.ADMIN) {
            role = Role.CUSTOMER;
        }

        // Create user
        User user;
        if (role == Role.FARMER) {
            user = new Farmer();
        } else {
            user = new Customer();
        }

        user.setName(name);
        user.setEmail(email.toLowerCase());
        user.setMobile(mobile);
        user.setRole(role);

        // Hash password and save
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        userRepository.saveWithPassword(user, passwordHash);

        // Create session
        String token = sessionManager.createSession(user.getUserId());

        LoginResponse response = new LoginResponse(
                user.getUserId(),
                token,
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
        response.setExpiresAt(sessionManager.getExpiresAt(token));

        return response;
    }

    public void logout(String token) {
        sessionManager.invalidateSession(token);
    }

    public void changePassword(String userId, String oldPassword, String newPassword)
            throws AuthenticationException, InvalidInputException {

        String currentHash = userRepository.getPasswordHash(userId);
        if (currentHash == null || !BCrypt.checkpw(oldPassword, currentHash)) {
            throw new AuthenticationException(AuthenticationException.Reason.INVALID_CREDENTIALS);
        }

        validationService.validatePassword(newPassword);

        String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        userRepository.updatePassword(userId, newHash);

        // Invalidate all sessions for security
        sessionManager.invalidateAllSessions(userId);
    }

    public Optional<User> validateSession(String token) {
        return sessionManager.validateToken(token);
    }
}
