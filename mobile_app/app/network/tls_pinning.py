from __future__ import annotations

import hashlib
import socket
import ssl


def verify_certificate_pin(base_url: str, expected_fingerprint_sha256: str) -> bool:
    if not expected_fingerprint_sha256:
        return True

    host, port = _parse_host_port(base_url)
    context = ssl.create_default_context()

    with socket.create_connection((host, port), timeout=5) as sock:
        with context.wrap_socket(sock, server_hostname=host) as secure_sock:
            cert_bin = secure_sock.getpeercert(binary_form=True)

    actual = hashlib.sha256(cert_bin).hexdigest().lower()
    expected = expected_fingerprint_sha256.lower().replace(":", "")
    return actual == expected


def _parse_host_port(base_url: str) -> tuple[str, int]:
    normalized = base_url.replace("https://", "", 1)
    host_port = normalized.split("/", 1)[0]
    if ":" in host_port:
        host, port = host_port.split(":", 1)
        return host, int(port)
    return host_port, 443
