echo "Launching SCEC-VDO..."

# Define classpath to include all jar files inside of the 'lib' dir and the 'classes' dir.
CLASSPATH=`find lib/ -maxdepth 1 -mindepth 1 -name '*.jar' | tr '\n' ':'`
CLASSPATH="classes:$CLASSPATH"

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
	MEM_GIGS=`expr $(./.get_memory.sh) \* 8 / 10`
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
