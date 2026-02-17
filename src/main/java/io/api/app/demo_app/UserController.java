package io.api.app.demo_app;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository repository;

    @GetMapping
    public List<User> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return repository.save(user);
    }
}
