#!/bin/bash

# Clones all the Pavanecce repositories
initializeWorkingDirAndScriptDir() {
    # Set working directory and remove all symbolic links
    workingDir=`pwd -P`

    # Go the script directory
    cd `dirname $0`
    # If the file itself is a symbolic link (ignoring parent directory links), then follow that link recursively
    # Note that scriptDir=`pwd -P` does not do that and cannot cope with a link directly to the file
    scriptFileBasename=`basename $0`
    while [ -L "$scriptFileBasename" ] ; do
        scriptFileBasename=`readlink $scriptFileBasename` # Follow the link
        cd `dirname $scriptFileBasename`
        scriptFileBasename=`basename $scriptFileBasename`
    done
    # Set script directory and remove other symbolic links (parent directory links)
    scriptDir=`pwd -P`
}
initializeWorkingDirAndScriptDir
pavanecceRootDir="$scriptDir/.."
startDateTime=`date +%s`
cd "$pavanecceRootDir"
for repository in `cat "${scriptDir}/repository-list.txt"` ; do
    echo
    if [ ! -d "$pavanecceRootDir/$repository" ]; then
        echo "==============================================================================="
        echo "Cloning repository: $repository"
        echo "==============================================================================="
        git clone git@github.com:ifu-lobuntu/$repository.git
        returnCode=$?
        if [ $returnCode != 0 ] ; then
            echo -n "Error cloning repository ${repository}. Continue? (Hit control-c to stop or enter to continue): "
            read ok
        fi
    else
        echo "==============================================================================="
        echo "Repository: $repository already cloned. Skipping..."
        echo "==============================================================================="
    fi
done

endDateTime=`date +%s`
spentSeconds=`expr $endDateTime - $startDateTime`

echo
echo "Total time: ${spentSeconds}s"
