from __future__ import annotations

import logging
from dataclasses import asdict

from kivy.app import App
from kivy.clock import Clock
from kivy.lang import Builder
from kivy.properties import NumericProperty, StringProperty
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.screenmanager import Screen, ScreenManager

from app.config import AppConfig, load_config
from app.network.api_client import ApiClient
from app.network.auth_manager import AuthManager
from app.network.offline_queue import OfflineActionQueue
from app.security.secure_store import SecureStore
from app.services.monitoring_service import MonitoringService

KV_PATH = "app/ui/main.kv"


class LoginScreen(Screen):
    status_text = StringProperty("")


class HomeScreen(Screen):
    user_email = StringProperty("")
    property_count = NumericProperty(0)
    active_alerts = NumericProperty(0)
    online_devices = NumericProperty(0)
    queue_size = NumericProperty(0)
    last_sync = StringProperty("Never")
    error_text = StringProperty("")


class RootScreen(ScreenManager):
    pass


class NriSentinelMobileApp(App):
    def build(self):
        self.title = "NRI Plot Sentinel"
        self._configure_logging()

        self._cfg: AppConfig = load_config()
        self._cfg.token_state_path.parent.mkdir(parents=True, exist_ok=True)

        self._api = ApiClient(
            base_url=self._cfg.base_url,
            request_timeout_secs=self._cfg.request_timeout_secs,
            cert_fingerprint_sha256=self._cfg.cert_fingerprint_sha256,
            logger=self._logger,
        )
        self._store = SecureStore(self._cfg.token_state_path, self._cfg.app_secret)
        self._auth = AuthManager(api_client=self._api, secure_store=self._store)
        self._offline_queue = OfflineActionQueue(self._cfg.queue_db_path)
        self._monitor = MonitoringService(
            auth_manager=self._auth,
            api_client=self._api,
            offline_queue=self._offline_queue,
            poll_interval_secs=self._cfg.poll_interval_secs,
            logger=self._logger,
        )

        Builder.load_file(KV_PATH)
        root = RootScreen()

        if self._auth.access_token():
            self._monitor.start()
            root.current = "home"
            Clock.schedule_interval(self._refresh_home_ui, 2)
        else:
            root.current = "login"

        return root

    def on_stop(self):
        self._monitor.stop()
        self._offline_queue.close()

    def login(self, email: str, password: str):
        login_screen: LoginScreen = self.root.get_screen("login")
        try:
            self._auth.login(email=email.strip(), password=password.strip())
            login_screen.status_text = "Login successful. Loading dashboard..."
            self._monitor.start()
            self.root.current = "home"
            Clock.schedule_interval(self._refresh_home_ui, 2)
        except Exception as exc:
            login_screen.status_text = f"Login failed: {exc}"

    def logout(self):
        self._monitor.stop()
        self._auth.logout()
        home: HomeScreen = self.root.get_screen("home")
        home.error_text = ""
        home.last_sync = "Never"
        self.root.current = "login"

    def refresh_now(self):
        self._monitor.refresh_once()
        self._monitor.flush_queue_once()
        self._refresh_home_ui(0)

    def acknowledge_alert(self, alert_id: str):
        self._monitor.acknowledge_alert(alert_id)
        self.refresh_now()

    def _refresh_home_ui(self, _dt):
        home: HomeScreen = self.root.get_screen("home")
        snapshot = self._monitor.snapshot()

        dashboard = snapshot.dashboard
        home.property_count = int(dashboard.get("property_count") or 0)
        home.active_alerts = int(dashboard.get("active_alerts") or len(snapshot.alerts))

        online = 0
        for device in snapshot.devices:
            if str(device.get("status", "")).lower() == "online":
                online += 1
        home.online_devices = online
        home.queue_size = self._offline_queue.size()
        home.last_sync = "Just now"
        home.error_text = snapshot.last_error

        home.ids.alert_list.text = self._format_alerts(snapshot.alerts)
        home.ids.device_list.text = self._format_devices(snapshot.devices)

    @staticmethod
    def _format_alerts(alerts):
        if not alerts:
            return "No active alerts"

        lines = []
        for item in alerts[:20]:
            title = item.get("title") or item.get("alert_type") or "Alert"
            severity = item.get("severity") or "info"
            location = item.get("location") or "Unknown location"
            alert_id = item.get("id") or item.get("alert_id") or "-"
            lines.append(f"[{severity.upper()}] {title} at {location} (id: {alert_id})")
        return "\n".join(lines)

    @staticmethod
    def _format_devices(devices):
        if not devices:
            return "No devices"

        lines = []
        for item in devices[:30]:
            name = item.get("name") or item.get("device_name") or "Pi Device"
            status = item.get("status") or "unknown"
            battery = item.get("battery_level")
            battery_txt = f"{battery}%" if battery is not None else "n/a"
            lines.append(f"{name} | status={status} | battery={battery_txt}")
        return "\n".join(lines)

    def _configure_logging(self):
        self._logger = logging.getLogger("nri_sentinel_mobile")
        self._logger.setLevel(logging.INFO)
        if not self._logger.handlers:
            handler = logging.StreamHandler()
            fmt = logging.Formatter("%(asctime)s %(levelname)s %(message)s")
            handler.setFormatter(fmt)
            self._logger.addHandler(handler)


def main():
    NriSentinelMobileApp().run()


if __name__ == "__main__":
    main()
