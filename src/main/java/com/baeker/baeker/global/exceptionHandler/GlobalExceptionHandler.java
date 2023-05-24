package com.baeker.baeker.global.exceptionHandler;

import com.baeker.baeker.global.exception.NumberInputException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNotFoundError(Model model, NoHandlerFoundException ex) {
        model.addAttribute("exception", ex);
        return "error/404";
    }

    @ExceptionHandler(NumberInputException.class)
    public String numberInputError(Model model, NumberInputException ex) {
        model.addAttribute("exception", ex);
        return "error/500";
    }
}
