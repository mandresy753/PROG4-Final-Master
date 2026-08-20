package com.example.demo.endpoint.rest.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.entity.JUser;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AppUserPrincipal;
import com.example.demo.security.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final JwtService jwtService;

  @PostMapping("/login")
  public LoginResponse login(@RequestBody LoginRequest request) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.email(), request.password()));
    } catch (Exception e) {
      throw new UnauthorizedException("Invalid email or password");
    }

    JUser user =
        userRepository
            .findByEmail(request.email())
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

    var token = jwtService.generateToken(AppUserPrincipal.of(user));
    return new LoginResponse(
        token,
        user.getId().toString(),
        user.getFirstName(),
        user.getLastName(),
        user.getRole().name());
  }
}
