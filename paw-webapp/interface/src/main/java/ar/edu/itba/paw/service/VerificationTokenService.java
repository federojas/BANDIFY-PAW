package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.VerificationToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationTokenService {

    Optional<VerificationToken> getToken(String token);

    VerificationToken createToken(long userId, String token, LocalDateTime expiryDate);

    void deleteTokenByUserId(long userId);

    boolean verify(String token);
}
