package com.ibn.config;

import com.ibn.core.entity.User;
import com.ibn.dao.UserDao;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class DefaultUserSeeder implements CommandLineRunner {

    private final UserDao userDao;

    public DefaultUserSeeder(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void run(String... args) {
        seedOrUpdateDefaultUser("banker001", "Banker@123", "BANKER", "banker001@ibn.local");
        seedOrUpdateDefaultUser("admin", "Admin@123", "ADMIN", "admin@ibn.local");
    }

    private void seedOrUpdateDefaultUser(String username, String password, String userType, String email) {
        LocalDateTime now = LocalDateTime.now();
        String hashedPassword = hashPassword(password);

        Optional<User> existingUser = userDao.findByUsernameIgnoreCase(username);
        User user = existingUser.orElseGet(User::new);

        if (user.getUserId() == null) {
            user.setUserId(UUID.randomUUID());
            user.setCreatedAt(now);
        }

        if (user.getIdpSubject() == null || user.getIdpSubject().trim().isEmpty()) {
            user.setIdpSubject("LOCAL-" + username.toUpperCase());
        }

        user.setUsername(username);
        user.setPwd(hashedPassword);
        user.setEmail(email);
        user.setPhone("9000000000");
        user.setUserType(userType);
        user.setStatus("ACTIVE");
        user.setUpdatedAt(now);

        userDao.save(user);
    }

    private String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            return "";
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            return rawPassword;
        }
    }
}