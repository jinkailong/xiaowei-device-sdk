本 `README` 文档的主要内容为`Device`目录下各个源代码目录的说明，`Device`目录主要包括以下几个目录：

- CtroModule
- Demo_Android
- Demo_Linux
- Demo_Mac
- Sample

### CtrlModule 目录

#### 目录说明

这个目录为控制层的 native 代码，提供播放控制的能力。[跳转到控制层 WiKi 文档](https://xiaowei.qcloud.com/wiki/#OpenSrc_Control_Module)：

- `build`：为编译相关的文件，其中提供了 Android 的 arm 和 x86 编译示例，厂商可以根据自己的编译链进行调整；
- `build.sh`和`fast_build.sh`为编译脚本示例，运行`fast_build.sh`后会将 so 文件输出到 out 目录；
- `include`：包含一些接口头文件；
- `library`：为控制层依赖的第三方库；
- `projects`：中有 Xcode 能打开的项目；
- `xweicontrol`：为具体业务逻辑实现的源码。
	
#### 编译说明

在`CtrlModule`目录，有示例编译脚本`fast_build.sh`，运行后 so 文件会输出到`CtrlModule/out`目录，其余平台可以参照 `fast_build.sh`进行脚本编写。

编译控制层需要依赖以下第三方库，您可以点击以下的链接跳转到相应的页面进行下载：

1. [log_c](https://github.com/rxi/log.c)：请将下载下来的开源库源码文件置于`Device/CtrlModule/library/log_c`目录下；
2. [opus](https://github.com/xiph/opus/releases/tag/v1.2.1)：请将下载下来的开源库源码文件置于`Device/CtrlModule/library/opus-1.2.1`目录下；
3. [RapidJson](https://github.com/Tencent/rapidjson/releases/tag/v1.1.0)：请将下载下来的开源库源码文件置于`Device/CtrlModule/library/rapidjson-1.1.0`目录下；
4. [shared_ptr](https://github.com/SRombauts/shared_ptr)：请将下载下来的开源库源码文件置于`Device/CtrlModule/library/shared_ptr`目录下。

> 注意：`Device/CtrlModule/library`目录下已经存在各个依赖库对应的目录以及编译依赖的配置文件，再进行覆盖时需要保留这些配置文件。

### Demo_Android 目录

这个目录为 Android 的 demo。[跳转到 Android 平台接入指引](https://xiaowei.qcloud.com/wiki/#OpenSrc_Android_Demo_Guide)。
	
- `app`为 Android 的 Demo；
- `control`为 CtrlModule 的 JNI；
- `wakeup`为 Android 的本地唤醒库；
- `xiaoweiSDK`为 小微 SDK 的 JNI，其中小微 SDK 的 native 部分为闭源的，相关 so 文件可以从官网下载。
	
	
#### 编译说明

首先需要编译 CtrlModule 和 JNI，在`Demo_Android`中，有示例的编译脚本`build_jni.sh`，会一次性编译出`xiaoweiSDK`和`control`两个 JNI 的 so 文件。您也可以放开 gradle 文件中的 cmake 注释，使用 cmake 进行编译。编译完 so 文件以后，可以使用 AndroidStudio 打开 Demo_Android 目录，运行 app，进行 apk 的构建和安装。
	
### Demo_Linux 目录

这个目录为 Linux 的 demo。[跳转到 Linux 平台接入指引](https://xiaowei.qcloud.com/wiki/#OpenSrc_Linux_Demo_Guide)。

### Demo_Mac目录

这个目录为为 Mac 的 demo，项目文件位于 `projects/osx/DeviceSDKDemo.xcodeproj`。[跳转到 MacOs 平台接入指引](https://xiaowei.qcloud.com/wiki/#OpenSrc_Mac_Demo_Guide)。

### Sample 目录

这个目录为一些需要自行实现的功能的示例代码。
	

		