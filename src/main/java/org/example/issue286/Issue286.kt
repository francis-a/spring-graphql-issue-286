package org.example.issue286

import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.DataFetchingEnvironment
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.execution.DataFetcherExceptionResolver
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter
import org.springframework.graphql.execution.ErrorType
import org.springframework.stereotype.Controller

@SpringBootApplication
@Controller
open class Issue286 {


    private val users = mapOf(
        "found" to User("found")
    )

    @QueryMapping(name = "userGet")
    fun userGet(@Argument(name = "username") username: String): User {
        return users[username] ?: throw NotFoundException(username)
    }

    data class NotFoundException(private val username: String) : RuntimeException("User $username was not found")
    data class User(val username: String)

    @Bean
    open fun dataFetcherExceptionResolver(): DataFetcherExceptionResolver {
        return object : DataFetcherExceptionResolverAdapter() {
            override fun resolveToSingleError(ex: Throwable, env: DataFetchingEnvironment): GraphQLError? {
                return when(ex) {
                    is NotFoundException -> {
                        GraphqlErrorBuilder.newError(env)
                            .errorType(ErrorType.NOT_FOUND)
                            .message(ex.message)
                            .build()
                    }
                    else -> {
                        GraphqlErrorBuilder.newError(env)
                            .errorType(ErrorType.INTERNAL_ERROR)
                            .message(ex.message)
                            .build()
                    }
                }
            }
        }
    }

}

fun main(args: Array<String>) {
    SpringApplication.run(Issue286::class.java, *args)
}
