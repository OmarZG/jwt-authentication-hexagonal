package org.zgo.auth.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.zgo.auth.application.port.out.UserPersistencePort;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserPersistencePort userPort;

    public UserDetailsServiceImpl(UserPersistencePort userPort) {
        this.userPort = userPort;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userPort.findByUsername(username)
                .map(u -> {
                    Set<GrantedAuthority> authorities = u.getRoles().stream()
                            .map(r -> new SimpleGrantedAuthority(r.name()))
                            .collect(Collectors.toSet());
                    return new org.springframework.security.core.userdetails.User(
                            u.getUsername(),
                            u.getPassword(),
                            true, true, true, true,
                            authorities
                    );
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}