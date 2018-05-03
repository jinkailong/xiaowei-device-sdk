
#################################################
#



override DIR_CUR:=$(shell pwd)

export PLATFORM:=
export ENABLESHARP:=
export ENABLEFILETRANSFER:=
export with_third:=
export TARGETS:=
export ADD_LDLIB:=

ifeq ($(PLATFORM),)
	PLATFORM:=android_x86_device
	PLTF_CONFIG_FILE:=$(DIR_CUR)/PlatformConfs/$(PLATFORM).conf
else
	PLTF_CONFIG_FILE:=$(DIR_CUR)/PlatformConfs/$(PLATFORM).conf
endif

all:
	$(MAKE) -C .. -f Project.mk DIR_BUILD_ROOT="$(DIR_CUR)/$(PLATFORM)" PLTF_CONFIG="$(PLTF_CONFIG_FILE)"

clean:
	$(MAKE) -C .. -f Project.mk DIR_BUILD_ROOT="$(DIR_CUR)/$(PLATFORM)" PLTF_CONFIG="$(PLTF_CONFIG_FILE)" clean
	@-rm -rf $(DIR_CUR)/$(PLATFORM)


rebuild:clean
	$(MAKE) -C .. -f Project.mk DIR_BUILD_ROOT="$(DIR_CUR)/$(PLATFORM)" PLTF_CONFIG="$(PLTF_CONFIG_FILE)"

#所有conf文件
CONF_EXTS = .conf

#取当前目录的全部conf文件
override CONF_CUR_DIR:=$(wildcard $(DIR_CUR)/PlatformConfs/*.conf)
override CONF_CUR_DIR:=$(notdir $(CONF_CUR_DIR:.conf=))
#override CONF_CUR_DIR:=$(filt-out .conf~,$(CONF_CUR_DIR))

#所有third.mk文件
override ALL_THIRD_MK:=$(wildcard $(DIR_CUR)/../third_part/third_make/*.third.mk)
override ALL_THIRD_MK:=$(notdir $(ALL_THIRD_MK:.third.mk=))

help:
	@echo ''
	@echo '*****************************************************************'
	@echo '* usage:                                                        *'
	@echo '*        make help                                      打印help*'
	@echo '*        简化make命令，可以不指明PLATFORM参数编译指定平台       *'
	@echo '*             如 make dashuai         make allwinner            *'
	@echo '*                make dashuai.clean   make allwinner.clean      *'
	@echo '*                make dashuai.rebuild make allwinner.rebuild    *'
	@echo '*        make PLATFORM=dashuai ENABLESHARP=no with_third="" ENABLEFILETRANSFER=no   *'
	@echo '*                 加载dashuai.conf，不编译sharp FileTransfer    *'
	@echo '*             PLATFORM:取值和xxx.conf的文件名一致               *'
	@echo '*             ENABLESHARP=no时不编译sharp 其他任意值都编译sharp *'
	@echo '*             ENABLEFILETRANSFER=no时不编译FileTransfer         *'
	@echo '*             with_third:编译第三方库，用空格分割               *'
	@echo '*                 支持编译的第三方库iconv openssl               *'
	@echo -n '*                          with_third="'
	@for trdmk in $(ALL_THIRD_MK); \
	do \
		echo -n $$trdmk" ";\
	done
	@echo "\b"'"'" *"
	@for trdmk in $(ALL_THIRD_MK); \
	do \
		echo  "*                          with_third="'"'$$trdmk'"'"   *";\
	done
	@for conf in $(CONF_CUR_DIR);\
	do \
		echo "*        make PLATFORM=$$conf    *" ;\
	done
	@echo '*                                                               *'
	@echo '*        make clean                       clean all build output*'
	@echo '*        make cleanall           clean all platform build output*'
	@echo '*        make deploy            自动部署makefile                *'
	@echo '*                               生成demo.conf makefile.env      *'
	@echo '*                               Project.mk 及根目录下的subdir.mk*'
	@echo '*        make deploy.conf                          生成demo.conf*'
	@echo '*        make deploy.subdir.mk               生成根目录subdir.mk*'
	@echo '*        make deploy.makefile.env               生成makefile.env*'
	@echo '*****************************************************************'
	@echo ''

%:$(DIR_CUR)/PlatformConfs/%.conf NoDepend
	$(MAKE) -C .. -f Project.mk DIR_BUILD_ROOT="$(DIR_CUR)/$*" PLTF_CONFIG="$(DIR_CUR)/PlatformConfs/$*.conf"

NoDepend:
	@echo "--------------------------------------------------------------------"

%.clean:
	$(MAKE) -C .. -f Project.mk DIR_BUILD_ROOT="$(DIR_CUR)/$*" PLTF_CONFIG="$(DIR_CUR)/PlatformConfs/$*.conf" clean
	@-rm -rf $(DIR_CUR)/$*

%.rebuild: %.clean
	$(MAKE) -C .. -f Project.mk DIR_BUILD_ROOT="$(DIR_CUR)/$*" PLTF_CONFIG="$(DIR_CUR)/PlatformConfs/$*.conf"

allplt:
	@for conf in $(CONF_CUR_DIR);\
	do \
	$(MAKE) -C .. -f Project.mk DIR_BUILD_ROOT="$(DIR_CUR)/$$conf" PLTF_CONFIG="$(DIR_CUR)/PlatformConfs/$$conf.conf" ;\
	done

cleanall:
	@for conf in $(CONF_CUR_DIR);\
	do \
	$(MAKE) -C .. -f Project.mk DIR_BUILD_ROOT="$(DIR_CUR)/$$conf" clean ;\
	rm -rf $(DIR_CUR)/$$conf ;\
	done

deploy: deploy.begin deploy.conf deploy.subdir.mk deploy.makefile.env deploy.Project.mk
	@echo "deploy end"

deploy.begin:
	@test -d $(DIR_CUR)/PlatformConfs || mkdir $(DIR_CUR)/PlatformConfs

deploy.conf:
	@echo "#交叉编译链">$(DIR_CUR)/PlatformConfs/demo.conf
	@echo "override CROSS:=">>$(DIR_CUR)/PlatformConfs/demo.conf
	@echo "">>$(DIR_CUR)/PlatformConfs/demo.conf
	@echo '#include $$(dir $$(PLTF_CONFIG))/common.make'>>$(DIR_CUR)/PlatformConfs/demo.conf
	@echo "">>$(DIR_CUR)/PlatformConfs/demo.conf
	@echo "#include路径">>$(DIR_CUR)/PlatformConfs/demo.conf
	@echo "override INCLUDEFLAGS:=">>$(DIR_CUR)/PlatformConfs/demo.conf
	@echo "">>$(DIR_CUR)/PlatformConfs/demo.conf
	@echo "#override INCLUDEFLAGS+=">>$(DIR_CUR)/PlatformConfs/demo.conf
	@echo "">>$(DIR_CUR)/PlatformConfs/demo.conf
	@echo "#link参数">>$(DIR_CUR)/PlatformConfs/demo.conf
	@echo "override LDPATH:=">>$(DIR_CUR)/PlatformConfs/demo.conf
	@echo "override LDLIB:=">>$(DIR_CUR)/PlatformConfs/demo.conf
	@echo "">>$(DIR_CUR)/PlatformConfs/demo.conf
	@echo "#C编译参数">>$(DIR_CUR)/PlatformConfs/demo.conf
	@echo "override CFLAGS:=">>$(DIR_CUR)/PlatformConfs/demo.conf
	@echo "">>$(DIR_CUR)/PlatformConfs/demo.conf
	@echo "#C++编译参数">>$(DIR_CUR)/PlatformConfs/demo.conf
	@echo "override CXXFLAGS:=">>$(DIR_CUR)/PlatformConfs/demo.conf
	@echo "deploy demo.conf OK"

deploy.subdir.mk:
	@echo "#############################################" > $(DIR_CUR)/../subdir.mk
	@echo "# 子目录makefile" >> $(DIR_CUR)/../subdir.mk
	@echo "# 只编译当前目录，不链接，需要链接可以在all中修改，并把all的init.subdir依赖去掉" >> $(DIR_CUR)/../subdir.mk
	@echo '#        all:$$(ALL_OBJS_CUR_DIR) all.subdir' >> $(DIR_CUR)/../subdir.mk
	@echo '#                @test -d $$(DIR_OBJ_OUTPUT) || mkdir -p $$(DIR_OBJ_OUTPUT)' >> $(DIR_CUR)/../subdir.mk
	@echo '#                $$(CC) -o $$@ $$(ALL_OBJS_CUR_DIR) $$(LDPATH) $$(LDLIB)' >> $(DIR_CUR)/../subdir.mk
	@echo "#############################################" >> $(DIR_CUR)/../subdir.mk
	@echo "" >> $(DIR_CUR)/../subdir.mk
	@echo "" >> $(DIR_CUR)/../subdir.mk
	@echo "# 当前目录需要排除编译的子目录" >> $(DIR_CUR)/../subdir.mk
	@echo "EXCLUDE_DIRS:=" >> $(DIR_CUR)/../subdir.mk
	@echo "" >> $(DIR_CUR)/../subdir.mk
	@echo "# 当前目录需要排除编译的源文件" >> $(DIR_CUR)/../subdir.mk
	@echo "EXCLUDE_SRCS:=" >> $(DIR_CUR)/../subdir.mk
	@echo "" >> $(DIR_CUR)/../subdir.mk
	@echo "#############################################" >> $(DIR_CUR)/../subdir.mk
	@echo "# 顶层makefile中引入可用的变量" >> $(DIR_CUR)/../subdir.mk
	@echo "" >> $(DIR_CUR)/../subdir.mk
	@echo "#CROSS" >> $(DIR_CUR)/../subdir.mk
	@echo "#CC" >> $(DIR_CUR)/../subdir.mk
	@echo "#CXX" >> $(DIR_CUR)/../subdir.mk
	@echo "#AR" >> $(DIR_CUR)/../subdir.mk
	@echo "#LD" >> $(DIR_CUR)/../subdir.mk
	@echo "#SHARED" >> $(DIR_CUR)/../subdir.mk
	@echo "#STATIC" >> $(DIR_CUR)/../subdir.mk
	@echo "#CFLAGS" >> $(DIR_CUR)/../subdir.mk
	@echo "#CXXFLAGS" >> $(DIR_CUR)/../subdir.mk
	@echo "#DIR_PROJ_ROOT" >> $(DIR_CUR)/../subdir.mk
	@echo "#INCLUDEFLAGS" >> $(DIR_CUR)/../subdir.mk
	@echo "#LDPATH" >> $(DIR_CUR)/../subdir.mk
	@echo "#LDLIB" >> $(DIR_CUR)/../subdir.mk
	@echo "#DIR_BUILD_ROOT" >> $(DIR_CUR)/../subdir.mk
	@echo "#DIR_OBJ" >> $(DIR_CUR)/../subdir.mk
	@echo "#DIR_BIN" >> $(DIR_CUR)/../subdir.mk
	@echo "#DIR_LIB" >> $(DIR_CUR)/../subdir.mk
	@echo "" >> $(DIR_CUR)/../subdir.mk
	@echo 'include $$(DIR_PROJ_ROOT)/makefile.env' >> $(DIR_CUR)/../subdir.mk
	@echo "" >> $(DIR_CUR)/../subdir.mk
	@echo "#############################################" >> $(DIR_CUR)/../subdir.mk
	@echo "# include makefile.env后，可用变量" >> $(DIR_CUR)/../subdir.mk
	@echo "" >> $(DIR_CUR)/../subdir.mk
	@echo "# 当前目录" >> $(DIR_CUR)/../subdir.mk
	@echo "#DIR_CUR" >> $(DIR_CUR)/../subdir.mk
	@echo "" >> $(DIR_CUR)/../subdir.mk
	@echo "# 当前目录的所有子目录" >> $(DIR_CUR)/../subdir.mk
	@echo "#ALL_SUB_DIRS" >> $(DIR_CUR)/../subdir.mk
	@echo "" >> $(DIR_CUR)/../subdir.mk
	@echo "# 当前目录的全部源文件" >> $(DIR_CUR)/../subdir.mk
	@echo "#SRCS_CUR_DIR" >> $(DIR_CUR)/../subdir.mk
	@echo "" >> $(DIR_CUR)/../subdir.mk
	@echo "# 当前目录的全部源文件对应.o & .d文件路径（.o为编译输出的obj文件，.d是编译依赖文件）" >> $(DIR_CUR)/../subdir.mk
	@echo "#ALL_OBJS_CUR_DIR" >> $(DIR_CUR)/../subdir.mk
	@echo "#ALL_DEP_CUR_DIR" >> $(DIR_CUR)/../subdir.mk
	@echo "" >> $(DIR_CUR)/../subdir.mk
	@echo "# 编译输出.o的目录，目录结构与源目录结构一致，只是根目录在DIR_OBJ" >> $(DIR_CUR)/../subdir.mk
	@echo "#DIR_OBJ_OUTPUT" >> $(DIR_CUR)/../subdir.mk
	@echo "" >> $(DIR_CUR)/../subdir.mk
	@echo "# 仅编译当前目录，不链接，需要的时候，可改写all的编译规则和依赖目标" >> $(DIR_CUR)/../subdir.mk
	@echo '# init.subdir会创建DIR_OBJ_OUTPUT，并把当前目录的.o编译结果写入$$(DIR_OBJ)/AllObj.txt，用于链接，防止多链接' >> $(DIR_CUR)/../subdir.mk
	@echo '# $$(ALL_OBJS_CUR_DIR)当前目录的全部源文件对应.o，make时会自动编译' >> $(DIR_CUR)/../subdir.mk
	@echo "# all.subdir继续遍历子目录make" >> $(DIR_CUR)/../subdir.mk
	@echo 'all:init.subdir $$(ALL_OBJS_CUR_DIR) all.subdir' >> $(DIR_CUR)/../subdir.mk
	@echo "" >> $(DIR_CUR)/../subdir.mk
	@echo "deploy subdir.mk OK"

deploy.Project.mk:
	@echo "#############################################" > $(DIR_CUR)/../Project.mk
	@echo "# root makefile" >> $(DIR_CUR)/../Project.mk
	@echo "# 使用的时候可以直接修改makefile或者如下方式使用：" >> $(DIR_CUR)/../Project.mk
	@echo '#        make CROSS=armv5tl-montavista-linux-gnueabi- DIR_BUILD_ROOT=$${PWD} TARGETS=$${PWD}/libXXX.so SHARED=yes' >> $(DIR_CUR)/../Project.mk
	@echo "#        > CROSS TARGETS SHARED STATIC CFLAGS CXXFLAGS INCLUDEFLAGS LDPATH LDLIB 可在make时指定" >> $(DIR_CUR)/../Project.mk
	@echo '#        > CROSS指定交叉编译链的前缀，DIR_BUILD_ROOT指明编译输出的objs，bin，lib目录，$${PWD}表示当前目录，TARGETS指明最终编译输出文件，SHARED指明这是编译的动态库' >> $(DIR_CUR)/../Project.mk
	@echo "#        > 编译中间产物 .o, .d都在objs目录" >> $(DIR_CUR)/../Project.mk
	@echo "#        > 变量section都可以在make的时候指定" >> $(DIR_CUR)/../Project.mk
	@echo "#" >> $(DIR_CUR)/../Project.mk
	@echo "#############################################" >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo "#############################################" >> $(DIR_CUR)/../Project.mk
	@echo "# 变量section 子目录下可用的变量(主要是交叉编译)" >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo "# 交叉编译链前缀" >> $(DIR_CUR)/../Project.mk
	@echo "export CROSS=" >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo 'export CC=$$(CROSS)gcc' >> $(DIR_CUR)/../Project.mk
	@echo 'export CXX=$$(CROSS)g++' >> $(DIR_CUR)/../Project.mk
	@echo 'export AR=$$(CROSS)ar' >> $(DIR_CUR)/../Project.mk
	@echo 'export LD=$$(CROSS)ld' >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo "export SHARED=no" >> $(DIR_CUR)/../Project.mk
	@echo "export STATIC=no" >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo "export CFLAGS=" >> $(DIR_CUR)/../Project.mk
	@echo "export CXXFLAGS=" >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo "# 工程根目录，此变量请勿修改" >> $(DIR_CUR)/../Project.mk
	@echo 'export DIR_PROJ_ROOT=$$(shell pwd)' >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo "# include 相关" >> $(DIR_CUR)/../Project.mk
	@echo "export INCLUDEFLAGS=" >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo "# link 相关" >> $(DIR_CUR)/../Project.mk
	@echo "export LDPATH:=" >> $(DIR_CUR)/../Project.mk
	@echo "export LDLIB:=" >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo "# 编译输出目录" >> $(DIR_CUR)/../Project.mk
	@echo 'export DIR_BUILD_ROOT=$$(shell pwd)/build' >> $(DIR_CUR)/../Project.mk
	@echo 'export DIR_OBJ=$$(DIR_BUILD_ROOT)/objs' >> $(DIR_CUR)/../Project.mk
	@echo 'export DIR_BIN=$$(DIR_BUILD_ROOT)/bin' >> $(DIR_CUR)/../Project.mk
	@echo 'export DIR_LIB=$$(DIR_BUILD_ROOT)/lib' >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo "# 变量section end" >> $(DIR_CUR)/../Project.mk
	@echo "#############################################" >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo "EMPTY=" >> $(DIR_CUR)/../Project.mk
	@echo 'SPACE=$$(EMPTY) $$(EMPTY)' >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo 'CUR_PATH_NAMES = $$(subst /,$$(SPACE),$$(subst $$(SPACE),_,$$(shell pwd)))' >> $(DIR_CUR)/../Project.mk
	@echo 'PROGRAM = $$(word $$(words $$(CUR_PATH_NAMES)),$$(CUR_PATH_NAMES))' >> $(DIR_CUR)/../Project.mk
	@echo "# 工程输出文件" >> $(DIR_CUR)/../Project.mk
	@echo "# TARGETS指定输出文件，可以在make时指定 或者在PLTF_CONFIG中指定" >> $(DIR_CUR)/../Project.mk
	@echo "TARGETS:=" >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo "# 工程配置，可以在工程配置中覆盖CC等交叉编译，CFLAGS等编译选项" >> $(DIR_CUR)/../Project.mk
	@echo "PLTF_CONFIG:=" >> $(DIR_CUR)/../Project.mk
	@echo 'ifneq ($$(PLTF_CONFIG),)' >> $(DIR_CUR)/../Project.mk
	@echo '	-include $$(PLTF_CONFIG)' >> $(DIR_CUR)/../Project.mk
	@echo "endif" >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo "# 生成编译输出文件名" >> $(DIR_CUR)/../Project.mk
	@echo 'ifeq ($$(STATIC), yes)' >> $(DIR_CUR)/../Project.mk
	@echo '	ifeq ($$(TARGETS),)' >> $(DIR_CUR)/../Project.mk
	@echo '		TARGETS:=$$(DIR_BUILD_ROOT)/lib/lib$$(PROGRAM).a' >> $(DIR_CUR)/../Project.mk
	@echo "	endif" >> $(DIR_CUR)/../Project.mk
	@echo "else" >> $(DIR_CUR)/../Project.mk
	@echo '	ifeq ($$(SHARED), yes)' >> $(DIR_CUR)/../Project.mk
	@echo '		ifeq ($$(TARGETS),)' >> $(DIR_CUR)/../Project.mk
	@echo '			TARGETS:=$$(DIR_BUILD_ROOT)/lib/lib$$(PROGRAM).so' >> $(DIR_CUR)/../Project.mk
	@echo "		endif" >> $(DIR_CUR)/../Project.mk
	@echo "	else" >> $(DIR_CUR)/../Project.mk
	@echo '		ifeq ($$(TARGETS),)' >> $(DIR_CUR)/../Project.mk
	@echo '			TARGETS:=$$(DIR_BUILD_ROOT)/bin/$$(PROGRAM)' >> $(DIR_CUR)/../Project.mk
	@echo "		endif" >> $(DIR_CUR)/../Project.mk
	@echo "	endif" >> $(DIR_CUR)/../Project.mk
	@echo "endif" >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo 'THIRD_TARGET:=$$(foreach t,$$(with_third),$$(t).third)' >> $(DIR_CUR)/../Project.mk
	@echo 'override LDPATH:=$$(foreach t,$$(with_third),-L$$(DIR_BUILD_ROOT)/$$(t)/lib) $$(LDPATH)' >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo 'all : init init.subdir all.subdir objs_cur_dir $$(THIRD_TARGET) $$(DIR_OBJ)/Target.mk' >> $(DIR_CUR)/../Project.mk
	@echo '	$$(MAKE) -C $$(DIR_OBJ) -f Target.mk STATIC='"'"'$$(STATIC)'"'"' SHARED='"'"'$$(SHARED)'"'" >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo "#############################################" >> $(DIR_CUR)/../Project.mk
	@echo "# 当前目录需要排除编译的子目录 可以在PLTF_CONFIG中指定" >> $(DIR_CUR)/../Project.mk
	@echo "EXCLUDE_DIRS+= " >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo "#############################################" >> $(DIR_CUR)/../Project.mk
	@echo "# 当前目录需要排除编译的源文件 可以在PLTF_CONFIG中指定" >> $(DIR_CUR)/../Project.mk
	@echo "EXCLUDE_SRCS+= " >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo 'include $$(DIR_PROJ_ROOT)/makefile.env' >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo "init :" >> $(DIR_CUR)/../Project.mk
	@echo '	@rm -rf $$(DIR_OBJ)/AllObj.txt $$(DIR_OBJ)/Target.mk' >> $(DIR_CUR)/../Project.mk
	@echo '	@test -d $$(DIR_BIN) || mkdir -p $$(DIR_BIN)' >> $(DIR_CUR)/../Project.mk
	@echo '	@test -d $$(DIR_OBJ) || mkdir -p $$(DIR_OBJ)' >> $(DIR_CUR)/../Project.mk
	@echo '	@test -d $$(DIR_LIB) || mkdir -p $$(DIR_LIB)' >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo 'objs_cur_dir:$$(ALL_OBJS_CUR_DIR)' >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo '$$(DIR_OBJ)/Target.mk:' >> $(DIR_CUR)/../Project.mk
	@echo '	@echo '"'"'all : $$(TARGETS)'"'"' > $$@' >> $(DIR_CUR)/../Project.mk
	@echo '	@echo "" >> $$@' >> $(DIR_CUR)/../Project.mk
	@echo '	@echo '"'"'$$(TARGETS) : $$$$(shell cat $$$$(DIR_OBJ)/AllObj.txt)'"'"' >> $$@' >> $(DIR_CUR)/../Project.mk
	@echo '	@echo '"'"'ifeq ($$$$(STATIC), yes)'"'"' >> $$@' >> $(DIR_CUR)/../Project.mk
	@echo '	@echo '"'"'	$$$$(AR) rcs $$$$@ $$$$^'"'"' >> $$@' >> $(DIR_CUR)/../Project.mk
	@echo '	@echo '"'"'else'"'"' >> $$@' >> $(DIR_CUR)/../Project.mk
	@echo '	@echo '"'"'ifeq ($$$$(SHARED), yes)'"'"' >> $$@' >> $(DIR_CUR)/../Project.mk
	@echo '	@echo '"'"'	$$$$(CXX) -o $$$$@ $$$$^ $$$$(LDPATH) $$$$(LDLIB) -qwer -shared'"'"' >> $$@' >> $(DIR_CUR)/../Project.mk
	@echo '	@echo '"'"'else'"'"' >> $$@' >> $(DIR_CUR)/../Project.mk
	@echo '	@echo '"'"'	$$$$(CXX) -o $$$$@ $$$$^ $$$$(LDPATH) $$$$(LDLIB)'"'"' >> $$@' >> $(DIR_CUR)/../Project.mk
	@echo '	@echo '"'"'endif'"'"' >> $$@' >> $(DIR_CUR)/../Project.mk
	@echo '	@echo '"'"'endif'"'"' >> $$@' >> $(DIR_CUR)/../Project.mk
	@echo '	@echo '"'"''"'"' >> $$@' >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo "%.third:" >> $(DIR_CUR)/../Project.mk
	@echo '	$$(MAKE) -C $$(DIR_PROJ_ROOT)/third_part/third_make -f $$*.third.mk' >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo ".PHONY:clean" >> $(DIR_CUR)/../Project.mk
	@echo "clean:" >> $(DIR_CUR)/../Project.mk
	@echo '	rm -rf $$(DIR_OBJ) $$(DIR_BIN) $$(DIR_LIB) $$(TARGETS)' >> $(DIR_CUR)/../Project.mk
	@echo "" >> $(DIR_CUR)/../Project.mk
	@echo "deploy Project.mk OK"


deploy.makefile.env:
	@echo "" > $(DIR_CUR)/../makefile.env
	@echo "#取当前目录" >> $(DIR_CUR)/../makefile.env
	@echo 'override DIR_CUR=$$(shell pwd)' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo "#取所有字目录，排除当前目录 & EXCLUDE_DIRS" >> $(DIR_CUR)/../makefile.env
	@echo 'override ALL_SUB_DIRS:=$$(shell find $$(DIR_CUR) -maxdepth 1 -type d)' >> $(DIR_CUR)/../makefile.env
	@echo 'override ALL_SUB_DIRS:=$$(filter-out $$(addprefix $$(DIR_CUR)/,$$(notdir $$(EXCLUDE_DIRS))),$$(ALL_SUB_DIRS))' >> $(DIR_CUR)/../makefile.env
	@echo 'override ALL_SUB_DIRS:=$$(filter-out $$(DIR_CUR),$$(ALL_SUB_DIRS))' >> $(DIR_CUR)/../makefile.env
	@echo 'override ALL_SUB_DIRS_TRG:=$$(addsuffix .dirbuild,$$(ALL_SUB_DIRS))' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo "#所有支持的源文件后缀" >> $(DIR_CUR)/../makefile.env
	@echo "SRCEXTS = .c .C .cc .cpp .CPP .c++ .cxx .cp" >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo "#取当前目录的全部源文件" >> $(DIR_CUR)/../makefile.env
	@echo 'override SRCS_CUR_DIR:=$$(foreach d,$$(DIR_CUR),$$(wildcard $$(addprefix $$(d)/*,$$(SRCEXTS))))' >> $(DIR_CUR)/../makefile.env
	@echo 'override SRCS_CUR_DIR:=$$(filter-out $$(addprefix $$(DIR_CUR)/,$$(notdir $$(EXCLUDE_SRCS))),$$(SRCS_CUR_DIR))' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo "#生成.o & .d文件路径（.o为编译输出的obj文件，.d是编译依赖文件）" >> $(DIR_CUR)/../makefile.env
	@echo 'override ALL_OBJS_CUR_DIR=$$(subst $$(DIR_PROJ_ROOT),$$(DIR_OBJ),$$(addsuffix .o, $$(basename $$(SRCS_CUR_DIR))))' >> $(DIR_CUR)/../makefile.env
	@echo 'override ALL_DEP_CUR_DIR=$$(ALL_OBJS_CUR_DIR:.o=.d)' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo "#使编译输出的.o目录结构和工程目录一致，防止同名文件的冲突" >> $(DIR_CUR)/../makefile.env
	@echo 'override DIR_OBJ_OUTPUT=$$(subst $$(DIR_PROJ_ROOT),$$(DIR_OBJ),$$(DIR_CUR))' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo ".PHONY : all" >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo "#子目录make" >> $(DIR_CUR)/../makefile.env
	@echo "%.subdir:" >> $(DIR_CUR)/../makefile.env
	@echo '	@for dir in $$(ALL_SUB_DIRS);\\' >> $(DIR_CUR)/../makefile.env
	@echo "	do \\" >> $(DIR_CUR)/../makefile.env
	@echo '	if test -e $$$$dir/subdir.mk; \\' >> $(DIR_CUR)/../makefile.env
	@echo '	then $$(MAKE) -C $$$$dir -f subdir.mk $$* ||exit 1; fi;\\' >> $(DIR_CUR)/../makefile.env
	@echo "	done" >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo 'all.subdir: $$(ALL_SUB_DIRS_TRG)' >> $(DIR_CUR)/../makefile.env
	@echo '	@echo $$(MAKE) target: $$@ in $$(DIR_CUR) complete!' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo "%.dirbuild:" >> $(DIR_CUR)/../makefile.env
	@echo '	if test -e $$*/subdir.mk; then $$(MAKE) -C $$* -f subdir.mk all ||exit 1; fi;' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo "#包含.d依赖文件" >> $(DIR_CUR)/../makefile.env
	@echo '-include $$(ALL_DEP_CUR_DIR)' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo "#.d文件生成规则，SRCEXTS修改需要添加此部分内容" >> $(DIR_CUR)/../makefile.env
	@echo '$$(DIR_OBJ_OUTPUT)/%.d:$$(DIR_CUR)/%.c' >> $(DIR_CUR)/../makefile.env
	@echo '	@test -d $$(DIR_OBJ_OUTPUT) || mkdir -p $$(DIR_OBJ_OUTPUT)' >> $(DIR_CUR)/../makefile.env
	@echo '	@set -e; rm -f $$@; $$(CC) -MM $$< $$(CFLAGS) $$(ADD_CFLAGS) $$(INCLUDEFLAGS) > $$@.$$$$$$$$; \\' >> $(DIR_CUR)/../makefile.env
	@echo '	sed '"'"'s,\($$*\)\.o[ :]*,$$(DIR_OBJ_OUTPUT)/\\1.o $$@ : ,g'"'"' < $$@.$$$$$$$$ > $$@; \\' >> $(DIR_CUR)/../makefile.env
	@echo '	rm -f $$@.$$$$$$$$' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo '$$(DIR_OBJ_OUTPUT)/%.d:$$(DIR_CUR)/%.C' >> $(DIR_CUR)/../makefile.env
	@echo '	@test -d $$(DIR_OBJ_OUTPUT) || mkdir -p $$(DIR_OBJ_OUTPUT)' >> $(DIR_CUR)/../makefile.env
	@echo '	@set -e; rm -f $$@; $$(CXX) -MM $$< $$(CXXFLAGS) $$(ADD_CXXFLAGS) $$(INCLUDEFLAGS) > $$@.$$$$$$$$; \\' >> $(DIR_CUR)/../makefile.env
	@echo '	sed '"'"'s,\($$*\)\.o[ :]*,$$(DIR_OBJ_OUTPUT)/\\1.o $$@ : ,g'"'"' < $$@.$$$$$$$$ > $$@; \\' >> $(DIR_CUR)/../makefile.env
	@echo '	rm -f $$@.$$$$$$$$' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo '$$(DIR_OBJ_OUTPUT)/%.d:$$(DIR_CUR)/%.cc' >> $(DIR_CUR)/../makefile.env
	@echo '	@test -d $$(DIR_OBJ_OUTPUT) || mkdir -p $$(DIR_OBJ_OUTPUT)' >> $(DIR_CUR)/../makefile.env
	@echo '	@set -e; rm -f $$@; $$(CXX) -MM $$< $$(CXXFLAGS) $$(ADD_CXXFLAGS) $$(INCLUDEFLAGS) > $$@.$$$$$$$$; \\' >> $(DIR_CUR)/../makefile.env
	@echo '	sed '"'"'s,\($$*\)\.o[ :]*,$$(DIR_OBJ_OUTPUT)/\\1.o $$@ : ,g'"'"' < $$@.$$$$$$$$ > $$@; \\' >> $(DIR_CUR)/../makefile.env
	@echo '	rm -f $$@.$$$$$$$$' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo '$$(DIR_OBJ_OUTPUT)/%.d:$$(DIR_CUR)/%.cpp' >> $(DIR_CUR)/../makefile.env
	@echo '	@test -d $$(DIR_OBJ_OUTPUT) || mkdir -p $$(DIR_OBJ_OUTPUT)' >> $(DIR_CUR)/../makefile.env
	@echo '	@set -e; rm -f $$@; $$(CXX) -MM $$< $$(CXXFLAGS) $$(ADD_CXXFLAGS) $$(INCLUDEFLAGS) > $$@.$$$$$$$$; \\' >> $(DIR_CUR)/../makefile.env
	@echo '	sed '"'"'s,\($$*\)\.o[ :]*,$$(DIR_OBJ_OUTPUT)/\\1.o $$@ : ,g'"'"' < $$@.$$$$$$$$ > $$@; \\' >> $(DIR_CUR)/../makefile.env
	@echo '	rm -f $$@.$$$$$$$$' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo '$$(DIR_OBJ_OUTPUT)/%.d:$$(DIR_CUR)/%.CPP' >> $(DIR_CUR)/../makefile.env
	@echo '	@test -d $$(DIR_OBJ_OUTPUT) || mkdir -p $$(DIR_OBJ_OUTPUT)' >> $(DIR_CUR)/../makefile.env
	@echo '	@set -e; rm -f $$@; $$(CXX) -MM $$< $$(CXXFLAGS) $$(ADD_CXXFLAGS) $$(INCLUDEFLAGS) > $$@.$$$$$$$$; \\' >> $(DIR_CUR)/../makefile.env
	@echo '	sed '"'"'s,\($$*\)\.o[ :]*,$$(DIR_OBJ_OUTPUT)/\\1.o $$@ : ,g'"'"' < $$@.$$$$$$$$ > $$@; \\' >> $(DIR_CUR)/../makefile.env
	@echo '	rm -f $$@.$$$$$$$$' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo '$$(DIR_OBJ_OUTPUT)/%.d:$$(DIR_CUR)/%.c++' >> $(DIR_CUR)/../makefile.env
	@echo '	@test -d $$(DIR_OBJ_OUTPUT) || mkdir -p $$(DIR_OBJ_OUTPUT)' >> $(DIR_CUR)/../makefile.env
	@echo '	@set -e; rm -f $$@; $$(CXX) -MM $$< $$(CXXFLAGS) $$(ADD_CXXFLAGS) $$(INCLUDEFLAGS) > $$@.$$$$$$$$; \\' >> $(DIR_CUR)/../makefile.env
	@echo '	sed '"'"'s,\($$*\)\.o[ :]*,$$(DIR_OBJ_OUTPUT)/\\1.o $$@ : ,g'"'"' < $$@.$$$$$$$$ > $$@; \\' >> $(DIR_CUR)/../makefile.env
	@echo '	rm -f $$@.$$$$$$$$' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo '$$(DIR_OBJ_OUTPUT)/%.d:$$(DIR_CUR)/%.cxx' >> $(DIR_CUR)/../makefile.env
	@echo '	@test -d $$(DIR_OBJ_OUTPUT) || mkdir -p $$(DIR_OBJ_OUTPUT)' >> $(DIR_CUR)/../makefile.env
	@echo '	@set -e; rm -f $$@; $$(CXX) -MM $$< $$(CXXFLAGS) $$(ADD_CXXFLAGS) $$(INCLUDEFLAGS) > $$@.$$$$$$$$; \\' >> $(DIR_CUR)/../makefile.env
	@echo '	sed '"'"'s,\($$*\)\.o[ :]*,$$(DIR_OBJ_OUTPUT)/\\1.o $$@ : ,g'"'"' < $$@.$$$$$$$$ > $$@; \\' >> $(DIR_CUR)/../makefile.env
	@echo '	rm -f $$@.$$$$$$$$' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo '$$(DIR_OBJ_OUTPUT)/%.d:$$(DIR_CUR)/%.cp' >> $(DIR_CUR)/../makefile.env
	@echo '	@test -d $$(DIR_OBJ_OUTPUT) || mkdir -p $$(DIR_OBJ_OUTPUT)' >> $(DIR_CUR)/../makefile.env
	@echo '	@set -e; rm -f $$@; $$(CXX) -MM $$< $$(CXXFLAGS) $$(ADD_CXXFLAGS) $$(INCLUDEFLAGS) > $$@.$$$$$$$$; \\' >> $(DIR_CUR)/../makefile.env
	@echo '	sed '"'"'s,\($$*\)\.o[ :]*,$$(DIR_OBJ_OUTPUT)/\\1.o $$@ : ,g'"'"' < $$@.$$$$$$$$ > $$@; \\' >> $(DIR_CUR)/../makefile.env
	@echo '	rm -f $$@.$$$$$$$$' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo "#.o文件编译规则，SRCEXTS修改需要添加此部分内容" >> $(DIR_CUR)/../makefile.env
	@echo '$$(DIR_OBJ_OUTPUT)/%.o:$$(DIR_CUR)/%.c' >> $(DIR_CUR)/../makefile.env
	@echo '	$$(CC) -c $$< $$(CFLAGS) $$(ADD_CFLAGS) $$(INCLUDEFLAGS) -o $$@' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo '$$(DIR_OBJ_OUTPUT)/%.o:$$(DIR_CUR)/%.C' >> $(DIR_CUR)/../makefile.env
	@echo '	$$(CXX) -c $$< $$(CXXFLAGS) $$(ADD_CXXFLAGS) $$(INCLUDEFLAGS) -o $$@' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo '$$(DIR_OBJ_OUTPUT)/%.o:$$(DIR_CUR)/%.cc' >> $(DIR_CUR)/../makefile.env
	@echo '	$$(CXX) -c $$< $$(CXXFLAGS) $$(ADD_CXXFLAGS) $$(INCLUDEFLAGS) -o $$@' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo '$$(DIR_OBJ_OUTPUT)/%.o:$$(DIR_CUR)/%.cpp' >> $(DIR_CUR)/../makefile.env
	@echo '	$$(CXX) -c $$< $$(CXXFLAGS) $$(ADD_CXXFLAGS) $$(INCLUDEFLAGS) -o $$@' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo '$$(DIR_OBJ_OUTPUT)/%.o:$$(DIR_CUR)/%.CPP' >> $(DIR_CUR)/../makefile.env
	@echo '	$$(CXX) -c $$< $$(CXXFLAGS) $$(ADD_CXXFLAGS) $$(INCLUDEFLAGS) -o $$@' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo '$$(DIR_OBJ_OUTPUT)/%.o:$$(DIR_CUR)/%.c++' >> $(DIR_CUR)/../makefile.env
	@echo '	$$(CXX) -c $$< $$(CXXFLAGS) $$(ADD_CXXFLAGS) $$(INCLUDEFLAGS) -o $$@' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo '$$(DIR_OBJ_OUTPUT)/%.o:$$(DIR_CUR)/%.CXX' >> $(DIR_CUR)/../makefile.env
	@echo '	$$(CXX) -c $$< $$(CXXFLAGS) $$(ADD_CXXFLAGS) $$(INCLUDEFLAGS) -o $$@' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo '$$(DIR_OBJ_OUTPUT)/%.o:$$(DIR_CUR)/%.cp' >> $(DIR_CUR)/../makefile.env
	@echo '	$$(CXX) -c $$< $$(CXXFLAGS) $$(ADD_CXXFLAGS) $$(INCLUDEFLAGS) -o $$@' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo "init.subdir :" >> $(DIR_CUR)/../makefile.env
	@echo '	@test -d $$(DIR_OBJ_OUTPUT) || mkdir -p $$(DIR_OBJ_OUTPUT)' >> $(DIR_CUR)/../makefile.env
	@echo '	@echo $$(ALL_OBJS_CUR_DIR) >> $$(DIR_OBJ)/AllObj.txt' >> $(DIR_CUR)/../makefile.env
	@echo "" >> $(DIR_CUR)/../makefile.env
	@echo "deploy makefile.env OK"


