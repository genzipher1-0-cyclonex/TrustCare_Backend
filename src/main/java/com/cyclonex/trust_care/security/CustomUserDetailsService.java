package com.cyclonex.trust_care.security;

import com.cyclonex.trust_care.entity.RolePermission;
import com.cyclonex.trust_care.entity.User;
import com.cyclonex.trust_care.repository.RolePermissionRepository;
import com.cyclonex.trust_care.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        // Load authorities (role + permissions)
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add role as authority
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName()));
        
        // Add permissions as authorities
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRole_Id(user.getRole().getId());
        for (RolePermission rolePermission : rolePermissions) {
            authorities.add(new SimpleGrantedAuthority(rolePermission.getPermission().getPermissionName()));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                authorities
        );
    }
}
