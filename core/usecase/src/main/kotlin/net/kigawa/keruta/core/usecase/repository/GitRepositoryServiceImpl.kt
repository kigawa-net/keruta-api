/**
 * Implementation of the GitRepositoryService interface.
 */
package net.kigawa.keruta.core.usecase.repository

import net.kigawa.keruta.core.domain.model.Repository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
open class GitRepositoryServiceImpl(
    private val gitRepositoryRepository: GitRepositoryRepository,
) : GitRepositoryService {

    companion object {
        private const val DEFAULT_SETUP_SCRIPT = """#!/bin/sh
# This is the default setup script for Keruta repositories.
# This script is executed after the repository is cloned.
# You can customize this script to install dependencies, set up the environment, etc.

set -e

echo "Starting setup script for repository"

# Check if package.json exists and run npm install
if [ -f "package.json" ]; then
    echo "Found package.json, running npm install"
    npm install
fi

# Check if requirements.txt exists and run pip install
if [ -f "requirements.txt" ]; then
    echo "Found requirements.txt, running pip install"
    pip install -r requirements.txt
fi

# Check if build.gradle or build.gradle.kts exists and run gradle build
if [ -f "build.gradle" ] || [ -f "build.gradle.kts" ]; then
    echo "Found Gradle build file, running gradle build"
    ./gradlew build
fi

# Check if pom.xml exists and run mvn install
if [ -f "pom.xml" ]; then
    echo "Found pom.xml, running mvn install"
    mvn install
fi

echo "Setup script completed successfully"
"""
    }

    override fun getAllRepositories(): List<Repository> {
        return gitRepositoryRepository.findAll()
    }

    override fun getRepositoryById(id: String): Repository {
        if (id.isBlank()) {
            throw IllegalArgumentException("Repository ID cannot be empty")
        }
        return gitRepositoryRepository.findById(id) ?: throw NoSuchElementException("Repository not found with id: $id")
    }

    override fun getSetupScript(id: String): String {
        val repository = getRepositoryById(id)
        return repository.setupScript
    }

    override fun createRepository(repository: Repository): Repository {
        val validatedRepository = repository.copy(
            isValid = validateRepositoryUrl(repository.url),
            setupScript = if (repository.setupScript == "") DEFAULT_SETUP_SCRIPT else repository.setupScript,
        )
        return gitRepositoryRepository.save(validatedRepository)
    }

    override fun updateRepository(id: String, repository: Repository): Repository {
        val existingRepository = getRepositoryById(id)
        val updatedRepository = repository.copy(
            id = existingRepository.id,
            isValid = validateRepositoryUrl(repository.url),
            setupScript = if (repository.setupScript == "") existingRepository.setupScript else repository.setupScript,
            createdAt = existingRepository.createdAt,
            updatedAt = LocalDateTime.now(),
        )
        return gitRepositoryRepository.save(updatedRepository)
    }

    override fun deleteRepository(id: String) {
        if (!gitRepositoryRepository.deleteById(id)) {
            throw NoSuchElementException("Repository not found with id: $id")
        }
    }

    override fun validateRepositoryUrl(url: String): Boolean {
        return gitRepositoryRepository.validateUrl(url)
    }

    override fun getRepositoriesByName(name: String): List<Repository> {
        return gitRepositoryRepository.findByName(name)
    }
}
