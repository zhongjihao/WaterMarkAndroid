如果您觉得该项目对您有用，请扫描以下二维码打赏1元，多多支持 \
![afd](webwxgetmsgimg.jpeg)




# WaterMarkAndroid
Android平台Camera基于freetype和opencv实现预览拍照支持中文水印

本工程包括如下部分

一 主工程
     Camera采集数据,预览和拍照支持显示时间水印,位置水印,以及机型水印等 \
     集成百度地图获取具体位置信息

二 freetype子工程, 实现中文水印 \
  1 下载freetype库版本为2.10.1,地址为 \
     http://sourceforge.net/projects/freetype/files/freetype2/

  2 解压后将源码拷贝到freetype子工程jni目录下

  3 cd jni/freetype-2.10.1/

  4 编译toolchain \
	android-ndk-r17b/build/tools/make-standalone-toolchain.sh --platform=android-22 \
	   --install-dir=/home/zhongjihao/freetype/ --arch=arm --force
  
  5 设置环境变量PATH \
	export PATH=$PATH:/home/zhongjihao/freetype/bin \
	export CC=arm-linux-androideabi-gcc \
	export CXX=arm-linux-androideabi-g++

  6 配置freetype编译参数
 
    ./configure --host=arm-linux-androideabi --prefix=/freetype --without-zlib --with-png=no --with-harfbuzz=no

  7 编译,编译出来的文件会在jni/freetype-2.10.1/freetype/目录下 \
	  make -j4 \
	  make install DESTDIR=$(pwd)

  8 将编译出来的freetype目录下头文件拷贝到jni/include \
  9 将编译出来的freetype目录下的库文件libfreetype.so拷贝到jni/lib

  10 cd jni进入到jni目录下，执行ndk-build \
  11 AS编译运行即可
    

三 opencv_java子工程 \
  1 下载opencv-3.4.3-android-sdk版本，地址为 \
    https://jaist.dl.sourceforge.net/project/opencvlibrary/opencv-android

  2 解压,将解压后的sdk目录java/src/org全部拷贝到opencv_java子工程java目录下
  
  3 将sdk/native/libs/armeabi-v7a中的libopencv_java3.so拷贝到子工程freetype/src/main/jni/lib下

  4 将sdk/native/jni/include目录下opencv和opencv2全部拷贝到子工程freetype/src/main/jni/include下

运行截图 \
![afd](watermark.jpeg)
