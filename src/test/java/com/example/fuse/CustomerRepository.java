package com.example.fuse;

import com.example.fuse.annotation.Fusible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Override
    @NonNull
    @Fusible
    Optional<Customer> findById(@NonNull Long id);


}
