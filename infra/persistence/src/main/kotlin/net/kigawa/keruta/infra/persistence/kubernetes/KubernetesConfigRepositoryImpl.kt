package net.kigawa.keruta.infra.persistence.kubernetes

import net.kigawa.keruta.core.domain.model.KubernetesConfig
import net.kigawa.keruta.core.domain.repository.KubernetesConfigRepository
import org.springframework.stereotype.Repository

/**
 * KubernetesConfigRepositoryの実装
 */
@Repository
open class KubernetesConfigRepositoryImpl(
    private val mongoRepository: MongoKubernetesConfigRepository,
) : KubernetesConfigRepository {

    override suspend fun findAll(): List<KubernetesConfig> {
        return mongoRepository.findAll().map { it.toDomain() }
    }

    override suspend fun findById(id: String): KubernetesConfig? {
        return mongoRepository.findById(id).orElse(null)?.toDomain()
    }

    override suspend fun findByName(name: String): KubernetesConfig? {
        return mongoRepository.findByName(name)?.toDomain()
    }

    override suspend fun findActiveConfigs(): List<KubernetesConfig> {
        return mongoRepository.findByIsActiveTrue().map { it.toDomain() }
    }

    override suspend fun findByTag(tag: String): List<KubernetesConfig> {
        return mongoRepository.findByTagsContaining(tag).map { it.toDomain() }
    }

    override suspend fun findByNamespace(namespace: String): List<KubernetesConfig> {
        return mongoRepository.findByNamespace(namespace).map { it.toDomain() }
    }

    override suspend fun save(config: KubernetesConfig): KubernetesConfig {
        val entity = KubernetesConfigEntity.fromDomain(config)
        return mongoRepository.save(entity).toDomain()
    }

    override suspend fun create(config: KubernetesConfig): KubernetesConfig {
        val entity = KubernetesConfigEntity.fromDomain(config)
        return mongoRepository.save(entity).toDomain()
    }

    override suspend fun update(config: KubernetesConfig): KubernetesConfig {
        val entity = KubernetesConfigEntity.fromDomain(config)
        return mongoRepository.save(entity).toDomain()
    }

    override suspend fun deleteById(id: String): Boolean {
        return try {
            mongoRepository.deleteById(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun searchByNamePattern(pattern: String): List<KubernetesConfig> {
        return mongoRepository.findByNameRegex(pattern).map { it.toDomain() }
    }

    override suspend fun findAllTags(): List<String> {
        return mongoRepository.findAll()
            .flatMap { it.tags }
            .distinct()
            .sorted()
    }

    override suspend fun getConfigStats(): Map<String, Any> {
        val allConfigs = mongoRepository.findAll()
        val activeConfigs = allConfigs.filter { it.isActive }

        val statsByAuthType = allConfigs.groupBy { it.authType }
            .mapValues { it.value.size }

        val statsByNamespace = allConfigs.groupBy { it.namespace }
            .mapValues { it.value.size }

        return mapOf(
            "totalConfigs" to allConfigs.size,
            "activeConfigs" to activeConfigs.size,
            "inactiveConfigs" to (allConfigs.size - activeConfigs.size),
            "statsByAuthType" to statsByAuthType,
            "statsByNamespace" to statsByNamespace,
            "uniqueNamespaces" to allConfigs.map { it.namespace }.distinct().size,
            "uniqueClusters" to allConfigs.map { it.clusterUrl }.distinct().size,
            "totalTags" to allConfigs.flatMap { it.tags }.distinct().size,
        )
    }
}
