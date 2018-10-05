package com.rf.beatrice

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration

fun migrateDB(dataSource: String) {
    val flyway = Flyway(FluentConfiguration()
            .dataSource(dataSource, "", "")
            .baselineVersion("1")
            .baselineOnMigrate(true)
            .schemas("PUBLIC")
    )
    //flyway.repair()
    flyway.migrate()
}