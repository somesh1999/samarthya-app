package org.samarthya.collect.projects

import org.samarthya.collect.shared.strings.UUIDGenerator
import java.util.function.Supplier

class InMemProjectsRepositoryTest : ProjectsRepositoryTest() {
    override fun buildSubject(): ProjectsRepository {
        return InMemProjectsRepository(UUIDGenerator(),)
    }

    override fun buildSubject(clock: Supplier<Long>): ProjectsRepository {
        return InMemProjectsRepository(UUIDGenerator(), clock)
    }
}
