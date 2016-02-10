#!/bin/sh

# Mount JENKINS_HOME NFS share when environment variable is found
if [ "$JENKINS_HOME_NFS_PATH" != "" ]; then
    mkdir -p /var/jenkins_home
	mount $JENKINS_HOME_NFS_PATH /var/jenkins_home
fi

dockerd-entrypoint.sh "$@"

