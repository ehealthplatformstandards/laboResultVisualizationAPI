ARG version
FROM --platform=$BUILDPLATFORM docker.taktik.be/icure/kraken-base:$version as builder

FROM ghcr.io/graalvm/graalvm-ce:ol9-java17-22.3.2
ARG version
ARG TARGETARCH
COPY --from=builder /build/icure-kraken-$version.jar ./
COPY docker-entrypoint.sh /
RUN curl -sSL https://github.com/krallin/tini/releases/download/v0.19.0/tini-$TARGETARCH -o /usr/local/bin/tini && chmod +x /usr/local/bin/tini
VOLUME /tmp
RUN chmod +x /docker-entrypoint.sh
ENTRYPOINT ["/usr/local/bin/tini", "--", "/docker-entrypoint.sh"]
#HEALTHCHECK --interval=150s --timeout=5m --retries=1 --start-period=10m CMD curl -f -s --retry 10 --max-time 2 --retry-delay 15 --retry-all-errors http://localhost:8080/actuator/health/liveness || (kill -s 15 -1 && (sleep 10; kill -s 9 -1))
