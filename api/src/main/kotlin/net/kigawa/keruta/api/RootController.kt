package net.kigawa.keruta.api

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.view.RedirectView

/**
 * Controller for handling the root and welcome pages.
 * Redirects to the admin dashboard.
 */
@Controller
class RootController {

    /**
     * Redirects the root path to the admin dashboard.
     *
     * @return RedirectView to the admin dashboard
     */
    @GetMapping("/")
    fun rootToAdmin(): RedirectView {
        return RedirectView("/admin")
    }

    /**
     * Redirects the welcome path to the admin dashboard.
     *
     * @return RedirectView to the admin dashboard
     */
    @GetMapping("/welcome")
    fun welcomeToAdmin(): RedirectView {
        return RedirectView("/admin")
    }
}
