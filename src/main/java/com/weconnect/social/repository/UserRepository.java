package com.weconnect.social.repository;

import com.weconnect.social.model.AuthProvider;
import com.weconnect.social.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByAuthProviderAndProviderSubject(AuthProvider authProvider, String providerSubject);

    Optional<User> findByEmail(String email);
}
