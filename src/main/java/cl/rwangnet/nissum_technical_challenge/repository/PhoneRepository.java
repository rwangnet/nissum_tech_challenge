package cl.rwangnet.nissum_technical_challenge.repository;

import cl.rwangnet.nissum_technical_challenge.model.Phone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PhoneRepository extends JpaRepository<Phone, UUID> {
}