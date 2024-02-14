<div align="center">
<h1> RWPP - multiplatform launcher for Rusted Warfare </h1>

----
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.22-blue.svg?logo=kotlin)](http://kotlinlang.org)

</div>

> Tencent QQ Group: 150450999

## Implemented
 - Ban Units
 - IME fixed
 - Transferring mod (experimental)
 - Blacklists
 - List filter
 - More room options (Such as lock room and team look)
 - External Resource
 - New UI

## In Progress
 - Single player
 - Better tmx
 - Replace List with flow
 - ...

# Download
You can download RWPP release version in the packages.

Or you can download other versions in the QQ group.

# Build
run task `shared:rebuildJar` to build necessary libs at first.

OpenJdk 17 or above

for desktop, run task `desktop:packageReleaseUberJarForCurrentOS`

for android, assets and res are missing for some reason,
you can find them in your Rusted Warfare client.
# Run
- install Java 17
- Copy RWPP to your rw root directory.
- Run launcher.bat (you may need to change the rwpp jar name)