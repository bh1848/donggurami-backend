package com.USWCicrcleLink.server.user.domain.repository;

import com.USWCicrcleLink.server.user.domain.User;
import org.hibernate.boot.archive.internal.JarProtocolArchiveDescriptor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}
