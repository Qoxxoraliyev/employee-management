package com.muhammadali.employee_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muhammadali.employee_management.dto.auth.LoginRequestDTO;
import com.muhammadali.employee_management.dto.UsersRequestDTO;
import com.muhammadali.employee_management.dto.UsersResponseDTO;
import com.muhammadali.employee_management.enums.Status;
import com.muhammadali.employee_management.exceptions.*;
import com.muhammadali.employee_management.security.jwt.JwtService;
import com.muhammadali.employee_management.service.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsersController.class)
public class UsersControllerTests {

    @Autowired public MockMvc mockMvc;
    @Autowired public ObjectMapper objectMapper;

    @MockBean public UsersService usersService;
    @MockBean public AuthenticationManager authenticationManager;
    @MockBean public JwtService jwtService;

    public UsersRequestDTO requestDTO;
    public UsersResponseDTO responseDTO;
    public LoginRequestDTO loginDTO;

    @BeforeEach
    public void setUp() {
        requestDTO = new UsersRequestDTO(
                "john",
                "john@example.com",
                "secret",
                1L,
                1L,
                Status.ACTIVE
        );

        responseDTO = new UsersResponseDTO(
                1L,
                "john",
                "john@example.com",
                "ADMIN",
                "ACTIVE"
        );

        loginDTO = new LoginRequestDTO("john", "secret");
    }


    @Test
    public void create_success() throws Exception {
        when(usersService.save(any(UsersRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));

        verify(usersService).save(requestDTO);
    }

    @Test
    public void create_duplicateUsername() throws Exception {
        when(usersService.save(any(UsersRequestDTO.class)))
                .thenThrow(new ResourceAlreadyExistsException("User", "username", "john"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict());

        verify(usersService).save(requestDTO);
    }

    @Test
    public void getByUsername_success() throws Exception {
        when(usersService.findByUsername("john")).thenReturn(responseDTO);

        mockMvc.perform(get("/api/users/by-username").param("username", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));

        verify(usersService).findByUsername("john");
    }

    @Test
    public void getByUsername_notFound() throws Exception {
        when(usersService.findByUsername("john"))
                .thenThrow(new ResourceNotFoundException("User", "username", "john"));

        mockMvc.perform(get("/api/users/by-username").param("username", "john"))
                .andExpect(status().isNotFound());

        verify(usersService).findByUsername("john");
    }

    @Test
    public void getByUsernameLike_success() throws Exception {
        when(usersService.findByUsernameLike("jo")).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/users/by-username-like").param("username", "jo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("john"));

        verify(usersService).findByUsernameLike("jo");
    }

    @Test
    public void update_success() throws Exception {
        when(usersService.update(eq(1L), any(UsersRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));

        verify(usersService).update(1L, requestDTO);
    }

    @Test
    public void update_duplicateUsername() throws Exception {
        when(usersService.update(eq(1L), any(UsersRequestDTO.class)))
                .thenThrow(new ResourceAlreadyExistsException("User", "username", "john"));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict());

        verify(usersService).update(1L, requestDTO);
    }

    @Test
    public void findAll_success() throws Exception {
        when(usersService.findAll()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("john"));

        verify(usersService).findAll();
    }

    @Test
    public void generateToken_success() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken("john", "secret");
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken("john")).thenReturn("jwt-token");

        mockMvc.perform(post("/api/users/generateToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("jwt-token"));

        verify(authenticationManager).authenticate(any());
        verify(jwtService).generateToken("john");
    }

    @Test
    public void generateToken_invalid() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new UsernameNotFoundException("Invalid user request"));

        mockMvc.perform(post("/api/users/generateToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());

        verify(authenticationManager).authenticate(any());
        verifyNoInteractions(jwtService);
    }

    @Test
    public void getByRole_success() throws Exception {
        when(usersService.findByRole("ADMIN")).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/users/by-role").param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roleName").value("ADMIN"));

        verify(usersService).findByRole("ADMIN");
    }

    @Test
    public void getUsersCount_success() throws Exception {
        when(usersService.getTotalUsersCount()).thenReturn(5L);

        mockMvc.perform(get("/api/users/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(usersService).getTotalUsersCount();
    }

    @Test
    public void delete_success() throws Exception {
        doNothing().when(usersService).delete(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("successful"));

        verify(usersService).delete(1L);
    }

    @Test
    public void delete_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("User", "id", 1L))
                .when(usersService).delete(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNotFound());

        verify(usersService).delete(1L);
    }
}
