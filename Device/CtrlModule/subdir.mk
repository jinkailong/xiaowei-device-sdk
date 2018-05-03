#############################################
# 子目录makefile
# 只编译当前目录，不链接，需要链接可以在all中修改，并把all的init.subdir依赖去掉
#        all:$(ALL_OBJS_CUR_DIR) all.subdir
#                @test -d $(DIR_OBJ_OUTPUT) || mkdir -p $(DIR_OBJ_OUTPUT)
#                $(CC) -o $@ $(ALL_OBJS_CUR_DIR) $(LDPATH) $(LDLIB)
#############################################


# 当前目录需要排除编译的子目录
EXCLUDE_DIRS:=

# 当前目录需要排除编译的源文件
EXCLUDE_SRCS:=

#############################################
# 顶层makefile中引入可用的变量

#CROSS
#CC
#CXX
#AR
#LD
#SHARED
#STATIC
#CFLAGS
#CXXFLAGS
#DIR_PROJ_ROOT
#INCLUDEFLAGS
#LDPATH
#LDLIB
#DIR_BUILD_ROOT
#DIR_OBJ
#DIR_BIN
#DIR_LIB

include $(DIR_PROJ_ROOT)/makefile.env

#############################################
# include makefile.env后，可用变量

# 当前目录
#DIR_CUR

# 当前目录的所有子目录
#ALL_SUB_DIRS

# 当前目录的全部源文件
#SRCS_CUR_DIR

# 当前目录的全部源文件对应.o & .d文件路径（.o为编译输出的obj文件，.d是编译依赖文件）
#ALL_OBJS_CUR_DIR
#ALL_DEP_CUR_DIR

# 编译输出.o的目录，目录结构与源目录结构一致，只是根目录在DIR_OBJ
#DIR_OBJ_OUTPUT

# 仅编译当前目录，不链接，需要的时候，可改写all的编译规则和依赖目标
# init.subdir会创建DIR_OBJ_OUTPUT，并把当前目录的.o编译结果写入$(DIR_OBJ)/AllObj.txt，用于链接，防止多链接
# $(ALL_OBJS_CUR_DIR)当前目录的全部源文件对应.o，make时会自动编译
# all.subdir继续遍历子目录make
all:init.subdir $(ALL_OBJS_CUR_DIR) all.subdir

