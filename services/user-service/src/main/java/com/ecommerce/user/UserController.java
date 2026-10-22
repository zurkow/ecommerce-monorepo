package com.ecommerce.user;

import com.ecommerce.common.contract.UserDto;
import com.ecommerce.common.error.BusinessException;
import com.ecommerce.common.error.NotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository users;

    public UserController(UserRepository users) {
        this.users = users;
    }

    @GetMapping
    public List<UserDto> list() {
        return users.findAll().stream().map(User::toDto).toList();
    }

    @GetMapping("/{id}")
    public UserDto get(@PathVariable Long id) {
        return find(id).toDto();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserRequest req) {
        if (users.existsByEmail(req.email())) {
            throw new BusinessException("Email already registered: " + req.email());
        }
        return users.save(new User(req.email(), req.firstName(), req.lastName(), req.role())).toDto();
    }

    @PutMapping("/{id}")
    @Transactional
    public UserDto update(@PathVariable Long id, @Valid @RequestBody UserRequest req) {
        User user = find(id);
        if (!user.getEmail().equals(req.email()) && users.existsByEmail(req.email())) {
            throw new BusinessException("Email already registered: " + req.email());
        }
        user.update(req.email(), req.firstName(), req.lastName(), req.role());
        return user.toDto();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        users.delete(find(id));
    }

    private User find(Long id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("User " + id + " not found"));
    }
}
