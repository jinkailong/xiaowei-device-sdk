override SHARED:=yes

COMMON_INCLUDE:= \
	-I. \
	-I$(DIR_PROJ_ROOT)/include \
	-I$(DIR_PROJ_ROOT)/xweicontrol \
	-I$(DIR_PROJ_ROOT)/xweicontrol/App \
	-I$(DIR_PROJ_ROOT)/xweicontrol/Media \
	-I$(DIR_PROJ_ROOT)/xweicontrol/TXCModules \
	-I$(DIR_PROJ_ROOT)/library/rapidjson-1.1.0/include/rapidjson \
	-I$(DIR_PROJ_ROOT)/library/opus-1.2.1/include \
	-I$(DIR_PROJ_ROOT)/library/opus-1.2.1/silk \
	-I$(DIR_PROJ_ROOT)/library/opus-1.2.1/silk/float \
	-I$(DIR_PROJ_ROOT)/library/opus-1.2.1/silk/fixed \
	-I$(DIR_PROJ_ROOT)/library/opus-1.2.1/celt \
	-I$(DIR_PROJ_ROOT)/../../DeviceSDK/interface/linux \
	-I$(DIR_PROJ_ROOT)/library/easyJSON 


COMMON_CFLAGS:=-Wall -Os -fmessage-length=0 -g3 -fPIC -fvisibility=hidden -D_GNU_SOURCE
COMMON_CXXFLAGS:=-Wall -Os -fmessage-length=0 -g3 -fPIC -fvisibility=hidden -D_GNU_SOURCE

export ADD_CFLAGS:=
export ADD_CXXFLAGS:=

ADD_CFLAGS +=-DUSE_ALLOCA -DENABLE_OPUS -DOPUS_BUILD -DFIXED_POINT
ADD_CXXFLAGS+=-DUSE_ALLOCA -DENABLE_OPUS -DOPUS_BUILD -DFIXED_POINT

#############################################
# 根目录需要排除编译的子目录
EXCLUDE_DIRS+=

#############################################
# 根目录需要排除编译的源文件
EXCLUDE_SRCS+=



