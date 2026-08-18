package com.example.demo.endpoint.web;

import com.example.demo.enums.UserRole;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AppUserPrincipal;
import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
public class AuthViewController {

  private static final int COOKIE_MAX_AGE_SECONDS = 24 * 60 * 60;

  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final JwtService jwtService;

  @GetMapping("/login")
  public String loginForm() {
    return "login";
  }

  @PostMapping("/login")
  public String login(
      @RequestParam String email,
      @RequestParam String password,
      Model model,
      HttpServletResponse response) {
    try {
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    } catch (Exception e) {
      model.addAttribute("error", "Email ou mot de passe incorrect");
      return "login";
    }

    var user = userRepository.findByEmail(email).orElseThrow();

    if (user.getRole() != UserRole.ADMIN) {
      model.addAttribute("error", "Cette interface est réservée aux administrateurs");
      return "login";
    }

    var token = jwtService.generateToken(AppUserPrincipal.of(user));
    response.addCookie(buildCookie(token, COOKIE_MAX_AGE_SECONDS));

    return "redirect:/promotions";
  }

  @GetMapping("/logout")
  public String logout(HttpServletResponse response) {
    response.addCookie(buildCookie("", 0));
    return "redirect:/login";
  }

  private Cookie buildCookie(String value, int maxAgeSeconds) {
    var cookie = new Cookie(JwtAuthenticationFilter.JWT_COOKIE_NAME, value);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(maxAgeSeconds);
    return cookie;
  }
}
