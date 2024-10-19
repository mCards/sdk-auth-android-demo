# mCards Android Auth SDK Demo App

The mCards android Auth SDK encapsulates the following functionality:

1. Authenticate via auth0, either by refreshing the user's existing session or requiring credentials entry
2. Collect user profile and/or address data
3. Change user region

# Usage
Implementing apps MUST override the following strings for auth0 to work:

<string name="auth0_domain">your value here</string>
<string name="auth0_scheme">your value here</string>

These values are gotten from the mCards team after setting up the client's auth0 instance.

This can also be done by specifying manifest placeholders directly in the gradle script?

e.g. addManifestPlaceholders(mapOf("auth0Domain" to "@string/auth0_domain", "auth0Scheme" to "@string/auth0_scheme"))

This will probably override the manifest placeholders in the sdk in the merged manifest?

# Importing the Auth SDK
Add the following to your module-level build.gradle:

Groovy:
```
implementation "com.mcards.sdk:auth:$latestVersion"
```

Kotlin:
```
implementation("com.mcards.sdk:auth:$latestVersion")
```

And the following to the project settings.gradle:
```
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven {
            url = uri("https://maven.pkg.github.com/Wantsa/sdk-auth-android")
            credentials {
                username = GITHUB_USERNAME
                password = GITHUB_TOKEN
            }
        }
    }
}
```

# Documentation
Documentation is exported to the project ```/documentation``` folder in both html and javadoc styles.

Conceptual documentation is available at: TODO
