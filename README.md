<div align="center">
<h1> RWPP </h1>
<div align="center">
  <strong>multiplatform launcher for Rusted Warfare</strong>
</div>
<br />
<div align="center">
 <img src = "https://github.com/Minxyzgo/RWPP/blob/main/shared/src/desktopMain/resources/logo.png" width = "100px"/>
</div>
<br />

----
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.22-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.6.11-brightgreen)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![License](https://img.shields.io/github/license/Minxyzgo/RWPP)]()
[![Issues](https://img.shields.io/github/issues/Minxyzgo/RWPP)]()
</div>

## Implemented
 - __Ban Units__
 - __IME fixed__
 - __Transferring mod__ (experimental)
 - __Blacklists__
 - __List filter__
 - __More room options__ (Such as lock room and team look)
 - __External Resource__
 - __New UI__

# Download
You can download RWPP release version in the releases.

# Run
- Install Java 17
- Copy RWPP to your rw root directory.
> eg. SteamLibrary\steamapps\common\Rusted Warfare\
- Run launcher.bat

# Build
Run gradle task `shared:rebuildJar` to build necessary libs at first.

OpenJdk 17 or above

For desktop, run task `desktop:packageReleaseUberJarForCurrentOS`

For android, assets and res are missing for some reason,
you can find them in your Rusted Warfare client.

# Contribute
If you want to create international support for your native language,
You can go `shared/src/commonMain/composeResources/files` to create a new bundle.

# Thanks

[RW-HPS](https://github.com/RW-HPS/RW-HPS)
