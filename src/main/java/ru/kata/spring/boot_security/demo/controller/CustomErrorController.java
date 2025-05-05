package ru.kata.spring.boot_security.demo.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError() {
        // Здесь можно добавить логику обработки ошибок, например, отображение страницы с ошибкой
        return "customErrorPage";  // Имя страницы ошибки, которую вы хотите отобразить
    }

}
