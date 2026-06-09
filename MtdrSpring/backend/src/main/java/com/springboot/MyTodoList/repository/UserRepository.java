package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.transaction.Transactional;
import java.util.Optional;

/**
 * Repositorio encargado de gestionar las operaciones de persistencia
 * relacionadas con la entidad.
 *
 * Además de las operaciones CRUD proporcionadas por JpaRepository,
 * incluye consultas personalizadas para la búsqueda de usuarios por
 * correo electrónico y la obtención del siguiente identificador disponible.
 */
@Repository
@Transactional
@EnableTransactionManagement
public interface UserRepository extends JpaRepository<User, Long> {

    /** Busca un usuario por su correo electrónico, ignorando diferencias de mayúsculas/minúsculas */
    Optional<User> findByEmailIgnoreCase(String email);

    @Query("SELECT COALESCE(MAX(u.id), 0) + 1 FROM User u")

    /** Obtiene el siguiente identificador disponible para un nuevo usuario */
    Long findNextAvailableId();
}
