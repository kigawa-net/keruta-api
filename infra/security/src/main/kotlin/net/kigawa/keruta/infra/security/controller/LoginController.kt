package net.kigawa.keruta.infra.security.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Controller for handling login page.
 */
@Controller
class LoginController {

    /**
     * Login page.
     *
     * @param error Error flag
     * @param model Model
     * @return The login view name
     */
    @GetMapping("/login")
    fun login(@RequestParam(required = false) error: Boolean?, model: Model): String {
        if (error == true) {
            model.addAttribute("error", "Invalid credentials")
        }
        return "login"
    }
}
