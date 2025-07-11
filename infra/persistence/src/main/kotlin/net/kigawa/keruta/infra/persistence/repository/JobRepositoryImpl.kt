/**
 * Implementation of the JobRepository interface using MongoDB.
 */
package net.kigawa.keruta.infra.persistence.repository

import net.kigawa.keruta.core.domain.model.Job
import net.kigawa.keruta.core.domain.model.JobStatus
import net.kigawa.keruta.core.usecase.repository.JobRepository
import net.kigawa.keruta.infra.persistence.entity.JobEntity
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class JobRepositoryImpl(private val mongoJobRepository: MongoJobRepository) : JobRepository {

    override fun findAll(): List<Job> {
        return mongoJobRepository.findAll().map { it.toDomain() }
    }

    override fun findById(id: String): Job? {
        return mongoJobRepository.findById(id).orElse(null)?.toDomain()
    }

    override fun findByTaskId(taskId: String): List<Job> {
        return mongoJobRepository.findByTaskId(taskId).map { it.toDomain() }
    }

    override fun save(job: Job): Job {
        val entity = JobEntity.fromDomain(job)
        return mongoJobRepository.save(entity).toDomain()
    }

    override fun updateStatus(id: String, status: JobStatus): Job {
        val job = findById(id) ?: throw NoSuchElementException("Job not found with id: $id")
        val updatedJob = job.copy(
            status = status,
            updatedAt = LocalDateTime.now(),
        )
        return save(updatedJob)
    }

    override fun updateLogs(id: String, logs: String): Job {
        val job = findById(id) ?: throw NoSuchElementException("Job not found with id: $id")
        val updatedLogs = if (job.logs != null) {
            "${job.logs}\n$logs"
        } else {
            logs
        }
        val updatedJob = job.copy(
            logs = updatedLogs,
            updatedAt = LocalDateTime.now(),
        )
        return save(updatedJob)
    }

    override fun deleteById(id: String): Boolean {
        return if (mongoJobRepository.existsById(id)) {
            mongoJobRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    override fun findByStatus(status: JobStatus): List<Job> {
        return mongoJobRepository.findByStatus(status.name).map { it.toDomain() }
    }
}
