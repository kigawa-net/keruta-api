package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.infra.persistence.entity.KubernetesConfigEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data MongoDB repository for KubernetesConfigEntity.
 */
@Repository
interface MongoKubernetesConfigRepository : MongoRepository<KubernetesConfigEntity, String> {
    /**
     * Finds the first Kubernetes configuration in the database.
     * Since we only need one configuration, we can use this method to get it.
     *
     * @return The first Kubernetes configuration entity, or null if none exists
     */
    fun findFirstByOrderByCreatedAtAsc(): KubernetesConfigEntity?
}
