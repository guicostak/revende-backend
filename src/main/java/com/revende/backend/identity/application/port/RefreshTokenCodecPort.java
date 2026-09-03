package com.revende.backend.identity.application.port;

public interface RefreshTokenCodecPort {

    String generate();

    String hash(String rawToken);
}
