package ru.yandex.practicum.filmorate.controller;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !(validateEmail(user.getEmail()))) {
            throw new ValidationException("Введён не корректный email");
        }

        if (user.getLogin() == null || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы!");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем!");
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("id должен быть указан!");
        }

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (newUser.getEmail() != null && !(newUser.getEmail().isBlank()) && validateEmail(newUser.getEmail())) {
                oldUser.setEmail(newUser.getEmail());
            }

            if (newUser.getLogin() != null && !(newUser.getLogin().isBlank())) {
                oldUser.setLogin(newUser.getLogin());
            }

            if (newUser.getName() != null && !(newUser.getName().isBlank())) {
                oldUser.setName(newUser.getName());
            } else {
                oldUser.setName(newUser.getLogin());
            }

            if (newUser.getBirthday() != null && !(newUser.getBirthday().isAfter(LocalDate.now()))) {
                oldUser.setBirthday(newUser.getBirthday());
            }
        }

        throw new ValidationException("Пользователя с id = " + newUser.getId() + " не найден!");
    }

    private boolean validateEmail(String email) {
        EmailValidator emailValidator = EmailValidator.getInstance();
        return emailValidator.isValid(email);
    }

    private int getNextId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
