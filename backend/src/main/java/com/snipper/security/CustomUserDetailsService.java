package com.snipper.security;

import com.snipper.model.User;
import com.snipper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Try to find user by username first
        User user = userRepository.findByUsernameAndIsActiveTrue(usernameOrEmail)
                .orElse(null);
        
        // If not found by username, try to find by email
        if (user == null) {
            user = userRepository.findByEmailAndIsActiveTrue(usernameOrEmail)
                    .orElse(null);
        }
        
        // If still not found, throw exception
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail);
        }

        return CustomUserPrincipal.create(user);
    }

    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return CustomUserPrincipal.create(user);
    }
}