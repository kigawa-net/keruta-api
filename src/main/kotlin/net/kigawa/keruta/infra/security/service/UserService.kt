package net.kigawa.keruta.infra.security.service

import jakarta.annotation.PostConstruct
import net.kigawa.keruta.infra.security.model.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/**
 * Service for user management.
 */
@Service
open class UserService(
    private val passwordEncoder: PasswordEncoder,
) : UserDetailsService {

    @Value("\${auth.admin.username:admin}")
    private lateinit var adminUsername: String

    @Value("\${auth.admin.password:password}")
    private lateinit var adminPassword: String

    @Value("\${auth.api.username:keruta-api}")
    private lateinit var apiUsername: String

    @Value("\${auth.api.password:api-password}")
    private lateinit var apiPassword: String

    private val users = mutableMapOf<String, User>()

    /**
     * Initializes the user service with default users.
     */
    @PostConstruct
    fun init() {
        if (users.isEmpty()) {
            // Create admin user
            val encodedAdminPassword = passwordEncoder.encode(adminPassword)
            val adminUser = User.create(adminUsername, encodedAdminPassword, listOf("ADMIN"))
            users[adminUsername] = adminUser

            // Create API user
            val encodedApiPassword = passwordEncoder.encode(apiPassword)
            val apiUser = User.create(apiUsername, encodedApiPassword, listOf("API"))
            users[apiUsername] = apiUser
        }
    }

    /**
     * Loads a user by username.
     *
     * @param username The username
     * @return The user details
     * @throws UsernameNotFoundException If the user is not found
     */
    override fun loadUserByUsername(username: String): UserDetails {
        init()
        return users[username] ?: throw UsernameNotFoundException("User not found: $username")
    }

    /**
     * Validates a user's credentials.
     *
     * @param username The username
     * @param password The password
     * @return true if the credentials are valid, false otherwise
     */
    fun validateCredentials(username: String, password: String): Boolean {
        val user = try {
            loadUserByUsername(username)
        } catch (e: UsernameNotFoundException) {
            return false
        }

        return passwordEncoder.matches(password, user.password)
    }
}
