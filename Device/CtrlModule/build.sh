#build xweicontrol
echo "**************************************************************************************************"
echo "****************************       build xiaowei_control          *****************************"
echo "**************************************************************************************************"
XWEI_PROGDIR=$(cd $(dirname $0); pwd -P)
XWEICONTROLBUILD=$XWEI_PROGDIR/build

if [ "$1" == "x86" ]; then
    BUILDCONFIG=android_x86_device
else
    BUILDCONFIG=android_arm_device
fi;

cd $XWEICONTROLBUILD
make -j16 PLATFORM=$BUILDCONFIG  ENABLESHARP=no ENABLEFILETRANSFER=no LANCOMMUNICATION=yes MAIN_VERSION=$2 SUB_VERSION=$3 BUILD_NUMBER=$4

if [ $? -ne 0 ]; then
exit -1;
fi

mkdir -p $XWEI_PROGDIR/out/$1

cp $XWEICONTROLBUILD/$BUILDCONFIG/lib/libCtrlModule.so $XWEI_PROGDIR/out/$1/libCtrlModule.so

echo "**************************************************************************************************"
echo "***************************        build xiaowei_control finish         **************************"
echo "**************************************************************************************************"
