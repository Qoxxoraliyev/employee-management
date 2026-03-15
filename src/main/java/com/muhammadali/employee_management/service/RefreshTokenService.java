package com.muhammadali.employee_management.service;

import com.muhammadali.employee_management.entity.RefreshToken;
import com.muhammadali.employee_management.entity.Users;
import com.muhammadali.employee_management.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken save(Users user, String token){

        RefreshToken refreshToken=new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpireDate(new Date(System.currentTimeMillis()+1000L*60*60*24*7));

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verify(String token){
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(()->new RuntimeException("Refresh token not found"));
    }

    public void delete(String token){
        refreshTokenRepository.deleteByToken(token);
    }


}
