pluginManagement {
    repositories {
        repositories {
            //gradlePluginPortal()
            //       google()
//        mavenCentral()
            maven {
                url = uri("https://nexus.dq.vwgroup.com/repository/maven-public/")
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        //google()
//        mavenCentral()
        maven {
            url=uri("https://nexus.dq.vwgroup.com/repository/maven-public/")
        }
    }
}

rootProject.name = "AppGrid Compose"
include(":app")
 