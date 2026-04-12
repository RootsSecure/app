package com.rootssecure.sentinel.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * JSON contract for an alert event pushed by the Raspberry Pi Edge Node.
 * Mirrors the payload schema documented in the app brief exactly.
 *
 * Example payload:
 * {
 *   "alert_type": "manual_report",
 *   "vendor_event_id": "evt_ILLEGAL_CONSTRUCTION_1684534800000",
 *   "occurred_at": "2026-04-12T16:00:00+00:00",
 *   "metadata_json": { "edge_event_type": "ILLEGAL_CONSTRUCTION", ... },
 *   "media_refs": ["https://...proof.jpg"]
 * }
 */
@Serializable
data class AlertEventDto(
    @SerialName("alert_type")       val alertType: String,
    @SerialName("vendor_event_id")  val vendorEventId: String,
    @SerialName("occurred_at")      val occurredAt: String,
    @SerialName("metadata_json")    val metadataJson: AlertMetadataDto,
    @SerialName("media_refs")       val mediaRefs: List<String> = emptyList()
)

@Serializable
data class AlertMetadataDto(
    @SerialName("edge_event_type")       val edgeEventType: String,
    @SerialName("recommended_severity")  val recommendedSeverity: String,
    @SerialName("reason")                val reason: String,
    @SerialName("logic_level")           val logicLevel: String,
    @SerialName("motion_ratio")          val motionRatio: Double = 0.0
)
