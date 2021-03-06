#!/bin/bash

##
# The MIT License
#
# Copyright (c) 2015, CloudBees, Inc.
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.
##

#
# Install a private registry that can be used by the demo to push images to.
#

################### START Commented out since we don't need local registry

#echo '*************** Installing a local Docker Registry Service for the demo ***************'
#echo '***************            Please sit tight for a minute                ***************'

#REG_SETUP_PATH=/tmp/files/regup

#docker run -d --name registry --restart=always registry:0.9.1
#docker run -d -p 443:443 --name wf-registry-proxy -v $REG_SETUP_PATH:/etc/nginx/conf.d/ -v $REG_SETUP_PATH/sec:/var/registry/certs --link registry:registry nginx:1.9.0

#echo '***************         Docker Registry Service running now             ***************'

# In case some tagged images were left over from a previous run using a cache:
#(docker images -q examplecorp/spring-petclinic; docker images -q docker.example.com/examplecorp/spring-petclinic) | xargs docker rmi --no-prune=true --force

################### END

# quaha01:
# Appformer package sets an NFS mount point that we can't change.
# Perform a one-time create of a soft link to the NFS mount point.
# This will occur for each new container/POD.
if [ "$DSS_LOCALMOUNTPOINT_JENKINS" ] && [ -e /var/firststart ]
then
    rm -f /var/firststart
    ln -s -f $DSS_LOCALMOUNTPOINT_JENKINS $JENKINS_HOME
    echo "Created soft link from $JENKINS_HOME to NFS mount point $DSS_LOCALMOUNTPOINT_JENKINS"
fi

#
# Remove the base workflow-demo "cd" job
#
rm -rf /usr/share/jenkins/ref/jobs/cd $JENKINS_HOME/jobs/cd

#
# Now run Jenkins.
#
#
/usr/local/bin/run.sh
