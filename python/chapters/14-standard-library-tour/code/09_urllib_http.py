"""urllib.parse, urllib.request and http.server - talking to a LOCAL server only."""
import json
import threading
from http import HTTPStatus
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.error import HTTPError
from urllib.parse import parse_qs, urlencode, urljoin, urlparse
from urllib.request import Request, urlopen


class Handler(BaseHTTPRequestHandler):
    def do_GET(self) -> None:
        url = urlparse(self.path)
        if url.path != "/greet":
            self.send_error(HTTPStatus.NOT_FOUND)
            return
        name = parse_qs(url.query).get("name", ["world"])[0]
        body = json.dumps({"message": f"hello {name}"}).encode()
        self.send_response(HTTPStatus.OK)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, *args) -> None:      # keep the demo output quiet
        pass


if __name__ == "__main__":
    u = urlparse("https://api.example.com:8443/v1/users?id=7&sort=name#top")
    print("parsed :", u.scheme, u.hostname, u.port, u.path, parse_qs(u.query))
    print("encode :", urlencode({"q": "type hints", "page": 2}))
    print("urljoin:", urljoin("https://x.org/docs/a/", "../b"))

    server = ThreadingHTTPServer(("127.0.0.1", 0), Handler)   # port 0 = any free
    threading.Thread(target=server.serve_forever, daemon=True).start()
    base = f"http://127.0.0.1:{server.server_port}"
    req = Request(f"{base}/greet?{urlencode({'name': 'Asha'})}",
                  headers={"Accept": "application/json"})
    with urlopen(req, timeout=5) as resp:
        print("GET    :", resp.status, resp.headers["Content-Type"], json.load(resp))
    try:
        urlopen(f"{base}/missing", timeout=5)
    except HTTPError as e:
        print("error  :", e.code, e.reason)
    server.shutdown()
