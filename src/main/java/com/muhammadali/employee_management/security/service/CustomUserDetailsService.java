package com.muhammadali.employee_management.security.service;

import com.muhammadali.employee_management.entity.Users;
import com.muhammadali.employee_management.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Optional<Users> userInfo = usersRepository.findByEmail(username);

        if (userInfo.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }

        Users user = userInfo.get();

        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(user.getRole().getName());

        Collection<? extends GrantedAuthority> authorities =
                Collections.singletonList(authority);

        return new User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}
