package com.example.ecommerce_solid.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.ecommerce_solid.dto.UserRequest;
import com.example.ecommerce_solid.dto.UserResponse;
import com.example.ecommerce_solid.model.User;
import com.example.ecommerce_solid.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserResponse createUser(
            @Valid @RequestBody UserRequest request) {

        User user = userService.createUser(request);

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());

        return response;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
}