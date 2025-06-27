package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка при добавлении фильма, не указано название");
            throw new ValidationException("У фильма должно быть название!");
        }

        if (film.getDescription().length() > 200) {
            log.warn("Ошибка при добавлении фильма, слишком длинное описание");
            throw new ValidationException("Длина описания больше максимально допустимой(200 символов)");
        }

        if (film.getReleaseDate().isBefore(LocalDate.parse("28.12.1895"))) {
            log.warn("Ошибка при добавлении фильма, дата релиза = {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза фильма не может быть раньше 28.12.1895");
        }

        if (!(durationCheck(film.getDuration()))) {
            log.warn("Ошибка при добавлении фильма, длительность = {}", film.getDuration());
            throw new ValidationException("Длительность должна быть больше 0");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);

        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Ошибка при обновлении данных фильма, не указан id");
            throw new ValidationException("id должен быть указан!");
        }

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            if (newFilm.getName() != null && !(newFilm.getName().isBlank())) {
                oldFilm.setName(newFilm.getName());
            }

            if (newFilm.getDescription() != null && !(newFilm.getDescription().isBlank())) {
                oldFilm.setDescription(newFilm.getDescription());
            }

            if (newFilm.getReleaseDate() != null) {
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }

            if (newFilm.getDuration() != null && durationCheck(newFilm.getDuration())) {
                oldFilm.setDuration(newFilm.getDuration());
            }
            return oldFilm;
        }

        log.warn("Ошибка при обновлении данных фильма, указан неверный id");
        throw new ValidationException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    private int getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private boolean durationCheck(Duration duration) {
        return duration.compareTo(Duration.ZERO) > 0;
    }
}
