# DiscordRPC library using java 16 sockets on unix-like systems  

This is both good and bad, it's good because the only native library it needs is the standard library itself, so it should work on every platform discord works, it is bad because it cannot be used on a project that doesn't use java 16 or higher  

Joining and spectating hasn't been thoroughly tested yet, if you test it yourself, please make an issue detailing your experience  

This project had some inspiration from https://github.com/jagrosh/DiscordIPC, that uses the Apache license version 2  

# Installation  

Add the jitpack repository and this dependency to your build.gradle or maven equivalent, replacing ${version} with a tag, release, or commit hash  
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.NepNep21:DiscordRPC4j16:${version}'
}
```

There are example projects at https://github.com/NepNep21/YARPC-forge and https://github.com/NepNep21/YARPC-fabric