package com.flightapp.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.flightapp.entity.ERole;
import com.flightapp.entity.Role;
import com.flightapp.repository.RoleRepository;

@Component
public class DataInitializer implements CommandLineRunner{
	private final RoleRepository roleRepo;

    public DataInitializer(RoleRepository roleRepo) {
        this.roleRepo = roleRepo;
    }

    @Override
    public void run(String... args) throws Exception {
    	roleRepo.findByName(ERole.ROLE_USER)
            .switchIfEmpty(roleRepo.save(new Role(null, ERole.ROLE_USER))).subscribe();
    	roleRepo.findByName(ERole.ROLE_ADMIN)
            .switchIfEmpty(roleRepo.save(new Role(null, ERole.ROLE_ADMIN))).subscribe();            
    }
}
