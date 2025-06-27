package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class UserController {
    private Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !(validateEmail(user.getEmail()))) {
            log.warn("Ошибка при создании пользователя, некорректный email");
            throw new ValidationException("Введён не корректный email");
        }

        if (user.getLogin() == null || user.getLogin().contains(" ")) {
            log.warn("Ошибка при создании пользователя, некорректный login");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы!");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.trace("Установлено имя пользователя по умолчанию");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка при создании пользователя, некорректная дата рождения");
            throw new ValidationException("Дата рождения не может быть в будущем!");
        }

        user.setId(getNextId());
        log.trace("Установлен id пользователя");
        users.put(user.getId(), user);
        log.info("Пользователь добавлен");
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        if (newUser.getId() == null) {
            log.warn("Ошибка при обновления данных пользователя, не указан id");
            throw new ValidationException("id должен быть указан!");
        }

        if (users.containsKey(newUser.getId())) {
            log.trace("Пользователь с id = {} найден", newUser.getId());
            User oldUser = users.get(newUser.getId());
            if (newUser.getEmail() != null && !(newUser.getEmail().isBlank()) && validateEmail(newUser.getEmail())) {
                oldUser.setEmail(newUser.getEmail());
                log.trace("Обновлён email пользователя");
            }

            if (newUser.getLogin() != null && !(newUser.getLogin().isBlank())) {
                oldUser.setLogin(newUser.getLogin());
                log.trace("Обновлен логин пользователя");
            }

            if (newUser.getName() != null && !(newUser.getName().isBlank())) {
                oldUser.setName(newUser.getName());
                log.trace("Обновлено имя пользователя");
            } else {
                oldUser.setName(newUser.getLogin());
                log.trace("Имя пользователя утановлено по умолчанию");
            }
            if (newUser.getBirthday() != null && !(newUser.getBirthday().isAfter(LocalDate.now()))) {
                oldUser.setBirthday(newUser.getBirthday());
                log.trace("День рождения пользователя обновлено");
            }
        }

        log.warn("Ошибка при обновления данных пользователя, указан не верный id");
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
