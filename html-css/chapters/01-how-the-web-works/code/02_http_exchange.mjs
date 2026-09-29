// A real HTTP/1.1 exchange on your own machine: a tiny server + a raw TCP client, so you see the exact bytes.
import http from 'node:http';
import net from 'node:net';

function send(res, status, headers, body = '') {
  res.sendDate = false;                                  // no Date header -> identical output on every run
  res.writeHead(status, { ...headers, 'Content-Length': Buffer.byteLength(body) });
  res.end(body);
}
const server = http.createServer((req, res) => {
  if (req.url === '/') send(res, 200, { 'Content-Type': 'text/html; charset=utf-8', 'Cache-Control': 'max-age=60' },
                            '<!DOCTYPE html><title>Hi</title><h1>Hello</h1>');
  else if (req.url === '/old') send(res, 301, { Location: '/' });
  else send(res, 404, { 'Content-Type': 'text/plain' }, 'Not found');
});
await new Promise(r => server.listen(0, '127.0.0.1', r));   // port 0 = any free port
const port = server.address().port;

function raw(path) {
  return new Promise(resolve => {
    const sock = net.connect(port, '127.0.0.1', () => {
      const request = `GET ${path} HTTP/1.1\r\nHost: localhost\r\nAccept: text/html\r\nConnection: close\r\n\r\n`;
      console.log('>>> REQUEST\n' + request.replaceAll('\r\n', '\n').trimEnd());
      sock.write(request);
    });
    let data = '';
    sock.on('data', d => (data += d));
    sock.on('end', () => { console.log('<<< RESPONSE\n' + data.replaceAll('\r\n', '\n').trimEnd() + '\n'); resolve(); });
  });
}
await raw('/');
await raw('/old');
await raw('/missing');
server.close();
