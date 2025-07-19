package net.kigawa.keruta.infra.app.agent

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class KerutaAgentServiceImplTest {

    private val kerutaAgentService = KerutaAgentServiceImpl()

    @Test
    fun `service should be initialized correctly`() {
        assertNotNull(kerutaAgentService)
    }
}
