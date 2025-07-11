package net.kigawa.keruta.api

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

/**
 * Controller for handling the root and welcome pages.
 * Returns a simple welcome message.
 */
@Controller
class RootController {

    /**
     * Returns a welcome message for the root path.
     *
     * @return Welcome message
     */
    @GetMapping("/")
    @ResponseBody
    fun root(): String {
        return "Welcome to Keruta API. Please use the API endpoints or refer to the documentation."
    }

    /**
     * Returns a welcome message for the welcome path.
     *
     * @return Welcome message
     */
    @GetMapping("/welcome")
    @ResponseBody
    fun welcome(): String {
        return "Welcome to Keruta API. Please use the API endpoints or refer to the documentation."
    }
}
