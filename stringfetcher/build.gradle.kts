import java.net.URL
import java.io.FileOutputStream
import java.util.zip.ZipFile
import java.nio.charset.Charset

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 32
    defaultConfig {
        minSdk = 21
        targetSdk = 32
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    sourceSets.getByName("main") {
        res.srcDir("src/main/res")
        res.srcDir("build/generated/strings")
    }
}

dependencies {
    api("junit:junit:4.13.2")
    implementation("junit:junit:4.13.2")
    testImplementation("junit:junit:4.13.2")
}

configurations {
    implementation {
        resolutionStrategy.failOnVersionConflict()
    }
}

/**
 * 下载解压字符串
 */

fun download(remoteUrl: String, filePath: String) {
    val startTime = System.currentTimeMillis()
    URL(remoteUrl).openConnection().let {
        // todo 可能会有重定向
        val fileOutputStream = FileOutputStream(filePath)
        val bytes = it.getInputStream().readBytes()
        fileOutputStream.write(bytes)
        it.getInputStream().close()
        val endTime = System.currentTimeMillis()
        println("download finished,cost: ${(endTime - startTime) / 1000f}s, url=$remoteUrl")
    }
}

fun unzip(zipPath: String, outPath: String) {
    val startTime = System.currentTimeMillis()
    val zip = ZipFile(File(zipPath), Charset.forName("utf-8"))
    zip.entries().toList().forEach {
        if (!it.isDirectory) {
            val outFile = File(outPath + File.separator + it.name)
            File(outFile.parent).mkdirs()
            val outPutStream = FileOutputStream(outFile)
            val bytes = zip.getInputStream(it).readBytes()
            outPutStream.write(bytes)
            zip.getInputStream(it).close()
        }
    }
    zip.close()
    val endTime = System.currentTimeMillis()
    println("unzip finished,cost: ${(endTime - startTime) / 1000f}s")
    println("zip path=$zipPath, unzip path=$outPath")
}

var isFinish = false
val taskCommand = "string"

// 添加空task 让./gradlew string命令不会报错
tasks.register(taskCommand) {
    doLast {
        println("string task finish! (￣▽￣)\"")
    }
}

android.libraryVariants.all {
    if (!isFinish && gradle.startParameter.taskRequests.getOrNull(0) != null) {
        val args = gradle.startParameter.taskRequests.getOrNull(0)?.args ?: arrayListOf()
        println("task startParameter $args")
        val requestUrl = "http://localhost:9090/strings/download"
        val jarPath = rootProject.rootDir.absolutePath + "/stringfetcher/strings.zip"
        val outPath = rootProject.rootDir.absolutePath + "/stringfetcher/build/generated"
        if (args.contains(taskCommand) && !gradle.startParameter.isOffline) {
            download(requestUrl, jarPath)
        }
        val outDir = file(outPath)
        outDir.delete()
        outDir.mkdirs()
        unzip(jarPath, outPath)
        isFinish = true
    }
}