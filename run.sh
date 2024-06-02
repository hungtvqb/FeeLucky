#!/bin/sh

scriptdir="/u01/lucky/LUCKY/FeeLucky"
java -Xmx1024m -Xms256m -cp $scriptdir/FeeLucky.jar:$scriptdir/lib/* mono.Start $scriptdir/ApplicationResources.properties >/dev/null 2>&1 &
