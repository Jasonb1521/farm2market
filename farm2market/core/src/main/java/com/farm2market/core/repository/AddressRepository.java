package com.farm2market.core.repository;

import com.farm2market.core.model.Address;
import java.util.List;
import java.util.Optional;

public interface AddressRepository extends Repository<Address, String> {

    List<Address> findByUserId(String userId);

    Optional<Address> findDefaultByUserId(String userId);

    void setDefault(String userId, String addressId);

    void clearDefault(String userId);

    long countByUserId(String userId);
}
