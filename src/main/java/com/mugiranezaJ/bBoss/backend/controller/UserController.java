package com.mugiranezaJ.bBoss.backend.controller;


import com.mugiranezaJ.bBoss.backend.config.jwt.JwtUtils;
import com.mugiranezaJ.bBoss.backend.dto.LoginRequest;
import com.mugiranezaJ.bBoss.backend.dto.SignupRequest;
import com.mugiranezaJ.bBoss.backend.model.Role;
import com.mugiranezaJ.bBoss.backend.model.RoleType;
import com.mugiranezaJ.bBoss.backend.model.User;
import com.mugiranezaJ.bBoss.backend.payload.response.LoginResponse;
import com.mugiranezaJ.bBoss.backend.repository.RoleRepository;
import com.mugiranezaJ.bBoss.backend.repository.UserRepository;
import com.mugiranezaJ.bBoss.backend.payload.response.ResponseBody;
import com.mugiranezaJ.bBoss.backend.service.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

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

//    @PostMapping("/login")
//    public ResponseEntity<Map<String, Object>> userLogin(@RequestBody LoginRequest loginRequest){
//        response =  new HashMap<>();
//        try{
//            Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
//            if(userOptional.isPresent() && Objects.equals(userOptional.get().getPassword(), loginRequest.getPassword())){
//                response.put("message", "login successful");
//                response.put("data", userOptional);
//                return new ResponseEntity<>(response, HttpStatus.OK);
//            }else {
//                response.put("message", "email or password is not correct");
//                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//            response.put("message", "login failed, please try again later");
//            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

        // check if username is already taken
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return new ResponseEntity<>(new ResponseBody(
                    HttpStatus.CONFLICT.value(),
                    "Username is already taken",
                    null), HttpStatus.CONFLICT);
        }

        // check if email is already in the database
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity<>(new ResponseBody(
                    HttpStatus.CONFLICT.value(),
                    "Email is already in use",
                    null), HttpStatus.CONFLICT);
        }

        // check if password is at least 8 characters
        if (signUpRequest.getPassword().length() < 8){
            return new ResponseEntity<>(new ResponseBody(
                    HttpStatus.BAD_REQUEST.value(),
                    "Password must be at least 8 characters",
                    null
            ), HttpStatus.BAD_REQUEST);
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

        // get roles from request body
        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        // check if roles are provided, if not then default to user
        if (strRoles == null) {
            Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            // check role type and add it to roles of a user
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

        // set the users and save user to the database
        user.setRoles(roles);
        userRepository.save(user);

        // return the message when user successfully registered
        return new ResponseEntity<>(new ResponseBody(
                HttpStatus.CREATED.value(),
                "User registered successfully!",
                null), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        // check if username and password provided exist and are valid
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        // store the user info if authentication is successful and create jwt token
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        // get authenticated user details, and  roles(authorities) as a list of strings
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // return authentication details on successful login
        return ResponseEntity.ok(new LoginResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }
}
