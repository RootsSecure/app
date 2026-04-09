from __future__ import annotations

import logging
import signal
import threading
import time

from .api_transport import GatewayApiClient
from .camera_adapter import JsonlCameraAdapter
from .config import load_config_from_env
from .event_classifier import DetectionClassifier
from .event_queue import EventQueue
from .event_uploader import EventUploader
from .hardware import get_stable_hardware_id
from .heartbeat_sender import HeartbeatSender
from .logging_config import setup_logging
from .session_manager import SessionManager


def _run_detection_pipeline(
    adapter: JsonlCameraAdapter,
    classifier: DetectionClassifier,
    uploader: EventUploader,
    stop_event: threading.Event,
    logger: logging.Logger,
) -> None:
    for detection in adapter.iter_detections(stop_event):
        try:
            event = classifier.normalize(detection)
            uploader.enqueue_event(event)
        except Exception as exc:
            logger.warning(
                "Failed to normalize or queue detection.",
                extra={"error": str(exc), "vendor_event_id": detection.vendor_event_id},
            )


def run() -> None:
    config = load_config_from_env()
    setup_logging(config.log_level)
    logger = logging.getLogger("nri_plot_sentinel_pi")

    config.state_dir.mkdir(parents=True, exist_ok=True)

    hardware_id = get_stable_hardware_id(config.hardware_id)
    logger.info("Hardware ID ready.", extra={"hardware_id": hardware_id})

    api_client = GatewayApiClient(
        base_url=config.base_url,
        timeout_secs=config.request_timeout_secs,
        verify_tls=config.verify_tls,
        logger=logger,
    )

    session_manager = SessionManager(
        api_client=api_client,
        provisioning_token=config.provisioning_token,
        hardware_id=hardware_id,
        state_path=config.session_state_path,
        connect_metadata=config.connect_metadata,
        expiry_skew_secs=config.session_expiry_skew_secs,
        connect_retry_base_secs=config.connect_retry_base_secs,
        connect_retry_max_secs=config.connect_retry_max_secs,
        logger=logger,
    )

    queue = EventQueue(config.queue_db_path)

    uploader = EventUploader(
        api_client=api_client,
        session_manager=session_manager,
        queue=queue,
        flush_interval_secs=config.event_flush_interval_secs,
        flush_batch_size=config.event_flush_batch_size,
        max_backoff_secs=config.queue_max_backoff_secs,
        logger=logger,
    )

    heartbeat_sender = HeartbeatSender(
        api_client=api_client,
        session_manager=session_manager,
        queue=queue,
        interval_secs=config.heartbeat_interval_secs,
        network_host=config.network_check_host,
        network_port=config.network_check_port,
        network_timeout_secs=config.network_check_timeout_secs,
        base_metadata=config.heartbeat_metadata,
        logger=logger,
    )

    classifier = DetectionClassifier()
    camera_adapter = JsonlCameraAdapter(
        events_path=config.camera_events_path,
        poll_interval_secs=config.camera_poll_interval_secs,
        logger=logger,
    )

    stop_event = threading.Event()

    def _signal_handler(signum: int, _frame: object | None) -> None:
        logger.info("Shutdown signal received.", extra={"signal": signum})
        stop_event.set()

    signal.signal(signal.SIGINT, _signal_handler)
    signal.signal(signal.SIGTERM, _signal_handler)

    detection_thread = threading.Thread(
        target=_run_detection_pipeline,
        args=(camera_adapter, classifier, uploader, stop_event, logger),
        name="detection-pipeline",
        daemon=True,
    )

    try:
        session_manager.get_session(force_refresh=False)
        uploader.start()
        heartbeat_sender.start()
        heartbeat_sender.send_once()
        detection_thread.start()

        logger.info("NRI Plot Sentinel Pi client started.")
        while not stop_event.wait(1):
            pass
    finally:
        logger.info("Stopping NRI Plot Sentinel Pi client.")
        stop_event.set()
        heartbeat_sender.stop()
        uploader.stop()
        detection_thread.join(timeout=5)
        queue.close()


def main() -> None:
    run()


if __name__ == "__main__":
    main()
