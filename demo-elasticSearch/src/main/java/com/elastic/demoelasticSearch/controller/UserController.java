package com.elastic.demoelasticSearch.controller;

import com.elastic.demoelasticSearch.model.User;
import com.elastic.demoelasticSearch.search.SearchRequestDTO;
import com.elastic.demoelasticSearch.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/elastic/api/post")
    public ResponseEntity createUser(@RequestBody User user) throws IOException {
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }
    @GetMapping("/elastic/api/get/{id}")
    public ResponseEntity getUserById(@RequestParam String id) throws IOException {
        return new ResponseEntity<>(userService.findById(id),HttpStatus.OK);
    }
    @PutMapping("/elastic/api/put")
    public ResponseEntity updateUser(@RequestBody User user,@PathVariable String id) throws IOException {
        return new ResponseEntity<>(userService.updateUser(user,id),HttpStatus.OK);
    }
    @PostMapping("/search")
    public List<User> search(@RequestBody SearchRequestDTO dto){
        return userService.search(dto);
    }

    @GetMapping(value = "/elastic/api/getAll/user",produces = MediaType.APPLICATION_JSON_VALUE)
    public  List<User> getAllUser(){
        return userService.getAllUser();
    }

    @GetMapping(value = "/elastic/api/{firstName}",produces = MediaType.APPLICATION_JSON_VALUE)
    public List<User> getUserByName(@PathVariable String firstName){
        return userService.getUserByName(firstName);
    }

//    @DeleteMapping("/elastic/api/delete")
//    public ResponseEntity deleteUser(@RequestParam String id) throws IOException {
//        return new ResponseEntity<>(userService.deleteUser(id),HttpStatus.OK);
//    }
}
