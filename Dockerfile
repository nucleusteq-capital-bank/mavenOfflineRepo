# Dockerfile.offline-repo
FROM eclipse-temurin:21-jre-alpine

# Location where offline repo will live inside the image
ENV OFFLINE_REPO_DIR=/opt/offline-repo

# Create directory
RUN mkdir -p ${OFFLINE_REPO_DIR}

# Copy the generated offline repo
COPY offline-repo/ ${OFFLINE_REPO_DIR}/

# Optional: show size during build (debugging)
RUN echo "Offline repo size:" && du -sh ${OFFLINE_REPO_DIR}

# Nothing to run – this is a data image
