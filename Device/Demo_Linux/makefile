#if compile x86 demo on unbuntu 64.
#export LD_LIBRARY_PATH=../CtrlModule/controlCxx/build/x86/lib:../../DeviceSDK/release/linux/x86

TARGETS := ./DeviceDemo
OBJS	:= $(patsubst %.cpp, %.o, $(wildcard *.cpp))
OBJS	+= $(patsubst %.c, %.o, $(wildcard *.c))

#如果您需要编译嵌入式平台下的libnassdk.so, 请设置CROSS变量
CROSS   := 


#需要包含的头文件搜索路径
INCLUDE += -I./
INCLUDE += -I../../DeviceSDK/interface/linux
INCLUDE += -I../CtrlModule/include

#需要引用的库
LIB     += -ldl 
LIB     += -L. -lstdc++ -lpthread -lCtrlModule -lxiaoweiSDK
LIB		+= -L../../DeviceSDK/release/linux/x86
LIB		+= -L../CtrlModule/build/x86/lib
LIB     += -lasound -lrt

#需要的标志位
CC	:= $(CROSS)gcc
CXX	:= $(CROSS)g++
STRIP	:= $(CROSS)strip
CFLAGS  += -fdiagnostics-color  -fPIC -g -D_X86_
CXXFLAGS+= -fdiagnostics-color  -fpermissive -Wall -Wno-deprecated -Wno-conversion-null -fpermissive -Wno-format  -std=c++0x  -fPIC -lstdc++ -g -D_X86_

#增加根据d文件自动推导编译的标志
CFLAGS += $(INCLUDE)
CXXFLAGS += $(INCLUDE)

#deps
DEPS    += $(patsubst %.o, %.d, $(OBJS))

.PHONY: all
all: $(DEPS) $(OBJS)
	$(CXX) -o $(TARGETS) $(OBJS) $(LIB)
	
-include $(DEPS)

%.d: %.cpp
	@echo "make $@"
	@set -e $@ &&  rm -f $@; \
	$(CXX) -MM $(CFLAGS) $(CXXFLAGS) $< > $@; \

%.d: %.c
	$(CC) -MM $(CFLAGS) -c $< -o $@

.PHONY:clean
clean:
	rm -rf $(TARGETS) $(OBJS) $(DEPS)

