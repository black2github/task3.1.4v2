package ru.kata.spring.boot_security.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.kata.spring.boot_security.demo.*;
import ru.kata.spring.boot_security.demo.configs.util.UserValidator;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import javax.validation.Valid;
import java.util.List;


@RequestMapping("/rest")
@RestController
@CrossOrigin
public class RestApiController {
    private static final Logger log = LoggerFactory.getLogger(RestApiController.class);

    private final UserService userService;
    private UserValidator userValidator;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(userValidator);
    }

    @Autowired
    public RestApiController(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setUserValidator(UserValidator userValidator) {
        this.userValidator = userValidator;
    }

    /**
     * GET / - получение списка всех пользователей
     */
    @GetMapping()
    @ResponseBody
    public List<User> list() {
        log.debug("list: <- ");
        try {
            List<User> users = userService.listAll();
            log.debug("list: -> " + users);
            return users;
        } catch (Exception ex) {
            riseError("list", ex);
            return null;
        }
    }

    /**
     * GET /:id - получение данных о конкретном пользователе
     */
    @GetMapping("/{id}")
    @ResponseBody     // public ResponseEntity<User>
    public User show(@PathVariable("id") long id) {
        log.debug("show: <- id=" + id);
        try {
            User user = userService.find(id);
            log.debug("show: -> " + user);
            return user;
        } catch (Exception ex) {
            riseError("show", ex);
            return null;
        }
    }

    /**
     * POST /user -создание пользователя по данным из JSON запроса
     */
    @PostMapping("/user")
    @ResponseBody
    public User createUser(@RequestBody @Valid User user, BindingResult bindingResult) {
        log.debug("createUser: <- " + user);
        try {
            if (bindingResult.hasErrors()) {
                List<ObjectError> errs = bindingResult.getAllErrors();
                throw new InvalidFormatApplicationException(errs.get(0).getDefaultMessage());
            }
            User u = userService.create(user);
            log.trace("createUser: -> " + u);
            return u;
        } catch (Exception e) {
            riseError("createUser", e);
            return null;
        }
    }

    /**
     * PATCH /user - обновление данных пользователя c id из JSON объекта
     */
    @PatchMapping("/user")
    @ResponseBody
    public User updateUser(@RequestBody @Valid User user, BindingResult bindingResult) {
        log.debug("updateUser: <- " + user);
        try {
            if (bindingResult.hasErrors()) {
                List<ObjectError> errs = bindingResult.getAllErrors();
                for (ObjectError error : errs) {
                    if (! "password".equals(error.getCode())) {
                        throw new InvalidFormatApplicationException(errs.get(0).getDefaultMessage());
                    }
                }
            }
            User usr = userService.update(user);
            log.debug("updateUser: -> " + usr);
            return usr;
        } catch (Exception ex) {
            riseError("updateUser", ex);
            return null;
        }
    }

    /**
     * DELETE /user - удаление пользователя c id
     */
    @DeleteMapping("/user")
    @ResponseBody
    public void deleteUser(@RequestParam(value = "id") Long id) {
        log.debug(String.format("deleteUser: <- id=%d", id));
        try {
            userService.delete(id);
            log.debug("deleteUser: -> .");
        } catch (Exception ex) {
            riseError("deleteUser", ex);
        }
    }

    /*
     * Формирование ResponseStatusException для передачи клиенту расширенной информации об ошибке.
     * Требует установки в application.properties настройки server.error.include-message=always
     */
    private void riseError(String methodName, Exception e) {
        log.warn(methodName + ": error -> " + e.getMessage());
        if ( e instanceof SecurityApplicationException) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } else if (e instanceof DataIntegrityViolationException
            || e instanceof ApplicationException) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } else if (e instanceof JsonProcessingException) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}

