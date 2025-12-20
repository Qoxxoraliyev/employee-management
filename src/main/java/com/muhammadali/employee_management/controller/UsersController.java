package com.muhammadali.employee_management.controller;
import com.muhammadali.employee_management.dto.UsersRequestDTO;
import com.muhammadali.employee_management.dto.UsersResponseDTO;
import com.muhammadali.employee_management.dto.auth.LoginRequestDTO;
import com.muhammadali.employee_management.security.jwt.JwtService;
import com.muhammadali.employee_management.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {


    private final UsersService usersService;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;


    @PostMapping
    public ResponseEntity<UsersResponseDTO> create(@RequestBody UsersRequestDTO dto){
        return ResponseEntity.ok(usersService.save(dto));
    }


    @GetMapping("/by-username")
    public ResponseEntity<UsersResponseDTO> getByUsername(@RequestParam String username){
        return ResponseEntity.ok(usersService.findByUsername(username));
    }


    @GetMapping("/by-username-like")
    public ResponseEntity<List<UsersResponseDTO>> getByUsernameLike(@RequestParam String username ){
        return ResponseEntity.ok(usersService.findByUsernameLike(username));
    }


    @PutMapping("/{id}")
    public ResponseEntity<UsersResponseDTO> update(@PathVariable Long id,
                                                   @RequestBody UsersRequestDTO dto){
        return ResponseEntity.ok(usersService.update(id,dto));
    }


    @GetMapping
    public ResponseEntity<List<UsersResponseDTO>> findAll(){
        return ResponseEntity.ok(usersService.findAll());
    }


    @PostMapping("/generateToken")
    public String authenticateAndGetToken(@RequestBody LoginRequestDTO
                                          loginRequestDTO){
        Authentication authentication=authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.username(),
                        loginRequestDTO.password())
        );
        if (authentication.isAuthenticated()){
            return jwtService.generateToken(loginRequestDTO.username());
        }
        else {
            throw  new UsernameNotFoundException("Invalid user request");
        }
    }


    @GetMapping("/by-role")
    public ResponseEntity<List<UsersResponseDTO>> getByRole(@RequestParam String role){
        return ResponseEntity.ok(usersService.findByRole(role));
    }


    @GetMapping("/count")
    public ResponseEntity<Long> getUsersCount(){
        return ResponseEntity.ok(usersService.getTotalUsersCount());
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){
        usersService.delete(id);
        return ResponseEntity.ok("successful");
    }


}
