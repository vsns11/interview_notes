// 1.10 Networking fundamentals, on the loopback interface only (no external network is touched).
// TCP = one ordered byte stream; UDP = separate datagrams; DNS lookup of "localhost" comes from the hosts file;
// net.BlockList plays a tiny firewall; a Map plays a home router's NAT table.
import net from 'node:net';
import dgram from 'node:dgram';
import dns from 'node:dns/promises';
import assert from 'node:assert/strict';

// --- 1. Name resolution without the Internet: "localhost" is answered by the OS (hosts file) ---
const addrs = await dns.lookup('localhost', { all: true });
console.log('localhost ->', addrs.map(a => `${a.address} (IPv${a.family})`).join(', '));
assert.ok(addrs.some(a => a.address === '127.0.0.1' || a.address === '::1'));
console.log('isIPv4("203.0.113.10") =', net.isIPv4('203.0.113.10'), '| isIPv6("2001:db8::10") =', net.isIPv6('2001:db8::10'));

// --- 2. TCP: three writes go in, one byte stream comes out (message boundaries are NOT kept) ---
const tcpReceived = await new Promise((resolve) => {
  let data = '';
  const server = net.createServer((sock) => {
    console.log(`TCP server sees client ${sock.remoteAddress}:${sock.remotePort} (ephemeral port picked by the OS)`);
    sock.on('data', (chunk) => { data += chunk; });
    sock.on('end', () => { server.close(); resolve(data); });
  });
  server.listen(0, '127.0.0.1', () => {
    const { port } = server.address();
    console.log(`TCP server listening on 127.0.0.1:${port}`);
    const client = net.connect(port, '127.0.0.1', () => {   // connect() = SYN, SYN-ACK, ACK
      client.write('one|'); client.write('two|'); client.write('three|');
      client.end();                                          // FIN: "I have finished sending"
    });
  });
});
console.log('TCP bytes received      :', JSON.stringify(tcpReceived), '(in order, as one stream)');
assert.equal(tcpReceived, 'one|two|three|');

// --- 3. UDP: three sends arrive as three separate datagrams, no connection, no handshake ---
const udpReceived = await new Promise((resolve) => {
  const got = [];
  const rx = dgram.createSocket('udp4');
  rx.on('message', (msg) => { got.push(msg.toString()); if (got.length === 3) { rx.close(); resolve(got); } });
  rx.bind(0, '127.0.0.1', () => {
    const { port } = rx.address();
    const tx = dgram.createSocket('udp4');
    let sent = 0;
    for (const m of ['one', 'two', 'three']) tx.send(m, port, '127.0.0.1', () => { if (++sent === 3) tx.close(); });
  });
});
console.log('UDP datagrams received  :', JSON.stringify(udpReceived), '(boundaries kept; on a real network some could be lost or reordered)');
assert.equal(udpReceived.length, 3);

// --- 4. A firewall rule set: allow-list the private ranges, deny everything else ---
const privateNets = new net.BlockList();                     // RFC 1918 private IPv4 ranges + IPv6 ULA
privateNets.addSubnet('10.0.0.0', 8);
privateNets.addSubnet('172.16.0.0', 12);
privateNets.addSubnet('192.168.0.0', 16);
privateNets.addSubnet('fc00::', 7, 'ipv6');
for (const ip of ['192.168.1.20', '10.4.5.6', '172.20.0.1', '203.0.113.10', '8.8.8.8']) {
  const isPrivate = privateNets.check(ip);
  console.log(`firewall: ${ip.padEnd(13)} private=${String(isPrivate).padEnd(5)} -> ${isPrivate ? 'ALLOW (admin port 22)' : 'DENY'}`);
}
assert.equal(privateNets.check('fd12:3456::1', 'ipv6'), true);
assert.equal(privateNets.check('203.0.113.10'), false);

// --- 5. NAT (network address translation) as a home router does it: many private sockets, one public IP ---
const PUBLIC_IP = '203.0.113.5';
const nat = new Map(); let nextPort = 40000;                  // public port -> private socket
function outbound(privIp, privPort) {                         // rewrite the source of an outgoing packet
  for (const [pub, priv] of nat) if (priv === `${privIp}:${privPort}`) return `${PUBLIC_IP}:${pub}`;
  nat.set(++nextPort, `${privIp}:${privPort}`);
  return `${PUBLIC_IP}:${nextPort}`;
}
const inbound = (pubPort) => nat.get(pubPort) ?? 'DROP (no mapping: unsolicited inbound traffic)';
console.log('NAT out: 192.168.1.10:51000 ->', outbound('192.168.1.10', 51000));
console.log('NAT out: 192.168.1.11:51000 ->', outbound('192.168.1.11', 51000));
console.log('NAT in : reply to :40001     ->', inbound(40001));
console.log('NAT in : packet to :40999    ->', inbound(40999));
assert.equal(inbound(40002), '192.168.1.11:51000');
console.log('all assertions passed');
