package com.mugiranezaJ.bBoss.backend.controller;


import com.mugiranezaJ.bBoss.backend.model.LoginRequest;
import com.mugiranezaJ.bBoss.backend.model.User;
import com.mugiranezaJ.bBoss.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("api/v1")
public class UserController {

    @Autowired
    UserRepository userRepository;

    Map<String, Object> response;
    @GetMapping("/welcome")
    public ResponseEntity<Map<String, Object>> sayHi(){
        response.put("message", "hi there! welcome to bBoss api.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody User user){
        response = new HashMap<>();
        try {
            User newUser = userRepository.save(new User(user));
            response.put("message", "user created successfully");
            response.put("data", newUser);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }catch (Exception e){
            response.put("message", "There was error while creating a user, please try again");
            response.put("error", e);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
}
