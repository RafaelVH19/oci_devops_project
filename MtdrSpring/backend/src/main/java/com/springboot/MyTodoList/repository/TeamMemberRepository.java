package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.TeamMember;
import com.springboot.MyTodoList.model.TeamMemberId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

/**
 * Repositorio encargado de gestionar las relaciones entre usuarios
 * y equipos de trabajo.
 *
 * Administra la entidad, que representa la
 * pertenencia de un usuario a un equipo específico mediante una
 * clave compuesta.
 */
@Repository
@Transactional
@EnableTransactionManagement
public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMemberId> {

	/** Obtiene todos los miembros del equipo por ID de equipo */
	List<TeamMember> findByIdTeamId(Long teamId);

	/** Obtiene todos los equipos a los que pertenece un usuario por ID de usuario */
	List<TeamMember> findByIdMemberUserId(Long memberUserId);
}