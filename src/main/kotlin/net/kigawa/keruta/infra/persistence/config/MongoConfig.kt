package net.kigawa.keruta.infra.persistence.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@EnableMongoRepositories(basePackages = ["net.kigawa.keruta.infra.persistence"])
class MongoConfig : AbstractMongoClientConfiguration() {

    @Value("\${spring.data.mongodb.host}")
    private lateinit var host: String

    @Value("\${spring.data.mongodb.port}")
    private lateinit var port: String

    @Value("\${spring.data.mongodb.database}")
    private lateinit var database: String

    @Value("\${spring.data.mongodb.username:}")
    private var username: String = ""

    @Value("\${spring.data.mongodb.password:}")
    private var password: String = ""

    @Value("\${spring.data.mongodb.authentication-database:}")
    private var authSource: String = ""

    override fun getDatabaseName(): String {
        return database
    }

    override fun mongoClient(): MongoClient {
        val connectionString = if (username.isNotEmpty() && password.isNotEmpty()) {
            ConnectionString("mongodb://$username:$password@$host:$port/$database?authSource=$authSource")
        } else {
            ConnectionString("mongodb://$host:$port/$database")
        }

        val clientSettings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build()

        return MongoClients.create(clientSettings)
    }
}
