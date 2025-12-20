package com.muhammadali.employee_management.security.service;

import com.muhammadali.employee_management.entity.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UsersDetails implements UserDetails {

    private String username;
    private String password;
    private List<GrantedAuthority> authorities;

    public UsersDetails(Users user){
        this.username=user.getEmail();
        this.password=user.getPassword();
        this.authorities=List.of(new SimpleGrantedAuthority(user.getRole().getName()));
    }


    @Override
    public Collection<?extends GrantedAuthority> getAuthorities(){
        return authorities;
    }


    @Override
    public String getPassword(){
        return password;
    }

    @Override
    public String getUsername(){
        return username;
    }

    @Override
    public boolean isAccountNonExpired(){
        return true;
    }

    @Override
    public boolean isAccountNonLocked(){
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired(){
        return true;
    }


    @Override
    public boolean isEnabled(){
        return true;
    }


}
