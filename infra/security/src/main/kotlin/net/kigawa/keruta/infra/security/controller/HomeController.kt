package net.kigawa.keruta.infra.security.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

/**
 * Controller for handling home page.
 */
@Controller
class HomeController {

    /**
     * Home page.
     *
     * @param user Authenticated user
     * @param model Model
     * @return The home view name
     */
    @GetMapping("/user-home")
    fun home(@AuthenticationPrincipal user: OAuth2User?, model: Model): String {
        if (user != null) {
            model.addAttribute("name", user.getAttribute<String>("name") ?: user.name)
            model.addAttribute("email", user.getAttribute<String>("email"))
            model.addAttribute("attributes", user.attributes)
        }
        return "home"
    }
}
