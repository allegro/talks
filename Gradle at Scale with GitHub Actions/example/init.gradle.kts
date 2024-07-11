allprojects {
    apply(plugin = "maven-publish")
    afterEvaluate {
        configurePublishingRepositories()
        configurePublications()
        maybeLogRecommendations()
    }
}

fun Project.configurePublishingRepositories() {
    if (publishing.repositories.isEmpty()) {
        configureArtifactory()
    } else {
        extra["setup-gradle.custom-publishing-detected"] = true
        logger.lifecycleYellow("Module $friendlyName has custom publishing repositories and setup-gradle will not modify them")
    }
}

fun Project.configureArtifactory() {
    publishing {
        repositories {
            maven {
                if (version.toString().endsWith("SNAPSHOT")) {
                    name = "artifactorySnapshots"
                    url = uri("https://artifactory.local/artifactory/snapshots")
                } else {
                    name = "artifactoryReleases"
                    url = uri("https://artifactory.local/artifactory/releases")
                }

                credentials {
                    username = SECRETS.get('ARTIFACTORY_USERNAME')
                    password = SECRETS.get('ARTIFACTORY_PASSWORD')
                }
            }
        }
    }
}

fun Project.configurePublications() {
    if (publishing.publications.isEmpty()) {
        if (pluginManager.hasPlugin("java-library")) {
            configureLibraryPublication()
        }
        if (pluginManager.hasPlugin("application")) {
            apply(plugin = "phoenix-provisioning")
            configureAppPublication()
        }
    } else {
        extra["setup-gradle.custom-publishing-detected"] = true
        logger.lifecycleYellow("Module $friendlyName has custom publications and setup-gradle will not modify them")
    }
}

fun Project.configureLibraryPublication() {
    publishing {
        publications.create<MavenPublication>("library") {
            from(components["java"])
        }
        logger.lifecycleGreen(
            "Configured module $friendlyName to be published as library (because it has the 'java-library' plugin)"
        )
    }
}

fun Project.configureAppPublication() {
    publishing {
        publications.create<MavenPublication>("app") {
            if (!System.getenv("ARTIFACT_ID").isNullOrBlank()) {
                artifactId = System.getenv("ARTIFACT_ID")
            }
            artifact(tasks["distZip"]) { classifier = "deploy" }
        }

        logger.lifecycleGreen(
            "Configured module $friendlyName to be published as deployable application " +
                "(because it has the 'application' plugin)"
        )
    }
}

val Project.friendlyName: String
    get() = when (path) {
        ":" -> "<root>"
        else -> path
    }

fun Project.publishing(configuration: PublishingExtension.() -> Unit) {
    configure<PublishingExtension>(configuration)
}

val Project.publishing: PublishingExtension
    get() = extensions.getByType<PublishingExtension>()

fun Logger.lifecycleRed(message: String) = lifecycle(RED + message + RESET)
fun Logger.lifecycleGreen(message: String) = lifecycle(GREEN + message + RESET)
fun Logger.lifecycleYellow(message: String) = lifecycle(YELLOW + message + RESET)
fun Logger.lifecycleBlue(message: String) = lifecycle(BLUE + message + RESET)

val RESET = "\u001b[0m"
val RED = "\u001b[31m"
val GREEN = "\u001b[32m"
val YELLOW = "\u001b[33m"
val BLUE = "\u001B[34m"

fun Project.maybeLogRecommendations() {
    if (extra.has("setup-gradle.custom-publishing-detected")) {
        logger.lifecycleYellow(
            "Unless you have any special requirements, it is recommended to remove explicit " +
                "'publishing' block from your build.gradle[.kts] and let the " +
                "setup-gradle configure it for you."
        )
    }
}

fun Project.breakBuild(message: String) {
    logger.lifecycleRed(message)
    throw GradleException(message)
}