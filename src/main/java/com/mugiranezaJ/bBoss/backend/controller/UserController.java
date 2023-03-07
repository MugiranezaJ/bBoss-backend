package com.mugiranezaJ.bBoss.backend.controller;


import com.mugiranezaJ.bBoss.backend.config.jwt.JwtUtils;
import com.mugiranezaJ.bBoss.backend.dto.SignupRequest;
import com.mugiranezaJ.bBoss.backend.model.LoginRequest;
import com.mugiranezaJ.bBoss.backend.model.Role;
import com.mugiranezaJ.bBoss.backend.model.RoleType;
import com.mugiranezaJ.bBoss.backend.model.User;
import com.mugiranezaJ.bBoss.backend.repository.RoleRepository;
import com.mugiranezaJ.bBoss.backend.repository.UserRepository;
import com.mugiranezaJ.bBoss.backend.payload.response.ResponseBody;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("api/v1/auth")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    Map<String, Object> response;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    RoleRepository roleRepository;

    @GetMapping("/welcome")
    public ResponseEntity<Map<String, Object>> sayHi(){
        response.put("message", "hi there! welcome to bBoss api.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> userLogin(@RequestBody LoginRequest loginRequest){
        response =  new HashMap<>();
        try{
            Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
            if(userOptional.isPresent() && Objects.equals(userOptional.get().getPassword(), loginRequest.getPassword())){
                response.put("message", "login successful");
                response.put("data", userOptional);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }else {
                response.put("message", "email or password is not correct");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        }catch (Exception e){
            e.printStackTrace();
            response.put("message", "login failed, please try again later");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return new ResponseEntity<>(new ResponseBody(
                    HttpStatus.CONFLICT.value(),
                    "Username is already taken",
                    null), HttpStatus.CONFLICT);
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity<>(new ResponseBody(
                    HttpStatus.CONFLICT.value(),
                    "Email is already in use",
                    null), HttpStatus.CONFLICT);
        }

        // Create new user's account
        User user = new User(
                signUpRequest.getFirstName(),
                signUpRequest.getLastName(),
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                signUpRequest.getPhone(),
                passwordEncoder.encode(signUpRequest.getPassword()),
                signUpRequest.isActive()
                );

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "agent":
                        Role modRole = roleRepository.findByName(RoleType.ROLE_AGENT)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return new ResponseEntity<>(new ResponseBody(
                HttpStatus.CREATED.value(),
                "User registered successfully!",
                null), HttpStatus.CREATED);
    }
}
