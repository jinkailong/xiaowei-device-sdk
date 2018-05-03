#############################################
# root makefile
# 使用的时候可以直接修改makefile或者如下方式使用：
#        make CROSS=armv5tl-montavista-linux-gnueabi- DIR_BUILD_ROOT=${PWD} TARGETS=${PWD}/libXXX.so SHARED=yes
#        > CROSS TARGETS SHARED STATIC CFLAGS CXXFLAGS INCLUDEFLAGS LDPATH LDLIB 可在make时指定
#        > CROSS指定交叉编译链的前缀，DIR_BUILD_ROOT指明编译输出的objs，bin，lib目录，${PWD}表示当前目录，TARGETS指明最终编译输出文件，SHARED指明这是编译的动态库
#        > 编译中间产物 .o, .d都在objs目录
#        > 变量section都可以在make的时候指定
#
#############################################

#############################################
# 变量section 子目录下可用的变量(主要是交叉编译)

# 交叉编译链前缀
export CROSS=

# use cross own define param 
ifeq ($(OVERRIDE_CROSS), yes)
else
    export CC=$(CROSS)gcc
    export CXX=$(CROSS)g++
    export AR=$(CROSS)ar
    export LD=$(CROSS)ld
endif

export SHARED=no
export STATIC=no

export CFLAGS=
export CXXFLAGS=

# 工程根目录，此变量请勿修改
export DIR_PROJ_ROOT=$(shell pwd)

# include 相关
export INCLUDEFLAGS=

# link 相关
export LDPATH:=
export LDLIB:=

# 编译输出目录
export DIR_BUILD_ROOT=$(shell pwd)/build
export DIR_OBJ=$(DIR_BUILD_ROOT)/objs
export DIR_BIN=$(DIR_BUILD_ROOT)/bin
export DIR_LIB=$(DIR_BUILD_ROOT)/lib

# 变量section end
#############################################


EMPTY=
SPACE=$(EMPTY) $(EMPTY)

CUR_PATH_NAMES = $(subst /,$(SPACE),$(subst $(SPACE),_,$(shell pwd)))
PROGRAM = $(word $(words $(CUR_PATH_NAMES)),$(CUR_PATH_NAMES))
# 工程输出文件
# TARGETS指定输出文件，可以在make时指定 或者在PLTF_CONFIG中指定
TARGETS:=

# 工程配置，可以在工程配置中覆盖CC等交叉编译，CFLAGS等编译选项
PLTF_CONFIG:=
ifneq ($(PLTF_CONFIG),)
	-include $(PLTF_CONFIG)
endif

# 生成编译输出文件名
ifeq ($(STATIC), yes)
	ifeq ($(TARGETS),)
		TARGETS:=$(DIR_BUILD_ROOT)/lib/lib$(PROGRAM).a
	endif
else
	ifeq ($(SHARED), yes)
		ifeq ($(TARGETS),)
			TARGETS:=$(DIR_BUILD_ROOT)/lib/lib$(PROGRAM).so
		endif
	else
		ifeq ($(TARGETS),)
			TARGETS:=$(DIR_BUILD_ROOT)/bin/$(PROGRAM)
		endif
	endif
endif

THIRD_TARGET:=$(foreach t,$(with_third),$(t).third)
override LDPATH:=$(foreach t,$(with_third),-L$(DIR_BUILD_ROOT)/$(t)/lib) $(LDPATH)

all : init init.subdir all.subdir objs_cur_dir $(THIRD_TARGET) $(DIR_OBJ)/Target.mk
	$(MAKE) -C $(DIR_OBJ) -f Target.mk STATIC='$(STATIC)' SHARED='$(SHARED)'

#############################################
# 当前目录需要排除编译的子目录 可以在PLTF_CONFIG中指定
EXCLUDE_DIRS+= 

#############################################
# 当前目录需要排除编译的源文件 可以在PLTF_CONFIG中指定
EXCLUDE_SRCS+= 

include $(DIR_PROJ_ROOT)/makefile.env
export LIBTXDEVICE:=$(ADD_LDLIB)

init :
	@rm -rf $(DIR_OBJ)/AllObj.txt $(DIR_OBJ)/Target.mk
	@test -d $(DIR_BIN) || mkdir -p $(DIR_BIN)
	@test -d $(DIR_OBJ) || mkdir -p $(DIR_OBJ)
	@test -d $(DIR_LIB) || mkdir -p $(DIR_LIB)

objs_cur_dir:$(ALL_OBJS_CUR_DIR)

$(DIR_OBJ)/Target.mk:
	@echo 'all : $(TARGETS)' > $@
	@echo "" >> $@
	@echo '$(TARGETS) : $$(shell cat $$(DIR_OBJ)/AllObj.txt)' >> $@
	@echo 'ifeq ($$(STATIC), yes)' >> $@
	@echo '	$$(AR) rcs $$@ $$^' >> $@
	@echo 'else' >> $@
	@echo 'ifeq ($$(SHARED), yes)' >> $@
	@echo '	$$(CXX) -o $$@ $$^ $$(LDPATH) $$(LIBTXDEVICE) $$(LDLIB) -fPIC -shared' >> $@
	@echo 'else' >> $@
	@echo '	$$(CXX) -o $$@ $$^ $$(LDPATH) $$(LIBTXDEVICE) $$(LDLIB) -fPIC' >> $@
	@echo 'endif' >> $@
	@echo 'endif' >> $@
	@echo '' >> $@

%.third:
	$(MAKE) -C $(DIR_PROJ_ROOT)/third_part/third_make -f $*.third.mk

.PHONY:clean
clean:
	rm -rf $(DIR_OBJ) $(DIR_BIN) $(DIR_LIB) $(TARGETS)

