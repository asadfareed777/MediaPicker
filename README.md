# AndroidLibraryJitpack

Here’s a `README.md` file that provides instructions for adding a dependency from JitPack in your project's `build.gradle` file:

```markdown
# Adding JitPack Dependency

To add a dependency from JitPack to your Android project, follow these steps:

## Step 1: Add JitPack Repository

Open your project-level `build.gradle` file (usually located at the root of your project) and add the JitPack repository to the `repositories` section.

```gradle
allprojects {
    repositories {
        mavenCentral()
        maven { url "https://jitpack.io" }
    }
}
```

## Step 2: Add Dependency

Next, open your app-level `build.gradle` file (usually located in `app/build.gradle`) and add the required dependency in the `dependencies` section.

```gradle
dependencies {
     implementation 'com.github.asadfareed777:MediaPicker:LatestReleaseHere'
     implementation 'pub.devrel:easypermissions:3.0.0'
}
```

## Step 3: Sync Project

After adding the repository and the dependency, sync your project with Gradle files. You can do this by clicking the "Sync Now" notification that appears in the IDE or by selecting "File" > "Sync Project with Gradle Files" from the menu.

## Example

Here is an example of how your `build.gradle` files should look:

### Project-level build.gradle

```gradle
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath "com.android.tools.build:gradle:4.2.2"
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url "https://jitpack.io" }
    }
}
```

### App-level build.gradle

```gradle
apply plugin: 'com.android.application'

android {
    compileSdkVersion 30
    defaultConfig {
        applicationId "com.example.myapp"
        minSdkVersion 16
        targetSdkVersion 30
        versionCode 1
        versionName "1.0"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}

dependencies {
    implementation 'com.android.support:appcompat-v7:28.0.0'
    implementation 'com.github.asadfareed777:MediaPicker:LatestReleaseHere'
    implementation 'pub.devrel:easypermissions:3.0.0'
     
}
```

### How to Usage media picker in an activity

# Modes

Here are the modes for media selection and operations:

- `MODE_TAKE_IMAGE_CAMERA_ONLY`
- `MODE_TAKE_IMAGE_BOTH_CAMERA_GALLERY`
- `MODE_CHOOSE_ONLY_ANY_FILE`
- `MODE_TAKE_PIC_OR_CHOOSE_ANY_FILE`
- `MODE_TAKE_VIDEO_BOTH_RECORD_GALLERY`
- `MODE_RECORD_VIDEO_ONLY`
- `MODE_GALLERY_VIDEO_ONLY`
- `MODE_TAKE_IMAGE_GALLERY_ONLY`


```
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity(), ImagePickerUtility.ImagePickerListener{

    var imagePickerUtility: ImagePickerUtility? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // initialize declared object
        imagePickerUtility = ImagePickerUtility(
            this, null,
            this, ImagePickerUtility.MODE_TAKE_IMAGE_BOTH_CAMERA_GALLERY, true
        )
        // fetch image you can use click listeners or other logic
        imagePickerUtility!!.methodRequiresPermission()
    }
    // add onActivityResult like this
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imagePickerUtility!!.onActivityResult(this, requestCode, resultCode, data)
}

    // here you will receive captured,selected images and videos
    override fun onImageRequestCompleted(filePath: String?, resultCode: Int, imageUri: Uri?) {
        runOnUiThread {
            if (filePath != null) {
                val file = File(filePath)
                val file_size = (file.length() / 1024).toString().toInt()
                Log.wtf("fileSize", file_size.toString())
                Toast.makeText(this, filePath, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```


## Conclusion

By following the steps above, you will be able to add dependencies from JitPack to your Android project. If you encounter any issues, refer to the JitPack documentation or the library's repository for more information.
```

This `README.md` file provides clear instructions on how to add the JitPack repository and the specified dependency to an Android project's `build.gradle` files.