package com.groovo.server.session.repository;

import com.groovo.server.session.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, String> {}
