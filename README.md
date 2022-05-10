![java 17](https://img.shields.io/badge/java-17-brightgreen.svg)
---
# Android Platforms Downloader
A tiny Java tool just to download the up-to-date Android platform files (android.jar).

## Build
Run Maven to build: `mvn`  
(Output jar-file will be placed in  `target/build`)

## Usage
Use as follows: `java -jar FX-APD.jar [from] [to] [repository/updatesite]`  
**Examples:**
- no optional parameters: `java -jar FX-APD-1.0.jar`
- with optional parameter: `java -jar FX-APD.jar 19 32 https://dl.google.com/android/repository/repository2-3.xml`

(Downloaded and extracted files will become available in a local `platforms` directory.)