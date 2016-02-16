FROM docker:1.10-dind

# Install support for NFS mounting
RUN apk add --no-cache nfs-utils

# Call custom entrypoint script
COPY ca-entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/ca-entrypoint.sh
ENTRYPOINT ["ca-entrypoint.sh"]
