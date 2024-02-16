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
[![](https://img.shields.io/badge/Jetpack%20Compose-1.5.12-brightgreen)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![](https://img.shields.io/github/license/Minxyzgo/RWPP")]()
[![](https://img.shields.io/github/issues/Minxyzgo/RWPP")]()
</div>

> Tencent QQ Group: 150450999

## Implemented
 - __Ban Units__
 - __IME fixed__
 - __Transferring mod__ (experimental)
 - __Blacklists__
 - __List filter__
 - __More room options__ (Such as lock room and team look)
 - __External Resource__
 - __New UI__

## In Progress
 - __Single player__
 - __Better tmx__
 - __Replace List with flow__
 - ...

# Download
You can download RWPP release version in the packages.

Or you can download other versions in the QQ group.

# Build
Run gradle task `shared:rebuildJar` to build necessary libs at first.

OpenJdk 17 or above

For desktop, run task `desktop:packageReleaseUberJarForCurrentOS`

For android, assets and res are missing for some reason,
you can find them in your Rusted Warfare client.
# Run
- install Java 17
- Copy RWPP to your rw root directory.
- Run launcher.bat (you may need to change the rwpp jar name)