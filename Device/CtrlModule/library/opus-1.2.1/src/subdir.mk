#####################################
# 子目录makefile
# 只编译当前目录，不链接，需要链接可以在all中修改，并把all的init.subdir依赖去掉
#        all:$(ALL_OBJS_CUR_DIR) all.subdir
#                $(CC) -o $@ $(ALL_OBJS_CUR_DIR) $(LDFLAGS)
#####################################


# 当前目录需要排除编译的子目录
#EXCLUDE_DIRS:=bin lib include

# 当前目录需要排除编译的源文件
#EXCLUDE_SRCS:=foo.c test.cpp

include $(DIR_PROJ_ROOT)/makefile.env

#仅编译当前目录，不链接
all:init.subdir $(ALL_OBJS_CUR_DIR) all.subdir

