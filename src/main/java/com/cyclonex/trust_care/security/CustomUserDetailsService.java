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
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository, RolePermissionRepository rolePermissionRepository) {
        this.userRepository = userRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        // Load permissions for the user's role
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add role as authority (ROLE_ prefix for role-based access)
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName()));
        
        // Load and add all permissions for this role
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRole_Id(user.getRole().getId());
        List<GrantedAuthority> permissionAuthorities = rolePermissions.stream()
                .map(rp -> new SimpleGrantedAuthority(rp.getPermission().getPermissionName()))
                .collect(Collectors.toList());
        
        authorities.addAll(permissionAuthorities);

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                authorities
        );
    }
}
