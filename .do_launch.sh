if [[ $# -ne 1 ]];then
	echo "only for internal use"
	exit 2
fi

echo "Launching SCEC-VDO..."

# define classpath to include all jar files inside of the 'lib' dir, except 'vtk.jar'. also include the 'classes' dir, and the passed in platform specific vtk.jar file
CLASSPATH=`find lib/ -maxdepth 1 -mindepth 1 -name '*.jar' | fgrep -v vtk.jar | tr '\n' ':'`
CLASSPATH="classes:$1:$CLASSPATH"

echo "=== CLASSPATH ==="
echo "$CLASSPATH"
echo "================="
echo

echo "Detecting memory requirements..."
# maxmimum memory in gigabytes. should be close to, but not over, total memory available
# can set externally with VDO_MEM_GB environmental variable
if [[ ! -z "$VDO_MEM_GB" ]];then
	MEM_GIGS=$VDO_MEM_GB
	echo "Using global environmental variable VDO_MEM_GB"
else
	echo "VDO_MEM_GB environmental variable is not set, will automatically detect maximum memory as 80% of total system memory"
	TOT_MEM=`free | grep Mem | awk '{print $2}'`
	TOT_MEM_MB=`expr $TOT_MEM / 1024`
	TARGET_MEM_MB=`expr $TOT_MEM_MB \* 8 / 10`
	MEM_GIGS=`expr $TARGET_MEM_MB / 1024`
fi
echo "     will use up to $MEM_GIGS GB of memory"
echo
if [[ -e $JAVA_HOME ]];then
	JAVA="$JAVA_HOME/bin/java"
else
	JAVA=`which java`
fi
echo "===== JAVA ======"
echo "Launching with: $JAVA"
$JAVA -version
echo "================="

$JAVA -splash:resources/SCECVDOlogo.png -Xms1G -Xmx${MEM_GIGS}G -cp $CLASSPATH org.scec.vtk.main.MainGUI
