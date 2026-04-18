package com.rootssecure.sentinel.domain.model

import java.time.Instant

/**
 * Pure Kotlin domain model for an alert event. No Android or Room imports.
 *
 * This is what the UI layer and UseCases work with — the data layer is
 * responsible for mapping [AlertEventEntity] → [AlertEvent].
 */
data class AlertEvent(
    val id: String,
    val title: String,          // e.g. "ILLEGAL CONSTRUCTION"
    val reason: String,         // e.g. "JCB actively detected for >5 frames"
    val severity: AlertSeverity,
    val occurredAt: Instant,
    val imageUrl: String,       // First image URL
    val mediaRefs: List<String> = emptyList(),
    val confidence: Double = 0.0,
    val burstCount: Int = 1,
    val isFlagged: Boolean,
    val isMock: Boolean
)
