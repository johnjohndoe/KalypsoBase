# Fetch Sources
echo Updating source code
#rm -rf source
#mkdir source
#svn co http://kalypsobase.svn.sourceforge.net/svnroot/kalypsobase/trunk source

# Copy source
echo Cleanup build folder
#rm -rf build
#mkdir build
#mkdir build/features
#mkdir build/plugins
#echo Copying source to build folder
#rsync -ah source/ build/plugins/
#rsync -ah source/KalypsoBaseFeature build/features/

# Call Eclipse
eclipseInstall=/opt/eclipse_3.4.2
pdeBuildPluginVersion=3.4.1.R34x_v20081217

#rm -rf eclipse
export OPTS="-Dbuilder=$PWD/build/plugins/KalypsoBaseProduct/script -DbuildDirectory=$PWD/build -DbuildType=I -Dtimestamp=006 -DbaseLocation=$eclipseInstall"

mkdir eclipse
/opt/eclipse_3.4.2/eclipse -application org.eclipse.ant.core.antRunner -data eclipse/workspace -buildfile $eclipseInstall/plugins/org.eclipse.pde.build_$pdeBuildPluginVersion/scripts/productBuild/productBuild.xml $OPTS
