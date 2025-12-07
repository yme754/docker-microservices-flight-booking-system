package com.flightapp.controller;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.entity.ERole;
import com.flightapp.entity.Role;
import com.flightapp.entity.User;
import com.flightapp.payload.request.LoginRequest;
import com.flightapp.payload.request.SignupRequest;
import com.flightapp.payload.response.JwtResponse;
import com.flightapp.payload.response.MessageResponse;
import com.flightapp.repository.RoleRepository;
import com.flightapp.repository.UserRepository;
import com.flightapp.security.jwt.JwtUtils;
import com.flightapp.security.service.UserImplementation;
import com.flightapp.security.service.UserServiceImplementation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	private final ReactiveAuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/signin")
    public Mono<ResponseEntity<?>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return authenticationManager
            .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()))
            .map(authentication -> {
                String jwt = jwtUtils.generateJwtToken(authentication);
                UserImplementation userDetails = (UserImplementation) authentication.getPrincipal();             
                java.util.List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority()).collect(Collectors.toList());
                return ResponseEntity.ok(new JwtResponse(jwt,userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles));
            });
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity<?>> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        return userRepository.existsByUsername(signUpRequest.getUsername())
            .flatMap(exists -> {
                if (exists) return Mono.just(ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!")));
                return userRepository.existsByEmail(signUpRequest.getEmail())
                    .flatMap(emailExists -> {
                        if (emailExists) return Mono.just(ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!")));
                        User user = new User(signUpRequest.getUsername(),signUpRequest.getEmail(),encoder.encode(signUpRequest.getPassword()));
                        return roleRepository.findByName(ERole.ROLE_USER)
                            .flatMap(userRole -> {
                                Set<Role> roles = new HashSet<>();
                                roles.add(userRole);
                                user.setRoles(roles);
                                return userRepository.save(user);
                            })
                            .switchIfEmpty(userRepository.save(user)) 
                            .map(savedUser -> ResponseEntity.ok(new MessageResponse("User registered successfully!")));
                    });
            });
    }
}
