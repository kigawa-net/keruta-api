package net.kigawa.keruta.infra.security.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.util.ReflectionTestUtils

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @InjectMocks
    private lateinit var userService: UserService

    @Test
    fun `should load admin user successfully`() {
        // Given
        ReflectionTestUtils.setField(userService, "adminUsername", "admin")
        ReflectionTestUtils.setField(userService, "adminPassword", "password")
        ReflectionTestUtils.setField(userService, "apiUsername", "keruta-api")
        ReflectionTestUtils.setField(userService, "apiPassword", "api-password")

        `when`(passwordEncoder.encode("password")).thenReturn("encoded-password")
        `when`(passwordEncoder.encode("api-password")).thenReturn("encoded-api-password")

        // When
        userService.init() // Manually call init to set up users
        val user = userService.loadUserByUsername("admin")

        // Then
        assertNotNull(user)
        assertEquals("admin", user.username)
        assertEquals("encoded-password", user.password)
        assertTrue(user.authorities.any { it.authority == "ROLE_ADMIN" })
    }

    @Test
    fun `should load api user successfully`() {
        // Given
        ReflectionTestUtils.setField(userService, "adminUsername", "admin")
        ReflectionTestUtils.setField(userService, "adminPassword", "password")
        ReflectionTestUtils.setField(userService, "apiUsername", "keruta-api")
        ReflectionTestUtils.setField(userService, "apiPassword", "api-password")

        `when`(passwordEncoder.encode("password")).thenReturn("encoded-password")
        `when`(passwordEncoder.encode("api-password")).thenReturn("encoded-api-password")

        // When
        userService.init() // Manually call init to set up users
        val user = userService.loadUserByUsername("keruta-api")

        // Then
        assertNotNull(user)
        assertEquals("keruta-api", user.username)
        assertEquals("encoded-api-password", user.password)
        assertTrue(user.authorities.any { it.authority == "ROLE_API" })
    }

    @Test
    fun `should throw exception when user not found`() {
        // Given
        ReflectionTestUtils.setField(userService, "adminUsername", "admin")
        ReflectionTestUtils.setField(userService, "adminPassword", "password")
        ReflectionTestUtils.setField(userService, "apiUsername", "keruta-api")
        ReflectionTestUtils.setField(userService, "apiPassword", "api-password")

        `when`(passwordEncoder.encode("password")).thenReturn("encoded-password")
        `when`(passwordEncoder.encode("api-password")).thenReturn("encoded-api-password")

        // When/Then
        userService.init() // Manually call init to set up users

        try {
            userService.loadUserByUsername("non-existent-user")
            throw AssertionError("Expected UsernameNotFoundException was not thrown")
        } catch (e: UsernameNotFoundException) {
            assertEquals("User not found: non-existent-user", e.message)
        }
    }
}
