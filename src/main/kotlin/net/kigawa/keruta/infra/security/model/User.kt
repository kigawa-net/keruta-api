package net.kigawa.keruta.infra.security.model

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * User model for authentication.
 */
data class User(
    private val username: String,
    private val password: String,
    private val authorities: Collection<GrantedAuthority> = listOf(),
    private val accountNonExpired: Boolean = true,
    private val accountNonLocked: Boolean = true,
    private val credentialsNonExpired: Boolean = true,
    private val enabled: Boolean = true,
) : UserDetails {

    override fun getUsername(): String = username

    override fun getPassword(): String = password

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun isAccountNonExpired(): Boolean = accountNonExpired

    override fun isAccountNonLocked(): Boolean = accountNonLocked

    override fun isCredentialsNonExpired(): Boolean = credentialsNonExpired

    override fun isEnabled(): Boolean = enabled

    companion object {
        /**
         * Creates a user with the given username, password, and roles.
         */
        fun create(username: String, password: String, roles: List<String> = listOf("USER")): User {
            val authorities = roles.map { SimpleGrantedAuthority("ROLE_$it") }
            return User(username, password, authorities)
        }
    }
}
