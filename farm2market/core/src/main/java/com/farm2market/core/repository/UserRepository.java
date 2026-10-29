package com.farm2market.core.repository;

import com.farm2market.core.enums.Role;
import com.farm2market.core.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends Repository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByMobile(String mobile);

    Optional<User> findByEmailOrMobile(String emailOrMobile);

    List<User> findByRole(Role role);

    List<User> findByRole(Role role, int limit, int offset);

    boolean existsByEmail(String email);

    boolean existsByMobile(String mobile);

    List<User> search(String query, int limit);

    long countByRole(Role role);

    void updatePassword(String userId, String newPasswordHash);

    void updateProfileImage(String userId, String imageUrl);
}
