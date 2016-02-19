#!/bin/sh

JENKINS_HOME=/var/jenkins_home_ca

# Mount JENKINS_HOME NFS share when environment variable is found
if [ "$JENKINS_HOME_NFS_PATH" != "" ]; then    
    # Current mount point
    mkdir -p $JENKINS_HOME
    mount $JENKINS_HOME_NFS_PATH $JENKINS_HOME
    echo "Mounted NFS share $JENKINS_HOME_NFS_PATH on $JENKINS_HOME"
fi

# Create Maven repository cache directory
mkdir /m2repo

# Cleanup if restarting container
rm -f /var/run/docker.pid

dockerd-entrypoint.sh "$@"

