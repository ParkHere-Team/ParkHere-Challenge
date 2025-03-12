plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.4.3"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "1.9.25"
	id("org.openapi.generator") version "7.11.0"
	id("io.swagger.core.v3.swagger-gradle-plugin") version "2.2.29"
}

group = "eu.parkHere"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.openapitools:jackson-databind-nullable:0.2.6")
	// https://mvnrepository.com/artifact/org.postgresql/postgresql
	implementation("org.postgresql:postgresql:42.7.5")
	// https://mvnrepository.com/artifact/jakarta.validation/jakarta.validation-api
	implementation("jakarta.validation:jakarta.validation-api:3.1.1")
	// https://mvnrepository.com/artifact/io.springfox/springfox-oas
	implementation("io.springfox:springfox-oas:3.0.0")
	// https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
	implementation("com.squareup.okhttp3:okhttp:4.12.0")

	// https://mvnrepository.com/artifact/jakarta.servlet/jakarta.servlet-api
	compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")


	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
	testImplementation("io.mockk:mockk:1.13.4")
	testImplementation("org.assertj:assertj-core:3.24.2")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.register<Jar>("sourcesJar") {

}

// OpenAPI Generator for Configuration API
tasks.register("openApiGenerateConfiguration", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
	generatorName.set("kotlin")
	inputSpec.set("$projectDir/api-specs/configuration-service-api-specs.yaml")
	outputDir.set("$projectDir/build/generated-sources/configuration-service")
	apiPackage.set("eu.parkHere.challenge.api")
	modelPackage.set("eu.parkHere.challenge.model")
	additionalProperties.set(mapOf(
		"useSpringBoot3" to "true",
		"useSwaggerUI" to "true",
		"interfaceOnly" to "true",
		"useJakartaEe" to "true",
		"serializationLibrary" to "jackson",
		"idea" to "true",
		"generateApiTests" to "false",
		"library" to "jvm-spring-webclient"
	))
}

// OpenAPI Generator for Reservations API
tasks.register("openApiGenerateReservations", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
	generatorName.set("kotlin-spring")
	inputSpec.set("$projectDir/api-specs/reservation-service-api-specs.yaml")
	outputDir.set("$projectDir/build/generated-sources/reservation-service")
	apiPackage.set("eu.parkHere.challenge.api")
	modelPackage.set("eu.parkHere.challenge.model")
	additionalProperties.set(mapOf(
		"useSpringBoot3" to "true",
		"useSwaggerUI" to "true",
		"interfaceOnly" to "true",
		"useJakartaEe" to "true",
		"serializationLibrary" to "jackson",
		"idea" to "true"
	))
}
tasks.withType(Copy::class).all { duplicatesStrategy = DuplicatesStrategy.INCLUDE }

tasks.register<Copy>("copyBuildFilesToSource") {
	dependsOn("openApiGenerateConfiguration", "openApiGenerateReservations") // Ensure build task runs first

	copy {
		from("$projectDir/build/generated-sources/configuration-service/src/main/kotlin/eu/parkHere/challenge") // Replace with the actual source directory within the build folder
		into("$projectDir/src/main/kotlin/eu/parkHere/challenge/client/configuration")

		from("$projectDir/build/generated-sources/configuration-service/src/main/kotlin/org") // Replace with the actual source directory within the build folder
		into("$projectDir/src/main/kotlin/eu/parkHere/challenge/client/configuration")// Replace with the destination directory in your source
	}

	copy {
		from("$projectDir/build/generated-sources/reservation-service/src/main/kotlin/eu/parkHere/challenge") // Replace with the actual source directory within the build folder
		into("$projectDir/src/main/kotlin/eu/parkHere/challenge/api/reservation") // Replace with the destination directory in your source

		from("$projectDir/build/generated-sources/reservation-service/src/main/kotlin/org") // Replace with the actual source directory within the build folder
		into("$projectDir/src/main/kotlin/eu/parkHere/challenge/api/reservation")
	}
}

// Automatically generate OpenAPI code before compilation
tasks.named("compileKotlin") {
	mustRunAfter("copyBuildFilesToSource")
	dependsOn("openApiGenerateConfiguration", "openApiGenerateReservations")
}

// Ensure OpenAPI generation runs before building the project
tasks.named("build") {
	dependsOn("copyBuildFilesToSource")
}

