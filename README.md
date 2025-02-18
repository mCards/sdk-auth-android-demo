# mCards Android Auth SDK Demo App

The mCards android Auth SDK encapsulates the following functionality:

1. Authenticate via auth0, either by refreshing the user's existing session or requiring credentials entry
2. Collect user profile and/or address data
3. Change user region

# Usage
Implementing apps MUST override this string value for auth0 to work:

```<string name="auth0_domain">your value here</string>```

These values are gotten from the mCards team after setting up the client's auth0 instance.

You must then also update the manifest placeholders in the build.gradle file:

e.g. ```addManifestPlaceholders(mapOf("auth0Domain" to "@string/auth0_domain", "auth0Scheme" to "your app ID"))```

# Importing the Auth SDK
The mCards android SDKs are provided via a bill of materials. Add the following to your module-level build.gradle:

Groovy:
```
implementation(platform("com.mcards.sdk:bom:$latestVersion"))
implementation "com.mcards.sdk:auth"
```

Kotlin:
```
implementation(platform("com.mcards.sdk:bom:$latestVersion"))
implementation("com.mcards.sdk:auth")
```

And the following to the project settings.gradle (groovy):
```
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven {
            url = uri("https://maven.pkg.github.com/mymcard/sdk-bom-android")
            credentials {
                username = GITHUB_USERNAME
                password = GITHUB_TOKEN
            }
        }
    }
}
```

# Documentation
\\\\\Add documentation links here/////
