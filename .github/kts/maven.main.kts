#!/usr/bin/env kotlin

@file:DependsOn("io.github.typesafegithub:github-workflows-kt:4.0.0")


@file:Repository("https://repo.maven.apache.org/maven2/")
@file:Repository("https://bindings.krzeminski.it")

@file:DependsOn("actions:checkout:v7")
@file:DependsOn("actions:cache:v6")
@file:DependsOn("actions:setup-java:v6")
@file:DependsOn("codecov:codecov-action:v7")


import io.github.typesafegithub.workflows.actions.actions.Cache
import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.SetupJava
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.expressions.Contexts
import io.github.typesafegithub.workflows.dsl.expressions.Contexts.secrets
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.ConsistencyCheckJobConfig
import io.github.typesafegithub.workflows.actions.codecov.CodecovAction

workflow(
    name = "Maven all in one",
    on = listOf(
        PullRequest(),
        Push(
            branches = listOf("main"),
        ),
    ),
    sourceFile = __FILE__,
    consistencyCheckJobConfig = ConsistencyCheckJobConfig.Disabled
) {

    val SONAR_2026 by Contexts.secrets
    val SONAR_TOKEN by Contexts.env

    job(
        id = "build",
        runsOn = RunnerType.UbuntuLatest,
    ) {
        uses(
            name = "Checkout",
            action = Checkout(fetchDepth = Checkout.FetchDepth.Infinite)
        )
        uses(
            name = "Cache - Maven Repository",
            action = Cache(
                path = listOf(
                    "~/.m2/repository",
                ),
                key = "${'$'}{{ runner.os }}-maven-${'$'}{{ hashFiles('**/pom.xml') }}",
                restoreKeys = listOf(
                    "${'$'}{{ runner.os }}-maven-",
                ),
            ),
        )
        uses(
            name = "JDK 21",
            action = SetupJava(
                distribution = SetupJava.Distribution.Corretto,
                javaVersion = "21",
            ),
        )

        run(
            name = "Build with Maven, no testing",
            command = "./mvnw -B clean install -DskipTests",
        )

        run(
            name = "Test - Unit",
            command = "./mvnw -B test",
        )

        run(
            name = "Test - Integration",
            command = "./mvnw -B verify -DskipUnitTests",
        )
    }

}


