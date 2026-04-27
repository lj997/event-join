package com.eventjoin.config;

import com.eventjoin.entity.User;
import com.eventjoin.enums.UserRole;
import com.eventjoin.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void run(String... args) {
        String adminUsername = "admin";
        
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@eventjoin.com");
            admin.setRole(UserRole.ADMIN);
            
            userRepository.save(admin);
            System.out.println("==============================");
            System.out.println("内置管理员账号已创建:");
            System.out.println("用户名: admin");
            System.out.println("密码: admin123");
            System.out.println("==============================");
        }
    }
}
