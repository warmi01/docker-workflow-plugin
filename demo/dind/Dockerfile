FROM docker:1.10-dind

# Install support for NFS mounting
RUN apk add --no-cache nfs-utils

# Call custom entrypoint script
COPY ca-entrypoint.sh /usr/local/bin/
ENTRYPOINT ["ca-entrypoint.sh"]
