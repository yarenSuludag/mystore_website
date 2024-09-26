package com.bmt.MyStore.repositories;

import com.bmt.MyStore.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Integer> {

    public AppUser findByEmail(String email);


}
